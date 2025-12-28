package com.yapp.love.domain.user.repository

import com.yapp.love.domain.user.model.User

interface UserRepository {
    fun findAll(): List<User>

    fun save(user: User): User

    fun findById(id: Long): User?

    fun deleteById(id: Long)
}
