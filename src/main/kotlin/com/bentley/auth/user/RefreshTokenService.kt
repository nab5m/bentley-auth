package com.bentley.auth.user

import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val refreshTokenMapper: RefreshTokenMapper,
) {

    fun create(refreshToken: RefreshToken) {
        refreshTokenMapper.insert(refreshToken)
    }

    fun delete(userId: Long, token: String) {
        refreshTokenMapper.deleteByUserIdAndToken(userId, token)
    }
}