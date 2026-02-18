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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
    @Operation(summary = "FCM 토큰 등록")
    @PostMapping("/fcm-token")
    fun registerFcmToken(
        @AuthUser userId: Long,
        @Valid @RequestBody request: FcmTokenRequest,
    ) {
        fcmTokenService.registerToken(userId, request.token, request.deviceId)
    }

    @Operation(summary = "알림 목록 조회")
    @GetMapping
    fun getNotifications(
        @AuthUser userId: Long,
    ): NotificationListResponse {
        val notifications = notificationService.getNotifications(userId)

        return NotificationListResponse(
            notifications = notifications.map { NotificationResponse.from(it) }
        )
    }

    @Operation(summary = "알림 읽음 처리")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
        ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
        //TODO : 401
    )
    @PatchMapping("/{notificationId}/read")
    fun markAsRead(
        @AuthUser userId: Long,
        @PathVariable notificationId: Long,
    ) {
        notificationService.markAsRead(userId, notificationId)
    }

    @Operation(summary = "모든 알림 읽음 처리")
    @PatchMapping("/read-all")
    fun markAllAsRead(
        @AuthUser userId: Long,
    ) {
        notificationService.markAllAsRead(userId)
    }

    @Operation(summary = "알림 설정 초기화 (온보딩)")
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

    @Operation(summary = "알림 설정 조회")
    @GetMapping("/settings")
    fun getNotificationSettings(
        @AuthUser userId: Long,
    ): NotificationSettingResponse {
        val setting = notificationSettingService.getSetting(userId)
        return NotificationSettingResponse.from(setting)
    }

    @Operation(summary = "푸쉬 알림 설정 변경")
    @PatchMapping("/settings/poke")
    fun updatePokePushSetting(
        @AuthUser userId: Long,
        @Valid @RequestBody request: PushSettingRequest,
    ): NotificationSettingResponse {
        val setting = notificationSettingService.updatePokePush(userId, request.enabled)
        return NotificationSettingResponse.from(setting)
    }

    @Operation(summary = "마케팅 알림 설정 변경")
    @PatchMapping("/settings/marketing")
    fun updateMarketingPushSetting(
        @AuthUser userId: Long,
        @Valid @RequestBody request: PushSettingRequest,
    ): NotificationSettingResponse {
        val setting = notificationSettingService.updateMarketingPush(userId, request.enabled)
        return NotificationSettingResponse.from(setting)
    }

    @Operation(summary = "야간 알림 설정 변경")
    @PatchMapping("/settings/night")
    fun updateNightPushSetting(
        @AuthUser userId: Long,
        @Valid @RequestBody request: PushSettingRequest,
    ): NotificationSettingResponse {
        val setting = notificationSettingService.updateNightPush(userId, request.enabled)
        return NotificationSettingResponse.from(setting)
    }
}


