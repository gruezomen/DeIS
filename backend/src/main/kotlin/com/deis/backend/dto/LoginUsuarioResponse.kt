package com.deis.backend.dto

data class LoginUsuarioResponse(
    val id: String?,
    val nombre: String,
    val apellido: String,
    val gmail: String,
    val rol: String,
    val facultadesIds: List<String>,
    val mensaje: String,
    val fotoPerfilUrl: String? = null,
    val fotoGoogleUrl: String? = null
)