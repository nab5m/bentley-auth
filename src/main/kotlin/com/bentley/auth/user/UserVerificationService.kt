package com.bentley.auth.user

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserVerificationService(
    private val userVerificationMapper: UserVerificationMapper,
) {
    fun create(userVerification: UserVerification) {
        userVerificationMapper.insert(userVerification)
    }

    fun update(userVerification: UserVerification) {
        userVerification.updatedAt = LocalDateTime.now()
        userVerificationMapper.update(userVerification)
    }

    fun getOrNullByUserIdAndCode(userId: Long, code: String): UserVerification? {
        return userVerificationMapper.findByUserIdAndCode(userId, code)
    }
}