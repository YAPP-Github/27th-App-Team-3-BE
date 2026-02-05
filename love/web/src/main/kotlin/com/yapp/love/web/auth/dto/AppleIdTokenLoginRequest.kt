package com.yapp.love.web.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Apple ID Token 로그인 요청")
data class AppleIdTokenLoginRequest(
    @field:NotBlank(message = "ID Token은 필수입니다")
    @Schema(description = "Apple에서 받은 ID Token", required = true)
    val idToken: String,
    @field:NotBlank(message = "Authorization Code는 필수입니다")
    @Schema(description = "Apple에서 받은 Authorization Code (회원탈퇴 시 토큰 revoke용)", required = true)
    val authorizationCode: String,
)
