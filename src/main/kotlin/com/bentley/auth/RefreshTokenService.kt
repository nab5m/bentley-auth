package com.bentley.auth

import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val refreshTokenMapper: RefreshTokenMapper,
) {

    fun create(refreshToken: RefreshToken) {
        refreshTokenMapper.insert(refreshToken)
    }

    fun delete(userType: UserType, userId: Long, token: String) {
        refreshTokenMapper.deleteByUserTypeAndUserIdAndToken(userType, userId, token)
    }
}