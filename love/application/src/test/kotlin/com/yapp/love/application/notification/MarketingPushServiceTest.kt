package com.yapp.love.application.notification

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.NotificationSettingRepository
import com.yapp.love.domain.notification.model.FcmToken
import com.yapp.love.domain.notification.model.NotificationSetting
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MarketingPushServiceTest : DescribeSpec({

    val notificationSettingRepository = mockk<NotificationSettingRepository>()
    val fcmTokenRepository = mockk<FcmTokenRepository>()
    val fcmPushService = mockk<FcmPushService>(relaxed = true)

    fun serviceWithHour(hour: Int): MarketingPushService {
        val clock = Clock.fixed(
            Instant.parse("2026-01-01T${hour.toString().padStart(2, '0')}:00:00Z")
                .minusSeconds(9 * 3600), // UTC → KST(+9) 보정
            ZoneId.of("Asia/Seoul"),
        )
        return MarketingPushService(
            notificationSettingRepository = notificationSettingRepository,
            fcmTokenRepository = fcmTokenRepository,
            fcmPushService = fcmPushService,
            clock = clock,
        )
    }

    beforeEach {
        clearAllMocks()
    }

    describe("sendMarketingPush") {

        context("낮 시간대(09시)인 경우") {
            val service = serviceWithHour(9)

            it("마케팅 동의 유저 전체에게 전송한다") {
                val settings = listOf(
                    NotificationSetting.create(userId = 1L, isMarketingPushEnabled = true, isNightPushEnabled = false),
                    NotificationSetting.create(userId = 2L, isMarketingPushEnabled = true, isNightPushEnabled = true),
                )
                val tokens = listOf(
                    FcmToken.create(userId = 1L, token = "token1", deviceId = "device1"),
                    FcmToken.create(userId = 2L, token = "token2", deviceId = "device2"),
                )
                every { notificationSettingRepository.findAllByIsMarketingPushEnabledTrue() } returns settings
                every { fcmTokenRepository.findByUserIdIn(listOf(1L, 2L)) } returns tokens

                service.sendMarketingPush(title = "이벤트", body = "내용")

                verify { fcmPushService.sendMulticast(tokens, "이벤트", "내용", null) }
            }
        }

        context("야간 시간대(22시)인 경우") {
            val service = serviceWithHour(22)

            it("야간 푸시 허용 유저에게만 전송한다") {
                val settings = listOf(
                    NotificationSetting.create(userId = 1L, isMarketingPushEnabled = true, isNightPushEnabled = false),
                    NotificationSetting.create(userId = 2L, isMarketingPushEnabled = true, isNightPushEnabled = true),
                )
                val tokens = listOf(
                    FcmToken.create(userId = 2L, token = "token2", deviceId = "device2"),
                )
                every { notificationSettingRepository.findAllByIsMarketingPushEnabledTrue() } returns settings
                every { fcmTokenRepository.findByUserIdIn(listOf(2L)) } returns tokens

                service.sendMarketingPush(title = "이벤트", body = "내용")

                verify { fcmPushService.sendMulticast(tokens, "이벤트", "내용", null) }
            }

            it("야간 허용 유저가 없으면 전송하지 않는다") {
                val settings = listOf(
                    NotificationSetting.create(userId = 1L, isMarketingPushEnabled = true, isNightPushEnabled = false),
                )
                every { notificationSettingRepository.findAllByIsMarketingPushEnabledTrue() } returns settings

                service.sendMarketingPush(title = "이벤트", body = "내용")

                verify(exactly = 0) { fcmPushService.sendMulticast(any(), any(), any(), any()) }
            }
        }

        context("마케팅 동의 유저가 없는 경우") {
            val service = serviceWithHour(9)

            it("전송하지 않는다") {
                every { notificationSettingRepository.findAllByIsMarketingPushEnabledTrue() } returns emptyList()

                service.sendMarketingPush(title = "이벤트", body = "내용")

                verify(exactly = 0) { fcmPushService.sendMulticast(any(), any(), any(), any()) }
            }
        }

        context("deepLink가 있는 경우") {
            val service = serviceWithHour(9)

            it("deepLink를 포함하여 전송한다") {
                val settings = listOf(
                    NotificationSetting.create(userId = 1L, isMarketingPushEnabled = true),
                )
                val tokens = listOf(FcmToken.create(userId = 1L, token = "token1", deviceId = "device1"))
                every { notificationSettingRepository.findAllByIsMarketingPushEnabledTrue() } returns settings
                every { fcmTokenRepository.findByUserIdIn(listOf(1L)) } returns tokens

                service.sendMarketingPush(title = "이벤트", body = "내용", deepLink = "love://event")

                verify { fcmPushService.sendMulticast(tokens, "이벤트", "내용", "love://event") }
            }
        }
    }
})