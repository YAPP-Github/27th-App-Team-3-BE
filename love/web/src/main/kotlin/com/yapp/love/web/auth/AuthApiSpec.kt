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

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "Apple 로그인",
    description = "Apple authorization code로 로그인합니다. 신규 사용자는 자동으로 가입됩니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = [Content(schema = Schema(implementation = OAuthLoginResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "인증 실패",
                            value = """{"status": 401, "code": "G4010", "message": "Apple 인증에 실패했습니다."}""",
                        ),
                        ExampleObject(
                            name = "토큰 만료",
                            value = """{"status": 401, "code": "G4011", "message": "Apple 인증 토큰이 만료되었습니다."}""",
                        ),
                        ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = """{"status": 401, "code": "G4012", "message": "Apple 인증 토큰이 유효하지 않습니다."}""",
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
@Operation(
    summary = "Google 로그인",
    description = "Google authorization code로 로그인합니다. 신규 사용자는 자동으로 가입됩니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = [Content(schema = Schema(implementation = OAuthLoginResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "인증 실패",
                            value = """{"status": 401, "code": "G4010", "message": "Google 인증에 실패했습니다."}""",
                        ),
                        ExampleObject(
                            name = "토큰 만료",
                            value = """{"status": 401, "code": "G4011", "message": "Google 인증 토큰이 만료되었습니다."}""",
                        ),
                        ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = """{"status": 401, "code": "G4012", "message": "Google 인증 토큰이 유효하지 않습니다."}""",
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
@Operation(
    summary = "Kakao 로그인",
    description = "Kakao authorization code로 로그인합니다. 신규 사용자는 자동으로 가입됩니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = [Content(schema = Schema(implementation = OAuthLoginResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "인증 실패",
                            value = """{"status": 401, "code": "G4010", "message": "카카오 인증에 실패했습니다."}""",
                        ),
                        ExampleObject(
                            name = "토큰 만료",
                            value = """{"status": 401, "code": "G4011", "message": "카카오 인증 토큰이 만료되었습니다."}""",
                        ),
                        ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = """{"status": 401, "code": "G4012", "message": "카카오 인증 토큰이 유효하지 않습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "429",
            description = "요청 횟수 초과",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "요청 횟수 초과",
                            value = """{"status": 429, "code": "G4290", "message": "요청이 너무 많습니다."}""",
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
                            name = "유효하지 않은 토큰",
                            value = """{"status": 401, "code": "G4012", "message": "유효하지 않은 토큰입니다."}""",
                        ),
                        ExampleObject(
                            name = "토큰 타입 불일치",
                            value = """{"status": 401, "code": "G4013", "message": "적절한 타입의 토큰이 아닙니다."}""",
                        ),
                        ExampleObject(
                            name = "토큰 폐기됨",
                            value = """{"status": 401, "code": "G4014", "message": "폐기된 토큰입니다."}""",
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
