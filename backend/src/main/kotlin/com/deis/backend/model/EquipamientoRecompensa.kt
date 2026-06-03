package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "equipamiento_recompensa")
data class EquipamientoRecompensa(
    @Id
    val id: String? = null,
    val usuarioId: String,
    val medallaCodigo: String? = null,
    val marcoCodigo: String? = null,
    val tituloCodigo: String? = null
)