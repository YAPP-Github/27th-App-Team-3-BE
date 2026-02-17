package com.yapp.love.web.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Kakao 로그인 요청")
data class KakaoLoginRequest(
    @field:NotBlank(message = "Authorization code는 필수입니다")
    @Schema(description = "Kakao에서 받은 authorization code", required = true)
    val code: String,
)
