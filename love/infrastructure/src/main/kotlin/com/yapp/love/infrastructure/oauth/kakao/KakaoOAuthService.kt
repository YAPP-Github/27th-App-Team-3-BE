package com.yapp.love.infrastructure.oauth.kakao

import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.SignedJWT
import com.yapp.love.application.auth.port.OAuthProvider
import com.yapp.love.application.auth.port.OAuthUserInfo
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.infrastructure.oauth.kakao.config.KakaoOAuthProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.Date

private val logger = KotlinLogging.logger {}

@Component
class KakaoOAuthService(
    private val kakaoProperties: KakaoOAuthProperties,
    private val kakaoPublicKeyProvider: KakaoPublicKeyProvider,
) : OAuthProvider {
    companion object {
        private const val KAKAO_ISSUER = "https://kauth.kakao.com"
    }

    override fun getProviderType(): SocialProvider = SocialProvider.KAKAO

    override fun authenticateWithIdToken(idToken: String): OAuthUserInfo {
        return verifyAndExtractUserInfo(idToken)
    }

    private fun verifyAndExtractUserInfo(idToken: String): OAuthUserInfo {
        val signedJWT = try {
            SignedJWT.parse(idToken)
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse Kakao ID token as JWT" }
            throw IllegalArgumentException("Invalid JWT format")
        }

        verifySignature(signedJWT)

        val claims = signedJWT.jwtClaimsSet

        if (claims.issuer != KAKAO_ISSUER) {
            logger.error { "Kakao ID token issuer mismatch: ${claims.issuer}" }
            throw IllegalStateException("Invalid Kakao ID token issuer")
        }

        if (!claims.audience.contains(kakaoProperties.clientId)) {
            logger.error { "Kakao ID token audience mismatch: ${claims.audience}" }
            throw IllegalStateException("Invalid Kakao ID token audience")
        }

        if (claims.expirationTime == null || claims.expirationTime.before(Date())) {
            logger.error { "Kakao ID token expired: ${claims.expirationTime}" }
            throw IllegalStateException("Kakao ID token has expired")
        }

        val providerId = claims.subject
            ?: throw IllegalStateException("Kakao ID token missing 'sub' claim")

        return OAuthUserInfo(
            providerId = providerId,
            email = claims.getStringClaim("email"),
        )
    }

    private fun verifySignature(signedJWT: SignedJWT) {
        val kid = signedJWT.header.keyID
            ?: throw IllegalStateException("Kakao ID token missing 'kid' in header")

        val publicKey = kakaoPublicKeyProvider.getPublicKey(kid)
        val verifier = RSASSAVerifier(publicKey)

        if (!signedJWT.verify(verifier)) {
            logger.error { "Kakao ID token signature verification failed" }
            throw IllegalStateException("Invalid Kakao ID token signature")
        }

        logger.debug { "Kakao ID token signature verified successfully" }
    }
}
