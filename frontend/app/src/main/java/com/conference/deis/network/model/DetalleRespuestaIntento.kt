package com.conference.deis.network.model

data class RespuestaIntentoDetalleRequest(
    val preguntaId: String,
    val enunciado: String,
    val categoria: String,
    val respuestaSeleccionada: String,
    val respuestaCorrecta: String,
    val esCorrecta: Boolean,
    val orden: Int
)

data class DetalleRespuestaIntentoResponse(
    val id: String? = null,
    val intentoId: String = "",
    val preguntaId: String = "",
    val enunciado: String = "",
    val categoria: String = "",
    val respuestaSeleccionada: String = "",
    val respuestaCorrecta: String = "",
    val esCorrecta: Boolean = false,
    val orden: Int = 0
)
data class ErrorPreguntaCategoriaResponse(
    val intentoId: String = "",
    val preguntaId: String = "",
    val enunciado: String = "",
    val categoria: String = "",
    val respuestaSeleccionada: String = "",
    val respuestaCorrecta: String = "",
    val orden: Int = 0
)

data class ErroresPorCategoriaResponse(
    val categoria: String = "",
    val totalErrores: Int = 0,
    val errores: List<ErrorPreguntaCategoriaResponse> = emptyList()
)