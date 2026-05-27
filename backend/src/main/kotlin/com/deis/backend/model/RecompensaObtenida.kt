package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "recompensas_obtenidas")
data class RecompensaObtenida(
    @Id
    val id: String? = null,
    val usuarioId: String,
    val recompensaCodigo: String,
    val fechaObtencion: LocalDateTime = LocalDateTime.now()
)