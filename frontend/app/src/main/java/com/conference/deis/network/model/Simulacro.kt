package com.conference.deis.network.model

data class Simulacro(
    val id: String? = null,
    val bancoId: String? = null,
    val tiempo: Int,
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val puntaje: Double = 0.0,
    val preguntaIds: List<String> = emptyList()
)

data class CrearSimulacroRequest(
    val bancoId: String,
    val tiempo: Int,
    val preguntaIds: List<String> = emptyList()
)
