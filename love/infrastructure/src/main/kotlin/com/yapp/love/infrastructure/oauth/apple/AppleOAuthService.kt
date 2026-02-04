package com.yapp.love.infrastructure.oauth.apple

import com.fasterxml.jackson.databind.ObjectMapper
import com.yapp.love.application.auth.port.OAuthProvider
import com.yapp.love.application.auth.port.OAuthUserInfo
import com.yapp.love.application.auth.port.SocialRefreshTokenProvider
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.infrastructure.oauth.apple.config.AppleOauthProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class AppleOAuthService(
    private val appleOauthClient: AppleOauthClient,
    private val appleProperties: AppleOauthProperties,
) : OAuthProvider, SocialRefreshTokenProvider {
    override fun getProviderType(): SocialProvider = SocialProvider.APPLE

    override fun revokeToken(refreshToken: String) {
        appleOauthClient.revokeToken(refreshToken)
    }

    override fun exchangeCodeForRefreshToken(authorizationCode: String): String? {
        return try {
            val tokenResponse = appleOauthClient.exchangeCodeForToken(authorizationCode)
            tokenResponse.refreshToken
        } catch (e: Exception) {
            logger.error(e) { "Failed to exchange authorization code for refresh token" }
            null
        }
    }

    override fun authenticateWithIdToken(idToken: String): OAuthUserInfo {
        return verifyAndExtractUserInfo(idToken = idToken)
    }

    private fun verifyAndExtractUserInfo(
        idToken: String,
        socialRefreshToken: String? = null,
    ): OAuthUserInfo {
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
            socialRefreshToken = socialRefreshToken,
        )
    }

    private fun parseJsonToClaims(json: String): Map<String, Any> {
        val mapper = ObjectMapper()
        return mapper.readValue(json, Map::class.java) as Map<String, Any>
    }
}
