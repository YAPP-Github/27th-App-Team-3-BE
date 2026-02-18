package com.yapp.love.infrastructure.sqs.producer

import com.fasterxml.jackson.databind.ObjectMapper
import com.yapp.love.infrastructure.sqs.message.PushNotificationMessage
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class PushNotificationProducer(
    private val sqsTemplate: SqsTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${sqs.queue.notification}")
    private val notificationQueue: String,
) {
    fun sendPushNotification(message: PushNotificationMessage) {
        try {
            val jsonMessage = objectMapper.writeValueAsString(message)
            sqsTemplate.send(notificationQueue, jsonMessage)
            logger.info { "푸시 알림 메시지 발행: userId=${message.userId}" }
        } catch (e: Exception) {
            logger.error(e) { "푸시 알림 메시지 발행 실패: userId=${message.userId}" }
        }
    }
}
