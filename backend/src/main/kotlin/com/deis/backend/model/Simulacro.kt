package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "simulacros")
data class Simulacro(
    @Id
    val id: String? = null,
    val nombre: String = "Simulacro",
    val bancoId: String? = null,
    val tiempo: Int = 0,
    val tiempoLimiteMinutos: Int = 0,
    val horaInicio: String,
    val horaFin: String,
    val puntaje: Double = 0.0,
    val preguntaIds: List<String> = emptyList(),
    val facultadId: String? = null,
    val facultadNombre: String? = null,
    val creadoPor: String? = null,
    val zonaHorariaCreador: String = "America/La_Paz",
    val programado: Boolean = false,
    val eliminado: Boolean = false
)