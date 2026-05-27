package com.conference.deis.network.model

data class LogroItemResponse(
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val desbloqueado: Boolean,
    val fechaDesbloqueo: String? = null
)

data class LogrosUsuarioResponse(
    val usuarioId: String,
    val desbloqueados: List<LogroItemResponse>,
    val pendientes: List<LogroItemResponse>
)