package com.conference.deis.network.model

data class IntentoSimulacro(
    val id: String? = null,
    val usuarioId: String,
    val bancoId: String,
    val puntaje: Int,
    val totalPreguntas: Int,
    val fecha: String? = null
)
