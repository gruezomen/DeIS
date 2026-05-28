package com.conference.deis.network.model

data class HistorialIntentoResponse(
    val id: String? = null,
    val usuarioId: String = "",
    val bancoId: String = "",
    val tipo: String = "SIMULACRO",
    val puntaje: Int = 0,
    val totalPreguntas: Int = 0,
    val respuestasCorrectas: Int = 0,
    val respuestasIncorrectas: Int = 0,
    val nota: Double = 0.0,
    val fecha: String? = null,
    val estado: String = "BAJO",
    val mensaje: String = ""
)