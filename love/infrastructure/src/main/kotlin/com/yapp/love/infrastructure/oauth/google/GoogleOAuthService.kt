package com.yapp.love.infrastructure.oauth.google

import com.yapp.love.application.auth.OAuthProvider
import com.yapp.love.application.auth.OAuthUserInfo
import com.yapp.love.domain.auth.adapter.OAuthProvider
import com.yapp.love.domain.auth.adapter.OAuthUserInfo
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.infrastructure.oauth.google.dto.GoogleTokenResult
import org.springframework.stereotype.Component

@Component
class GoogleOAuthService(
    private val googleOAuthClient: GoogleOAuthClient,
    private val googleIdTokenVerifier: GoogleIdTokenVerifier,
) : OAuthProvider {
    override fun getProviderType(): SocialProvider = SocialProvider.GOOGLE

    override fun authenticate(code: String): OAuthUserInfo {
        val tokenResponse = googleOAuthClient.exchangeCodeForToken(code)
        val oauthUserInfo = googleIdTokenVerifier.verify(tokenResponse.idToken)

        return oauthUserInfo
    }

    fun exchangeCodeForToken(code: String): GoogleTokenResult {
        val response = googleOAuthClient.exchangeCodeForToken(code)
        return GoogleTokenResult(
            accessToken = response.accessToken,
            idToken = response.idToken,
            refreshToken = response.refreshToken,
        )
    }

    fun verifyIdToken(idToken: String): OAuthUserInfo {
        return googleIdTokenVerifier.verify(idToken)
    }
}
