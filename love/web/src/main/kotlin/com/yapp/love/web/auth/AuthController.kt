package com.yapp.love.web.auth

import com.yapp.love.application.auth.dto.AppleLoginCommand
import com.yapp.love.application.auth.dto.GoogleLoginCommand
import com.yapp.love.application.auth.service.AuthService
import com.yapp.love.web.auth.dto.AppleLoginRequest
import com.yapp.love.web.auth.dto.GoogleLoginRequest
import com.yapp.love.web.auth.dto.OAuthLoginResponse
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
}
