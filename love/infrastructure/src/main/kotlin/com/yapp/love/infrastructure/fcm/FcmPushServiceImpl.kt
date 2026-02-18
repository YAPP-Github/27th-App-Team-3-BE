package com.yapp.love.infrastructure.fcm

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.infrastructure.sqs.message.PushNotificationMessage
import com.yapp.love.infrastructure.sqs.producer.PushNotificationProducer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class FcmPushServiceImpl(
    private val pushNotificationProducer: PushNotificationProducer,
) : FcmPushService {
    override fun sendPushToUser(
        userId: Long,
        title: String,
        body: String,
        deepLink: String?,
    ): Boolean {
        return try {
            pushNotificationProducer.sendPushNotification(
                PushNotificationMessage(
                    userId = userId,
                    title = title,
                    body = body,
                    deepLink = deepLink,
                ),
            )
            true
        } catch (e: Exception) {
            logger.error(e) { "푸시 알림 메시지 발행 실패: userId=$userId" }
            false
        }
    }
}
