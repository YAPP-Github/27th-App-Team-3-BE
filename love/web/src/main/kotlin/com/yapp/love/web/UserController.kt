package com.yapp.love.web

import com.yapp.love.application.user.UserService
import com.yapp.love.domain.user.model.User
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    fun getAllUsers(): List<User> {
        return userService.getAllUsers()
    }
}
