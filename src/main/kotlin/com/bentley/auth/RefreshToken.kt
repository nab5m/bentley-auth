package com.bentley.auth

import com.bentley.core.Entity
import java.time.LocalDateTime

data class RefreshToken(
    val userType: UserType,
    val userId: Long,
    val token: String,
    val expiresAt: LocalDateTime,
) : Entity()
