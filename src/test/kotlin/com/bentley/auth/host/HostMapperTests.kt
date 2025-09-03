package com.bentley.auth.host

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@MybatisTest
class HostMapperTests {

    @Autowired
    private lateinit var hostMapper: HostMapper

    private lateinit var testHost: Host

    @BeforeEach
    fun setup() {
        testHost = Host(
            email = "host@example.com",
            password = "password123",
            firstName = "Host",
            lastName = "User",
            status = Host.Status.ACTIVE,
            deactivatedAt = null,
        )
        hostMapper.insert(testHost)
    }

    @Test
    fun `test findById`() {
        val host = hostMapper.findById(testHost.id)
        Assertions.assertNotNull(host)
        Assertions.assertEquals(testHost.email, host?.email)
    }

    @Test
    fun `test findByEmail`() {
        val host = hostMapper.findByEmail(testHost.email)
        Assertions.assertNotNull(host)
        Assertions.assertEquals(testHost.email, host?.email)
    }
}