package com.yapp.love.web

import com.yapp.love.application.user.UserService
import com.yapp.love.domain.user.model.User
import com.yapp.love.web.auth.AuthUser
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun getMyInfo(
        @AuthUser userId: Long,
    ): User {
        return userService.getUserById(userId)
            ?: throw com.yapp.love.globalutils.exception.GlobalException(
                com.yapp.love.globalutils.exception.GlobalErrorCode.NOT_FOUND,
            )
    }
}
