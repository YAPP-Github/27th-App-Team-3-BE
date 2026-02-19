package com.yapp.love.application.photolog

import com.yapp.love.application.couple.CoupleService
import com.yapp.love.application.notification.event.DailyGoalAchievedEvent
import com.yapp.love.application.notification.event.PhotologCreatedEvent
import com.yapp.love.application.notification.event.ReactionCreatedEvent
import com.yapp.love.application.photolog.dto.ReactionInfo
import com.yapp.love.application.storage.FileStoragePort
import com.yapp.love.application.storage.PresignedUrlResult
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.photolog.model.Photolog
import com.yapp.love.domain.photolog.model.ReactionType
import com.yapp.love.domain.photolog.repository.PhotologRepository
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional(readOnly = true)
class PhotologService(
    private val photologRepository: PhotologRepository,
    private val fileStoragePort: FileStoragePort,
    private val coupleService: CoupleService,
    private val goalRepository: GoalRepository,
    private val coupleInfoRepository: CoupleInfoRepository,
    private val notificationEventPublisher: ApplicationEventPublisher,
) {
    private val logger = KotlinLogging.logger {}

    fun findByGoalIdsAndVerificationDate(
        goalIds: List<Long>,
        verificationDate: LocalDate,
    ): List<Photolog> {
        return photologRepository.findByGoalIdsAndVerificationDate(goalIds, verificationDate)
    }

    /**
     * 특정 날짜 이후의 인증 기록 삭제
     *
     * @param goalId 목표 ID
     * @param endDate 이 날짜 다음날부터 오늘까지의 인증 기록을 삭제
     * @throws GlobalException 삭제 실패 시
     */
    @Transactional
    fun deleteByGoalIdAfterEndDate(
        goalId: Long,
        endDate: LocalDate,
    ) {
        val from = endDate.plusDays(1)
        val to = LocalDate.now()

        if (!from.isAfter(to)) {
            try {
                photologRepository.deleteByGoalIdAndVerificationDateBetween(goalId, from, to)
                logger.info {
                    "Deleted photologs for goalId=$goalId, dateRange=$from to $to"
                }
            } catch (e: DataAccessException) {
                logger.error(e) {
                    "Failed to delete photologs for goalId=$goalId, dateRange=$from to $to"
                }
                throw GlobalException(
                    GlobalErrorCode.INTERNAL_SERVER_ERROR,
                    "인증 기록 삭제 중 오류가 발생했습니다.",
                )
            }
        } else {
            logger.debug {
                "No photologs to delete for goalId=$goalId (from=$from is after to=$to)"
            }
        }
    }

    fun generateUploadUrl(
        userId: Long,
        goalId: Long,
    ): PresignedUrlResult {
        val key = "photolog/$goalId/${userId}_${UUID.randomUUID()}.jpg"
        return fileStoragePort.generatePresignedUploadUrl(key)
    }

    @Transactional
    fun createPhotolog(
        goalId: Long,
        userId: Long,
        fileName: String,
        comment: String?,
        verificationDate: LocalDate,
    ): Photolog {
        validatePhotologCreation(goalId, userId, verificationDate)

        val saved = photologRepository.save(
            Photolog(
                goalId = goalId,
                userId = userId,
                verificationDate = verificationDate,
                uploadedAt = Instant.now(),
                fileName = fileName,
                comment = comment,
            )
        )

        notificationEventPublisher.publishEvent(PhotologCreatedEvent(userId = userId, goalId = goalId))
        checkAndPublishDailyGoalAchieved(userId, verificationDate)

        return saved
    }

    private fun validatePhotologCreation(goalId: Long, userId: Long, verificationDate: LocalDate) {
        val goal = findGoalOrThrow(goalId)
        val coupleInfo = findCoupleInfoOrThrow(userId)
        validateGoalOwnership(goal, coupleInfo)
        validateNoDuplicatePhotolog(goalId, userId, verificationDate)
    }

    private fun findGoalOrThrow(goalId: Long) =
        goalRepository.findActiveGoalById(goalId)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "존재하지 않는 목표입니다.")

    private fun findCoupleInfoOrThrow(userId: Long) =
        coupleInfoRepository.findByUserId(userId)
            ?: throw GlobalException(GlobalErrorCode.FORBIDDEN, "커플 정보가 없습니다.")

    private fun validateGoalOwnership(goal: Goal, coupleInfo: CoupleInfo) {
        if (goal.coupleId != coupleInfo.id) {
            throw GlobalException(GlobalErrorCode.FORBIDDEN, "해당 목표에 대한 권한이 없습니다.")
        }
    }

    private fun validateNoDuplicatePhotolog(goalId: Long, userId: Long, verificationDate: LocalDate) {
        photologRepository.findByGoalIdAndUserIdAndVerificationDate(goalId, userId, verificationDate)
            ?.let { throw GlobalException(GlobalErrorCode.INVALID_INPUT_VALUE, "이미 오늘 인증을 완료했습니다.") }
    }

    private fun checkAndPublishDailyGoalAchieved(userId: Long, verificationDate: LocalDate) {
        val coupleInfo = coupleInfoRepository.findByUserId(userId) ?: return
        val coupleId = coupleInfo.id ?: return

        val allGoals = goalRepository.findActiveGoalsByCoupleIdAndDate(coupleId, verificationDate)
        if (allGoals.isEmpty()) return

        val allGoalIds = allGoals.map { it.id!! }
        val allPhotologs = photologRepository.findByGoalIdsAndVerificationDate(allGoalIds, verificationDate)

        val user1Completed = allGoalIds.all { goalId -> allPhotologs.any { it.goalId == goalId && it.userId == coupleInfo.user1Id } }
        val user2Completed = allGoalIds.all { goalId -> allPhotologs.any { it.goalId == goalId && it.userId == coupleInfo.user2Id } }

        if (user1Completed && user2Completed) {
            notificationEventPublisher.publishEvent(
                DailyGoalAchievedEvent(
                    user1Id = coupleInfo.user1Id,
                    user2Id = coupleInfo.user2Id,
                ),
            )
        }
    }

    @Transactional
    fun addReaction(
        photologId: Long,
        userId: Long,
        reaction: ReactionType,
    ): ReactionInfo {
        val photolog =
            photologRepository.findById(photologId)
                ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "인증샷을 찾을 수 없습니다.")

        if (photolog.userId == userId) {
            throw GlobalException(GlobalErrorCode.FORBIDDEN, "자신의 인증샷에는 리액션을 남길 수 없습니다.")
        }

        val coupleInfo = coupleService.getCoupleInfoByUserId(userId)
        val partnerUserId = coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, userId)

        if (photolog.userId != partnerUserId) {
            throw GlobalException(GlobalErrorCode.FORBIDDEN, "상대방의 인증샷에만 리액션을 남길 수 있습니다.")
        }

        photolog.reaction = reaction
        photologRepository.save(photolog)

        notificationEventPublisher.publishEvent(
            ReactionCreatedEvent(
                reactorUserId = userId,
                photologOwnerId = photolog.userId,
                goalId = photolog.goalId,
                verificationDate = photolog.verificationDate,
            ),
        )

        return ReactionInfo(
            photologId = photolog.id!!,
            reaction = reaction,
        )
    }

    @Transactional
    fun deletePhotolog(photologId: Long, userId: Long) {
        val photolog = photologRepository.findById(photologId)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "인증샷을 찾을 수 없습니다.")

        if (photolog.userId != userId) {
            throw GlobalException(GlobalErrorCode.FORBIDDEN, "본인의 인증샷만 삭제할 수 있습니다.")
        }

        photologRepository.delete(photolog)
    }

    @Transactional
    fun updatePhotolog(
        photologId: Long,
        userId: Long,
        fileName: String,
        comment: String?,
    ) {
        val photolog = photologRepository.findById(photologId)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "인증샷을 찾을 수 없습니다.")

        if (photolog.userId != userId) {
            throw GlobalException(GlobalErrorCode.FORBIDDEN, "본인의 인증샷만 수정할 수 있습니다.")
        }

        photolog.updateContent(fileName, comment)
        photologRepository.save(photolog)
    }
}
