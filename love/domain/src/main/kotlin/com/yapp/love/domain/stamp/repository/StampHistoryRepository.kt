package com.yapp.love.domain.stamp.repository

import com.yapp.love.domain.stamp.model.StampHistory
import java.time.LocalDate

interface StampHistoryRepository {
    fun save(stampHistory: StampHistory): StampHistory

    fun findByPhotologId(photologId: Long): StampHistory?

    fun findByGoalIdAndUserIdAndStampDateBetween(
        goalId: Long,
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<StampHistory>

    fun findByGoalIdsAndUserIdsAndStampDateBetween(
        goalIds: List<Long>,
        userIds: List<Long>,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<StampHistory>

    fun deleteByPhotologId(photologId: Long)
}
