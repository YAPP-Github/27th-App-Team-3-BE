package com.yapp.love.web.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Apple 로그인 요청")
data class AppleLoginRequest(
    @field:NotBlank(message = "authorization code는 필수입니다")
    @Schema(description = "Apple로부터 받은 authorization code", required = true)
    val code: String,
)
