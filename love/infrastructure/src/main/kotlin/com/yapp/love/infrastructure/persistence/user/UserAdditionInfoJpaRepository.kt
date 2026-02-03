package com.yapp.love.infrastructure.persistence.user

import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.domain.user.model.UserAdditionInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAdditionInfoJpaRepository : UserAdditionInfoRepository, JpaRepository<UserAdditionInfo, Long>