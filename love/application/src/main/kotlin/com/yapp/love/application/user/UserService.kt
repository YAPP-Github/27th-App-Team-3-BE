package com.yapp.love.application.user

import com.yapp.love.domain.user.model.User
import com.yapp.love.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    @Transactional
    fun createUser(
        name: String,
        email: String,
    ): User {
        val user = User(name = name, email = email)
        return userRepository.save(user)
    }
}
