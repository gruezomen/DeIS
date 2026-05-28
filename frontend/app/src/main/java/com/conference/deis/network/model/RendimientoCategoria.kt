package com.conference.deis.network.model

data class RespuestaCategoriaRequest(
    val preguntaId: String,
    val categoria: String,
    val esCorrecta: Boolean
)

data class RendimientoCategoriaResponse(
    val categoria: String,
    val totalPreguntas: Int,
    val correctas: Int,
    val incorrectas: Int,
    val porcentaje: Double,
    val estado: String
)