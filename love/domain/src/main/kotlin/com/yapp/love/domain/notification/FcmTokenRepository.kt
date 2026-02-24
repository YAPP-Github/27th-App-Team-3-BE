package com.yapp.love.domain.notification

import com.yapp.love.domain.notification.model.FcmToken

interface FcmTokenRepository {
    fun save(fcmToken: FcmToken): FcmToken

    fun findByUserId(userId: Long): List<FcmToken>

    fun findByUserIdIn(userIds: List<Long>): List<FcmToken>

    fun findByUserIdAndDeviceId(
        userId: Long,
        deviceId: String,
    ): FcmToken?

    fun findByUserIdAndToken(userId: Long, token: String): FcmToken?

    fun findByDeviceIdAndUserIdNot(deviceId: String, userId: Long): List<FcmToken>

    fun delete(fcmToken: FcmToken)

    fun deleteAll(fcmTokens: List<FcmToken>)

    fun deleteByTokenAndUserIdNot(token: String, userId: Long)

    fun deleteByUserId(userId: Long)
}
