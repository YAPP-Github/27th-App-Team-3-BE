package com.yapp.love.web.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.yapp.love.globalutils.exception.ErrorResponse
import com.yapp.love.globalutils.exception.GlobalErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        val error = ErrorResponse.from(GlobalErrorCode.UNAUTHORIZED)
        objectMapper.writeValue(response.outputStream, error)
    }
}
