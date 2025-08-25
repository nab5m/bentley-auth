package com.bentley.auth.user

import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OAuth2ClientService(
    private val clientRegistrationRepository: ClientRegistrationRepository,
) {
    fun exchange(registrationId: String, authorizationCode: String, state: String?): OAuth2AccessTokenResponse {
        val client = clientRegistrationRepository.findByRegistrationId(registrationId)
            ?: throw IllegalArgumentException("Unknown registrationId: $registrationId")

        val authRequest = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(client.providerDetails.authorizationUri)
            .clientId(client.clientId)
            .redirectUri("http://localhost:8080/v1/oauth2/google/login")
            .scopes(client.scopes)
            .state(state)
            .build()

        val authResponse = OAuth2AuthorizationResponse.success(authorizationCode)
            .redirectUri("http://localhost:8080/v1/oauth2/google/login")
            .state(state)
            .build()

        val exchange = OAuth2AuthorizationExchange(authRequest, authResponse)
        val grant = OAuth2AuthorizationCodeGrantRequest(client, exchange)

        return RestClientAuthorizationCodeTokenResponseClient().getTokenResponse(grant)
    }

    fun loadUser(
        registrationId: String,
        token: OAuth2AccessTokenResponse
    ): OAuth2User {
        val client = clientRegistrationRepository.findByRegistrationId(registrationId)
            ?: throw IllegalArgumentException("Unknown registrationId: $registrationId")

        when (registrationId) {
            "google", "apple" -> {
                val idToken = token.additionalParameters["id_token"] as? String
                    ?: throw IllegalArgumentException("ID Token not found in token response")

                val jwkSetUri = when (registrationId) {
                    "google" -> "https://www.googleapis.com/oauth2/v3/certs"
                    "apple" -> "https://appleid.apple.com/auth/keys"
                    else -> throw IllegalArgumentException("Unsupported registrationId: $registrationId")
                }
                val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                    .jwsAlgorithm(SignatureAlgorithm.RS256)
                    .build()
                val decodedJwt = jwtDecoder.decode(idToken)

                val oidcIdToken =
                    OidcIdToken(idToken, Instant.now(), Instant.now().plusSeconds(3600), decodedJwt.claims)
                val req = OidcUserRequest(client, token.accessToken, oidcIdToken, token.additionalParameters)
                return OidcUserService().loadUser(req)
            }
            else -> {
                val req = OAuth2UserRequest(client, token.accessToken, token.additionalParameters)
                return DefaultOAuth2UserService().loadUser(req)
            }
        }
    }

    fun getSubject(registrationId: String, oAuth2User: OAuth2User): String? {
        return when (registrationId) {
            "kakao" -> oAuth2User.attributes["id"]?.toString()
            else -> oAuth2User.attributes["sub"] as? String
        }
    }

    fun getEmail(registrationId: String, oAuth2User: OAuth2User): String? {
        return when (registrationId) {
            "kakao" -> {
                val kakaoAccount = oAuth2User.attributes["kakao_account"] as? Map<*, *>
                kakaoAccount?.get("email") as? String
            }
            else -> oAuth2User.attributes["email"] as? String
        }
    }

    fun getFirstName(registrationId: String, oAuth2User: OAuth2User): String? {
        return when (registrationId) {
            "kakao" -> {
                val kakaoAccount = oAuth2User.attributes["kakao_account"] as? Map<*, *>
                val profile = kakaoAccount?.get("profile") as? Map<*, *>
                profile?.get("nickname") as? String
            }
            else -> oAuth2User.attributes["given_name"] as? String
        }
    }

    fun getLastName(registrationId: String, oAuth2User: OAuth2User): String? {
        return when (registrationId) {
            "kakao" -> null
            else -> oAuth2User.attributes["family_name"] as? String
        }
    }
}