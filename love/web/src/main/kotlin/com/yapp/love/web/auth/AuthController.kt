package com.yapp.love.web.auth

import com.yapp.love.application.auth.dto.AppleLoginCommand
import com.yapp.love.application.auth.dto.GoogleLoginCommand
import com.yapp.love.application.auth.dto.RefreshTokenCommand
import com.yapp.love.application.auth.service.AuthService
import com.yapp.love.web.auth.dto.AppleLoginRequest
import com.yapp.love.web.auth.dto.GoogleLoginRequest
import com.yapp.love.web.auth.dto.OAuthLoginResponse
import com.yapp.love.web.auth.dto.RefreshTokenRequest
import com.yapp.love.web.auth.dto.TokenRefreshResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(
        summary = "Apple 로그인",
        description = "Apple authorization code로 로그인합니다. 신규 사용자는 자동으로 가입됩니다.",
    )
    @PostMapping("/apple")
    fun loginWithApple(
        @Valid @RequestBody request: AppleLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val command = AppleLoginCommand(code = request.code)
        val result = authService.appleLogin(command)
        return ResponseEntity.ok(OAuthLoginResponse.from(result))
    }

    @Operation(
        summary = "Google 로그인",
        description = "Google authorization code로 로그인합니다. 신규 사용자는 자동으로 가입됩니다.",
    )
    @PostMapping("/google")
    fun loginWithGoogle(
        @Valid @RequestBody request: GoogleLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val command = GoogleLoginCommand(code = request.code)
        val result = authService.googleLogin(command)
        return ResponseEntity.ok(OAuthLoginResponse.from(result))
    }

    @Operation(
        summary = "토큰 갱신",
        description = "RefreshToken으로 새로운 AccessToken과 RefreshToken을 발급받습니다.",
    )
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ResponseEntity<TokenRefreshResponse> {
        val command = RefreshTokenCommand(refreshToken = request.refreshToken)
        val result = authService.refreshToken(command)
        return ResponseEntity.ok(TokenRefreshResponse.from(result))
    }

    @Operation(
        summary = "로그아웃",
        description = "로그아웃합니다. 저장된 RefreshToken이 삭제됩니다.",
    )
    @PostMapping("/logout")
    fun logout(
        @AuthUser userId: Long,
    ): ResponseEntity<Map<String, String>> {
        authService.logout(userId)
        return ResponseEntity.ok(mapOf("message" to "로그아웃되었습니다."))
    }
}
