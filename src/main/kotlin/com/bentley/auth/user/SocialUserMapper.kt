package com.bentley.auth.user

import org.apache.ibatis.annotations.Mapper

@Mapper
interface SocialUserMapper {
    fun insert(socialUser: SocialUser): Int
    fun findByRegistrationIdAndSubject(registrationId: String, subject: String): SocialUser?
}