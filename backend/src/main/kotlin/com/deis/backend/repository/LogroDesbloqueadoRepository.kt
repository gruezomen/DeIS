package com.deis.backend.repository

import com.deis.backend.model.LogroDesbloqueado
import org.springframework.data.mongodb.repository.MongoRepository

interface LogroDesbloqueadoRepository : MongoRepository<LogroDesbloqueado, String> {
    fun findByUsuarioId(usuarioId: String): List<LogroDesbloqueado>
    fun existsByUsuarioIdAndLogroCodigo(usuarioId: String, logroCodigo: String): Boolean
}