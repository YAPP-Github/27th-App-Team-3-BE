package com.yapp.love.application.notification.port

interface FcmPushService {
    fun sendPushToUser(
        userId: Long,
        title: String,
        body: String,
        deepLink: String? = null,
    )

    fun subscribeToTopic(token: String, topic: String)

    fun unsubscribeFromTopic(token: String, topic: String)

    fun unsubscribeFromTopicOrThrow(token: String, topic: String)

    fun sendToTopic(
        topic: String,
        title: String,
        body: String,
        deepLink: String? = null,
    )
}
