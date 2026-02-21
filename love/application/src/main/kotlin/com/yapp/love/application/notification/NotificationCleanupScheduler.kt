package com.yapp.love.application.notification

import com.yapp.love.domain.notification.NotificationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Component
class NotificationCleanupScheduler(
    private val notificationRepository: NotificationRepository,
) {
    companion object {
        private const val RETENTION_DAYS = 14L
    }

    @Scheduled(cron = "0 0 3 * * *")
    @SchedulerLock(name = "deleteOldNotifications", lockAtMostFor = "30m", lockAtLeastFor = "5m")
    @Transactional
    fun deleteOldNotifications() {
        val threshold = LocalDateTime.now().minusDays(RETENTION_DAYS)
        val deleted = notificationRepository.deleteCreatedBefore(threshold)
        logger.info { "오래된 알림 삭제 완료: ${deleted}건 (기준: $threshold)" }
    }
}