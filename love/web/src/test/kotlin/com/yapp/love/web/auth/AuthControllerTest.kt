package com.yapp.love.web.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.yapp.love.application.auth.dto.OAuthLoginResult
import com.yapp.love.application.auth.service.AuthService
import com.yapp.love.web.auth.dto.AppleLoginRequest
import com.yapp.love.web.auth.dto.GoogleLoginRequest
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class)
@Import(AuthControllerTest.MockConfig::class)
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var authService: AuthService

    @TestConfiguration
    class MockConfig {
        @Bean
        fun authService(): AuthService = mockk()
    }

    @BeforeEach
    fun setUp() {
        clearMocks(authService)
    }

    @Test
    @DisplayName("Apple 로그인 성공 - 신규 사용자")
    fun `apple login success - new user`() {
        // given
        val request = AppleLoginRequest(code = "valid_authorization_code")
        val expectedResult =
            OAuthLoginResult(
                userId = 1L,
                accessToken = "access_token_123",
                refreshToken = "refresh_token_456",
                isNewUser = true,
            )

        every { authService.appleLogin(any()) } returns expectedResult

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.accessToken").value("access_token_123"))
            .andExpect(jsonPath("$.refreshToken").value("refresh_token_456"))
            .andExpect(jsonPath("$.isNewUser").value(true))

        verify(exactly = 1) { authService.appleLogin(any()) }
    }

    @Test
    @DisplayName("Apple 로그인 성공 - 기존 사용자")
    fun `apple login success - existing user`() {
        // given
        val request = AppleLoginRequest(code = "valid_authorization_code")
        val expectedResult =
            OAuthLoginResult(
                userId = 42L,
                accessToken = "access_token_existing",
                refreshToken = "refresh_token_existing",
                isNewUser = false,
            )

        every { authService.appleLogin(any()) } returns expectedResult

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(42))
            .andExpect(jsonPath("$.isNewUser").value(false))
    }

    @Test
    @DisplayName("Apple 로그인 실패 - code 누락")
    fun `apple login fail - missing code`() {
        // given
        val invalidRequest = """{"code": ""}"""

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest),
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Google 로그인 성공 - 신규 사용자")
    fun `google login success - new user`() {
        // given
        val request = GoogleLoginRequest(code = "valid_google_authorization_code")
        val expectedResult =
            OAuthLoginResult(
                userId = 1L,
                accessToken = "google_access_token_123",
                refreshToken = "google_refresh_token_456",
                isNewUser = true,
            )

        every { authService.googleLogin(any()) } returns expectedResult

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.accessToken").value("google_access_token_123"))
            .andExpect(jsonPath("$.refreshToken").value("google_refresh_token_456"))
            .andExpect(jsonPath("$.isNewUser").value(true))

        verify(exactly = 1) { authService.googleLogin(any()) }
    }

    @Test
    @DisplayName("Google 로그인 성공 - 기존 사용자")
    fun `google login success - existing user`() {
        // given
        val request = GoogleLoginRequest(code = "valid_google_authorization_code")
        val expectedResult =
            OAuthLoginResult(
                userId = 42L,
                accessToken = "google_access_token_existing",
                refreshToken = "google_refresh_token_existing",
                isNewUser = false,
            )

        every { authService.googleLogin(any()) } returns expectedResult

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(42))
            .andExpect(jsonPath("$.isNewUser").value(false))
    }

    @Test
    @DisplayName("Google 로그인 실패 - code 누락")
    fun `google login fail - missing code`() {
        // given
        val invalidRequest = """{"code": ""}"""

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest),
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }
}
