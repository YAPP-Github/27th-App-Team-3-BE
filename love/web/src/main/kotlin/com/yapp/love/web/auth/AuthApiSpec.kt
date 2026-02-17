package com.yapp.love.web.auth

import com.yapp.love.globalutils.exception.ErrorResponse
import com.yapp.love.web.auth.dto.OAuthLoginResponse
import com.yapp.love.web.auth.dto.TokenRefreshResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@SecurityRequirements
@Operation(
    summary = "Apple 로그인",
    description = "Apple ID Token으로 로그인합니다. 모바일 앱에서 Sign in with Apple 사용 시 이 API를 사용합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = [Content(schema = Schema(implementation = OAuthLoginResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "ID Token 검증 실패",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = """{"status": 400, "code": "G4000", "message": "Invalid JWT format"}""",
                        ),
                        ExampleObject(
                            name = "토큰 만료",
                            value = """{"status": 400, "code": "G4000", "message": "Apple ID token has expired"}""",
                        ),
                        ExampleObject(
                            name = "서명 검증 실패",
                            value = """{"status": 400, "code": "G4000", "message": "Invalid Apple ID token signature"}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "서버 오류",
                            value = """{"status": 500, "code": "G5000", "message": "서버 내부 오류가 발생했습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class AppleLoginApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@SecurityRequirements
@Operation(
    summary = "Google 로그인",
    description = "Google ID Token으로 로그인합니다. 모바일 앱에서 Google Sign-In SDK 사용 시 이 API를 사용합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = [Content(schema = Schema(implementation = OAuthLoginResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "ID Token 검증 실패",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = """{"status": 400, "code": "G4000", "message": "Invalid Google ID token"}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "서버 오류",
                            value = """{"status": 500, "code": "G5000", "message": "서버 내부 오류가 발생했습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class GoogleLoginApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@SecurityRequirements
@Operation(
    summary = "Kakao 로그인",
    description = "Kakao ID Token으로 로그인합니다. 모바일 앱에서 Kakao SDK 사용 시 이 API를 사용합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = [Content(schema = Schema(implementation = OAuthLoginResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "ID Token 검증 실패",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = """{"status": 400, "code": "G4000", "message": "Invalid JWT format"}""",
                        ),
                        ExampleObject(
                            name = "토큰 만료",
                            value = """{"status": 400, "code": "G4000", "message": "Kakao ID token has expired"}""",
                        ),
                        ExampleObject(
                            name = "서명 검증 실패",
                            value = """{"status": 400, "code": "G4000", "message": "Invalid Kakao ID token signature"}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "서버 오류",
                            value = """{"status": 500, "code": "G5000", "message": "서버 내부 오류가 발생했습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class KakaoLoginApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@SecurityRequirements
@Operation(
    summary = "토큰 갱신",
    description = "RefreshToken으로 새로운 AccessToken과 RefreshToken을 발급받습니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            content = [Content(schema = Schema(implementation = TokenRefreshResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "토큰 만료",
                            value = """{"status": 401, "code": "G4011", "message": "토큰이 만료되었습니다."}""",
                        ),
                        ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = """{"status": 401, "code": "G4012", "message": "유효하지 않은 토큰입니다."}""",
                        ),
                        ExampleObject(
                            name = "토큰 타입 불일치",
                            value = """{"status": 401, "code": "G4013", "message": "적절한 타입의 토큰이 아닙니다."}""",
                        ),
                        ExampleObject(
                            name = "토큰 폐기됨",
                            value = """{"status": 401, "code": "G4014", "message": "최신화된 Refresh Token이 토큰이 아닙니다."}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "서버 오류",
                            value = """{"status": 500, "code": "G5000", "message": "서버 내부 오류가 발생했습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class RefreshTokenApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "로그아웃",
    description = "로그아웃합니다. 저장된 RefreshToken이 삭제됩니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
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
                        ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = """{"status": 401, "code": "G4012", "message": "유효하지 않은 토큰입니다."}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "서버 오류",
                            value = """{"status": 500, "code": "G5000", "message": "서버 내부 오류가 발생했습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class LogoutApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
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
    value = [
        ApiResponse(
            responseCode = "200",
            description = "회원탈퇴가 완료되었습니다.",
            content = [Content(examples = [
                ExampleObject(
                    value = """{"message": "회원탈퇴가 완료되었습니다."}""",
                ),
            ])],
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
                        ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = """{"status": 401, "code": "G4012", "message": "유효하지 않은 토큰입니다."}""",
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
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "서버 오류",
                            value = """{"status": 500, "code": "G5000", "message": "서버 내부 오류가 발생했습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class WithdrawApiSpec