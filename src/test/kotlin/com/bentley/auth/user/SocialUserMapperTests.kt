package com.bentley.auth.user

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@MybatisTest
class SocialUserMapperTests {

    @Autowired
    private lateinit var socialUserMapper: SocialUserMapper

    private lateinit var testSocialUser: SocialUser

    @BeforeEach
    fun setup() {
        testSocialUser = SocialUser(
            userId = 1L,
            registrationId = "google",
            subject = "testSubject123",
        )
        socialUserMapper.insert(testSocialUser)
    }

    @Test
    fun `test insert`() {
        Assertions.assertNotNull(testSocialUser.id)
    }

    @Test
    fun `test findByRegistrationIdAndSubject`() {
        val socialUser = socialUserMapper.findByRegistrationIdAndSubject(
            testSocialUser.registrationId,
            testSocialUser.subject
        )
        Assertions.assertNotNull(socialUser)
        Assertions.assertEquals(testSocialUser.userId, socialUser?.userId)
        Assertions.assertEquals(testSocialUser.registrationId, socialUser?.registrationId)
        Assertions.assertEquals(testSocialUser.subject, socialUser?.subject)
    }
}
