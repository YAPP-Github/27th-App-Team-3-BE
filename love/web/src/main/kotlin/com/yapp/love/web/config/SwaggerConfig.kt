package com.yapp.love.web.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "Bearer Authentication"

        return OpenAPI()
            .info(apiInfo())
            .servers(
                listOf(
                    Server().url("/").description("Current Server"),
                ),
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description(
                                """
                                ## AccessToken과 RefreshToken

                                ### 사용 방법
                                - **AccessToken**: 일반 API 요청 시 `Authorization` 헤더에 사용
                                - **RefreshToken**: AccessToken 갱신 시에만 사용 (일반 API 요청에 사용 불가)

                                ### 헤더 형식
                                ```
                                Authorization: Bearer {accessToken}
                                ```

                                ### 주의사항
                                ⚠️ **RefreshToken을 일반 API 요청의 Authorization 헤더에 사용할 경우 401 Unauthorized가 반환됩니다.**

                                ### 401 응답 예시
                                RefreshToken을 Authorization 헤더에 넣을 경우:
                                ```json
                                {
                                    "status": 401,
                                    "code": "G4010",
                                    "message": "인증되지 않은 사용자입니다."
                                }
                                ```
                                """.trimIndent(),
                            ),
                    ),
            )
    }

    private fun apiInfo(): Info {
        return Info()
            .title("TWIX PROJECT API")
            .description(
                """
                YAPP APP 3팀 TWIX 백엔드 API 문서
                """.trimIndent(),
            )
            .version("v1.0.0")
    }
}
