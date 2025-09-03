package com.bentley.auth

import org.apache.ibatis.annotations.Mapper

@Mapper
interface RefreshTokenMapper {
    fun insert(refreshToken: RefreshToken): Int
    fun deleteByUserTypeAndUserIdAndToken(userType: UserType, userId: Long, token: String): Int
}