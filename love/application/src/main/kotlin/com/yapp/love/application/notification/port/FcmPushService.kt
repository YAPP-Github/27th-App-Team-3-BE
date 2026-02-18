package com.yapp.love.application.notification.port

interface FcmPushService {
    fun sendPushToUser(
        userId: Long,
        title: String,
        body: String,
        deepLink: String? = null,
    ): Boolean
}
