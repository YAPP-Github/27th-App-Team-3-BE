package com.yapp.love.infrastructure.logging

// TODO: social-login 브랜치 머지 후 활성화
// import org.springframework.core.Ordered
// import org.springframework.core.annotation.Order
// import org.springframework.security.core.context.SecurityContextHolder
// import org.springframework.security.oauth2.jwt.Jwt
// import org.springframework.stereotype.Component

/**
 * Spring Security와 JWT 인증이 있는 환경에서 사용하는 MDC 로깅 필터
 *
 * SecurityContext에서 JWT 토큰을 읽어 사용자 ID를 MDC에 추가합니다.
 *
 * TODO: social-login 브랜치 머지 후 활성화
 * - @Component 주석 해제
 * - SimpleMdcLoggingFilter의 @Component 제거
 * - @Component
 * - @Order(Ordered.HIGHEST_PRECEDENCE)
 */
class SecurityMdcLoggingFilter : BaseMdcLoggingFilter() {
    /**
     * SecurityContext에서 인증된 사용자 ID를 추출합니다.
     *
     * @return 사용자 ID 또는 null (GUEST로 처리됨)
     */
    override fun resolveUserId(): String? {
        // TODO: social-login 브랜치 머지 후 구현
        // val authentication = SecurityContextHolder.getContext().authentication ?: return null
        // if (!authentication.isAuthenticated) return null
        //
        // return when (val principal = authentication.principal) {
        //     is Jwt -> principal.subject
        //     is String -> principal.takeIf { it != "anonymousUser" }
        //     else -> null
        // }
        return null
    }
}
