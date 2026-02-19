package com.yapp.love.web.notification

import com.yapp.love.application.notification.FcmTokenService
import com.yapp.love.application.notification.NotificationService
import com.yapp.love.application.notification.NotificationSettingService
import com.yapp.love.web.auth.AuthUser
import com.yapp.love.web.notification.dto.request.FcmTokenRequest
import com.yapp.love.web.notification.dto.request.InitNotificationSettingRequest
import com.yapp.love.web.notification.dto.request.PushSettingRequest
import com.yapp.love.web.notification.dto.response.NotificationListResponse
import com.yapp.love.web.notification.dto.response.NotificationResponse
import com.yapp.love.web.notification.dto.response.NotificationSettingResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val notificationSettingService: NotificationSettingService,
    private val fcmTokenService: FcmTokenService,
) {
    @RegisterFcmTokenApiSpec
    @PostMapping("/fcm-token")
    fun registerFcmToken(
        @AuthUser userId: Long,
        @Valid @RequestBody request: FcmTokenRequest,
    ) {
        fcmTokenService.registerToken(userId, request.token, request.deviceId)
    }

    @GetNotificationsApiSpec
    @GetMapping
    fun getNotifications(
        @AuthUser userId: Long,
    ): NotificationListResponse {
        val notifications = notificationService.getNotifications(userId)

        return NotificationListResponse(
            notifications = notifications.map { NotificationResponse.from(it) },
        )
    }

    @MarkAsReadApiSpec
    @PatchMapping("/{notificationId}/read")
    fun markAsRead(
        @AuthUser userId: Long,
        @PathVariable notificationId: Long,
    ) {
        notificationService.markAsRead(userId, notificationId)
    }

    @MarkAllAsReadApiSpec
    @PatchMapping("/read-all")
    fun markAllAsRead(
        @AuthUser userId: Long,
    ) {
        notificationService.markAllAsRead(userId)
    }

    @InitNotificationSettingApiSpec
    @PostMapping("/settings/init")
    fun initNotificationSettings(
        @AuthUser userId: Long,
        @Valid @RequestBody request: InitNotificationSettingRequest,
    ): NotificationSettingResponse {
        val setting = notificationSettingService.initSetting(
            userId = userId,
            isPokePushEnabled = request.isPushEnabled,
            isMarketingPushEnabled = request.isMarketingPushEnabled,
            isNightPushEnabled = request.isNightPushEnabled,
        )
        return NotificationSettingResponse.from(setting)
    }

    @GetNotificationSettingApiSpec
    @GetMapping("/settings")
    fun getNotificationSettings(
        @AuthUser userId: Long,
    ): NotificationSettingResponse {
        val setting = notificationSettingService.getSetting(userId)
        return NotificationSettingResponse.from(setting)
    }

    @UpdatePokePushSettingApiSpec
    @PatchMapping("/settings/poke")
    fun updatePokePushSetting(
        @AuthUser userId: Long,
        @Valid @RequestBody request: PushSettingRequest,
    ): NotificationSettingResponse {
        val setting = notificationSettingService.updatePokePush(userId, request.enabled)
        return NotificationSettingResponse.from(setting)
    }

    @UpdateMarketingPushSettingApiSpec
    @PatchMapping("/settings/marketing")
    fun updateMarketingPushSetting(
        @AuthUser userId: Long,
        @Valid @RequestBody request: PushSettingRequest,
    ): NotificationSettingResponse {
        val setting = notificationSettingService.updateMarketingPush(userId, request.enabled)
        return NotificationSettingResponse.from(setting)
    }

    @UpdateNightPushSettingApiSpec
    @PatchMapping("/settings/night")
    fun updateNightPushSetting(
        @AuthUser userId: Long,
        @Valid @RequestBody request: PushSettingRequest,
    ): NotificationSettingResponse {
        val setting = notificationSettingService.updateNightPush(userId, request.enabled)
        return NotificationSettingResponse.from(setting)
    }
}
