package com.bentley.auth.user

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@MybatisTest
class UserMapperTests {

    @Autowired
    private lateinit var userMapper: UserMapper

    private lateinit var testUser: User

    @BeforeEach
    fun setup() {
        testUser = User(
            email = "test@example.com",
            password = "password123",
            phone = "1234567890",
            firstName = "Test",
            lastName = "User",
            status = User.Status.ACTIVE,
            deactivatedAt = null,
        )
        userMapper.insert(testUser)
    }

    @Test
    fun `test findById`() {
        val user = userMapper.findById(testUser.id)
        Assertions.assertNotNull(user)
        Assertions.assertEquals(testUser.email, user?.email)
    }

    @Test
    fun `test findByEmail`() {
        val user = userMapper.findByEmail(testUser.email)
        Assertions.assertNotNull(user)
        Assertions.assertEquals(testUser.email, user?.email)
    }

    @Test
    fun `test update`() {
        testUser.password = "newPassword123"
        testUser.deactivatedAt = LocalDateTime.now()
        userMapper.update(testUser)

        val user = userMapper.findById(testUser.id)
        Assertions.assertNotNull(user)
        Assertions.assertEquals(testUser.password, user?.password)
        Assertions.assertEquals(testUser.deactivatedAt, user?.deactivatedAt)
    }
}