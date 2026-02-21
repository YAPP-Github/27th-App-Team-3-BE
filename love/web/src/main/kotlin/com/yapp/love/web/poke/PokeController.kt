package com.yapp.love.web.poke

import com.yapp.love.application.poke.PokeService
import com.yapp.love.web.auth.AuthUser
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
    ): ResponseEntity<PokeResponse> {
        pokeService.poke(userId, goalId)
        return ResponseEntity.ok(PokeResponse(message = "찌르기를 보냈습니다."))
    }
}

data class PokeResponse(
    val message: String,
)
