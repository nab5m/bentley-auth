package com.bentley.auth.controller

import com.bentley.auth.core.JwtService
import com.bentley.auth.user.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.constraints.ConstraintDescriptions
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID.randomUUID
import kotlin.test.Test


@WebMvcTest(controllers = [UserController::class])
@AutoConfigureRestDocs(
    uriScheme = "https",
    uriHost = "auth.bentley.com",
    uriPort = 443,
)
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

        val userConstraints = ConstraintDescriptions(User::class.java)

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
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("email").description(userConstraints.descriptionsForProperty("email").joinToString(", ")),
                        fieldWithPath("password").description(userConstraints.descriptionsForProperty("password").joinToString(", ")),
                        fieldWithPath("phone").description("phone").optional(),
                        fieldWithPath("firstName").description("firstName").optional(),
                        fieldWithPath("lastName").description("lastName").optional(),
                    )
                )
            )
    }

    @Test
    fun login() {
        val email = "bentley.kim@handys.co.kr"
        val password = "password123"
        val user = User(
            email = email,
            password = "encodedPassword",
            phone = null,
            firstName = "JunYoung",
            lastName = "Kim",
            status = User.Status.ACTIVE,
            deactivatedAt = null,
        )
        user.id = 1L
        val accessToken = randomUUID().toString()
        val accessTokenExpiredAt = Instant.now().plusSeconds(3600)
        val refreshToken = randomUUID().toString()
        val refreshTokenExpiredAt = Instant.now().plusSeconds(2592000)
        doReturn(user).`when`(userService).getOrNullByEmail(email)
        doReturn(true).`when`(passwordEncoder).matches(password, user.password)
        doReturn(JwtService.Token(accessToken, accessTokenExpiredAt)).`when`(jwtService)
            .generateAccessToken(user.id, user.email)
        doReturn(JwtService.Token(refreshToken, refreshTokenExpiredAt)).`when`(jwtService)
            .generateRefreshToken(user.id, user.email)

        mockMvc.perform(
            post("/v1/login").contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password"
                    }
                """.trimIndent()
                )
        ).andExpect(status().isOk)
            .andDo(print())
            .andDo(
                document(
                    "login",
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("email").description("User email, required"),
                        fieldWithPath("password").description("password, required"),
                    ),
                    responseFields(
                        fieldWithPath("accessToken").description("accessToken"),
                        fieldWithPath("accessTokenExpiresAt").description("accessTokenExpiresAt"),
                        fieldWithPath("refreshToken").description("refreshToken"),
                        fieldWithPath("refreshTokenExpiresAt").description("refreshTokenExpiresAt"),
                    )
                )
            )
    }
}