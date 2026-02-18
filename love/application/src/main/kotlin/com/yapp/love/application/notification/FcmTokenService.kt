package com.yapp.love.application.notification

import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.model.FcmToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FcmTokenService(
    private val fcmTokenRepository: FcmTokenRepository,
) {
    @Transactional
    fun registerToken(userId: Long, token: String, deviceId: String) {
        val existingToken = fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId)

        if (existingToken != null) {
            existingToken.updateToken(token)
            fcmTokenRepository.save(existingToken)
        } else {
            fcmTokenRepository.save(FcmToken.create(userId, token, deviceId))
        }
    }
}