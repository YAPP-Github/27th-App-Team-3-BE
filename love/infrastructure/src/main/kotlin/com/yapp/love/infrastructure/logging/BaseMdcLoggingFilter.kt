package com.yapp.love.infrastructure.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * MDC (Mapped Diagnostic Context) 기반 로깅 필터의 기본 구현
 *
 * 모든 HTTP 요청에 대해 다음 정보를 MDC에 추가합니다:
 * - traceId: 요청 추적 ID (X-Request-ID 헤더에서 가져오거나 자동 생성)
 * - clientIp: 클라이언트 IP (X-Forwarded-For, X-Real-IP 헤더 고려)
 * - requestInfo: HTTP 메서드와 URI
 * - userId: 사용자 ID (서브클래스에서 구현)
 *
 * 서브클래스는 resolveUserId()를 오버라이드하여 사용자 ID 추출 로직을 제공할 수 있습니다.
 */
abstract class BaseMdcLoggingFilter : OncePerRequestFilter() {
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
     * 사용자 ID를 추출합니다.
     * 서브클래스에서 오버라이드하여 Security Context, JWT 등에서 사용자 정보를 추출할 수 있습니다.
     *
     * @return 사용자 ID (null인 경우 GUEST로 처리됨)
     */
    protected abstract fun resolveUserId(): String?
}
