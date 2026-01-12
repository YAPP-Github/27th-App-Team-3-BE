package com.yapp.love.infrastructure.logging

import org.springframework.security.core.context.SecurityContextHolder

/**
 * Spring Security와 JWT 인증이 있는 환경에서 사용하는 MDC 로깅 필터
 *
 * SecurityContext에서 JWT 토큰을 읽어 사용자 ID를 MDC에 추가합니다.
 * SecurityConfig에서 JwtAuthenticationFilter 이후에 실행되도록 설정됩니다.
 *
 * @Component가 없으므로 SecurityConfig에서 Bean으로 등록해야 합니다.
 */
class SecurityMdcLoggingFilter : BaseMdcLoggingFilter() {
    /**
     * SecurityContext에서 인증된 사용자 ID를 추출합니다.
     *
     * @return 사용자 ID 또는 null (GUEST로 처리됨)
     */
    override fun resolveUserId(): String? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        if (!authentication.isAuthenticated) return null

        return when (val principal = authentication.principal) {
            is Long -> principal.toString()
            is String -> principal.takeIf { it != "anonymousUser" }
            else -> null
        }
    }
}
