package com.yapp.love.infrastructure.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.model.FcmToken
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class FcmPushServiceImpl(
    private val firebaseMessaging: FirebaseMessaging,
    private val fcmTokenRepository: FcmTokenRepository,
) : FcmPushService {
    override fun sendMulticast(
        tokens: List<FcmToken>,
        title: String,
        body: String,
        deepLink: String?,
        dryRun: Boolean,
    ) {
        if (tokens.isEmpty()) return

        val notification = Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build()

        tokens.chunked(100).forEach { chunk ->
            val multicastMessage =
                MulticastMessage.builder()
                    .addAllTokens(chunk.map { it.token })
                    .setNotification(notification)
                    .apply { if (deepLink != null) putData("deepLink", deepLink) }
                    .build()
            try {
                val response = firebaseMessaging.sendEachForMulticast(multicastMessage, dryRun)
                logger.info { "멀티캐스트 전송: success=${response.successCount}, failure=${response.failureCount}" }
                response.responses.forEachIndexed { index, sendResponse ->
                    if (!sendResponse.isSuccessful) {
                        val errorCode = sendResponse.exception?.messagingErrorCode
                        if (errorCode == MessagingErrorCode.UNREGISTERED) {
                            try {
                                fcmTokenRepository.delete(chunk[index])
                                logger.info { "만료 토큰 삭제: token=${chunk[index].token}" }
                            } catch (e: Exception) {
                                logger.error(e) { "만료 토큰 삭제 실패: token=${chunk[index].token}" }
                            }
                        } else {
                            logger.error(sendResponse.exception) {
                                "멀티캐스트 개별 전송 실패: token=${chunk[index].token}, errorCode=$errorCode"
                            }
                        }
                    }
                }
            } catch (e: FirebaseMessagingException) {
                logger.error(e) { "멀티캐스트 전송 실패: chunkSize=${chunk.size}, errorCode=${e.messagingErrorCode}" }
            }
        }
    }

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
