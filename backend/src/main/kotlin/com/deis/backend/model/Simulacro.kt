package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "simulacros")
data class Simulacro(
    @Id
    val id: String? = null,
    val bancoId: String? = null,
    val tiempo: Int,
    val horaInicio: String,
    val horaFin: String,
    val puntaje: Double = 0.0,
    val preguntaIds: List<String> = emptyList()
)
