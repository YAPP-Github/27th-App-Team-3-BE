package com.yapp.love.application.notification.port

import com.yapp.love.domain.notification.model.FcmToken

interface FcmPushService {
    fun sendMulticast(
        tokens: List<FcmToken>,
        title: String,
        body: String,
        deepLink: String? = null,
        dryRun: Boolean = false,
    )

    fun sendPushToUser(
        userId: Long,
        title: String,
        body: String,
        deepLink: String? = null,
    )
}