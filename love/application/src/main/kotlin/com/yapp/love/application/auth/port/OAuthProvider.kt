package com.yapp.love.application.auth.port

import com.yapp.love.domain.user.model.SocialProvider

interface OAuthProvider {
    fun getProviderType(): SocialProvider

    fun authenticate(code: String): OAuthUserInfo
}

data class OAuthUserInfo(
    val providerId: String,
    val email: String? = null,
)
