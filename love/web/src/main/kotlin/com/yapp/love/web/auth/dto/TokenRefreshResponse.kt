package com.yapp.love.web.auth.dto

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun from(result: com.yapp.love.application.auth.dto.TokenRefreshResult): TokenRefreshResponse {
            return TokenRefreshResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
            )
        }
    }
}
