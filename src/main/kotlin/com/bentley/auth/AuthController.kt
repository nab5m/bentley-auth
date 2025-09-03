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
        val userType = decodedJwt.claims["userType"]?.toString() ?: throw IllegalArgumentException("Invalid token: missing userType claim")
        val userId = decodedJwt.claims["userId"]?.asLong() ?: throw IllegalArgumentException("Invalid token: missing userId claim")

        val accessToken = jwtService.generateAccessToken(UserType.valueOf(userType), userId)

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
        val userType = decodedJwt.claims["userType"]?.toString() ?: throw IllegalArgumentException("Invalid token: missing userType claim")
        val userId = decodedJwt.claims["userId"]?.asLong() ?: throw IllegalArgumentException("Invalid token: missing userId claim")

        refreshTokenService.delete(UserType.valueOf(userType), userId, logoutRequest.refreshToken)
    }
}