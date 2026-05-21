package com.conference.deis.network.model

data class RegisterResponse(
    val id: String?,
    val nombre: String,
    val apellido: String,
    val gmail: String,
    val rol: String,
    val facultadesIds: List<String>,
    val mensaje: String
)