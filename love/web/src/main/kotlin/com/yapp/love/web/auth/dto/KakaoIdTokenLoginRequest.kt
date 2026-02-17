package com.yapp.love.web.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Kakao ID Token 로그인 요청")
data class KakaoIdTokenLoginRequest(
    @field:NotBlank(message = "ID Token은 필수입니다")
    @Schema(description = "카카오에서 받은 ID Token", required = true)
    val idToken: String,
)
