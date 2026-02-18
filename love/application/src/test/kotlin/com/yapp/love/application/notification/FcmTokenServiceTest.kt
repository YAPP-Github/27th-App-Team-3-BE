package com.yapp.love.application.notification

import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.model.FcmToken
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class FcmTokenServiceTest : DescribeSpec({

    val fcmTokenRepository = mockk<FcmTokenRepository>()

    val fcmTokenService = FcmTokenService(
        fcmTokenRepository = fcmTokenRepository,
    )

    val userId = 1L
    val token = "fcm-token-abc"
    val deviceId = "device-123"

    beforeEach {
        clearAllMocks()
    }

    describe("registerToken") {

        context("해당 디바이스의 토큰이 이미 존재하는 경우") {
            it("기존 토큰을 업데이트한다") {
                val existingToken = FcmToken.create(userId = userId, token = "old-token", deviceId = deviceId)
                every { fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId) } returns existingToken
                every { fcmTokenRepository.save(existingToken) } returns existingToken

                fcmTokenService.registerToken(userId, token, deviceId)

                existingToken.token shouldBe token
                verify { fcmTokenRepository.save(existingToken) }
                verify(exactly = 0) { fcmTokenRepository.save(match<FcmToken> { it !== existingToken }) }
            }
        }

        context("해당 디바이스의 토큰이 존재하지 않는 경우") {
            it("새 토큰을 생성하여 저장한다") {
                every { fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId) } returns null
                every { fcmTokenRepository.save(any()) } answers { firstArg() }

                fcmTokenService.registerToken(userId, token, deviceId)

                verify {
                    fcmTokenRepository.save(match<FcmToken> {
                        it.userId == userId &&
                            it.token == token &&
                            it.deviceId == deviceId
                    })
                }
            }
        }
    }
})