package com.bentley.auth.controller

import com.bentley.auth.user.UserService
import com.bentley.auth.user.UserVerification
import com.bentley.auth.user.UserVerificationService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "10s")
class UserControllerTests {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userVerificationService: UserVerificationService

    @Test
    fun `create user - verify email - login`() {
        val email = "kimjun136@naver.com"
        val password = "password123"
        val userJson = """
            {
                "email": "$email",
                "password": "$password",
                "phone": "01057288382",
                "firstName": "JunYoung",
                "lastName": "Kim"
            }
        """
        webTestClient.post()
            .uri("/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userJson)
            .exchange()
            .expectStatus().isOk

        val user = userService.getOrNullByEmail(email) ?: throw IllegalStateException("User should exist after creation")
        val userVerification = UserVerification(user.id, "123456", expiresAt = user.createdAt.plusHours(1))
        userVerificationService.create(userVerification)

        val verifyJson = """
            {
                "email": "$email",
                "code": "${userVerification.code}"
            }
        """
        webTestClient.post()
            .uri("/v1/user/verify-email")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(verifyJson)
            .exchange()
            .expectStatus().isOk

        val loginJson = """
            {
                "email": "$email",
                "password": "$password"
            }
        """
        webTestClient.post()
            .uri("/v1/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginJson)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.accessToken").isNotEmpty
            .jsonPath("$.refreshToken").isNotEmpty
    }
}