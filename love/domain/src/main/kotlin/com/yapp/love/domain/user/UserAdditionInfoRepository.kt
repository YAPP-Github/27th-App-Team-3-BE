package com.yapp.love.domain.user

import com.yapp.love.domain.user.model.UserAdditionInfo

interface UserAdditionInfoRepository {
    fun save(userAdditionInfo: UserAdditionInfo): UserAdditionInfo

    fun findByUserId(userId: Long): UserAdditionInfo?

    fun deleteByUserId(userId: Long)
}
