package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "usuarios")
data class Usuario(
    @Id
    val id: String? = null,
    val nombre: String,
    val apellido: String,
    val gmail: String,
    val contrasena: String,
    val rol: String
)