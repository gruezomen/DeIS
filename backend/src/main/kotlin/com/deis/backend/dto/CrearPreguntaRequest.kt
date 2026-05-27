package com.deis.backend.dto

data class CrearPreguntaRequest(
    val enunciado: String,
    val solucion: String,
    val dificultad: String,
    val categoria: String,
    val tipo: String = "SELECCION_MULTIPLE",
    val opciones: List<String>,
    val indiceCorrecta: Int,
    val bancoPreguntaId: String? = null
)