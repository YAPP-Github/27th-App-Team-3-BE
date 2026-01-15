package com.yapp.love.web.security

import com.yapp.love.application.auth.port.TokenProvider
import com.yapp.love.globalutils.exception.GlobalException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
) : OncePerRequestFilter() {
    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        const val TOKEN_ERROR_ATTRIBUTE = "TOKEN_ERROR"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val token = resolveToken(request)

            if (token != null && tokenProvider.validateToken(token)) {
                val userId = tokenProvider.getUserIdFromToken(token)
                val tokenType = tokenProvider.getTokenType(token)

                if (tokenType == TokenProvider.TOKEN_TYPE_ACCESS) {
                    val authentication = createAuthentication(userId, request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (e: GlobalException) {
            logger.warn { "JWT token error: ${e.errorCode.getCode()} - ${e.getCustomMessage()}" }
            // 토큰 에러 정보를 request attribute에 저장
            request.setAttribute(TOKEN_ERROR_ATTRIBUTE, e.errorCode)
            SecurityContextHolder.clearContext()
        } catch (e: Exception) {
            logger.warn { "JWT authentication failed: ${e.message}" }
            // 필터에서 예외를 던지면 GlobalExceptionHandler가 처리할 수 없으므로
            // SecurityContext를 clear하고 계속 진행
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }

    private fun createAuthentication(
        userId: Long,
        request: HttpServletRequest,
    ): UsernamePasswordAuthenticationToken {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = UsernamePasswordAuthenticationToken(userId, null, authorities)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        return authentication
    }
}
