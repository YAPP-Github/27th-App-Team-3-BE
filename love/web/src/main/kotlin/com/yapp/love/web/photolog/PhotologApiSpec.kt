package com.yapp.love.web.photolog

import com.yapp.love.globalutils.exception.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "인증샷 목록 조회",
    description = "날짜별 또는 목표별로 인증샷 목록을 조회합니다.\n\n" +
        "**하위 호환 안내**: `goalId` 없이 `targetDate`만 전달하면 이전 방식(날짜별 전체 목표 조회)과 동일하게 동작합니다.",
    parameters = [
        Parameter(
            name = "targetDate",
            description = "조회할 날짜 (yyyy-MM-dd)",
            required = true,
        ),
        Parameter(
            name = "goalId",
            description = "특정 목표 필터 (알림 탭 시 사용). 미입력 시 전체 목표 반환.",
            required = false,
        ),
    ],
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = PhotologListResponse::class))],
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
                            name = "다른 커플의 목표",
                            value = """{"status": 403, "code": "G4030", "message": "해당 목표에 대한 권한이 없습니다."}""",
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
                            name = "목표 없음 (goalId 지정 시)",
                            value = """{"status": 404, "code": "G4040", "message": "목표를 찾을 수 없습니다."}""",
                        ),
                        ExampleObject(
                            name = "커플 해제됨",
                            value = """{"status": 404, "code": "G4041", "message": "커플을 찾을 수 없습니다."}""",
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
annotation class GetPhotologsByDateApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "리액션 추가/수정",
    description = "상대방의 인증샷에 리액션을 남기거나 수정합니다",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "리액션 추가/수정 성공",
            content = [Content(schema = Schema(implementation = ReactionResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "JSON 형식 오류",
                            description = "잘못된 리액션 타입 포함",
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
                            name = "자신의 인증샷",
                            value = """{"status": 403, "code": "G4030", "message": "자신의 인증샷에는 리액션을 남길 수 없습니다."}""",
                        ),
                        ExampleObject(
                            name = "다른 커플의 인증샷",
                            value = """{"status": 403, "code": "G4030", "message": "상대방의 인증샷에만 리액션을 남길 수 있습니다."}""",
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
                            name = "인증샷 없음",
                            value = """{"status": 404, "code": "G4040", "message": "인증샷을 찾을 수 없습니다."}""",
                        ),
                        ExampleObject(
                            name = "커플 해제됨",
                            value = """{"status": 404, "code": "G4041", "message": "커플을 찾을 수 없습니다."}""",
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
annotation class AddReactionApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "리액션 해제",
    description = "상대방의 인증샷에 남긴 리액션을 해제합니다",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "리액션 해제 성공",
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
                            name = "자신의 인증샷",
                            value = """{"status": 403, "code": "G4030", "message": "자신의 인증샷에는 리액션을 남길 수 없습니다."}""",
                        ),
                        ExampleObject(
                            name = "다른 커플의 인증샷",
                            value = """{"status": 403, "code": "G4030", "message": "상대방의 인증샷에만 리액션을 남길 수 있습니다."}""",
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
                            name = "인증샷 없음",
                            value = """{"status": 404, "code": "G4040", "message": "인증샷을 찾을 수 없습니다."}""",
                        ),
                        ExampleObject(
                            name = "커플 해제됨",
                            value = """{"status": 404, "code": "G4041", "message": "커플을 찾을 수 없습니다."}""",
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
annotation class RemoveReactionApiSpec
