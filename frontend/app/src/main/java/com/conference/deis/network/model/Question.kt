package com.conference.deis.network.model

data class Question(
    val id: String,
    val enunciado: String,
    val solucion: String,
    val video: String,
    val dificultad: String,
    val categoria: Category,
    val opciones: List<Option>
)

data class Category(
    val nombre: String,
    val descripcion: String
)

data class Option(
    val texto: String,
    val esCorrecta: Boolean
)
