package com.deis.backend.repository

import com.deis.backend.model.DetalleRespuestaIntento
import org.springframework.data.mongodb.repository.MongoRepository

interface DetalleRespuestaIntentoRepository : MongoRepository<DetalleRespuestaIntento, String> {

    fun findByIntentoIdOrderByOrdenAsc(intentoId: String): List<DetalleRespuestaIntento>
}