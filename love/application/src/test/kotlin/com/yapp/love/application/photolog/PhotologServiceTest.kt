package com.yapp.love.application.photolog

import com.yapp.love.application.couple.CoupleService
import com.yapp.love.application.storage.FileStoragePort
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.photolog.model.Photolog
import com.yapp.love.domain.photolog.model.ReactionType
import com.yapp.love.domain.photolog.repository.PhotologRepository
import com.yapp.love.domain.stamp.repository.StampHistoryRepository
import com.yapp.love.globalutils.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.time.LocalDate

class PhotologServiceTest : DescribeSpec({

    val photologRepository = mockk<PhotologRepository>()
    val fileStoragePort = mockk<FileStoragePort>()
    val coupleService = mockk<CoupleService>()
    val goalRepository = mockk<GoalRepository>()
    val coupleInfoRepository = mockk<CoupleInfoRepository>()
    val stampHistoryRepository = mockk<StampHistoryRepository>(relaxed = true)
    val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    val photologService =
        PhotologService(
            photologRepository = photologRepository,
            fileStoragePort = fileStoragePort,
            coupleService = coupleService,
            goalRepository = goalRepository,
            coupleInfoRepository = coupleInfoRepository,
            stampHistoryRepository = stampHistoryRepository,
            notificationEventPublisher = eventPublisher,
        )

    beforeEach {
        clearAllMocks()
    }

    describe("addReaction") {

        val myUserId = 1L
        val partnerUserId = 2L
        val coupleId = 100L
        val photologId = 10L

        val coupleInfo =
            CoupleInfo(
                id = coupleId,
                user1Id = myUserId,
                user2Id = partnerUserId,
                inviteCodeId = 1L,
                anniversaryDate = LocalDate.of(2024, 1, 1),
            )

        fun createPhotolog(userId: Long) =
            Photolog(
                id = photologId,
                goalId = 200L,
                userId = userId,
                verificationDate = LocalDate.of(2026, 2, 9),
                uploadedAt = Instant.now(),
                fileName = "test.jpg",
            )

        context("상대방 포토로그에 리액션을 남기는 경우") {

            it("리액션이 정상적으로 추가되어야 함") {
                // given
                val partnerPhotolog = createPhotolog(partnerUserId)

                every { photologRepository.findById(photologId) } returns partnerPhotolog
                every { coupleService.getCoupleInfoByUserId(myUserId) } returns coupleInfo
                every { coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, myUserId) } returns partnerUserId
                every { photologRepository.save(partnerPhotolog) } returns partnerPhotolog

                // when
                val result = photologService.addReaction(photologId, myUserId, ReactionType.ICON_HAPPY)

                // then
                result.photologId shouldBe photologId
                result.reaction shouldBe ReactionType.ICON_HAPPY
                verify(exactly = 1) { photologRepository.save(partnerPhotolog) }
            }

            it("기존 리액션을 수정할 수 있어야 함") {
                // given
                val partnerPhotolog = createPhotolog(partnerUserId)
                partnerPhotolog.reaction = ReactionType.ICON_HAPPY

                every { photologRepository.findById(photologId) } returns partnerPhotolog
                every { coupleService.getCoupleInfoByUserId(myUserId) } returns coupleInfo
                every { coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, myUserId) } returns partnerUserId
                every { photologRepository.save(partnerPhotolog) } returns partnerPhotolog

                // when
                val result = photologService.addReaction(photologId, myUserId, ReactionType.ICON_LOVE)

                // then
                result.reaction shouldBe ReactionType.ICON_LOVE
            }
        }

        context("자신의 포토로그에 리액션을 남기는 경우") {

            it("FORBIDDEN 예외가 발생해야 함") {
                // given
                val myPhotolog = createPhotolog(myUserId)

                every { photologRepository.findById(photologId) } returns myPhotolog

                // when & then
                val exception =
                    shouldThrow<GlobalException> {
                        photologService.addReaction(photologId, myUserId, ReactionType.ICON_HAPPY)
                    }

                exception.getCustomMessage() shouldBe "자신의 인증샷에는 리액션을 남길 수 없습니다."
                verify(exactly = 0) { photologRepository.save(any()) }
            }
        }

        context("다른 커플의 포토로그에 리액션을 남기는 경우") {

            it("FORBIDDEN 예외가 발생해야 함") {
                // given
                val otherUserId = 999L
                val otherPhotolog = createPhotolog(otherUserId)

                every { photologRepository.findById(photologId) } returns otherPhotolog
                every { coupleService.getCoupleInfoByUserId(myUserId) } returns coupleInfo
                every { coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, myUserId) } returns partnerUserId

                // when & then
                val exception =
                    shouldThrow<GlobalException> {
                        photologService.addReaction(photologId, myUserId, ReactionType.ICON_HAPPY)
                    }

                exception.getCustomMessage() shouldBe "상대방의 인증샷에만 리액션을 남길 수 있습니다."
                verify(exactly = 0) { photologRepository.save(any()) }
            }
        }

        context("존재하지 않는 포토로그에 리액션을 남기는 경우") {

            it("NOT_FOUND 예외가 발생해야 함") {
                // given
                every { photologRepository.findById(photologId) } returns null

                // when & then
                val exception =
                    shouldThrow<GlobalException> {
                        photologService.addReaction(photologId, myUserId, ReactionType.ICON_HAPPY)
                    }

                exception.getCustomMessage() shouldBe "인증샷을 찾을 수 없습니다."
            }
        }
    }

    describe("removeReaction") {

        val myUserId = 1L
        val partnerUserId = 2L
        val coupleId = 100L
        val photologId = 10L

        val coupleInfo =
            CoupleInfo(
                id = coupleId,
                user1Id = myUserId,
                user2Id = partnerUserId,
                inviteCodeId = 1L,
                anniversaryDate = LocalDate.of(2024, 1, 1),
            )

        fun createPhotolog(
            userId: Long,
            reaction: ReactionType? = null,
        ) = Photolog(
            id = photologId,
            goalId = 200L,
            userId = userId,
            verificationDate = LocalDate.of(2026, 2, 9),
            uploadedAt = Instant.now(),
            fileName = "test.jpg",
        ).also { it.reaction = reaction }

        context("상대방 포토로그의 리액션을 해제하는 경우") {

            it("리액션이 정상적으로 해제되어야 함") {
                // given
                val partnerPhotolog = createPhotolog(partnerUserId, ReactionType.ICON_HAPPY)

                every { photologRepository.findById(photologId) } returns partnerPhotolog
                every { coupleService.getCoupleInfoByUserId(myUserId) } returns coupleInfo
                every { coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, myUserId) } returns partnerUserId
                every { photologRepository.save(partnerPhotolog) } returns partnerPhotolog

                // when
                photologService.removeReaction(photologId, myUserId)

                // then
                partnerPhotolog.reaction shouldBe null
                verify(exactly = 1) { photologRepository.save(partnerPhotolog) }
            }

            it("이미 리액션이 없는 상태에서도 정상 처리되어야 함") {
                // given
                val partnerPhotolog = createPhotolog(partnerUserId, reaction = null)

                every { photologRepository.findById(photologId) } returns partnerPhotolog
                every { coupleService.getCoupleInfoByUserId(myUserId) } returns coupleInfo
                every { coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, myUserId) } returns partnerUserId
                every { photologRepository.save(partnerPhotolog) } returns partnerPhotolog

                // when
                photologService.removeReaction(photologId, myUserId)

                // then
                partnerPhotolog.reaction shouldBe null
                verify(exactly = 1) { photologRepository.save(partnerPhotolog) }
            }
        }

        context("자신의 포토로그에 리액션 해제를 시도하는 경우") {

            it("FORBIDDEN 예외가 발생해야 함") {
                // given
                val myPhotolog = createPhotolog(myUserId, ReactionType.ICON_HAPPY)

                every { photologRepository.findById(photologId) } returns myPhotolog

                // when & then
                val exception =
                    shouldThrow<GlobalException> {
                        photologService.removeReaction(photologId, myUserId)
                    }

                exception.getCustomMessage() shouldBe "자신의 인증샷에는 리액션을 남길 수 없습니다."
                verify(exactly = 0) { photologRepository.save(any()) }
            }
        }

        context("다른 커플의 포토로그에 리액션 해제를 시도하는 경우") {

            it("FORBIDDEN 예외가 발생해야 함") {
                // given
                val otherUserId = 999L
                val otherPhotolog = createPhotolog(otherUserId, ReactionType.ICON_HAPPY)

                every { photologRepository.findById(photologId) } returns otherPhotolog
                every { coupleService.getCoupleInfoByUserId(myUserId) } returns coupleInfo
                every { coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, myUserId) } returns partnerUserId

                // when & then
                val exception =
                    shouldThrow<GlobalException> {
                        photologService.removeReaction(photologId, myUserId)
                    }

                exception.getCustomMessage() shouldBe "상대방의 인증샷에만 리액션을 남길 수 있습니다."
                verify(exactly = 0) { photologRepository.save(any()) }
            }
        }

        context("존재하지 않는 포토로그에 리액션 해제를 시도하는 경우") {

            it("NOT_FOUND 예외가 발생해야 함") {
                // given
                every { photologRepository.findById(photologId) } returns null

                // when & then
                val exception =
                    shouldThrow<GlobalException> {
                        photologService.removeReaction(photologId, myUserId)
                    }

                exception.getCustomMessage() shouldBe "인증샷을 찾을 수 없습니다."
                verify(exactly = 0) { photologRepository.save(any()) }
            }
        }
    }
})

