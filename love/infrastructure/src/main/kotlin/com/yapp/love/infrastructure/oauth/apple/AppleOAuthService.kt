package com.yapp.love.infrastructure.oauth.apple

import com.yapp.love.application.auth.OAuthProvider
import com.yapp.love.application.auth.OAuthUserInfo
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.infrastructure.oauth.apple.service.AppleIdTokenVerifier
import org.springframework.stereotype.Component

@Component
class AppleOAuthService(
    private val appleOauthClient: AppleOauthClient,
    private val appleIdTokenVerifier: AppleIdTokenVerifier,
) : OAuthProvider {
    override fun getProviderType(): SocialProvider = SocialProvider.APPLE

    override fun authenticate(code: String): OAuthUserInfo {
        val tokenResponse = appleOauthClient.exchangeCodeForToken(code)
        val oauthUserInfo = appleIdTokenVerifier.verify(tokenResponse.idToken)

        return oauthUserInfo
    }
}
