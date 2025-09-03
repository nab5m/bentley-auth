package com.bentley.auth.controller

import com.bentley.auth.core.JwtService
import com.bentley.auth.user.OAuth2ClientService
import com.bentley.auth.user.RefreshTokenService
import com.bentley.auth.user.SocialUserService
import com.bentley.auth.user.UserService
import com.bentley.auth.user.UserVerificationMailService
import com.bentley.auth.user.UserVerificationService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.request.RequestDocumentation.formParameters
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@WebMvcTest(controllers = [UserController::class])
@AutoConfigureRestDocs
@Import(TestSecurityConfig::class)
class UserControllerTests @Autowired constructor(
    private val mockMvc: MockMvc,
) {

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var userVerificationService: UserVerificationService

    @MockitoBean
    private lateinit var userVerificationMailService: UserVerificationMailService

    @MockitoBean
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var refreshTokenService: RefreshTokenService

    @MockitoBean
    private lateinit var oAuth2ClientService: OAuth2ClientService

    @MockitoBean
    private lateinit var socialUserService: SocialUserService

    @MockitoBean
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun createUser() {
        doReturn("encodedPassword").`when`(passwordEncoder).encode(any())

        mockMvc.perform(
            post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(
                """
            {
                "email": "bentley.kim@handys.co.kr",
                "password": "password123",
                "phone": "01057288382",
                "firstName": "JunYoung",
                "lastName": "Kim"
            }
            """.trimIndent()
            )
        )
            .andExpect(status().isOk)
            .andDo(print())
            .andDo(
                document(
                    "user-create",
                    requestFields(
                        fieldWithPath("email").description("User email"),
                        fieldWithPath("password").description("password"),
                        fieldWithPath("phone").description("phone").optional(),
                        fieldWithPath("firstName").description("firstName").optional(),
                        fieldWithPath("lastName").description("lastName").optional(),
                    )
                )
            )
    }
}