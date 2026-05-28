package com.deis.backend.dto

data class CrearSimulacroRequest(
    val nombre: String,
    val fechaInicio: String,
    val fechaFin: String,
    val bancoId: String? = null,
    val preguntaIds: List<String> = emptyList(),
    val creadoPor: String? = null,
    val zonaHorariaCreador: String? = "America/La_Paz"
)

data class SimulacroResponse(
    val id: String?,
    val nombre: String,
    val bancoId: String?,
    val tiempo: Int,
    val horaInicio: String,
    val horaFin: String,
    val estado: EstadoSimulacro,
    val puntaje: Double,
    val preguntaIds: List<String>,
    val totalPreguntas: Int,
    val creadoPor: String?,
    val zonaHorariaCreador: String,
    val segundosRestantes: Long,
    val programado: Boolean,
    val eliminado: Boolean
)

enum class EstadoSimulacro {
    PENDIENTE,
    ACTIVO,
    FINALIZADO
}
