package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "preguntas")
data class Pregunta(
    @Id
    val id: String? = null,
    val enunciado: String,
    val solucion: String,
    val video: String,
    val dificultad: Dificultad,
    val categoria: Categoria,
    val tipo: TipoPregunta = TipoPregunta.SELECCION_MULTIPLE,
    val opciones: List<Opcion> = emptyList()
)