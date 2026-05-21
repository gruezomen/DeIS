package com.conference.deis.network.model

data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String
)