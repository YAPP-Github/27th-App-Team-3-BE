package com.yapp.love.web.notification

import com.yapp.love.application.notification.NotificationService
import com.yapp.love.web.auth.AuthUser
import com.yapp.love.web.notification.dto.response.NotificationListResponse
import com.yapp.love.web.notification.dto.response.NotificationResponse
import com.yapp.love.web.notification.dto.response.NotificationSettingResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.*

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {
    @Operation(summary = "FCM 토큰 등록")
    @PostMapping("/fcm-token")
    fun registerFcmToken(
        @AuthUser userId: Long,
        @Valid @RequestBody request: FcmTokenRequest,
    ) {
        notificationService.registerFcmToken(userId, request.token, request.deviceId)
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

    @Operation(summary = "알림 설정 조회")
    @GetMapping("/settings")
    fun getNotificationSettings(
        @AuthUser userId: Long,
    ): NotificationSettingResponse {
        val setting = notificationService.getNotificationSetting(userId)
        return NotificationSettingResponse(
            isNotificationEnabled = setting.isNightPushNotificationEnabled,
            isNightNotificationsEnabled = setting.isNightPushNotificationEnabled,
        )
    }

    @Operation(summary = "푸쉬 알림 동의")
    @PostMapping("/settings")
    fun updateNotificationSetting(
        @AuthUser userId: Long,
        @Valid @RequestBody request: NotificationSettingRequest,
    ): NotificationSettingResponse {
        val setting = notificationService.updatePushNotification(userId, request.isNotificationEnabled)
        return NotificationSettingResponse(
            isNotificationEnabled = setting.isNightPushNotificationEnabled,
            isNightNotificationsEnabled = setting.isNightPushNotificationEnabled,
        )
    }

    @Operation(summary = "야간 알림 설정 변경")
    @PatchMapping("/settings/night")
    fun updateNightNotificationSetting(
        @AuthUser userId: Long,
        @Valid @RequestBody request: NightNotificationSettingRequest,
    ): NotificationSettingResponse {
        val setting = notificationService.updateNightPushNotification(userId, request.isNightNotificationEnabled)
        return NotificationSettingResponse(
            isNotificationEnabled = setting.isPushNotificationEnabled,
            isNightNotificationEnabled = setting.isNightPushNotificationEnabled,
        )
    }
}

data class FcmTokenRequest(
    @field:NotBlank(message = "FCM 토큰은 필수입니다.")
    val token: String,
    @field:NotBlank(message = "디바이스 ID는 필수입니다.")
    val deviceId: String,
)


data class NotificationSettingRequest(
    val isNotificationEnabled: Boolean,
)

data class NightNotificationSettingResponse(
    val isNightNotificationEnabled: Boolean,
)

data class NightNotificationSettingRequest(
    val isNightNotificationEnabled: Boolean,
)
