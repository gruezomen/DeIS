package com.conference.deis.network.model

data class CreateQuestionResponse(
    val id: String? = null,
    val enunciado: String,
    val solucion: String,
    val video: String,
    val dificultad: String,
    val categoria: CategoriaResponse,
    val opciones: List<OpcionResponse>
)

data class CategoriaResponse(
    val id: String? = null,
    val nombre: String,
    val descripcion: String
)

data class OpcionResponse(
    val texto: String,
    val esCorrecta: Boolean
)