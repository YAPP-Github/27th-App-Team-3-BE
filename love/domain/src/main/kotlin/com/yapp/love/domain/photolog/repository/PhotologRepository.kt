package com.yapp.love.domain.photolog.repository

import com.yapp.love.domain.photolog.model.Photolog
import java.time.LocalDate

interface PhotologRepository {
    fun save(photolog: Photolog): Photolog

    fun findById(id: Long): Photolog?

    fun findByGoalIdsAndVerificationDate(
        goalIds: List<Long>,
        verificationDate: LocalDate,
    ): List<Photolog>

    fun findByGoalIdAndUserIdAndVerificationDate(
        goalId: Long,
        userId: Long,
        verificationDate: LocalDate,
    ): Photolog?

    /**
     * 특정 목표의 날짜 범위 내 인증 기록 삭제
     */
    fun deleteByGoalIdAndVerificationDateBetween(
        goalId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Int
}
