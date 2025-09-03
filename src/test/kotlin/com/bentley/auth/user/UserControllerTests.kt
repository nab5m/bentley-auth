package com.bentley.auth.user

import com.bentley.auth.JwtService
import com.bentley.auth.OAuth2ClientService
import com.bentley.auth.RefreshTokenService
import com.bentley.auth.TestSecurityConfig
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.constraints.ConstraintDescriptions
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.Instant
import java.util.UUID
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
        Mockito.doReturn("encodedPassword").`when`(passwordEncoder).encode(ArgumentMatchers.any())

        val userConstraints = ConstraintDescriptions(User::class.java)

        mockMvc.perform(
            RestDocumentationRequestBuilders.post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(
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
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
            .andDo(
                MockMvcRestDocumentation.document(
                    "user-create",
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("email")
                            .description(userConstraints.descriptionsForProperty("email").joinToString(", ")),
                        PayloadDocumentation.fieldWithPath("password")
                            .description(userConstraints.descriptionsForProperty("password").joinToString(", ")),
                        PayloadDocumentation.fieldWithPath("phone").description("phone").optional(),
                        PayloadDocumentation.fieldWithPath("firstName").description("firstName").optional(),
                        PayloadDocumentation.fieldWithPath("lastName").description("lastName").optional(),
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
        val accessToken = UUID.randomUUID().toString()
        val accessTokenExpiredAt = Instant.now().plusSeconds(3600)
        val refreshToken = UUID.randomUUID().toString()
        val refreshTokenExpiredAt = Instant.now().plusSeconds(2592000)
        Mockito.doReturn(user).`when`(userService).getOrNullByEmail(email)
        Mockito.doReturn(true).`when`(passwordEncoder).matches(password, user.password)
        Mockito.doReturn(JwtService.Token(accessToken, accessTokenExpiredAt)).`when`(jwtService)
            .generateAccessToken(user.id)
        Mockito.doReturn(JwtService.Token(refreshToken, refreshTokenExpiredAt)).`when`(jwtService)
            .generateRefreshToken(user.id)

        mockMvc.perform(
            RestDocumentationRequestBuilders.post("/v1/user/login").contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "$email",
                        "password": "$password"
                    }
                """.trimIndent()
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
            .andDo(
                MockMvcRestDocumentation.document(
                    "login",
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("email").description("User email, required"),
                        PayloadDocumentation.fieldWithPath("password").description("password, required"),
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("accessToken").description("accessToken"),
                        PayloadDocumentation.fieldWithPath("accessTokenExpiresAt").description("accessTokenExpiresAt"),
                        PayloadDocumentation.fieldWithPath("refreshToken").description("refreshToken"),
                        PayloadDocumentation.fieldWithPath("refreshTokenExpiresAt")
                            .description("refreshTokenExpiresAt"),
                    )
                )
            )
    }
}