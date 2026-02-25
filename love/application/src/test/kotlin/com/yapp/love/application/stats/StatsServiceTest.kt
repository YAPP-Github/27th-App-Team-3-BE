package com.yapp.love.application.stats

import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.photolog.model.Photolog
import com.yapp.love.domain.photolog.repository.PhotologRepository
import com.yapp.love.domain.stamp.model.StampType
import com.yapp.love.domain.stamp.repository.StampHistoryRepository
import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.domain.user.model.UserAdditionInfo
import com.yapp.love.globalutils.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class StatsServiceTest : DescribeSpec({

    val coupleInfoRepository = mockk<CoupleInfoRepository>()
    val goalRepository = mockk<GoalRepository>()
    val photologRepository = mockk<PhotologRepository>()
    val stampHistoryRepository = mockk<StampHistoryRepository>()
    val userAdditionInfoRepository = mockk<UserAdditionInfoRepository>()

    val statsService = StatsService(
        coupleInfoRepository = coupleInfoRepository,
        goalRepository = goalRepository,
        photologRepository = photologRepository,
        stampHistoryRepository = stampHistoryRepository,
        userAdditionInfoRepository = userAdditionInfoRepository,
    )

    val myUserId = 1L
    val partnerUserId = 2L
    val coupleId = 100L
    val goalId = 200L

    val coupleInfo = CoupleInfo(
        id = coupleId,
        user1Id = myUserId,
        user2Id = partnerUserId,
        inviteCodeId = 1L,
        anniversaryDate = LocalDate.of(2024, 1, 1),
    )

    fun createGoal(
        repeatCycle: RepeatCycle = RepeatCycle.DAILY,
        repeatCount: Int = 1,
        startDate: LocalDate = LocalDate.of(2026, 2, 1),
        hasEndDate: Boolean = false,
        endDate: LocalDate? = null,
        goalStatus: GoalStatus = GoalStatus.IN_PROGRESS,
    ) = Goal(
        id = goalId,
        coupleId = coupleId,
        name = "운동하기",
        repeatCycle = repeatCycle,
        repeatCount = repeatCount,
        startDate = startDate,
        hasEndDate = hasEndDate,
        endDate = endDate,
        goalStatus = goalStatus,
        icon = GoalIcon.ICON_EXERCISE,
        stampType = StampType.CLOVER,
    )

    fun createPhotolog(
        userId: Long,
        verificationDate: LocalDate,
        fileName: String = "photo.jpg",
    ) = Photolog(
        id = userId * 100 + verificationDate.dayOfMonth.toLong(),
        goalId = goalId,
        userId = userId,
        verificationDate = verificationDate,
        uploadedAt = Instant.now(),
        fileName = fileName,
    )

    beforeEach {
        clearAllMocks()
    }

    describe("getStatsCalendar") {

        context("정상 조회") {

            it("해당 월의 인증 기록을 날짜별로 그룹핑하여 반환해야 함") {
                // given
                val yearMonth = YearMonth.of(2026, 2)
                val goal = createGoal()

                val photologs = listOf(
                    createPhotolog(myUserId, LocalDate.of(2026, 2, 1), "my1.jpg"),
                    createPhotolog(partnerUserId, LocalDate.of(2026, 2, 1), "partner1.jpg"),
                    createPhotolog(myUserId, LocalDate.of(2026, 2, 3), "my2.jpg"),
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every {
                    photologRepository.findByGoalIdAndVerificationDateBetween(
                        goalId,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 28),
                    )
                } returns photologs

                // when
                val result = statsService.getStatsCalendar(myUserId, goalId, yearMonth)

                // then
                result.goalId shouldBe goalId
                result.goalName shouldBe "운동하기"
                result.goalIcon shouldBe GoalIcon.ICON_EXERCISE
                result.yearMonth shouldBe yearMonth
                result.isCompleted shouldBe false
                result.completedDates.size shouldBe 2

                // 2/1 - 둘 다 인증
                result.completedDates[0].date shouldBe LocalDate.of(2026, 2, 1)
                result.completedDates[0].myPhotolog?.fileName shouldBe "my1.jpg"
                result.completedDates[0].partnerPhotolog?.fileName shouldBe "partner1.jpg"

                // 2/3 - 나만 인증
                result.completedDates[1].date shouldBe LocalDate.of(2026, 2, 3)
                result.completedDates[1].myPhotolog?.fileName shouldBe "my2.jpg"
                result.completedDates[1].partnerPhotolog shouldBe null
            }

            it("인증 기록이 없으면 빈 completedDates를 반환해야 함") {
                // given
                val yearMonth = YearMonth.of(2026, 2)
                val goal = createGoal()

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every {
                    photologRepository.findByGoalIdAndVerificationDateBetween(goalId, any(), any())
                } returns emptyList()

                // when
                val result = statsService.getStatsCalendar(myUserId, goalId, yearMonth)

                // then
                result.completedDates shouldBe emptyList()
            }

            it("COMPLETED 목표는 isCompleted가 true여야 함") {
                // given
                val yearMonth = YearMonth.of(2026, 2)
                val goal = createGoal(
                    goalStatus = GoalStatus.COMPLETED,
                    hasEndDate = true,
                    endDate = LocalDate.of(2026, 2, 20),
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every {
                    photologRepository.findByGoalIdAndVerificationDateBetween(goalId, any(), any())
                } returns emptyList()

                // when
                val result = statsService.getStatsCalendar(myUserId, goalId, yearMonth)

                // then
                result.isCompleted shouldBe true
            }
        }

        context("권한 검증") {

            it("커플 정보가 없으면 예외가 발생해야 함") {
                // given
                every { coupleInfoRepository.findByUserId(myUserId) } returns null

                // when & then
                shouldThrow<GlobalException> {
                    statsService.getStatsCalendar(myUserId, goalId, YearMonth.of(2026, 2))
                }
            }

            it("다른 커플의 목표 조회 시 예외가 발생해야 함") {
                // given
                val otherCoupleGoal = Goal(
                    id = goalId,
                    coupleId = 999L,
                    name = "다른 커플 목표",
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = LocalDate.of(2026, 2, 1),
                    goalStatus = GoalStatus.IN_PROGRESS,
                    icon = GoalIcon.ICON_DEFAULT,
                    stampType = StampType.CLOVER,
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns otherCoupleGoal

                // when & then
                shouldThrow<GlobalException> {
                    statsService.getStatsCalendar(myUserId, goalId, YearMonth.of(2026, 2))
                }
            }

            it("존재하지 않는 목표 조회 시 예외가 발생해야 함") {
                // given
                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns null

                // when & then
                shouldThrow<GlobalException> {
                    statsService.getStatsCalendar(myUserId, goalId, YearMonth.of(2026, 2))
                }
            }
        }
    }

    describe("getStatsSummary") {

        context("정상 조회") {

            it("전체 기간 누적 통계를 반환해야 함") {
                // given
                val goal = createGoal(
                    repeatCycle = RepeatCycle.DAILY,
                    startDate = LocalDate.of(2026, 2, 1),
                    hasEndDate = true,
                    endDate = LocalDate.of(2026, 2, 28),
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { userAdditionInfoRepository.findByUserId(myUserId) } returns
                    UserAdditionInfo(userId = myUserId, nickname = "마루")
                every { userAdditionInfoRepository.findByUserId(partnerUserId) } returns
                    UserAdditionInfo(userId = partnerUserId, nickname = "유리")
                every { stampHistoryRepository.countByGoalIdAndUserId(goalId, myUserId) } returns 15
                every { stampHistoryRepository.countByGoalIdAndUserId(goalId, partnerUserId) } returns 12

                // when
                val result = statsService.getStatsSummary(myUserId, goalId)

                // then
                result.myNickname shouldBe "마루"
                result.partnerNickname shouldBe "유리"
                result.totalCount shouldBe 28 // 2/1~2/28 = 28일
                result.myCompletedCount shouldBe 15
                result.partnerCompletedCount shouldBe 12
                result.repeatCycle shouldBe RepeatCycle.DAILY
                result.startDate shouldBe LocalDate.of(2026, 2, 1)
                result.endDate shouldBe LocalDate.of(2026, 2, 28)
            }

            it("닉네임이 없으면 기본값을 사용해야 함") {
                // given
                val goal = createGoal(
                    hasEndDate = true,
                    endDate = LocalDate.of(2026, 2, 28),
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { userAdditionInfoRepository.findByUserId(myUserId) } returns null
                every { userAdditionInfoRepository.findByUserId(partnerUserId) } returns null
                every { stampHistoryRepository.countByGoalIdAndUserId(goalId, any()) } returns 0

                // when
                val result = statsService.getStatsSummary(myUserId, goalId)

                // then
                result.myNickname shouldBe "나"
                result.partnerNickname shouldBe "상대방"
            }
        }

        context("calculateTotalTargetCount") {

            it("DAILY: 양끝 포함 일수를 반환해야 함") {
                // given - 2/18 ~ 2/25 = 8일
                val goal = createGoal(
                    repeatCycle = RepeatCycle.DAILY,
                    startDate = LocalDate.of(2026, 2, 18),
                    hasEndDate = true,
                    endDate = LocalDate.of(2026, 2, 25),
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { userAdditionInfoRepository.findByUserId(any()) } returns null
                every { stampHistoryRepository.countByGoalIdAndUserId(goalId, any()) } returns 0

                // when
                val result = statsService.getStatsSummary(myUserId, goalId)

                // then
                result.totalCount shouldBe 8
            }

            it("WEEKLY: 경과일 기준으로 주 수를 계산해야 함 (repeatCount=3)") {
                // given - 2/18 ~ 2/25: DAYS.between=7, ceil(7/7)=1주, 1*3=3
                val goal = createGoal(
                    repeatCycle = RepeatCycle.WEEKLY,
                    repeatCount = 3,
                    startDate = LocalDate.of(2026, 2, 18),
                    hasEndDate = true,
                    endDate = LocalDate.of(2026, 2, 25),
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { userAdditionInfoRepository.findByUserId(any()) } returns null
                every { stampHistoryRepository.countByGoalIdAndUserId(goalId, any()) } returns 0

                // when
                val result = statsService.getStatsSummary(myUserId, goalId)

                // then
                result.totalCount shouldBe 3
            }

            it("WEEKLY: 8일이면 2주로 계산해야 함 (repeatCount=3)") {
                // given - 2/18 ~ 2/26: DAYS.between=8, ceil(8/7)=2주, 2*3=6
                val goal = createGoal(
                    repeatCycle = RepeatCycle.WEEKLY,
                    repeatCount = 3,
                    startDate = LocalDate.of(2026, 2, 18),
                    hasEndDate = true,
                    endDate = LocalDate.of(2026, 2, 26),
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { userAdditionInfoRepository.findByUserId(any()) } returns null
                every { stampHistoryRepository.countByGoalIdAndUserId(goalId, any()) } returns 0

                // when
                val result = statsService.getStatsSummary(myUserId, goalId)

                // then
                result.totalCount shouldBe 6
            }

            it("MONTHLY: 걸친 월 수 × repeatCount를 반환해야 함") {
                // given - 1/15 ~ 2/25: 2개월 걸침, 2*2=4
                val goal = createGoal(
                    repeatCycle = RepeatCycle.MONTHLY,
                    repeatCount = 2,
                    startDate = LocalDate.of(2026, 1, 15),
                    hasEndDate = true,
                    endDate = LocalDate.of(2026, 2, 25),
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { userAdditionInfoRepository.findByUserId(any()) } returns null
                every { stampHistoryRepository.countByGoalIdAndUserId(goalId, any()) } returns 0

                // when
                val result = statsService.getStatsSummary(myUserId, goalId)

                // then
                result.totalCount shouldBe 4
            }

            it("종료일 미정이면 오늘 기준으로 계산해야 함") {
                // given
                val today = LocalDate.now()
                val startDate = today.minusDays(13) // 14일 전
                val goal = createGoal(
                    repeatCycle = RepeatCycle.DAILY,
                    startDate = startDate,
                    hasEndDate = false,
                    endDate = null,
                )

                every { coupleInfoRepository.findByUserId(myUserId) } returns coupleInfo
                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { userAdditionInfoRepository.findByUserId(any()) } returns null
                every { stampHistoryRepository.countByGoalIdAndUserId(goalId, any()) } returns 0

                // when
                val result = statsService.getStatsSummary(myUserId, goalId)

                // then
                result.totalCount shouldBe 14
            }

        }
    }
})
