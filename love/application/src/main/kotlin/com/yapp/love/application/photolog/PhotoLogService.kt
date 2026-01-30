package com.yapp.love.application.photolog

import com.yapp.love.domain.photolog.model.Photolog
import com.yapp.love.domain.photolog.repository.PhotologRepository
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class PhotologService(
    private val photologRepository: PhotologRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun findByGoalIdsAndVerificationDate(
        goalIds: List<Long>,
        verificationDate: LocalDate
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
    fun deleteByGoalIdAfterEndDate(goalId: Long, endDate: LocalDate) {
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
                    "인증 기록 삭제 중 오류가 발생했습니다."
                )
            }
        } else {
            logger.debug {
                "No photologs to delete for goalId=$goalId (from=$from is after to=$to)"
            }
        }
    }
}
