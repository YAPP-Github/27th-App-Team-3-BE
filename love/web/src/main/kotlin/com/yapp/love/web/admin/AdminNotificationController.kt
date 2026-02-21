package com.yapp.love.web.admin

import com.yapp.love.application.notification.FcmTokenService
import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import com.yapp.love.web.admin.dto.MarketingPushRequest
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@Tag(name = "Admin Notification", description = "어드민 알림 API")
@RestController
@RequestMapping("/api/admin/notifications")
class AdminNotificationController(
    private val fcmPushService: FcmPushService,
    @Value("\${admin.secret}")
    private val adminSecret: String,
) {
    @PostMapping("/marketing")
    fun sendMarketingPush(
        @RequestHeader("X-Admin-Secret") secret: String,
        @RequestBody request: MarketingPushRequest,
    ) {
        if (secret != adminSecret) {
            throw GlobalException(GlobalErrorCode.UNAUTHORIZED, "인증되지 않은 요청입니다.")
        }

        fcmPushService.sendToTopic(
            topic = FcmTokenService.MARKETING_TOPIC,
            title = request.title,
            body = request.body,
            deepLink = request.deepLink,
        )
    }
}
