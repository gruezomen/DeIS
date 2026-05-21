package com.deis.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ActualizarUsuarioRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    val nombre: String,

    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no tiene un formato válido")
    val correo: String,

    val contrasena: String? = null,

    val facultadesIds: List<String> = emptyList()
)
