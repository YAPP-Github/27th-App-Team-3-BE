package com.yapp.love.application.user

import com.yapp.love.domain.user.model.User
import com.yapp.love.domain.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun getUserById(id: Long): User? {
        return userRepository.findById(id)
    }
}
