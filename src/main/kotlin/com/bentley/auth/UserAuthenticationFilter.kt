package com.bentley.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

// TODO: user 모듈로 이동하고 싶음. filter 등록을 interface로 추상화해서 적용
class UserAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring("Bearer ".length)
            val decodedJWT = jwtService.verify(token)
            val userType = decodedJWT.claims["userType"]?.asString()
                ?: throw IllegalArgumentException("Invalid token: missing userType claim")
            val userId = decodedJWT.claims["userId"]?.asLong()
                ?: throw IllegalArgumentException("Invalid token: missing userId claim")

            val principal = UserPrincipal(userId)

            val authentication = UsernamePasswordAuthenticationToken(
                principal, null,
                listOf(GrantedAuthority { "ROLE_$userType" })
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}
