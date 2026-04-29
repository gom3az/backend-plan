package com.gomaa.tasks.services

import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Service
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Collectors

@Service
class TokenService() {

    val rsaKey: RSAKey by lazy {
        generateRsa()
    }

    val jwtEncoder: JwtEncoder by lazy {
        val jwkSet = JWKSet(rsaKey)
        val jwks = JWKSource { jwkSelector: JWKSelector?, _: SecurityContext? -> jwkSelector!!.select(jwkSet) }
        NimbusJwtEncoder(jwks)
    }

    val jwtDecoder: JwtDecoder by lazy {
        NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build()
    }

    fun generateToken(authentication: Authentication): String {
        val now = Instant.now()

        val scope = authentication.authorities.stream().map { obj: GrantedAuthority? -> obj!!.authority }
            .collect(Collectors.joining(" "))

        val claims = JwtClaimsSet.builder().issuer("self").issuedAt(now).expiresAt(now.plus(1, ChronoUnit.HOURS))
            .subject(authentication.name).claim("scope", scope).build()

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }

    fun generateRsa(): RSAKey {
        val keyPair: KeyPair = generateRsaKey()
        val publicKey = keyPair.public as RSAPublicKey?
        val privateKey = keyPair.private as RSAPrivateKey?
        return RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build()
    }

    fun generateRsaKey(): KeyPair {
        val keyPair: KeyPair?
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            keyPair = keyPairGenerator.generateKeyPair()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
        return keyPair
    }
}