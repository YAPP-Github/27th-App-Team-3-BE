package com.yapp.love.domain.user.repository

import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.domain.user.model.UserSocialAccount

interface UserSocialAccountRepository {
    fun findByProviderAndProviderId(
        provider: SocialProvider,
        providerId: String,
    ): UserSocialAccount?

    fun save(userSocialAccount: UserSocialAccount): UserSocialAccount
}
