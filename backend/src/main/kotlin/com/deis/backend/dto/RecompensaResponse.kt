package com.deis.backend.dto

data class RecompensaItemResponse(
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val fechaObtencion: String? = null
)

data class RecompensasUsuarioResponse(
    val usuarioId: String,
    val recompensas: List<RecompensaItemResponse>
)