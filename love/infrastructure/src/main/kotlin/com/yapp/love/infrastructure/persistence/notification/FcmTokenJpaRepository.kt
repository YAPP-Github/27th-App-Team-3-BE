package com.yapp.love.infrastructure.persistence.notification

import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.model.FcmToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface FcmTokenJpaRepositoryInterface : JpaRepository<FcmToken, Long> {
    fun findByUserId(userId: Long): List<FcmToken>

    fun findByUserIdAndDeviceId(
        userId: Long,
        deviceId: String,
    ): FcmToken?

    fun findByUserIdAndToken(userId: Long, token: String): FcmToken?

    fun deleteByTokenAndUserIdNot(token: String, userId: Long)

    fun deleteByUserId(userId: Long)
}

@Repository
class FcmTokenJpaRepository(
    private val jpaRepository: FcmTokenJpaRepositoryInterface,
) : FcmTokenRepository {
    override fun save(fcmToken: FcmToken): FcmToken {
        return jpaRepository.save(fcmToken)
    }

    override fun findByUserId(userId: Long): List<FcmToken> {
        return jpaRepository.findByUserId(userId)
    }

    override fun findByUserIdAndDeviceId(
        userId: Long,
        deviceId: String,
    ): FcmToken? {
        return jpaRepository.findByUserIdAndDeviceId(userId, deviceId)
    }

    override fun delete(fcmToken: FcmToken) {
        jpaRepository.delete(fcmToken)
    }

    override fun findByUserIdAndToken(userId: Long, token: String): FcmToken? {
        return jpaRepository.findByUserIdAndToken(userId, token)
    }

    override fun deleteByTokenAndUserIdNot(token: String, userId: Long) {
        jpaRepository.deleteByTokenAndUserIdNot(token, userId)
    }

    override fun deleteByUserId(userId: Long) {
        jpaRepository.deleteByUserId(userId)
    }
}
