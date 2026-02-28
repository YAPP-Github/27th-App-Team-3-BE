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
        every { fcmTokenRepository.findByDeviceIdAndUserIdNot(any(), any()) } returns emptyList()
        every { fcmTokenRepository.deleteAll(any()) } just Runs
        every { fcmTokenRepository.deleteByTokenAndUserIdNot(any(), any()) } just Runs
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

    describe("registerToken - 계정 전환") {

        context("같은 기기에 다른 유저의 토큰이 존재하는 경우") {
            it("이전 유저의 토큰을 삭제한다") {
                val otherUserId = 99L
                val oldToken = FcmToken.create(userId = otherUserId, token = "old-token", deviceId = deviceId)
                every { fcmTokenRepository.findByDeviceIdAndUserIdNot(deviceId, userId) } returns listOf(oldToken)
                every { fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId) } returns null
                every { fcmTokenRepository.save(any()) } answers { firstArg() }

                fcmTokenService.registerToken(userId, token, deviceId)

                verify { fcmTokenRepository.deleteAll(listOf(oldToken)) }
            }
        }
    }

    describe("deleteToken") {

        context("토큰이 존재하지 않는 경우") {
            it("아무것도 하지 않는다") {
                every { fcmTokenRepository.findByUserIdAndToken(userId, token) } returns null

                fcmTokenService.deleteToken(userId, token)

                verify(exactly = 0) { fcmTokenRepository.delete(any()) }
            }
        }

        context("토큰이 존재하는 경우") {
            it("토큰을 삭제한다") {
                val fcmToken = FcmToken.create(userId = userId, token = token, deviceId = deviceId)
                every { fcmTokenRepository.findByUserIdAndToken(userId, token) } returns fcmToken
                every { fcmTokenRepository.delete(fcmToken) } just Runs

                fcmTokenService.deleteToken(userId, token)

                verify { fcmTokenRepository.delete(fcmToken) }
            }
        }
    }
})