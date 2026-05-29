package com.conference.deis.network.model

data class Notificacion(
    val id: String? = null,
    val usuarioId: String,
    val titulo: String,
    val mensaje: String,
    val tipo: String = "SIMULACRO",
    val leida: Boolean = false,
    val fechaCreacion: String,
    val referenciaId: String? = null
)