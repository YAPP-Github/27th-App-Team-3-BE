package com.yapp.love.web.poke

import com.yapp.love.globalutils.exception.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "찌르기",
    description = "상대방을 찔러서 특정 날짜의 목표 인증을 독려합니다. " +
        "date 필드로 어떤 날짜의 인증을 독려할지 명시하며, 알림 딥링크에 그대로 전달됩니다.",
    requestBody = RequestBody(
        required = true,
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = PokeRequest::class),
                examples = [
                    ExampleObject(
                        name = "오늘 인증 독려",
                        summary = "오늘 날짜의 목표 인증을 찌릅니다.",
                        value = """{"date": "2026-05-18"}""",
                    ),
                    ExampleObject(
                        name = "과거 날짜 인증 독려",
                        summary = "캘린더에서 과거 미달성 날짜를 보고 찌릅니다.",
                        value = """{"date": "2026-05-15"}""",
                    ),
                ],
            ),
        ],
    ),
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "찌르기 성공",
            content = [Content(schema = Schema(implementation = PokeResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "진행중이지 않은 목표",
                            value = """{"status": 400, "code": "G4000", "message": "진행중이지 않은 목표는 찌를 수 없습니다."}""",
                        ),
                        ExampleObject(
                            name = "날짜 누락",
                            value = """{"status": 400, "code": "G4000", "message": "입력값이 올바르지 않습니다."}""",
                        ),
                        ExampleObject(
                            name = "JSON 형식 오류",
                            value = """{"status": 400, "code": "G4002", "message": "JSON 형식이 올바르지 않습니다."}""",
                        ),
                    ],
                ),
            ],
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
            responseCode = "403",
            description = "권한 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "목표 접근 권한 없음",
                            value = """{"status": 403, "code": "G4030", "message": "해당 목표에 접근 권한이 없습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "404",
            description = "리소스를 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "커플 정보 없음",
                            value = """{"status": 404, "code": "G4040", "message": "커플 정보가 없습니다."}""",
                        ),
                        ExampleObject(
                            name = "목표 없음",
                            value = """{"status": 404, "code": "G4040", "message": "목표를 찾을 수 없습니다."}""",
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
annotation class PokeApiSpec