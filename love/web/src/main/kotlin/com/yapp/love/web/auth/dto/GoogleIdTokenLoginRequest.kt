package com.yapp.love.web.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Google ID Token 로그인 요청")
data class GoogleIdTokenLoginRequest(
    @field:NotBlank(message = "ID Token은 필수입니다")
    @Schema(description = "Google에서 받은 ID Token", required = true)
    val idToken: String,
)
