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
        // 같은 기기의 다른 유저 레코드 삭제 (기기 계정 전환 대응)
        val oldTokens = fcmTokenRepository.findByDeviceIdAndUserIdNot(deviceId, userId)
        fcmTokenRepository.deleteAll(oldTokens)

        // 같은 토큰을 가진 다른 유저 레코드 삭제 (토큰 미갱신 케이스 대응)
        fcmTokenRepository.deleteByTokenAndUserIdNot(token, userId)

        val existingToken = fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId)

        if (existingToken != null) {
            existingToken.updateToken(token)
            fcmTokenRepository.save(existingToken)
        } else {
            fcmTokenRepository.save(FcmToken.create(userId, token, deviceId))
        }
    }

    @Transactional
    fun deleteToken(userId: Long, token: String) {
        val fcmToken = fcmTokenRepository.findByUserIdAndToken(userId, token) ?: return
        fcmTokenRepository.delete(fcmToken)
    }
}