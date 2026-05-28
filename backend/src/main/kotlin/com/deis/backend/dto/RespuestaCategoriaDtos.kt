package com.deis.backend.dto

data class RespuestaCategoriaRequest(
    val preguntaId: String,
    val categoria: String,
    val esCorrecta: Boolean
)

data class RegistrarRespuestaCategoriaRequest(
    val usuarioId: String,
    val intentoId: String? = null,
    val bancoId: String,
    val preguntaId: String,
    val categoria: String,
    val esCorrecta: Boolean,
    val tipo: String = "PRACTICA"
)

data class ActualizarRespuestaCategoriaRequest(
    val categoria: String? = null,
    val esCorrecta: Boolean? = null
)

data class RendimientoCategoriaResponse(
    val categoria: String,
    val totalPreguntas: Int,
    val correctas: Int,
    val incorrectas: Int,
    val porcentaje: Double,
    val estado: String
)