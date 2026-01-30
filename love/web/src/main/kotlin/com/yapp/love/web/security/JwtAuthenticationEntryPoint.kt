package com.yapp.love.web.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.yapp.love.globalutils.exception.ErrorCode
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

        // Filter에서 저장한 토큰 에러 정보 확인
        val errorCode = request.getAttribute(JwtAuthenticationFilter.TOKEN_ERROR_ATTRIBUTE) as? ErrorCode
            ?: GlobalErrorCode.UNAUTHORIZED

        val error = ErrorResponse.from(errorCode)
        objectMapper.writeValue(response.outputStream, error)
    }
}
