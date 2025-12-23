package com.yapp.love.globalutils.exception

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(controllers = [TestController::class])
@Import(GlobalExceptionHandler::class)
class GlobalExceptionHandlerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Nested
    @DisplayName("GlobalException 처리")
    inner class GlobalExceptionTest {
        @Test
        @DisplayName("GlobalException 발생 시 적절한 에러 응답을 반환한다")
        fun handleGlobalException() {
            mockMvc.perform(get("/test/global-exception"))
                .andDo(print())
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("G4040"))
                .andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."))
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException 처리")
    inner class ValidationExceptionTest {
        @Test
        @DisplayName("@Valid 유효성 검증 실패 시 400 에러를 반환한다")
        fun handleMethodArgumentNotValidException() {
            val invalidRequest = TestRequest(name = "", email = "a")

            mockMvc.perform(
                post("/test/validation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andDo(print())
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("G4000"))
        }
    }

    @Nested
    @DisplayName("HttpRequestMethodNotSupportedException 처리")
    inner class MethodNotAllowedTest {
        @Test
        @DisplayName("지원하지 않는 HTTP 메서드 요청 시 405 에러를 반환한다")
        fun handleHttpRequestMethodNotSupportedException() {
            mockMvc.perform(put("/test/get-only"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed)
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value("G4050"))
        }
    }

    @Nested
    @DisplayName("HttpMessageNotReadableException 처리")
    inner class MalformedJsonTest {
        @Test
        @DisplayName("잘못된 JSON 형식 요청 시 400 에러를 반환한다")
        fun handleHttpMessageNotReadableException() {
            val malformedJson = "{ invalid json }"

            mockMvc.perform(
                post("/test/validation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson),
            )
                .andDo(print())
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("G4002"))
        }
    }

    @Nested
    @DisplayName("Exception 처리")
    inner class GeneralExceptionTest {
        @Test
        @DisplayName("예상치 못한 예외 발생 시 500 에러를 반환한다")
        fun handleGeneralException() {
            mockMvc.perform(get("/test/unexpected-error"))
                .andDo(print())
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("G5000"))
        }
    }
}

// 테스트용 Request DTO
data class TestRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    val name: String,
    @field:Size(min = 5, message = "이메일은 5자 이상이어야 합니다")
    val email: String,
)

// 테스트용 Controller
@RestController
class TestController {
    @GetMapping("/test/global-exception")
    fun throwGlobalException() {
        throw GlobalException(GlobalErrorCode.NOT_FOUND)
    }

    @PostMapping("/test/validation")
    fun validateRequest(
        @Valid @RequestBody request: TestRequest,
    ): String {
        return "success"
    }

    @GetMapping("/test/get-only")
    fun getOnly(): String {
        return "get only"
    }

    @GetMapping("/test/unexpected-error")
    fun unexpectedError() {
        throw RuntimeException("Unexpected error!")
    }
}

// 테스트용 Application
@SpringBootApplication
class TestApplication
