package com.yapp.love.infrastructure.persistence.user

import com.yapp.love.domain.user.model.User
import com.yapp.love.domain.user.repository.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : UserRepository, JpaRepository<User, Long>
