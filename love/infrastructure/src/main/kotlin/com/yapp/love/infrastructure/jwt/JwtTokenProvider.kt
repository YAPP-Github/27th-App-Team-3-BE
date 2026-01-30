package com.yapp.love.infrastructure.jwt

import com.yapp.love.application.auth.port.TokenProvider
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val secret: String,
    @Value("\${jwt.access-token-expiration}")
    private val accessTokenExpiration: Long,
    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenExpiration: Long,
) : TokenProvider {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    override fun createAccessToken(userId: Long): String {
        return createToken(userId, accessTokenExpiration, TOKEN_TYPE_ACCESS)
    }

    override fun createRefreshToken(userId: Long): String {
        return createToken(userId, refreshTokenExpiration, TOKEN_TYPE_REFRESH)
    }

    override fun validateToken(token: String): Boolean {
        return try {
            val claims = parseToken(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    override fun getUserIdFromToken(token: String): Long {
        val claims = parseToken(token)
        return claims.subject.toLong()
    }

    override fun getTokenType(token: String): String {
        val claims = parseToken(token)
        return claims[CLAIM_TOKEN_TYPE] as? String ?: TOKEN_TYPE_ACCESS
    }

    override fun getRemainingExpirationTime(token: String): Long {
        return try {
            val claims = parseToken(token)
            val expiration = claims.expiration.time
            val now = Date().time
            maxOf(0, expiration - now)
        } catch (e: Exception) {
            0
        }
    }

    private fun createToken(
        userId: Long,
        expiration: Long,
        tokenType: String,
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim(CLAIM_TOKEN_TYPE, tokenType)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    companion object {
        const val TOKEN_TYPE_ACCESS = "access"
        const val TOKEN_TYPE_REFRESH = "refresh"
        private const val CLAIM_TOKEN_TYPE = "type"
    }
}
