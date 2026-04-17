package com.deis.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegistroUsuarioRequest(

    @field:NotBlank(message = "El nombre es obligatorio")
    val nombre: String,

    @field:NotBlank(message = "El apellido es obligatorio")
    val apellido: String,

    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no tiene un formato válido")
    val gmail: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    val contrasena: String
)