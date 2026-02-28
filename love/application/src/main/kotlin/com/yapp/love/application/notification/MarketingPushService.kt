package com.yapp.love.application.notification

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.NotificationSettingRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalTime
import java.time.ZoneId

@Service
class MarketingPushService(
    private val notificationSettingRepository: NotificationSettingRepository,
    private val fcmTokenRepository: FcmTokenRepository,
    private val fcmPushService: FcmPushService,
    private val clock: Clock = Clock.system(ZoneId.of("Asia/Seoul"))
) {
    companion object {
        private const val NIGHT_START_HOUR = 21
        private const val NIGHT_END_HOUR = 8
    }

    @Async("fcmMarketingPushExecutor")
    fun sendMarketingPush(
        title: String,
        body: String,
        deepLink: String? = null,
        dryRun: Boolean = false,
    ) {
        val settings = notificationSettingRepository.findAllByIsMarketingPushEnabledTrue()
        val targetSettings = if (isNightTime()) settings.filter { it.isNightPushEnabled } else settings
        if (targetSettings.isEmpty()) return

        val tokens = fcmTokenRepository.findByUserIdIn(targetSettings.map { it.userId })
        fcmPushService.sendMulticast(tokens, title, body, deepLink, dryRun)
    }

    private fun isNightTime(): Boolean {
        val hour = LocalTime.now(clock).hour
        return hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR
    }
}
