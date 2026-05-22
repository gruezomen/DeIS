package com.conference.deis.network.model

data class ComparacionRendimientoResponse(
    val usuarioId: String,
    val ultimoResultado: Double?,
    val resultadoAnterior: Double?,
    val diferencia: Double?,
    val estado: String,
    val mensaje: String,
    val totalIntentos: Int
)