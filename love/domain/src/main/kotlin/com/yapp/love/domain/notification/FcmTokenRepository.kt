package com.yapp.love.domain.notification

import com.yapp.love.domain.notification.model.FcmToken

interface FcmTokenRepository {
    fun save(fcmToken: FcmToken): FcmToken

    fun findByUserId(userId: Long): List<FcmToken>

    fun findByUserIdAndDeviceId(
        userId: Long,
        deviceId: String,
    ): FcmToken?

    fun deleteByUserId(userId: Long)
}
