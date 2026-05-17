package com.yapp.love.application.poke

import com.yapp.love.application.notification.event.PokedEvent
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.poke.PokeRepository
import com.yapp.love.domain.poke.model.Poke
import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class PokeService(
    private val pokeRepository: PokeRepository,
    private val coupleInfoRepository: CoupleInfoRepository,
    private val goalRepository: GoalRepository,
    private val userAdditionInfoRepository: UserAdditionInfoRepository,
    private val notificationEventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun poke(
        senderId: Long,
        goalId: Long,
        verificationDate: LocalDate,
    ) {
        // 1. 커플 정보 확인
        val coupleInfo =
            coupleInfoRepository.findByUserId(senderId)
                ?: throw GlobalException(GlobalErrorCode.COUPLE_NOT_FOUND, "커플 정보가 없습니다.")

        // 2. 상대방 ID 확인
        val receiverId =
            if (coupleInfo.user1Id == senderId) {
                coupleInfo.user2Id
            } else {
                coupleInfo.user1Id
            }

        // 3. 목표 확인 (해당 커플의 목표인지)
        val goal =
            goalRepository.findById(goalId)
                ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "목표를 찾을 수 없습니다.")

        if (goal.coupleId != coupleInfo.id) {
            throw GlobalException(GlobalErrorCode.FORBIDDEN, "해당 목표에 접근 권한이 없습니다.")
        }
        if (goal.goalStatus != GoalStatus.IN_PROGRESS) {
            throw GlobalException(GlobalErrorCode.INVALID_INPUT_VALUE, "진행중이지 않은 목표는 찌를 수 없습니다.")
        }

        // 5. 찌르기 저장
        val poke =
            Poke.create(
                senderId = senderId,
                receiverId = receiverId,
                goalId = goalId,
            )
        pokeRepository.save(poke)

        // 6. 알림 이벤트 발행
        val senderNickname = userAdditionInfoRepository.findByUserId(senderId)?.nickname ?: "상대방"
        notificationEventPublisher.publishEvent(
            PokedEvent(
                targetUserId = receiverId,
                senderNickname = senderNickname,
                goalId = goalId,
                goalName = goal.name,
                verificationDate = verificationDate,
            )
        )
    }
}
