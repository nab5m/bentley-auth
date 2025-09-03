package com.bentley.auth.user

import com.bentley.core.Entity

/**
 *  User의 소설 계정 연동
 *  User에서 파생된거라서 UserSocialAccount가 더 적절한 이름이었을 것
 */
data class SocialUser(
    val userId: Long,
    val registrationId: String,
    val subject: String,
) : Entity()
