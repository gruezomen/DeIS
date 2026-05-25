package com.conference.deis.network.model

data class GuardarIntentoResponse(
    val intento: IntentoSimulacro,
    val nuevosLogros: List<LogroDesbloqueadoResponse>
)

data class LogroDesbloqueadoResponse(
    val id: String? = null,
    val usuarioId: String,
    val logroCodigo: String,
    val fechaDesbloqueo: String? = null
)