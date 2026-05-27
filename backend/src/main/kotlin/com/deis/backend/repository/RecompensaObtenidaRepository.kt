package com.deis.backend.repository

import com.deis.backend.model.RecompensaObtenida
import org.springframework.data.mongodb.repository.MongoRepository

interface RecompensaObtenidaRepository : MongoRepository<RecompensaObtenida, String> {
    fun findByUsuarioId(usuarioId: String): List<RecompensaObtenida>
    fun existsByUsuarioIdAndRecompensaCodigo(usuarioId: String, recompensaCodigo: String): Boolean
}