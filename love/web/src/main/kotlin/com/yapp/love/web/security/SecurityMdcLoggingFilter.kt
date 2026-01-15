package com.yapp.love.web.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Spring Security와 JWT 인증이 있는 환경에서 사용하는 MDC 로깅 필터
 *
 * SecurityContext에서 JWT 토큰을 읽어 사용자 ID를 MDC에 추가합니다.
 * SecurityConfig에서 JwtAuthenticationFilter 이후에 실행되도록 설정됩니다.
 */
class SecurityMdcLoggingFilter : OncePerRequestFilter() {
    companion object {
        const val TRACE_ID = "traceId"
        const val USER_ID = "userId"
        const val CLIENT_IP = "clientIp"
        const val REQUEST_INFO = "requestInfo"
        const val DEFAULT_GUEST_USER = "GUEST"

        private const val HEADER_REQUEST_ID = "X-Request-ID"
        private const val HEADER_TRACE_ID = "X-Trace-ID"
        private const val HEADER_XFF = "X-Forwarded-For"
        private const val HEADER_X_REAL_IP = "X-Real-IP"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val traceId = resolveTraceId(request)

        try {
            MDC.put(TRACE_ID, traceId)
            MDC.put(CLIENT_IP, resolveClientIp(request))
            MDC.put(REQUEST_INFO, "${request.method} ${request.requestURI}")
            MDC.put(USER_ID, resolveUserId() ?: DEFAULT_GUEST_USER)

            response.setHeader(HEADER_REQUEST_ID, traceId)

            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }

    private fun resolveTraceId(request: HttpServletRequest): String {
        return request.getHeader(HEADER_REQUEST_ID)
            ?: request.getHeader(HEADER_TRACE_ID)
            ?: UUID.randomUUID().toString().replace("-", "").substring(0, 8)
    }

    private fun resolveClientIp(request: HttpServletRequest): String {
        val xff =
            request.getHeader(HEADER_XFF)
                ?.split(",")
                ?.firstOrNull()
                ?.trim()
                ?.takeIf { it.isNotBlank() && !it.equals("unknown", ignoreCase = true) }

        return xff
            ?: request.getHeader(HEADER_X_REAL_IP)?.takeIf { it.isNotBlank() }
            ?: request.remoteAddr
    }

    /**
     * SecurityContext에서 인증된 사용자 ID를 추출합니다.
     *
     * @return 사용자 ID 또는 null (GUEST로 처리됨)
     */
    private fun resolveUserId(): String? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        if (!authentication.isAuthenticated) return null

        return when (val principal = authentication.principal) {
            is Long -> principal.toString()
            is String -> principal.takeIf { it != "anonymousUser" }
            else -> null
        }
    }
}
