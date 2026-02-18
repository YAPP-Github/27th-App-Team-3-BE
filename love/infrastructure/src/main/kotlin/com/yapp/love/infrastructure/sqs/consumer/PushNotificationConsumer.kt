package com.yapp.love.infrastructure.sqs.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.infrastructure.sqs.message.PushNotificationMessage
import io.awspring.cloud.sqs.annotation.SqsListener
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class PushNotificationConsumer(
    private val firebaseMessaging: FirebaseMessaging,
    private val fcmTokenRepository: FcmTokenRepository,
    private val objectMapper: ObjectMapper,
) {
    @SqsListener("\${sqs.queue.notification}")
    fun handlePushNotification(jsonMessage: String) {
        val message = objectMapper.readValue(jsonMessage, PushNotificationMessage::class.java)
        logger.info { "푸시 알림 메시지 수신: userId=${message.userId}" }

        val tokens = fcmTokenRepository.findByUserId(message.userId)
        if (tokens.isEmpty()) {
            logger.warn { "FCM 토큰이 없습니다: userId=${message.userId}" }
            return
        }

        tokens.forEach { fcmToken ->
            try {
                val fcmMessage = buildFcmMessage(fcmToken.token, message)
                val response = firebaseMessaging.send(fcmMessage)
                logger.info { "FCM 전송 성공: userId=${message.userId}, messageId=$response" }
            } catch (e: Exception) {
                logger.error(e) { "FCM 전송 실패: userId=${message.userId}, token=${fcmToken.token}" }
            }
        }
    }

    private fun buildFcmMessage(
        token: String,
        message: PushNotificationMessage,
    ): Message {
        val builder =
            Message.builder()
                .setToken(token)
                .setNotification(
                    Notification.builder()
                        .setTitle(message.title)
                        .setBody(message.body)
                        .build(),
                )

        val data = message.data.toMutableMap()
        message.deepLink?.let { data["deepLink"] = it }

        if (data.isNotEmpty()) {
            builder.putAllData(data)
        }

        return builder.build()
    }
}
