package com.yapp.love.infrastructure.persistence.notification

import com.yapp.love.domain.notification.NotificationSettingRepository
import com.yapp.love.domain.notification.model.NotificationSetting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationSettingJpaRepository :
    NotificationSettingRepository,
    JpaRepository<NotificationSetting, Long> {
    override fun findByUserId(userId: Long): NotificationSetting?
}
