package com.deis.backend.repository

import com.deis.backend.model.RespuestaCategoria
import org.springframework.data.mongodb.repository.MongoRepository

interface RespuestaCategoriaRepository : MongoRepository<RespuestaCategoria, String> {

    fun findByUsuarioIdOrderByFechaDesc(usuarioId: String): List<RespuestaCategoria>

    fun findByUsuarioIdAndCategoriaOrderByFechaDesc(
        usuarioId: String,
        categoria: String
    ): List<RespuestaCategoria>
}