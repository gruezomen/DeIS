package com.deis.backend.repository

import com.deis.backend.model.Recompensa
import org.springframework.data.mongodb.repository.MongoRepository

interface RecompensaRepository : MongoRepository<Recompensa, String> {
    fun findByCodigo(codigo: String): Recompensa?
}