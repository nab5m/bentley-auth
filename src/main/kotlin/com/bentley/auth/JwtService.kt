package com.bentley.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Clock
import java.time.Instant
import java.util.Base64

@Service
class JwtService {

    @Value("\${jwt.public-key}")
    private lateinit var publicKeyContent: String

    @Value("\${jwt.private-key}")
    private lateinit var privateKeyContent: String

    companion object {
        const val ISSUER = "bentley-auth"
    }

    data class Token(
        val token: String,
        val expiresAt: Instant,
    )

    fun verify(token: String): DecodedJWT {
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(
            X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent))
        ) as RSAPublicKey

        return JWT.require(Algorithm.RSA256(publicKey, null))
            .withIssuer(ISSUER)
            .build()
            .verify(token)
    }

    fun generateAccessToken(userId: Long): Token {
        val now = Clock.systemDefaultZone().instant()
        val expiresAt = now.plusSeconds(3600L) // 1 hour

        val token = generateToken(userId, now, expiresAt)
        return Token(token, expiresAt)
    }

    fun generateRefreshToken(userId: Long): Token {
        val now = Clock.systemDefaultZone().instant()
        val expiresAt = now.plusSeconds(2592000L)   // 30 days

        val token = generateToken(userId, now, expiresAt)
        return Token(token, expiresAt)
    }

    private fun generateToken(userId: Long, now: Instant, expiresAt: Instant): String {
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey =
            keyFactory.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent))) as RSAPublicKey
        val privateKey = keyFactory.generatePrivate(
            PKCS8EncodedKeySpec(
                Base64.getDecoder().decode(privateKeyContent)
            )
        ) as RSAPrivateKey

        return JWT.create()
            .withSubject(userId.toString())
            .withIssuer(ISSUER)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(Algorithm.RSA256(publicKey, privateKey))
    }
}