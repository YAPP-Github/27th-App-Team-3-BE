package com.yapp.love.infrastructure.oauth.apple

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.SignedJWT
import com.yapp.love.application.auth.port.OAuthProvider
import com.yapp.love.application.auth.port.OAuthUserInfo
import com.yapp.love.application.auth.port.SocialRefreshTokenProvider
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.infrastructure.oauth.apple.config.AppleOauthProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.Date

private val logger = KotlinLogging.logger {}

@Component
class AppleOAuthService(
    private val appleOauthClient: AppleOauthClient,
    private val appleProperties: AppleOauthProperties,
    private val applePublicKeyProvider: ApplePublicKeyProvider,
) : OAuthProvider, SocialRefreshTokenProvider {

    companion object {
        private const val APPLE_ISSUER = "https://appleid.apple.com"
    }

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
        return verifyAndExtractUserInfo(idToken)
    }

    private fun verifyAndExtractUserInfo(
        idToken: String,
        socialRefreshToken: String? = null,
    ): OAuthUserInfo {
        val signedJWT = try {
            SignedJWT.parse(idToken)
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse Apple ID token as JWT" }
            throw IllegalArgumentException("Invalid JWT format")
        }

        // 1. 서명 검증
        verifySignature(signedJWT)

        // 2. 클레임 검증
        val claims = signedJWT.jwtClaimsSet

        // issuer 검증
        if (claims.issuer != APPLE_ISSUER) {
            logger.error { "Apple ID token issuer mismatch: ${claims.issuer}" }
            throw IllegalStateException("Invalid Apple ID token issuer")
        }

        // audience 검증
        if (!claims.audience.contains(appleProperties.clientId)) {
            logger.error { "Apple ID token audience mismatch: ${claims.audience}" }
            throw IllegalStateException("Invalid Apple ID token audience")
        }

        // 만료 검증
        if (claims.expirationTime == null || claims.expirationTime.before(Date())) {
            logger.error { "Apple ID token expired: ${claims.expirationTime}" }
            throw IllegalStateException("Apple ID token has expired")
        }

        val providerId = claims.subject
            ?: throw IllegalStateException("Apple ID token missing 'sub' claim")

        return OAuthUserInfo(
            providerId = providerId,
            email = claims.getStringClaim("email"),
            socialRefreshToken = socialRefreshToken,
        )
    }

    private fun verifySignature(signedJWT: SignedJWT) {
        val header = signedJWT.header
        val kid = header.keyID
            ?: throw IllegalStateException("Apple ID token missing 'kid' in header")

        val publicKey = applePublicKeyProvider.getPublicKey(kid)
        val verifier: JWSVerifier = RSASSAVerifier(publicKey)

        if (!signedJWT.verify(verifier)) {
            logger.error { "Apple ID token signature verification failed" }
            throw IllegalStateException("Invalid Apple ID token signature")
        }

        logger.debug { "Apple ID token signature verified successfully" }
    }
}
