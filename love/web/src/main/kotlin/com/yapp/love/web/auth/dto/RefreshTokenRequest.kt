package com.yapp.love.web.auth.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "RefreshToken은 필수입니다.")
    val refreshToken: String,
)
