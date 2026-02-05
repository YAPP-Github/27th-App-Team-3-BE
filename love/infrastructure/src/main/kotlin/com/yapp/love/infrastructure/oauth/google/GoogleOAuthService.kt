package com.yapp.love.infrastructure.oauth.google

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.yapp.love.application.auth.port.OAuthProvider
import com.yapp.love.application.auth.port.OAuthUserInfo
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.infrastructure.oauth.google.config.GoogleOAuthProperties
import org.springframework.stereotype.Component

@Component
class GoogleOAuthService(
    private val googleProperties: GoogleOAuthProperties,
) : OAuthProvider {

    private val verifier =
        GoogleIdTokenVerifier.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
        )
            .setAudience(
                listOf(
                    googleProperties.clientId,
                    googleProperties.iosClientId,
                ) + googleProperties.androidClientIds
            )
            .build()

    override fun getProviderType(): SocialProvider = SocialProvider.GOOGLE

    override fun authenticateWithIdToken(idToken: String): OAuthUserInfo {
        return verifyAndExtractUserInfo(idToken)
    }

    private fun verifyAndExtractUserInfo(idToken: String): OAuthUserInfo {
        val verifiedToken =
            verifier.verify(idToken)
                ?: throw IllegalStateException("Invalid Google ID token")

        val payload = verifiedToken.payload

        return OAuthUserInfo(
            providerId = payload.subject,
            email = payload.email,
        )
    }
}
