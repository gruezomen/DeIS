package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "bancos_preguntas")
data class BancoPregunta(
    @Id
    val id: String? = null,
    val facultadId: String,
    val administradorId: String,
    val preguntaIds: List<String> = emptyList()
)