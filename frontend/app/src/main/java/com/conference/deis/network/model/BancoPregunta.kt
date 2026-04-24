package com.conference.deis.network.model

data class BancoPregunta(
    val id: String,
    val facultadId: String,
    val administradorId: String,
    val preguntaIds: List<String> = emptyList()
)