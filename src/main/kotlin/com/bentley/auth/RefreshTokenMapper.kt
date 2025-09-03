package com.bentley.auth

import org.apache.ibatis.annotations.Mapper

@Mapper
interface RefreshTokenMapper {
    fun insert(refreshToken: RefreshToken): Int
    fun deleteByUserIdAndToken(userId: Long, token: String): Int
}