package com.yapp.love.application.user

import com.yapp.love.domain.user.model.User
import com.yapp.love.domain.user.model.UserAdditionInfo

data class UserInfo (
    val id: Long,
    val name: String,
    val email: String,
) {
    companion object {
        fun from(user: User, userAdditionInfo: UserAdditionInfo): UserInfo {
            return UserInfo(
                id = user.id!!,
                name = userAdditionInfo.nickname,
                email = user.email,
            )
        }
    }
}
