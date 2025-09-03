package com.bentley.auth

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
) {

    data class RefreshTokenRequest(
        val refreshToken: String,
    )

    @PostMapping("/v1/refresh-token")
    @Operation(summary = "토큰 재발급")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): TokenResponse {
        val decodedJwt = jwtService.verify(request.refreshToken)
        val userId = decodedJwt.subject.toLong()

        val accessToken = jwtService.generateAccessToken(userId)

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

    @DeleteMapping("/v1/refresh-token")
    fun logout(@RequestBody logoutRequest: LogoutRequest) {
        val decodedJwt = jwtService.verify(logoutRequest.refreshToken)
        refreshTokenService.delete(decodedJwt.subject.toLong(), logoutRequest.refreshToken) // TODO: subject를 userId로 쓰는게 좋은건 아닌 듯. 명확하게 claim에 key-value로 저장하는게 나을 듯
    }
}