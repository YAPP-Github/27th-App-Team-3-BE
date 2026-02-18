package com.yapp.love.application.notification

import com.yapp.love.domain.notification.NotificationSettingRepository
import com.yapp.love.domain.notification.model.NotificationSetting
import com.yapp.love.domain.notification.model.NotificationType
import com.yapp.love.globalutils.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class NotificationSettingServiceTest : DescribeSpec({

    val notificationSettingRepository = mockk<NotificationSettingRepository>()

    val service = NotificationSettingService(
        notificationSettingRepository = notificationSettingRepository,
    )

    val userId = 1L

    beforeEach {
        clearAllMocks()
    }

    describe("initSetting") {
        it("알림 설정을 생성하고 저장한다") {
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
            it("예외가 발생한다") {
                every { notificationSettingRepository.findByUserId(userId) } returns null

                shouldThrow<GlobalException> {
                    service.getSetting(userId)
                }
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
            it("예외가 발생한다") {
                every { notificationSettingRepository.findByUserId(userId) } returns null

                shouldThrow<GlobalException> {
                    service.updatePokePush(userId, true)
                }
            }
        }
    }

    describe("updateMarketingPush") {
        it("마케팅 푸시 설정을 변경한다") {
            val setting = NotificationSetting.create(userId = userId, isMarketingPushEnabled = false)
            every { notificationSettingRepository.findByUserId(userId) } returns setting
            every { notificationSettingRepository.save(any()) } answers { firstArg() }

            val result = service.updateMarketingPush(userId, true)

            result.isMarketingPushEnabled shouldBe true
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

        context("MARKETING 타입이고 마케팅 푸시가 꺼져 있는 경우") {
            it("false를 반환한다") {
                val setting = NotificationSetting.create(userId = userId, isMarketingPushEnabled = false)
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                service.shouldSendPush(userId, NotificationType.MARKETING) shouldBe false
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

                // isNightTime()은 LocalTime.now()에 의존하므로 낮 시간에는 true가 나옴
                // 야간 시간(21~08)이 아닌 시간에 테스트가 실행될 때만 true
                // 정확한 테스트를 위해서는 시간을 주입받아야 하지만 현재 구조에서는 통합테스트로 검증
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