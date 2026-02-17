package com.yapp.love.web.auth

import com.yapp.love.application.auth.dto.AppleIdTokenLoginCommand
import com.yapp.love.application.auth.dto.GoogleIdTokenLoginCommand
import com.yapp.love.application.auth.dto.KakaoIdTokenLoginCommand
import com.yapp.love.application.auth.dto.RefreshTokenCommand
import com.yapp.love.application.auth.service.AuthService
import com.yapp.love.web.auth.dto.AppleIdTokenLoginRequest
import com.yapp.love.web.auth.dto.GoogleIdTokenLoginRequest
import com.yapp.love.web.auth.dto.KakaoIdTokenLoginRequest
import com.yapp.love.web.auth.dto.OAuthLoginResponse
import com.yapp.love.web.auth.dto.RefreshTokenRequest
import com.yapp.love.web.auth.dto.TokenRefreshResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
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
    @PostMapping("/apple/token")
    fun loginWithAppleIdToken(
        @Valid @RequestBody request: AppleIdTokenLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val command = AppleIdTokenLoginCommand(
            idToken = request.idToken,
            authorizationCode = request.authorizationCode,
        )
        val result = authService.appleLoginWithIdToken(command)
        return ResponseEntity.ok(OAuthLoginResponse.from(result))
    }

    @GoogleLoginApiSpec
    @PostMapping("/google/token")
    fun loginWithGoogleIdToken(
        @Valid @RequestBody request: GoogleIdTokenLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val command = GoogleIdTokenLoginCommand(idToken = request.idToken)
        val result = authService.googleLoginWithIdToken(command)
        return ResponseEntity.ok(OAuthLoginResponse.from(result))
    }

    @KakaoLoginApiSpec
    @PostMapping("/kakao/token")
    fun loginWithKakaoIdToken(
        @Valid @RequestBody request: KakaoIdTokenLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val command = KakaoIdTokenLoginCommand(idToken = request.idToken)
        val result = authService.kakaoLoginWithIdToken(command)
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

    @WithdrawApiSpec
    @DeleteMapping("/withdraw")
    fun withdraw(
        @AuthUser userId: Long,
    ): ResponseEntity<Map<String, String>> {
        authService.withdraw(userId)
        return ResponseEntity.ok(mapOf("message" to "회원탈퇴가 완료되었습니다."))
    }
}