package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "recompensas")
data class Recompensa(
    @Id
    val id: String? = null,
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val activo: Boolean = true
)