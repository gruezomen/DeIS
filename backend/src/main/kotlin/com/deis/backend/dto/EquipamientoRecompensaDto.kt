package com.deis.backend.dto

data class EquipamientoRecompensaRequest(
    val medallaCodigo: String? = null,
    val marcoCodigo: String? = null,
    val tituloCodigo: String? = null,
    val limpiarCampo: String? = null
)

data class EquipamientoRecompensaResponse(
    val usuarioId: String,
    val medallaCodigo: String? = null,
    val marcoCodigo: String? = null,
    val tituloCodigo: String? = null
)