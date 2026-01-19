package com.yapp.love.web.auth

import com.yapp.love.application.auth.dto.AppleLoginCommand
import com.yapp.love.application.auth.dto.GoogleLoginCommand
import com.yapp.love.application.auth.dto.KakaoLoginCommand
import com.yapp.love.application.auth.dto.RefreshTokenCommand
import com.yapp.love.application.auth.service.AuthService
import com.yapp.love.web.auth.dto.AppleLoginRequest
import com.yapp.love.web.auth.dto.GoogleLoginRequest
import com.yapp.love.web.auth.dto.KakaoLoginRequest
import com.yapp.love.web.auth.dto.OAuthLoginResponse
import com.yapp.love.web.auth.dto.RefreshTokenRequest
import com.yapp.love.web.auth.dto.TokenRefreshResponse
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
    @AppleLoginApiSpec
    @PostMapping("/apple")
    fun loginWithApple(
        @Valid @RequestBody request: AppleLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val command = AppleLoginCommand(code = request.code)
        val result = authService.appleLogin(command)
        return ResponseEntity.ok(OAuthLoginResponse.from(result))
    }

    @GoogleLoginApiSpec
    @PostMapping("/google")
    fun loginWithGoogle(
        @Valid @RequestBody request: GoogleLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val command = GoogleLoginCommand(code = request.code)
        val result = authService.googleLogin(command)
        return ResponseEntity.ok(OAuthLoginResponse.from(result))
    }

    @KakaoLoginApiSpec
    @PostMapping("/kakao")
    fun loginWithKakao(
        @Valid @RequestBody request: KakaoLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val command = KakaoLoginCommand(code = request.code)
        val result = authService.kakaoLogin(command)
        return ResponseEntity.ok(OAuthLoginResponse.from(result))
    }

    @RefreshTokenApiSpec
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ResponseEntity<TokenRefreshResponse> {
        val command = RefreshTokenCommand(refreshToken = request.refreshToken)
        val result = authService.refreshToken(command)
        return ResponseEntity.ok(TokenRefreshResponse.from(result))
    }

    @LogoutApiSpec
    @PostMapping("/logout")
    fun logout(
        @AuthUser userId: Long,
    ): ResponseEntity<Map<String, String>> {
        authService.logout(userId)
        return ResponseEntity.ok(mapOf("message" to "로그아웃되었습니다."))
    }
}
