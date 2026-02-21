package com.yapp.love.web.notification

import com.yapp.love.globalutils.exception.ErrorResponse
import com.yapp.love.web.notification.dto.response.NotificationListResponse
import com.yapp.love.web.notification.dto.response.NotificationSettingResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "FCM 토큰 등록",
    description = "디바이스 FCM 토큰을 등록하거나 갱신합니다. (userId, deviceId) 쌍이 이미 존재하면 토큰을 업데이트하고, 없으면 새로 저장합니다.\n\n" +
        "한 유저가 여러 기기(아이폰, 아이패드 등)를 사용할 수 있으므로, userId만으로는 기기를 특정할 수 없습니다. " +
        "deviceId를 함께 사용해 '어떤 유저의 어떤 기기'인지 식별합니다.\n\n" +
        "같은 유저라도 기기마다 서로 다른 deviceId를 전달해야 각 기기로 독립적으로 알림을 받을 수 있습니다.\n\n" +
        "iOS에서는 UIDevice.current.identifierForVendor를 deviceId로 사용할 수 있습니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "토큰 등록 성공",
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "FCM 토큰 누락",
                            value = """{"status": 400, "code": "G4000", "message": "요청 본문의 값이 검증 조건을 충족하지 않습니다"}""",
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
    ],
)
annotation class RegisterFcmTokenApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "알림 목록 조회",
    description = "내 알림 목록을 최신순으로 조회합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "알림 목록 조회 성공",
            content = [Content(schema = Schema(implementation = NotificationListResponse::class))],
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
    ],
)
annotation class GetNotificationsApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "알림 읽음 처리",
    description = "특정 알림을 읽음 처리합니다. 본인의 알림만 처리 가능합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "읽음 처리 성공",
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "본인 알림이 아님",
                            value = """{"status": 400, "code": "G4000", "message": "본인의 알림만 읽음 처리할 수 있습니다."}""",
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
            description = "알림을 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "알림 없음",
                            value = """{"status": 404, "code": "G4040", "message": "알림을 찾을 수 없습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class MarkAsReadApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "모든 알림 읽음 처리",
    description = "내 모든 알림을 읽음 처리합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "전체 읽음 처리 성공",
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
    ],
)
annotation class MarkAllAsReadApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "알림 설정 초기화 (온보딩)",
    description = "온보딩 시 !!!!반드시!!!! 알림 설정을 초기화해야 합니다",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "알림 설정 초기화 성공",
            content = [Content(schema = Schema(implementation = NotificationSettingResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "올바르지 않은 입력값",
                            value = """{"status": 400, "code": "G4000", "message": "요청 본문의 값이 검증 조건을 충족하지 않습니다"}""",
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
    ],
)
annotation class InitNotificationSettingApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "알림 설정 조회",
    description = "내 알림 설정을 조회합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "알림 설정 조회 성공",
            content = [Content(schema = Schema(implementation = NotificationSettingResponse::class))],
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
            description = "알림 설정을 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "알림 설정 없음",
                            value = """{"status": 404, "code": "G4040", "message": "알림 설정을 찾을 수 없습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class GetNotificationSettingApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "찌르기 푸시 알림 설정 변경",
    description = "파트너 찌르기 푸시 알림 수신 여부를 변경합니다. 쿨타임 체크(3시간)은 클라이언트단에서 적용해야 합니다. 서버에는 연속 찌르기와 관련된 검증 로직이 없습니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "설정 변경 성공",
            content = [Content(schema = Schema(implementation = NotificationSettingResponse::class))],
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
            responseCode = "404",
            description = "알림 설정을 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "알림 설정 없음",
                            value = """{"status": 404, "code": "G4040", "message": "알림 설정을 찾을 수 없습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class UpdatePokePushSettingApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "마케팅 푸시 알림 설정 변경",
    description = "마케팅 푸시 알림 수신 여부를 변경합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "설정 변경 성공",
            content = [Content(schema = Schema(implementation = NotificationSettingResponse::class))],
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
            responseCode = "404",
            description = "알림 설정을 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "알림 설정 없음",
                            value = """{"status": 404, "code": "G4040", "message": "알림 설정을 찾을 수 없습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class UpdateMarketingPushSettingApiSpec

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "야간 푸시 알림 설정 변경",
    description = "야간(21시~익일 8시) 푸시 알림 수신 여부를 변경합니다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "설정 변경 성공",
            content = [Content(schema = Schema(implementation = NotificationSettingResponse::class))],
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
            responseCode = "404",
            description = "알림 설정을 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "알림 설정 없음",
                            value = """{"status": 404, "code": "G4040", "message": "알림 설정을 찾을 수 없습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class UpdateNightPushSettingApiSpec
