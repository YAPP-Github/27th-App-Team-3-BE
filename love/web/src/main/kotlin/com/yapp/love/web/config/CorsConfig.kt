package com.yapp.love.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * CORS 설정
 *
 * 참고: 모바일 앱(안드로이드/iOS)은 네이티브 앱이므로 CORS 제약이 없습니다.
 * CORS는 브라우저의 Same-Origin Policy 때문에 발생하는 문제입니다.
 *
 * 이 설정은 다음 목적으로 사용됩니다:
 * - Swagger UI 접근 (브라우저에서 접근)
 * - 개발/테스트 목적의 브라우저 API 호출
 * - 향후 웹 프론트엔드 추가 시 대비
 */
@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration =
            CorsConfiguration().apply {
                // 모든 origin 허용 (모바일 앱은 CORS 제약이 없으므로, 주로 Swagger UI용)
                allowedOrigins = listOf("*")
                allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                allowedHeaders = listOf("*")
                exposedHeaders = listOf("Authorization", "Content-Type")
                allowCredentials = false // "*"와 함께 사용할 수 없음
                maxAge = 3600L
            }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
