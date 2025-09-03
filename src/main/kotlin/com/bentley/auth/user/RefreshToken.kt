package com.bentley.auth.user

import com.bentley.core.Entity
import java.time.LocalDateTime

data class RefreshToken(
    val userId: Long,
    val token: String,
    val expiresAt: LocalDateTime,
) : Entity()
