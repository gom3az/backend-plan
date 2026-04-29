package com.gomaa.tasks.services

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey
import kotlin.time.Duration.Companion.hours

@Service
class TokenService(
    @Value($$"${jwt.secret}") private val jwtSecret: String
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))
    }

    private val expirationMs: Long = 1.hours.inWholeMilliseconds

    fun generateToken(username: String, roles: List<String>): String {
        val now = Date()
        val expiration = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String {
        return extractClaims(token).subject
    }

    @Suppress("UNCHECKED_CAST")
    fun extractRoles(token: String): List<String> {
        return extractClaims(token).get("roles", List::class.java) as? List<String> ?: emptyList()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = extractClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}