package com.deis.backend.dto

data class RegistrarPushTokenRequest(
    val usuarioId: String,
    val tokenFcm: String,
    val plataforma: String = "ANDROID"
)

data class RegistrarPushTokenResponse(
    val mensaje: String
)