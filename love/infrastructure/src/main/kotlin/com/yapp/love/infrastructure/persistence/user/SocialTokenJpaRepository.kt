package com.yapp.love.infrastructure.persistence.user

import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.domain.user.model.SocialToken
import com.yapp.love.domain.user.repository.SocialTokenRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SocialTokenJpaRepository : SocialTokenRepository, JpaRepository<SocialToken, Long> {
    override fun findByUserIdAndProvider(
        userId: Long,
        provider: SocialProvider,
    ): SocialToken?

    override fun deleteByUserIdAndProvider(
        userId: Long,
        provider: SocialProvider,
    )

    override fun deleteByUserId(userId: Long)
}