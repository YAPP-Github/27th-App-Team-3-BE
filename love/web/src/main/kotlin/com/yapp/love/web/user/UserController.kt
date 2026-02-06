package com.yapp.love.web.user

import com.yapp.love.application.user.UserInfo
import com.yapp.love.application.user.UserService
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import com.yapp.love.web.auth.AuthUser
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "유저 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun getMyInfo(
        @AuthUser userId: Long,
    ): UserInfo {
        return userService.getUserById(userId)
            ?: throw GlobalException(
                GlobalErrorCode.NOT_FOUND,
            )
    }
}
