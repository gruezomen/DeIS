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
    val tipo: String = "SIMULACRO",
    val puntaje: Int,
    val totalPreguntas: Int,
    val respuestasCorrectas: Int = puntaje,
    val respuestasIncorrectas: Int = (totalPreguntas - puntaje).coerceAtLeast(0),
    val fecha: LocalDateTime = LocalDateTime.now()
)
