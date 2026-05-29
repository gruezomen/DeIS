package com.deis.backend.repository

import com.deis.backend.model.Notificacion
import org.springframework.data.mongodb.repository.MongoRepository

interface NotificacionRepository : MongoRepository<Notificacion, String> {
    fun findByUsuarioIdOrderByFechaCreacionDesc(usuarioId: String): List<Notificacion>
}