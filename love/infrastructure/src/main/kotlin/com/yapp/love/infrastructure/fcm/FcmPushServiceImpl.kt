package com.yapp.love.infrastructure.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.Notification
import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.FcmTokenRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class FcmPushServiceImpl(
    private val firebaseMessaging: FirebaseMessaging,
    private val fcmTokenRepository: FcmTokenRepository,
) : FcmPushService {
    override fun sendPushToUser(
        userId: Long,
        title: String,
        body: String,
        deepLink: String?,
    ) {
        val tokens = fcmTokenRepository.findByUserId(userId)
        if (tokens.isEmpty()) {
            logger.warn { "FCM 토큰이 없습니다: userId=$userId" }
            return
        }

        tokens.forEach { fcmToken ->
            try {
                val message = buildFcmMessage(fcmToken.token, title, body, deepLink)
                val response = firebaseMessaging.send(message)
                logger.info { "FCM 전송 성공: userId=$userId, messageId=$response" }
            } catch (e: FirebaseMessagingException) {
                if (e.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
                    fcmTokenRepository.delete(fcmToken)
                    logger.info { "만료 토큰 삭제: userId=$userId, token=${fcmToken.token}" }
                } else {
                    logger.error(e) { "FCM 전송 실패: userId=$userId, token=${fcmToken.token}" }
                }
            }
        }
    }

    override fun sendToTopic(
        topic: String,
        title: String,
        body: String,
        deepLink: String?,
    ) {
        try {
            val message =
                Message.builder()
                    .setTopic(topic)
                    .setNotification(
                        Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build(),
                    )
                    .apply { if (deepLink != null) putData("deepLink", deepLink) }
                    .build()
            val response = firebaseMessaging.send(message)
            logger.info { "토픽 푸시 전송 성공: topic=$topic, messageId=$response" }
        } catch (e: FirebaseMessagingException) {
            logger.error(e) { "토픽 푸시 전송 실패: topic=$topic" }
        }
    }

    override fun subscribeToTopic(token: String, topic: String) {
        try {
            firebaseMessaging.subscribeToTopic(listOf(token), topic)
            logger.info { "토픽 구독 완료: token=$token, topic=$topic" }
        } catch (e: FirebaseMessagingException) {
            logger.error(e) { "토픽 구독 실패: token=$token, topic=$topic" }
        }
    }

    override fun unsubscribeFromTopic(token: String, topic: String) {
        try {
            firebaseMessaging.unsubscribeFromTopic(listOf(token), topic)
            logger.info { "토픽 구독 해제 완료: token=$token, topic=$topic" }
        } catch (e: FirebaseMessagingException) {
            logger.error(e) { "토픽 구독 해제 실패: token=$token, topic=$topic" }
        }
    }

    private fun buildFcmMessage(
        token: String,
        title: String,
        body: String,
        deepLink: String?,
    ): Message {
        val builder =
            Message.builder()
                .setToken(token)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build(),
                )

        if (deepLink != null) {
            builder.putData("deepLink", deepLink)
        }

        return builder.build()
    }
}