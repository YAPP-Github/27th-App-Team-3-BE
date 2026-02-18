package com.yapp.love.infrastructure.sqs.message

data class PushNotificationMessage(
    val userId: Long,
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val data: Map<String, String> = emptyMap(),
)
