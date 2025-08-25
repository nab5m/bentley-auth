package com.bentley.auth.user

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@MybatisTest
class UserVerificationMapperTests {

    @Autowired
    private lateinit var userVerificationMapper: UserVerificationMapper

    private lateinit var testUserVerification: UserVerification

    @BeforeEach
    fun setup() {
        testUserVerification = UserVerification(
            userId = 1L,
            code = "testCode123",
            verifiedAt = null,
            expiresAt = LocalDateTime.now().plusDays(1),
        )
        userVerificationMapper.insert(testUserVerification)
    }

    @Test
    fun `test insert`() {
        Assertions.assertNotNull(testUserVerification.id)
    }

    @Test
    fun `test update`() {
        testUserVerification.verifiedAt = LocalDateTime.now()
        Assertions.assertEquals(1, userVerificationMapper.update(testUserVerification))
    }

    @Test
    fun `test findByUserIdAndCode`() {
        val userVerification =
            userVerificationMapper.findByUserIdAndCode(testUserVerification.userId, testUserVerification.code)
        Assertions.assertNotNull(userVerification)
        Assertions.assertEquals(testUserVerification.userId, userVerification?.userId)
        Assertions.assertEquals(testUserVerification.code, userVerification?.code)
    }
}
