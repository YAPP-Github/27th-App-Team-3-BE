package com.yapp.love.domain.user.repository

import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.domain.user.model.User

interface UserRepository {
    fun save(user: User): User

    fun findById(id: Long): User?

    fun findByOauthProviderAndOauthProviderId(
        oauthProvider: SocialProvider,
        oauthProviderId: String,
    ): User?
}
