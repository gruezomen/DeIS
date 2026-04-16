package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "examenes")
data class Examen(
    @Id
    val id: String? = null,
    val bancoPreguntaId: String,
    val preguntaIds: List<String> = emptyList()
)