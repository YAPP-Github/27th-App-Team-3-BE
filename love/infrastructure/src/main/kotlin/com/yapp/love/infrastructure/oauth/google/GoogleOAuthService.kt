package com.yapp.love.infrastructure.oauth.google

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.yapp.love.application.auth.port.OAuthProvider
import com.yapp.love.application.auth.port.OAuthUserInfo
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.infrastructure.oauth.google.config.GoogleOAuthProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class GoogleOAuthService(
    private val googleOAuthClient: GoogleOAuthClient,
    private val googleProperties: GoogleOAuthProperties,
) : OAuthProvider {
    private val verifier =
        GoogleIdTokenVerifier.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
        )
            .setAudience(listOf(googleProperties.clientId))
            .build()

    override fun getProviderType(): SocialProvider = SocialProvider.GOOGLE

    override fun authenticate(code: String): OAuthUserInfo {
        val tokenResponse = googleOAuthClient.exchangeCodeForToken(code)
        return verifyIdToken(tokenResponse.idToken)
    }

    private fun verifyIdToken(idToken: String): OAuthUserInfo {
        val idToken =
            verifier.verify(idToken)
                ?: throw IllegalStateException("Invalid Google ID token")

        val payload = idToken.payload

        return OAuthUserInfo(
            providerId = payload.subject,
            email = payload.email,
        )
    }
}
