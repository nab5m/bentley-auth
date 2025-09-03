package com.bentley.auth.user

import com.bentley.core.Entity

data class SocialUser(
    val userId: Long,
    val registrationId: String,
    val subject: String,
) : Entity()
