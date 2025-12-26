package com.yapp.love.infrastructure.oauth.apple.service

import com.yapp.love.domain.auth.adapter.OAuthUserInfo
import com.yapp.love.infrastructure.oauth.apple.config.AppleOauthProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

private val logger = KotlinLogging.logger {}

@Service
class AppleIdTokenVerifier(
    webClientBuilder: WebClient.Builder,
    private val appleProperties: AppleOauthProperties,
) {
    private val webClient: WebClient =
        webClientBuilder
            .baseUrl(APPLE_KEYS_URI)
            .build()

    fun verify(idToken: String): OAuthUserInfo {
        // JWT 디코딩 (서명 검증 없이 클레임만 추출)
        val parts = idToken.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid JWT format")
        }

        val payloadJson = String(java.util.Base64.getUrlDecoder().decode(parts[1]))
        val claims = parseJsonToClaims(payloadJson)

        // audience 검증
        val aud = claims["aud"] as? String
        if (aud != appleProperties.clientId) {
            logger.error { "Apple ID token audience mismatch: $aud" }
            throw IllegalStateException("Invalid Apple ID token audience")
        }

        // issuer 검증
        val iss = claims["iss"] as? String
        if (iss != "https://appleid.apple.com") {
            logger.error { "Apple ID token issuer mismatch: $iss" }
            throw IllegalStateException("Invalid Apple ID token issuer")
        }

        return OAuthUserInfo(
            providerId = claims["sub"] as String,
            email = claims["email"] as? String,
        )
    }

    private fun parseJsonToClaims(json: String): Map<String, Any> {
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        return mapper.readValue(json, Map::class.java) as Map<String, Any>
    }

    companion object {
        private const val APPLE_KEYS_URI = "https://appleid.apple.com/auth/keys"
    }
}
