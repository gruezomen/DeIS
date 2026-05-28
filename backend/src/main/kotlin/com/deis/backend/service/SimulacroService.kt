
package com.deis.backend.service

import com.deis.backend.dto.CrearSimulacroRequest
import com.deis.backend.dto.EstadoSimulacro
import com.deis.backend.dto.SimulacroResponse
import com.deis.backend.model.Simulacro
import com.deis.backend.repository.SimulacroRepository
import org.springframework.stereotype.Service
import java.time.DateTimeException
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Service
class SimulacroService(
    private val simulacroRepository: SimulacroRepository
) {
    private val zonaHorariaPorDefecto = "America/La_Paz"

    fun crearSimulacro(request: CrearSimulacroRequest): SimulacroResponse {
        val nombre = request.nombre.trim()
        if (nombre.isBlank()) {
            throw IllegalArgumentException("El nombre del simulacro es obligatorio")
        }

        if (request.preguntaIds.isEmpty()) {
            throw IllegalArgumentException("Debe seleccionar al menos una pregunta")
        }

        val fechaInicio = parsearFecha(request.fechaInicio, "La fecha y hora de inicio no tiene un formato válido")
        val fechaFin = parsearFecha(request.fechaFin, "La fecha y hora de finalización no tiene un formato válido")
        val zonaHoraria = normalizarZonaHoraria(request.zonaHorariaCreador)

        if (!fechaFin.isAfter(fechaInicio)) {
            throw IllegalArgumentException("La fecha de finalización debe ser posterior a la fecha de inicio")
        }

        val tiempo = Duration.between(fechaInicio, fechaFin).toMinutes().toInt()
        if (tiempo <= 0) {
            throw IllegalArgumentException("La duración del simulacro debe ser mayor a cero minutos")
        }

        val simulacro = Simulacro(
            nombre = nombre,
            bancoId = null,
            tiempo = tiempo,
            horaInicio = fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            horaFin = fechaFin.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            preguntaIds = request.preguntaIds.distinct(),
            creadoPor = request.creadoPor?.takeIf { it.isNotBlank() },
            zonaHorariaCreador = zonaHoraria,
            programado = true,
            eliminado = false
        )

        return mapearAResponse(simulacroRepository.save(simulacro))
    }

    fun listarSimulacros(): List<SimulacroResponse> {
        return simulacroRepository.findAll()
            .filter { it.programado && !it.eliminado }
            .sortedByDescending { parsearFechaONull(it.horaInicio) ?: LocalDateTime.MIN }
            .map { mapearAResponse(it) }
    }

    fun obtenerSimulacroPorId(id: String): SimulacroResponse {
        val simulacro = simulacroRepository.findById(id).orElseThrow {
            NoSuchElementException("Simulacro no encontrado")
        }

        if (!simulacro.programado || simulacro.eliminado) {
            throw NoSuchElementException("Simulacro no encontrado")
        }

        return mapearAResponse(simulacro)
    }

    fun eliminarSimulacro(id: String) {
        val simulacro = simulacroRepository.findById(id).orElseThrow {
            NoSuchElementException("Simulacro no encontrado")
        }

        if (!simulacro.programado || simulacro.eliminado) {
            throw NoSuchElementException("Simulacro no encontrado")
        }

        simulacroRepository.save(simulacro.copy(eliminado = true))
    }

    fun estaActivo(id: String): Boolean {
        val simulacro = simulacroRepository.findById(id).orElse(null) ?: return false
        if (!simulacro.programado || simulacro.eliminado) return false
        return calcularEstado(simulacro) == EstadoSimulacro.ACTIVO
    }

    fun estaFinalizado(id: String): Boolean {
        val simulacro = simulacroRepository.findById(id).orElse(null) ?: return false
        if (!simulacro.programado || simulacro.eliminado) return false
        return calcularEstado(simulacro) == EstadoSimulacro.FINALIZADO
    }

    fun estaFinalizadoIncluyendoEliminados(id: String): Boolean {
        val simulacro = simulacroRepository.findById(id).orElse(null) ?: return true
        if (!simulacro.programado) return true
        return calcularEstado(simulacro) == EstadoSimulacro.FINALIZADO
    }

    private fun mapearAResponse(simulacro: Simulacro): SimulacroResponse {
        val zonaHoraria = normalizarZonaHoraria(simulacro.zonaHorariaCreador)
        val estado = calcularEstado(simulacro)

        return SimulacroResponse(
            id = simulacro.id,
            nombre = simulacro.nombre.ifBlank { "Simulacro" },
            bancoId = simulacro.bancoId,
            tiempo = simulacro.tiempo,
            horaInicio = simulacro.horaInicio,
            horaFin = simulacro.horaFin,
            estado = estado,
            puntaje = simulacro.puntaje,
            preguntaIds = simulacro.preguntaIds,
            totalPreguntas = simulacro.preguntaIds.size,
            creadoPor = simulacro.creadoPor,
            zonaHorariaCreador = zonaHoraria,
            segundosRestantes = calcularSegundosRestantes(simulacro),
            programado = simulacro.programado,
            eliminado = simulacro.eliminado
        )
    }

    private fun calcularEstado(simulacro: Simulacro): EstadoSimulacro {
        val inicio = parsearFechaONull(simulacro.horaInicio) ?: return EstadoSimulacro.PENDIENTE
        val fin = parsearFechaONull(simulacro.horaFin) ?: return EstadoSimulacro.PENDIENTE
        val ahora = ahoraSegunZona(simulacro.zonaHorariaCreador)

        return when {
            ahora.isBefore(inicio) -> EstadoSimulacro.PENDIENTE
            ahora.isAfter(fin) || ahora.isEqual(fin) -> EstadoSimulacro.FINALIZADO
            else -> EstadoSimulacro.ACTIVO
        }
    }

    private fun calcularSegundosRestantes(simulacro: Simulacro): Long {
        val fin = parsearFechaONull(simulacro.horaFin) ?: return 0L
        val ahora = ahoraSegunZona(simulacro.zonaHorariaCreador)

        return Duration.between(ahora, fin).seconds.coerceAtLeast(0L)
    }

    private fun ahoraSegunZona(zonaHoraria: String?): LocalDateTime {
        val zona = ZoneId.of(normalizarZonaHoraria(zonaHoraria))
        return ZonedDateTime.now(zona).toLocalDateTime()
    }

    private fun normalizarZonaHoraria(zonaHoraria: String?): String {
        val zona = zonaHoraria?.trim()?.takeIf { it.isNotBlank() } ?: zonaHorariaPorDefecto

        return try {
            ZoneId.of(zona).id
        } catch (_: DateTimeException) {
            zonaHorariaPorDefecto
        }
    }

    private fun parsearFecha(valor: String, mensajeError: String): LocalDateTime {
        return parsearFechaONull(valor) ?: throw IllegalArgumentException(mensajeError)
    }

    private fun parsearFechaONull(valor: String?): LocalDateTime? {
        if (valor.isNullOrBlank()) return null

        return try {
            LocalDateTime.parse(valor.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
