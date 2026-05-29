package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "notificaciones")
data class Notificacion(
    @Id
    val id: String? = null,
    val usuarioId: String,
    val titulo: String,
    val mensaje: String,
    val tipo: String = "SIMULACRO",
    val leida: Boolean = false,
    val fechaCreacion: String = LocalDateTime.now().toString(),
    val referenciaId: String? = null
)