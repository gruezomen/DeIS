package com.deis.backend.dto

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
    val id: String?,
    val intentoId: String,
    val preguntaId: String,
    val enunciado: String,
    val categoria: String,
    val respuestaSeleccionada: String,
    val respuestaCorrecta: String,
    val esCorrecta: Boolean,
    val orden: Int
)