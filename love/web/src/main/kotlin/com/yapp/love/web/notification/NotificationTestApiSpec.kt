package com.yapp.love.web.notification

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.parameters.RequestBody

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "알림 전송 테스트",
    description = "특정 유저에게 원하는 타입의 알림을 전송합니다.",
    requestBody = RequestBody(
        content = [
            Content(
                mediaType = "application/json",
                examples = [
                    ExampleObject(
                        name = "PARTNER_CONNECTED",
                        summary = "커플 연결 — titleArgs[0]: 상대방 닉네임",
                        value = """{"targetUserId": 52, "type": "PARTNER_CONNECTED", "titleArgs": ["지수"], "bodyArgs": [], "deepLinkParams": {}}""",
                    ),
                    ExampleObject(
                        name = "POKE",
                        summary = "찌르기 — titleArgs[0]: 보낸사람 닉네임, titleArgs[1]: 목표이름 / bodyArgs[0]: 보낸사람 닉네임",
                        value = """{"targetUserId": 52, "type": "POKE", "titleArgs": ["지수", "운동하기"], "bodyArgs": ["지수"], "deepLinkParams": {"goalId": "10", "date": "2026-02-20"}}""",
                    ),
                    ExampleObject(
                        name = "GOAL_COMPLETED",
                        summary = "목표 완료 — titleArgs[0]: 닉네임, titleArgs[1]: 목표이름 / bodyArgs[0]: 닉네임",
                        value = """{"targetUserId": 52, "type": "GOAL_COMPLETED", "titleArgs": ["지수", "운동하기"], "bodyArgs": ["지수"], "deepLinkParams": {"goalId": "186", "date": "2026-02-20"}}""",
                    ),
                    ExampleObject(
                        name = "REACTION",
                        summary = "반응 — titleArgs[0]: 닉네임 / bodyArgs[0]: 닉네임",
                        value = """{"targetUserId": 52, "type": "REACTION", "titleArgs": ["지수"], "bodyArgs": ["지수"], "deepLinkParams": {"photoLogId": "5"}}""",
                    ),
                    ExampleObject(
                        name = "DAILY_GOAL_ACHIEVED",
                        summary = "데일리 목표 달성 — titleArgs[0]: 닉네임",
                        value = """{"targetUserId": 52, "type": "DAILY_GOAL_ACHIEVED", "titleArgs": ["지수"], "bodyArgs": [], "deepLinkParams": {}}""",
                    ),
                    ExampleObject(
                        name = "GOAL_ENDED",
                        summary = "목표 종료 — titleArgs[0]: 목표이름",
                        value = """{"targetUserId": 52, "type": "GOAL_ENDED", "titleArgs": ["운동하기"], "bodyArgs": [], "deepLinkParams": {"goalId": "10"}}""",
                    ),

                ],
            ),
        ],
    ),
)
annotation class SendTestNotificationApiSpec
