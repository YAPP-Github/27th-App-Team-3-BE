package com.yapp.love.infrastructure.persistence.poke

import com.yapp.love.domain.poke.PokeRepository
import com.yapp.love.domain.poke.model.Poke
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface PokeJpaRepositoryInterface : JpaRepository<Poke, Long>

@Repository
class PokeJpaRepository(
    private val jpaRepository: PokeJpaRepositoryInterface,
) : PokeRepository {
    override fun save(poke: Poke): Poke {
        return jpaRepository.save(poke)
    }
}
