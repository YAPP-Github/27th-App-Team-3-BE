package com.yapp.love.web.auth

import org.springframework.security.core.annotation.AuthenticationPrincipal

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class AuthUser
