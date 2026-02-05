package com.yapp.love.web.auth

import com.yapp.love.application.auth.dto.AppleIdTokenLoginCommand
import com.yapp.love.application.auth.dto.GoogleIdTokenLoginCommand
import com.yapp.love.application.auth.dto.RefreshTokenCommand
import com.yapp.love.application.auth.service.AuthService
import com.yapp.love.globalutils.exception.ErrorResponse
import com.yapp.love.web.auth.dto.AppleIdTokenLoginRequest
import com.yapp.love.web.auth.dto.GoogleIdTokenLoginRequest
import com.yapp.love.web.auth.dto.OAuthLoginResponse
import com.yapp.love.web.auth.dto.RefreshTokenRequest
import com.yapp.love.web.auth.dto.TokenRefreshResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
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
    @SecurityRequirements
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
    @SecurityRequirements
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
    @SecurityRequirements
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
        description = """
            회원탈퇴를 진행합니다.

            **삭제되는 데이터:**
            - 커플 관계 및 상대방과의 연결 (커플이 있는 경우)
            - 등록된 모든 목표
            - 모든 포토로그 (인증샷)
            - 초대 코드
            - 프로필 정보
            - 온보딩 정보

            **Apple 로그인 사용자:**
            - Apple 계정 연동이 자동으로 해제됩니다 (토큰 revoke)
            - revoke 실패 시에도 탈퇴는 진행됩니다

            **주의사항:**
            - 탈퇴 후 데이터 복구가 불가능합니다
            - 커플 상대방의 데이터도 함께 삭제됩니다
        """,
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "회원탈퇴가 완료되었습니다.",
            content = [Content(examples = [
                ExampleObject(
                    value = """{"message": "회원탈퇴가 완료되었습니다."}"""
                )
            ])]
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "인증되지 않은 사용자",
                            value = """{"status": 401, "code": "G4010", "message": "인증되지 않은 사용자입니다."}""",
                        ),
                        ExampleObject(
                            name = "토큰 만료",
                            value = """{"status": 401, "code": "G4011", "message": "토큰이 만료되었습니다."}""",
                        ),
                    ],
                ),
            ],
            ),
        ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 유저",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "유저 정보 없음",
                            value = """{"status": 404, "code": "G4040", "message": "존재하지 않는 유저입니다."}""",
                        ),
                    ],
                ),
            ],
            ),
    )
    @DeleteMapping("/withdraw")
    fun withdraw(
        @AuthUser userId: Long,
    ): ResponseEntity<Map<String, String>> {
        authService.withdraw(userId)
        return ResponseEntity.ok(mapOf("message" to "회원탈퇴가 완료되었습니다."))
    }
}
