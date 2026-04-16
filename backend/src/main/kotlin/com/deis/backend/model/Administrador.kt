package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "administradores")
data class Administrador(
    @Id
    val id: String? = null,
    val usuarioId: String,
    val cargo: String
)