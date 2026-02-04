package com.yapp.love.application.auth.port

import com.yapp.love.domain.user.model.SocialProvider

interface SocialRefreshTokenProvider {
    fun getProviderType(): SocialProvider

    fun exchangeCodeForRefreshToken(authorizationCode: String): String?

    fun revokeToken(refreshToken: String)
}