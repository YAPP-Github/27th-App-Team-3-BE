package com.yapp.love.web.auth

import com.yapp.love.application.auth.dto.AppleIdTokenLoginCommand
import com.yapp.love.application.auth.dto.GoogleIdTokenLoginCommand
import com.yapp.love.application.auth.dto.RefreshTokenCommand
import com.yapp.love.application.auth.service.AuthService
import com.yapp.love.web.auth.dto.AppleIdTokenLoginRequest
import com.yapp.love.web.auth.dto.GoogleIdTokenLoginRequest
import com.yapp.love.web.auth.dto.OAuthLoginResponse
import com.yapp.love.web.auth.dto.RefreshTokenRequest
import com.yapp.love.web.auth.dto.TokenRefreshResponse
import io.swagger.v3.oas.annotations.Operation
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

    @Operation(
        summary = "Apple 로그인 (ID Token)",
        description = "Apple ID Token으로 로그인합니다. 모바일 앱에서 Sign in with Apple 사용 시 이 API를 사용합니다.",
    )
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


    @Operation(
        summary = "Google 로그인 (ID Token)",
        description = "Google ID Token으로 로그인합니다. 모바일 앱에서 Google Sign-In SDK 사용 시 이 API를 사용합니다.",
    )
    @PostMapping("/google/token")
    fun loginWithGoogleIdToken(
        @Valid @RequestBody request: GoogleIdTokenLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val command = GoogleIdTokenLoginCommand(idToken = request.idToken)
        val result = authService.googleLoginWithIdToken(command)
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

    @Operation(
        summary = "회원탈퇴",
        description = "회원탈퇴합니다. 소셜 로그인 연동 해제 및 모든 사용자 데이터가 삭제됩니다.",
    )
    @DeleteMapping("/withdraw")
    fun withdraw(
        @AuthUser userId: Long,
    ): ResponseEntity<Map<String, String>> {
        authService.withdraw(userId)
        return ResponseEntity.ok(mapOf("message" to "회원탈퇴가 완료되었습니다."))
    }
}
