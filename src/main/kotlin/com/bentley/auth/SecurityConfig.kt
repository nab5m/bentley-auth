package com.bentley.auth

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig {

    @Bean
    @ConditionalOnMissingBean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtService: JwtService): SecurityFilterChain {
        http {
            csrf {
                disable()
            }
            authorizeHttpRequests {
                authorize(HttpMethod.POST, "/v1/user", permitAll)
                authorize("/v1/user/login", permitAll)
                authorize("/v1/refresh-token", permitAll)
                authorize("/v1/user/resend-email-verification", permitAll)
                authorize("/v1/user/verify-email", permitAll)
                authorize("/v1/user/update-password", permitAll)
                authorize("/v1/user/oauth2/{registrationId}/login", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/error", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2Login {

            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(HostAuthenticationFilter(jwtService))
            addFilterBefore<UsernamePasswordAuthenticationFilter>(UserAuthenticationFilter(jwtService))
        }

        return http.build()
    }
}
