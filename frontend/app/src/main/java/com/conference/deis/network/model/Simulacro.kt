package com.conference.deis.network.model

data class Simulacro(
    val id: String? = null,
    val nombre: String = "Simulacro",
    val bancoId: String? = null,
    val tiempo: Int = 0,
    val tiempoLimiteMinutos: Int = 0,
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val estado: String = "PENDIENTE",
    val puntaje: Double = 0.0,
    val preguntaIds: List<String> = emptyList(),
    val totalPreguntas: Int = preguntaIds.size,
    val facultadId: String? = null,
    val facultadNombre: String? = null,
    val creadoPor: String? = null,
    val zonaHorariaCreador: String = "America/La_Paz",
    val segundosRestantes: Long = 0L,
    val programado: Boolean = false,
    val eliminado: Boolean = false
)

data class CrearSimulacroRequest(
    val nombre: String,
    val fechaInicio: String,
    val fechaFin: String,
    val tiempoLimiteMinutos: Int = 0,
    val bancoId: String? = null,
    val preguntaIds: List<String> = emptyList(),
    val facultadId: String? = null,
    val facultadNombre: String? = null,
    val creadoPor: String? = null,
    val zonaHorariaCreador: String? = "America/La_Paz"
)
