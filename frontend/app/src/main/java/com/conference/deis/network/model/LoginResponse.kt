package com.conference.deis.network.model

data class LoginResponse(
    val id: String?,
    val nombre: String,
    val apellido: String,
    val gmail: String,
    val rol: String,
    val mensaje: String
)