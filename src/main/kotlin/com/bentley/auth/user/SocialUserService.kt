package com.bentley.auth.user

import org.springframework.stereotype.Service

@Service
class SocialUserService(
    private val socialUserMapper: SocialUserMapper,
) {
    fun create(socialUser: SocialUser) {
        socialUserMapper.insert(socialUser)
    }

    fun getOrNullByRegistrationIdAndSubject(registrationId: String, subject: String): SocialUser? {
        return socialUserMapper.findByRegistrationIdAndSubject(registrationId, subject)
    }
}