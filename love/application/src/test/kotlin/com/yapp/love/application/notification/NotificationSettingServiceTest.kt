package com.yapp.love.application.notification

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.NotificationSettingRepository
import com.yapp.love.domain.notification.model.NotificationSetting
import com.yapp.love.domain.notification.model.NotificationType
import com.yapp.love.globalutils.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import com.yapp.love.domain.notification.model.FcmToken

class NotificationSettingServiceTest : DescribeSpec({

    val notificationSettingRepository = mockk<NotificationSettingRepository>()
    val fcmTokenRepository = mockk<FcmTokenRepository>(relaxed = true)
    val fcmPushService = mockk<FcmPushService>(relaxed = true)

    fun serviceWithHour(hour: Int): NotificationSettingService {
        val instant = Instant.parse("2024-01-01T${hour.toString().padStart(2, '0')}:00:00Z")
        val clock = Clock.fixed(instant, ZoneId.of("UTC"))
        return NotificationSettingService(
            notificationSettingRepository = notificationSettingRepository,
            fcmTokenRepository = fcmTokenRepository,
            fcmPushService = fcmPushService,
            clock = clock,
        )
    }

    val service = serviceWithHour(14) // 낮 시간(14시) 기본값

    val userId = 1L

    beforeEach {
        clearAllMocks()
    }

    describe("initSetting") {
        it("알림 설정을 생성하고 저장한다") {
            every { notificationSettingRepository.findByUserId(userId) } returns null
            every { notificationSettingRepository.save(any()) } answers { firstArg() }

            val result = service.initSetting(
                userId = userId,
                isPokePushEnabled = true,
                isMarketingPushEnabled = false,
                isNightPushEnabled = true,
            )

            result.userId shouldBe userId
            result.isPokePushEnabled shouldBe true
            result.isMarketingPushEnabled shouldBe false
            result.isNightPushEnabled shouldBe true
            verify { notificationSettingRepository.save(any()) }
        }
    }

    describe("getSetting") {
        context("설정이 존재하는 경우") {
            it("알림 설정을 반환한다") {
                val setting = NotificationSetting.create(userId = userId)
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                val result = service.getSetting(userId)

                result shouldBe setting
            }
        }

        context("설정이 존재하지 않는 경우") {
            it("기본값(전부 false)으로 설정을 생성하고 반환한다") {
                every { notificationSettingRepository.findByUserId(userId) } returns null
                every { notificationSettingRepository.save(any()) } answers { firstArg() }

                val result = service.getSetting(userId)

                result.isPokePushEnabled shouldBe false
                result.isMarketingPushEnabled shouldBe false
                result.isNightPushEnabled shouldBe false
                verify { notificationSettingRepository.save(any()) }
            }
        }
    }

    describe("updatePokePush") {
        it("찌르기 푸시 설정을 변경한다") {
            val setting = NotificationSetting.create(userId = userId, isPokePushEnabled = false)
            every { notificationSettingRepository.findByUserId(userId) } returns setting
            every { notificationSettingRepository.save(any()) } answers { firstArg() }

            val result = service.updatePokePush(userId, true)

            result.isPokePushEnabled shouldBe true
        }

        context("설정이 존재하지 않는 경우") {
            it("기본값으로 설정을 생성한 뒤 변경한다") {
                every { notificationSettingRepository.findByUserId(userId) } returns null
                every { notificationSettingRepository.save(any()) } answers { firstArg() }

                val result = service.updatePokePush(userId, true)

                result.isPokePushEnabled shouldBe true
            }
        }
    }

    describe("updateMarketingPush") {
        it("마케팅 푸시 설정을 변경한다") {
            val setting = NotificationSetting.create(userId = userId, isMarketingPushEnabled = false)
            every { notificationSettingRepository.findByUserId(userId) } returns setting
            every { notificationSettingRepository.save(any()) } answers { firstArg() }
            every { fcmTokenRepository.findByUserId(userId) } returns emptyList()

            val result = service.updateMarketingPush(userId, true)

            result.isMarketingPushEnabled shouldBe true
        }

        context("enabled = true이고 토큰이 있는 경우") {
            it("모든 토큰을 마케팅 토픽에 구독한다") {
                val setting = NotificationSetting.create(userId = userId, isMarketingPushEnabled = false)
                val tokens = listOf(
                    FcmToken.create(userId, "token-1", "device-1"),
                    FcmToken.create(userId, "token-2", "device-2"),
                )
                every { notificationSettingRepository.findByUserId(userId) } returns setting
                every { notificationSettingRepository.save(any()) } answers { firstArg() }
                every { fcmTokenRepository.findByUserId(userId) } returns tokens

                service.updateMarketingPush(userId, true)

                verify(exactly = 1) { fcmPushService.subscribeToTopic("token-1", FcmTokenService.MARKETING_TOPIC) }
                verify(exactly = 1) { fcmPushService.subscribeToTopic("token-2", FcmTokenService.MARKETING_TOPIC) }
                verify(exactly = 0) { fcmPushService.unsubscribeFromTopic(any(), any()) }
            }
        }

        context("enabled = false이고 토큰이 있는 경우") {
            it("모든 토큰을 마케팅 토픽에서 구독 해제한다") {
                val setting = NotificationSetting.create(userId = userId, isMarketingPushEnabled = true)
                val tokens = listOf(
                    FcmToken.create(userId, "token-1", "device-1"),
                    FcmToken.create(userId, "token-2", "device-2"),
                )
                every { notificationSettingRepository.findByUserId(userId) } returns setting
                every { notificationSettingRepository.save(any()) } answers { firstArg() }
                every { fcmTokenRepository.findByUserId(userId) } returns tokens

                service.updateMarketingPush(userId, false)

                verify(exactly = 1) { fcmPushService.unsubscribeFromTopic("token-1", FcmTokenService.MARKETING_TOPIC) }
                verify(exactly = 1) { fcmPushService.unsubscribeFromTopic("token-2", FcmTokenService.MARKETING_TOPIC) }
                verify(exactly = 0) { fcmPushService.subscribeToTopic(any(), any()) }
            }
        }
    }

    describe("updateNightPush") {
        it("야간 푸시 설정을 변경한다") {
            val setting = NotificationSetting.create(userId = userId, isNightPushEnabled = false)
            every { notificationSettingRepository.findByUserId(userId) } returns setting
            every { notificationSettingRepository.save(any()) } answers { firstArg() }

            val result = service.updateNightPush(userId, true)

            result.isNightPushEnabled shouldBe true
        }
    }

    describe("shouldSendPush") {

        context("설정이 존재하지 않는 경우") {
            it("false를 반환한다") {
                every { notificationSettingRepository.findByUserId(userId) } returns null

                service.shouldSendPush(userId, NotificationType.POKE) shouldBe false
            }
        }

        context("POKE 타입이고 찌르기 푸시가 꺼져 있는 경우") {
            it("false를 반환한다") {
                val setting = NotificationSetting.create(userId = userId, isPokePushEnabled = false)
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                service.shouldSendPush(userId, NotificationType.POKE) shouldBe false
            }
        }

        context("타입별 설정이 켜져 있고 야간이 아닌 경우") {
            it("true를 반환한다") {
                val setting = NotificationSetting.create(
                    userId = userId,
                    isPokePushEnabled = true,
                    isNightPushEnabled = false,
                )
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                serviceWithHour(14).shouldSendPush(userId, NotificationType.POKE) shouldBe true
            }
        }

        context("야간이고 야간 푸시가 꺼져 있는 경우") {
            it("false를 반환한다") {
                val setting = NotificationSetting.create(
                    userId = userId,
                    isPokePushEnabled = true,
                    isNightPushEnabled = false,
                )
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                serviceWithHour(22).shouldSendPush(userId, NotificationType.POKE) shouldBe false
            }
        }

        context("야간이지만 야간 푸시가 켜져 있는 경우") {
            it("true를 반환한다") {
                val setting = NotificationSetting.create(
                    userId = userId,
                    isPokePushEnabled = true,
                    isNightPushEnabled = true,
                )
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                serviceWithHour(22).shouldSendPush(userId, NotificationType.POKE) shouldBe true
            }
        }

        context("GOAL_COMPLETED 등 타입별 제한이 없는 경우") {
            it("설정이 존재하면 야간 여부에 따라 결정된다") {
                val setting = NotificationSetting.create(
                    userId = userId,
                    isPokePushEnabled = false,
                    isNightPushEnabled = true,
                )
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                // GOAL_COMPLETED는 타입별 제한이 없으므로 야간 설정만 체크
                // isNightPushEnabled가 true이면 야간이어도 전송 가능
                service.shouldSendPush(userId, NotificationType.GOAL_COMPLETED) shouldBe true
            }
        }
    }
})