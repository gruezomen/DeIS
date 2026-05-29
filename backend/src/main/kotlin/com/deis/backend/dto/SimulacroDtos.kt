package com.deis.backend.dto

data class CrearSimulacroRequest(
    val nombre: String,
    val fechaInicio: String,
    val fechaFin: String,
    val bancoId: String? = null,
    val preguntaIds: List<String> = emptyList(),
    val facultadId: String? = null,
    val facultadNombre: String? = null,
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
    val facultadId: String?,
    val facultadNombre: String?,
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
