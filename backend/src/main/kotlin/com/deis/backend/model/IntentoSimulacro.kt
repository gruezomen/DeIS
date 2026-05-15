package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "intentos_simulacros")
data class IntentoSimulacro(
    @Id
    val id: String? = null,
    val usuarioId: String,
    val bancoId: String,
    val puntaje: Int,
    val totalPreguntas: Int,
    val fecha: LocalDateTime = LocalDateTime.now()
)
