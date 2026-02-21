package com.yapp.love.domain.poke

import com.yapp.love.domain.poke.model.Poke

interface PokeRepository {
    fun save(poke: Poke): Poke
}
