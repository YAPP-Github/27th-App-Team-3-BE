package com.yapp.love.domain.user.repository

import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.domain.user.model.SocialToken

interface SocialTokenRepository {
    fun save(socialToken: SocialToken): SocialToken

    fun findByUserIdAndProvider(
        userId: Long,
        provider: SocialProvider,
    ): SocialToken?

    fun deleteByUserIdAndProvider(
        userId: Long,
        provider: SocialProvider,
    )

    fun deleteByUserId(userId: Long)
}