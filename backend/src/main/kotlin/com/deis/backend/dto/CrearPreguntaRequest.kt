package com.deis.backend.dto

data class CrearPreguntaRequest(
    val enunciado: String,
    val solucion: String,
    val dificultad: String,
    val categoria: String,
    val opciones: List<String>,
    val indiceCorrecta: Int
)