package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "logros")
data class Logro(
    @Id
    val id: String? = null,
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val condicionValor: Int,
    val activo: Boolean = true
)