package com.yapp.love.web.notification.dto.request

import jakarta.validation.constraints.NotBlank

data class FcmTokenRequest(
    @field:NotBlank(message = "FCM 토큰은 필수입니다.")
    val token: String,
    @field:NotBlank(message = "디바이스 ID는 필수입니다.")
    val deviceId: String,
)
