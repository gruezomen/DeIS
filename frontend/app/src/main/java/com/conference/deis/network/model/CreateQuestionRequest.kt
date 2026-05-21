package com.conference.deis.network.model

data class CreateQuestionRequest(
    val enunciado: String,
    val solucion: String,
    val dificultad: String,
    val categoria: String,
    val opciones: List<String>,
    val indiceCorrecta: Int,
    val bancoPreguntaId: String? = null
)