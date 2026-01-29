package com.yapp.love.application.photolog

import com.yapp.love.domain.photolog.repository.PhotologRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class PhotologService(
    private val photologRepository: PhotologRepository,
) {
    /**
     * 특정 날짜 이후의 인증 기록 삭제
     *
     * @param goalId 목표 ID
     * @param afterDate 이 날짜 다음날부터 오늘까지의 인증 기록을 삭제
     */
    @Transactional
    fun deleteByGoalIdAfterEndDate(goalId: Long, endDate: LocalDate): Int {
        val from = endDate.plusDays(1)
        val to = LocalDate.now()

        return if (!from.isAfter(to)) {
            photologRepository.deleteByGoalIdAndVerificationDateBetween(goalId, from, to)
        } else {
            0
        }
    }
}
