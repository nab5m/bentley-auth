package com.bentley.auth.user

import com.bentley.auth.JwtService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
class UserController(
    private val userService: UserService,
    private val userVerificationService: UserVerificationService,
    private val userVerificationMailService: UserVerificationMailService,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val oAuth2ClientService: OAuth2ClientService,
    private val socialUserService: SocialUserService,
    private val passwordEncoder: PasswordEncoder,
) {

    @PostMapping("/v1/user")
    @Operation(summary = "이메일 회원가입")
    fun createUser(@RequestBody @Valid user: User) {
        val existingUser = userService.getOrNullByEmail(user.email)
        if (existingUser != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User with email ${user.email} already exists")
        }

        user.password = passwordEncoder.encode(user.password)
        userService.create(user) // TODO: 이메일 포맷 검사, 비밀번호 정책

        userVerificationMailService.sendVerificationEmail(user)
    }

    data class ResendEmailVerificationRequest(
        val email: String,
    )

    @PostMapping("/v1/user/resend-email-verification")
    @Operation(summary = "이메일 인증번호 재전송")
    fun resendEmailVerification(@RequestBody request: ResendEmailVerificationRequest) {
        val user = userService.getOrNullByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User with email ${request.email} not found")
        userVerificationMailService.sendVerificationEmail(user)
    }

    data class VerifyEmailRequest(
        val email: String,
        val code: String,
    )

    @PostMapping("/v1/user/verify-email")
    @Operation(summary = "이메일 인증")
    fun verifyEmail(
        @RequestBody request: VerifyEmailRequest
    ) {
        val user = userService.getOrNullByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User with email ${request.email} not found")
        val userVerification = userVerificationService.getOrNullByUserIdAndCode(user.id, request.code)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification code")

        val now = LocalDateTime.now()
        if (userVerification.expiresAt.isBefore(now)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code has expired")
        }

        userVerification.verifiedAt = now
        userVerificationService.update(userVerification)

        user.status = User.Status.ACTIVE
        userService.update(user)
    }

    data class LoginRequest(
        val email: String,
        val password: String,
    )

    data class TokenResponse(
        val accessToken: String,
        val accessTokenExpiresAt: Instant,
        val refreshToken: String,
        val refreshTokenExpiresAt: Instant,
    )

    @PostMapping("/v1/login")
    @Operation(summary = "로그인")
    fun login(@RequestBody request: LoginRequest): TokenResponse {
        val user = userService.getOrNullByEmail(request.email).takeIf { it?.status == User.Status.ACTIVE }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User with email ${request.email} not found")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "email or password is incorrect")
        }

        return generateTokenResponse(user)
    }

    data class RefreshTokenRequest(
        val refreshToken: String,
    )

    @PostMapping("/v1/refresh-token")
    @Operation(summary = "토큰 재발급")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): TokenResponse {
        val decodedJwt = jwtService.verify(request.refreshToken)
        val userId = decodedJwt.subject.toLong()
        val user = userService.get(userId)

        val accessToken = jwtService.generateAccessToken(userId, user.email)

        return TokenResponse(
            accessToken = accessToken.token,
            accessTokenExpiresAt = accessToken.expiresAt,
            refreshToken = request.refreshToken,
            refreshTokenExpiresAt = decodedJwt.expiresAt.toInstant(),
        )
    }

    data class LogoutRequest(
        val refreshToken: String,
    )

    @PostMapping("/v1/logout")
    fun logout(@RequestBody logoutRequest: LogoutRequest) {
        val decodedJwt = jwtService.verify(logoutRequest.refreshToken)
        refreshTokenService.delete(decodedJwt.subject.toLong(), logoutRequest.refreshToken) // TODO: subject를 userId로 쓰는게 좋은건 아닌 듯. 명확하게 claim에 key-value로 저장하는게 나을 듯
    }

    @GetMapping("/v1/user/me")
    @Operation(summary = "내 정보 조회")
    fun me(@AuthenticationPrincipal userId: Long): User {
        return userService.get(userId)
    }

    data class UpdatePasswordRequest(
        val email: String,
        val code: String,
        val newPassword: String,
    )

    @PostMapping("/v1/user/update-password")
    @Operation(summary = "비밀번호 변경")
    fun updatePassword(@RequestBody request: UpdatePasswordRequest) {
        val user = userService.getOrNullByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User with email ${request.email} not found")

        val userVerification =
            userVerificationService.getOrNullByUserIdAndCode(user.id, request.code).takeIf { it?.verifiedAt == null }
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification code")

        val now = LocalDateTime.now()
        if (userVerification.expiresAt.isBefore(now)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code has expired")
        }

        user.password = passwordEncoder.encode(request.newPassword)
        userService.update(user)

        userVerification.verifiedAt = now
        userVerificationService.update(userVerification)
    }

    @DeleteMapping("/v1/user")
    @Operation(summary = "회원 탈퇴")
    fun delete(@AuthenticationPrincipal userId: Long) {
        userService.deactivate(userId)
    }

    data class OAuth2LoginRequest(
        val authorizationCode: String,
        val state: String?, // oauth2 state (optional)
    )

    @PostMapping("/v1/oauth2/{registrationId}/login")
    @Operation(summary = "OAuth2 로그인")
    fun oauth2Login(
        @PathVariable registrationId: String,
        @RequestBody request: OAuth2LoginRequest,
    ): TokenResponse {
        val oAuth2Token = oAuth2ClientService.exchange(registrationId, request.authorizationCode, request.state)
        val oAuth2User = oAuth2ClientService.loadUser(registrationId, oAuth2Token)

        val email = oAuth2ClientService.getEmail(registrationId, oAuth2User)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Email not found from OAuth2 provider")
        val user = userService.getOrNullByEmail(email) ?: run {
            val newUser = User(
                email = email,
                password = "", // OAuth2 로그인 사용자는 비밀번호 없음
                phone = null,
                firstName = oAuth2ClientService.getFirstName(registrationId, oAuth2User),
                lastName = oAuth2ClientService.getLastName(registrationId, oAuth2User),
                status = User.Status.ACTIVE,
                deactivatedAt = null,
            )

            userService.create(newUser)

            newUser
        }

        // TODO: 트랜잭션 처리 필요할 듯
        val oAuth2Subject = oAuth2ClientService.getSubject(registrationId, oAuth2User)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth2 user ID not found from OAuth2 provider")
        val socialUser = socialUserService.getOrNullByRegistrationIdAndSubject(registrationId, oAuth2Subject)
        if (socialUser == null) {
            val newSocialUser = SocialUser(
                userId = user.id,
                registrationId = registrationId,
                subject = oAuth2Subject,
            )
            socialUserService.create(newSocialUser)
        }

        return generateTokenResponse(user)
    }

    private fun generateTokenResponse(user: User): TokenResponse {
        val accessToken = jwtService.generateAccessToken(user.id, user.email)
        val refreshToken = jwtService.generateRefreshToken(user.id, user.email)

        refreshTokenService.create(
            RefreshToken(
                userId = user.id,
                token = refreshToken.token,
                expiresAt = LocalDateTime.ofInstant(refreshToken.expiresAt, ZoneId.systemDefault())
            )
        )

        return TokenResponse(
            accessToken = accessToken.token,
            accessTokenExpiresAt = accessToken.expiresAt,
            refreshToken = refreshToken.token,
            refreshTokenExpiresAt = refreshToken.expiresAt,
        )
    }
}