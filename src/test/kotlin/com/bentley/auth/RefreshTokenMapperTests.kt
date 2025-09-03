package com.bentley.auth

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@MybatisTest
class RefreshTokenMapperTests {

    @Autowired
    private lateinit var refreshTokenMapper: RefreshTokenMapper

    private lateinit var testRefreshToken: RefreshToken

    @BeforeEach
    fun setup() {
        testRefreshToken = RefreshToken(
            userId = 1L,
            token = "testRefreshToken123",
            expiresAt = LocalDateTime.now().plusDays(30),
        )
        refreshTokenMapper.insert(testRefreshToken)
    }

    @Test
    fun `test insert`() {
        Assertions.assertNotNull(testRefreshToken.id)
    }

    @Test
    fun `test deleteByToken`() {
        val rowsDeleted = refreshTokenMapper.deleteByUserIdAndToken(testRefreshToken.userId, testRefreshToken.token)
        Assertions.assertEquals(1, rowsDeleted)
    }
}