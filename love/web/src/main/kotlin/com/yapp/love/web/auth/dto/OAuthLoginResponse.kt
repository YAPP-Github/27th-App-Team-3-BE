package com.yapp.love.web.auth.dto

import com.yapp.love.application.auth.dto.OAuthLoginResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "OAuth 로그인 응답")
data class OAuthLoginResponse(
    @Schema(description = "사용자 ID")
    val userId: Long,
    @Schema(description = "액세스 토큰")
    val accessToken: String,
    @Schema(description = "리프레시 토큰")
    val refreshToken: String,
    @Schema(description = "신규 사용자 여부")
    val isNewUser: Boolean,
) {
    companion object {
        fun from(result: OAuthLoginResult): OAuthLoginResponse {
            return OAuthLoginResponse(
                userId = result.userId,
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                isNewUser = result.isNewUser,
            )
        }
    }
}
