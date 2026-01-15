package com.yapp.love.web.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Google 로그인 요청")
data class GoogleLoginRequest(
    @field:NotBlank(message = "Authorization code는 필수입니다")
    @Schema(description = "Google에서 받은 authorization code", required = true)
    val code: String,
)
