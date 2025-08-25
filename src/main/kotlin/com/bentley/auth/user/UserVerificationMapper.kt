package com.bentley.auth.user

import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserVerificationMapper {
    fun insert(userVerification: UserVerification): Int
    fun update(userVerification: UserVerification): Int
    fun findByUserIdAndCode(userId: Long, code: String): UserVerification?
}