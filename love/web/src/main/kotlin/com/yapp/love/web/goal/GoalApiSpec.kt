package com.yapp.love.web.goal

import com.yapp.love.globalutils.exception.ErrorResponse
import com.yapp.love.web.goal.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "목표 생성",
    description = "새로운 목표를 생성합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "201",
            description = "목표 생성 성공",
            content = [Content(schema = Schema(implementation = GoalResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "필수 값 누락",
                            value = """{"status": 400, "code": "G4001", "message": "필수 입력값이 누락되었습니다."}""",
                        ),
                        ExampleObject(
                            name = "DAILY 목표 repeatCount 오류",
                            value = """{"status": 400, "code": "G4000", "message": "DAILY 목표는 하루 한번만 가능합니다."}""",
                        ),
                        ExampleObject(
                            name = "WEEKLY 목표 repeatCount 오류",
                            value = """{"status": 400, "code": "G4000", "message": "WEEKLY 목표는 1번 이상 6번 이하로 가능합니다."}""",
                        ),
                        ExampleObject(
                            name = "MONTHLY 목표 repeatCount 오류",
                            value = """{"status": 400, "code": "G4000", "message": "MONTHLY 목표는 1번 이상 25 이하로 가능합니다."}""",
                        ),
                        ExampleObject(
                            name = "시작일 검증 오류",
                            value = """{"status": 400, "code": "G4000", "message": "시작일은 오늘 또는 미래여야 합니다."}""",
                        ),
                        ExampleObject(
                            name = "종료일 검증 오류",
                            value = """{"status": 400, "code": "G4000", "message": "종료일은 시작일 이후여야 합니다."}""",
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
            responseCode = "404",
            description = "리소스를 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "커플 정보 없음",
                            value = """{"status": 404, "code": "G4040", "message": "존재하지 않는 커플입니다."}""",
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
annotation class CreateGoalApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "목표 목록 조회",
    description = "특정 날짜의 목표 목록과 인증 정보를 조회합니다 : 홈  -> 목표 편집",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "목표 목록 조회 성공",
            content = [Content(schema = Schema(implementation = GoalListResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "날짜 형식 오류",
                            value = """{"status": 400, "code": "G4000", "message": "날짜 형식이 올바르지 않습니다."}""",
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
                            value = """{"status": 404, "code": "G4040", "message": "요청한 리소스를 찾을 수 없습니다."}""",
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
annotation class GetGoalsApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "목표 상세 조회",
    description = "특정 목표의 상세 정보를 조회합니다",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "목표 조회 성공",
            content = [Content(schema = Schema(implementation = GoalResponse::class))],
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
                            name = "목표 없음",
                            value = """{"status": 404, "code": "G4040", "message": "요청한 리소스를 찾을 수 없습니다."}""",
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
annotation class GetGoalApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "목표 수정",
    description = "기존 목표를 수정합니다 : : 홈  -> 목표 편집",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "목표 수정 성공",
            content = [Content(schema = Schema(implementation = GoalResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "필수 값 누락",
                            value = """{"status": 400, "code": "G4001", "message": "필수 입력값이 누락되었습니다."}""",
                        ),
                        ExampleObject(
                            name = "DAILY 목표 repeatCount 오류",
                            value = """{"status": 400, "code": "G4000", "message": "DAILY 목표는 하루 한번만 가능합니다."}""",
                        ),
                        ExampleObject(
                            name = "WEEKLY 목표 repeatCount 오류",
                            value = """{"status": 400, "code": "G4000", "message": "WEEKLY 목표는 1번 이상 6번 이하로 가능합니다."}""",
                        ),
                        ExampleObject(
                            name = "MONTHLY 목표 repeatCount 오류",
                            value = """{"status": 400, "code": "G4000", "message": "MONTHLY 목표는 1번 이상 25 이하로 가능합니다."}""",
                        ),
                        ExampleObject(
                            name = "시작일 검증 오류",
                            value = """{"status": 400, "code": "G4000", "message": "시작일은 오늘 또는 미래여야 합니다."}""",
                        ),
                        ExampleObject(
                            name = "종료일 검증 오류",
                            value = """{"status": 400, "code": "G4000", "message": "종료일은 시작일 이후여야 합니다."}""",
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
                            name = "목표 없음",
                            value = """{"status": 404, "code": "G4040", "message": "요청한 리소스를 찾을 수 없습니다."}""",
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
annotation class UpdateGoalApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "목표 삭제",
    description = "목표를 삭제합니다 (soft delete) : 홈  -> 목표 편집",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "목표 삭제 성공",
            content = [Content(schema = Schema(implementation = DeleteGoalResponse::class))],
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
                            name = "목표 없음",
                            value = """{"status": 404, "code": "G4040", "message": "요청한 리소스를 찾을 수 없습니다."}""",
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
annotation class DeleteGoalApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "목표 완료",
    description = "진행 중인 목표를 완료 처리합니다. : 홈  -> 목표 편집",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "목표 완료 성공",
            content = [Content(schema = Schema(implementation = CompleteGoalResponse::class))],
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
                            name = "목표 없음",
                            value = """{"status": 404, "code": "G4040", "message": "요청한 리소스를 찾을 수 없습니다."}""",
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
annotation class CompleteGoalApiSpec
