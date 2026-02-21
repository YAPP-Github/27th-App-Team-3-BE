package com.yapp.love.web.notification

import com.yapp.love.application.notification.NotificationService
import com.yapp.love.domain.notification.model.NotificationType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@Profile("local", "dev")
@Tag(name = "Notification Test", description = "알림 테스트 API (local/dev 전용)")
@RestController
@RequestMapping("/api/test/notifications")
class NotificationTestController(
    private val notificationService: NotificationService,
) {
    @SendTestNotificationApiSpec
    @PostMapping("/send")
    fun sendTestNotification(
        @RequestBody request: TestNotificationRequest,
    ): TestNotificationResponse {
        val type =
            try {
                NotificationType.valueOf(request.type)
            } catch (e: IllegalArgumentException) {
                return TestNotificationResponse(
                    success = false,
                    message = "유효하지 않은 타입입니다. 가능한 값: ${NotificationType.entries.map { it.name }}",
                )
            }

        return try {
            val deepLink = notificationService.sendNotification(
                targetUserId = request.targetUserId,
                type = type,
                titleArgs = request.titleArgs?.toTypedArray() ?: emptyArray(),
                bodyArgs = request.bodyArgs?.toTypedArray() ?: emptyArray(),
                deepLinkParams = request.deepLinkParams ?: emptyMap(),
            )
            TestNotificationResponse(
                success = true,
                message = "알림 전송 완료 (타입1: DB 저장, 타입2: FCM 푸시 시도)",
                deepLink = deepLink,
            )
        } catch (e: Exception) {
            TestNotificationResponse(
                success = false,
                message = "알림 전송 실패: ${e.message}",
            )
        }
    }

    @Operation(summary = "전체 알림 타입 목록", description = "사용 가능한 알림 타입과 템플릿을 조회합니다.")
    @GetMapping("/types")
    fun getNotificationTypes(): List<NotificationTypeInfo> {
        return NotificationType.entries.map {
            NotificationTypeInfo(
                type = it.name,
                titleTemplate = it.titleTemplate,
                bodyTemplate = it.bodyTemplate,
                action = it.action,
                exampleDeepLink = it.makeDeepLink(mapOf("exampleId" to "123")),
            )
        }
    }
}

data class TestNotificationRequest(
    val targetUserId: Long,
    val type: String,
    val titleArgs: List<String>? = null,
    val bodyArgs: List<String>? = null,
    val deepLinkParams: Map<String, String>? = null,
)

data class TestNotificationResponse(
    val success: Boolean,
    val message: String,
    val deepLink: String? = null,
)

data class NotificationTypeInfo(
    val type: String,
    val titleTemplate: String,
    val bodyTemplate: String,
    val action: String,
    val exampleDeepLink: String,
)
