package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "detalle_respuestas_intento")
data class DetalleRespuestaIntento(
    @Id
    val id: String? = null,
    val intentoId: String,
    val preguntaId: String,
    val enunciado: String,
    val categoria: String,
    val respuestaSeleccionada: String,
    val respuestaCorrecta: String,
    val esCorrecta: Boolean,
    val orden: Int
)