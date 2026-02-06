package com.yapp.love.application.user

import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.domain.user.model.User
import com.yapp.love.domain.user.repository.UserRepository
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userAdditionInfoRepository: UserAdditionInfoRepository
) {
    fun getUserById(id: Long): UserInfo {
        val user = userRepository.findById(id) ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "존재하지 않는 유저입니다.")
        val userAdditionInfo = userAdditionInfoRepository.findByUserId(id) ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "존재하지 않는 유저 정보입니다.")

        return UserInfo.from(user, userAdditionInfo)
    }
}
