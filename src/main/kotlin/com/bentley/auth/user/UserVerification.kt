package com.bentley.auth.user

import com.bentley.auth.core.Entity
import java.time.LocalDateTime

data class UserVerification(
    val userId: Long,
    val code: String,
    var verifiedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime,
) : Entity()
