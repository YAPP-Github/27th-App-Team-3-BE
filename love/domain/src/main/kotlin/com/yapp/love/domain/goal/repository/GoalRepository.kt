package com.yapp.love.domain.goal.repository

import com.yapp.love.domain.goal.model.Goal
import java.time.LocalDate

interface GoalRepository {
    fun save(goal: Goal): Goal

    fun findById(id: Long): Goal?

    /**
     * 특정 커플의 활성 목표 조회
     * 우선순위: DAILY > WEEKLY > MONTHLY
     * 정렬: 시작날짜 오름차순
     */
    fun findActiveGoalsByCoupleIdAndDate(
        coupleId: Long,
        targetDate: LocalDate,
    ): List<Goal>

    /**
     * ID로 활성 목표 조회 (삭제되지 않은 목표만)
     */
    fun findActiveGoalById(id: Long): Goal?

    /**
     * NOT_STARTED 상태의 목표 중 시작일이 today 이전인 것들을 IN_PROGRESS로 변경
     */
    fun updateNotStartedToInProgress(today: LocalDate): Int

    /**
     * IN_PROGRESS 상태의 목표 중 종료일이 today 이전인 것들을 COMPLETED로 변경
     */
    fun updateInProgressToCompleted(today: LocalDate): Int

    /**
     * 커플의 모든 목표 ID 조회
     */
    fun findIdsByCoupleId(coupleId: Long): List<Long>

    /**
     * 커플의 모든 목표 삭제
     */
    fun deleteByCoupleId(coupleId: Long)
}
