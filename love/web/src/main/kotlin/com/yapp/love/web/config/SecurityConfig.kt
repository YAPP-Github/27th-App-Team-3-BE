package com.yapp.love.web.config

import com.yapp.love.web.security.JwtAuthenticationEntryPoint
import com.yapp.love.web.security.JwtAuthenticationFilter
import com.yapp.love.web.security.SecurityMdcLoggingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val corsConfigurationSource: CorsConfigurationSource,
) {
    @Bean
    fun securityMdcLoggingFilter(): SecurityMdcLoggingFilter {
        return SecurityMdcLoggingFilter()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val mdcFilter = securityMdcLoggingFilter()

        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(
                        "/api/v1/auth/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/actuator/health",
                    )
                    .permitAll()
                    // @AuthUser를 사용하는 엔드포인트만 인증 필요
                    // 나머지는 permitAll로 설정하여 선택적 인증 지원
                    .anyRequest()
                    .permitAll()
            }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(mdcFilter, JwtAuthenticationFilter::class.java)

        return http.build()
    }
}
