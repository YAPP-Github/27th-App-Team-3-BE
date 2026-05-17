package com.yapp.love.web.poke

import com.yapp.love.application.poke.PokeService
import com.yapp.love.web.auth.AuthUser
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "Poke", description = "찌르기 API")
@RestController
@RequestMapping("/api/v1/pokes")
class PokeController(
    private val pokeService: PokeService,
) {
    @PokeApiSpec
    @PostMapping("/goals/{goalId}")
    fun poke(
        @AuthUser userId: Long,
        @PathVariable goalId: Long,
        @Valid @RequestBody request: PokeRequest,
    ): ResponseEntity<PokeResponse> {
        pokeService.poke(userId, goalId, request.date)
        return ResponseEntity.ok(PokeResponse(message = "찌르기를 보냈습니다."))
    }
}

data class PokeRequest(
    @field:NotNull(message = "인증 날짜는 필수입니다.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate,
)

data class PokeResponse(
    val message: String,
)
