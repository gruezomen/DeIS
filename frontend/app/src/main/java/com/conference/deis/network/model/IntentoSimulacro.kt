package com.conference.deis.network.model

data class IntentoSimulacro(
    val id: String? = null,
    val usuarioId: String,
    val bancoId: String,
    val tipo: String = "SIMULACRO",
    val puntaje: Int,
    val totalPreguntas: Int,
    val respuestasCorrectas: Int = puntaje,
    val respuestasIncorrectas: Int = (totalPreguntas - puntaje).coerceAtLeast(0),
    val fecha: String? = null,
    val respuestasPorCategoria: List<RespuestaCategoriaRequest> = emptyList(),
    val detalleRespuestas: List<RespuestaIntentoDetalleRequest> = emptyList()
)
