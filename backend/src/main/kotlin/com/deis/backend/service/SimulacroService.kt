package com.deis.backend.service

import com.deis.backend.dto.CrearSimulacroRequest
import com.deis.backend.dto.EstadoSimulacro
import com.deis.backend.dto.SimulacroResponse
import com.deis.backend.model.Simulacro
import com.deis.backend.repository.FacultadRepository
import com.deis.backend.repository.SimulacroRepository
import com.deis.backend.repository.UsuarioRepository
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
    private val simulacroRepository: SimulacroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val facultadRepository: FacultadRepository
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

        val facultadId = request.facultadId?.trim().orEmpty()
        if (facultadId.isBlank()) {
            throw IllegalArgumentException("Debe seleccionar la facultad del simulacro")
        }

        val facultadNombre = request.facultadNombre
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: facultadId

        val fechaInicio = parsearFecha(
            valor = request.fechaInicio,
            mensajeError = "La fecha y hora de inicio no tiene un formato válido"
        )
        val fechaFin = parsearFecha(
            valor = request.fechaFin,
            mensajeError = "La fecha y hora de finalización no tiene un formato válido"
        )
        val zonaHoraria = normalizarZonaHoraria(request.zonaHorariaCreador)

        if (!fechaFin.isAfter(fechaInicio)) {
            throw IllegalArgumentException("La fecha de finalización debe ser posterior a la fecha de inicio")
        }

        val tiempoAperturaMinutos = Duration.between(fechaInicio, fechaFin).toMinutes().toInt()
        if (tiempoAperturaMinutos <= 0) {
            throw IllegalArgumentException("La duración del horario abierto debe ser mayor a cero minutos")
        }

        val tiempoLimiteMinutos = request.tiempoLimiteMinutos
        if (tiempoLimiteMinutos <= 0) {
            throw IllegalArgumentException("El tiempo límite de la prueba debe ser mayor a cero minutos")
        }

        val simulacro = Simulacro(
            nombre = nombre,
            bancoId = null,
            // Se mantiene tiempo con el mismo valor por compatibilidad con pantallas antiguas.
            tiempo = tiempoLimiteMinutos,
            tiempoLimiteMinutos = tiempoLimiteMinutos,
            horaInicio = fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            horaFin = fechaFin.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            preguntaIds = request.preguntaIds.distinct(),
            facultadId = facultadId,
            facultadNombre = facultadNombre,
            creadoPor = request.creadoPor?.takeIf { it.isNotBlank() },
            zonaHorariaCreador = zonaHoraria,
            programado = true,
            eliminado = false
        )

        return mapearAResponse(simulacroRepository.save(simulacro))
    }

    fun listarSimulacros(): List<SimulacroResponse> {
        return simulacroRepository.findAll()
            .filter { it.programado && !it.eliminado && it.bancoId == null }
            .sortedByDescending { parsearFechaONull(it.horaInicio) ?: LocalDateTime.MIN }
            .map { mapearAResponse(it) }
    }

    fun listarSimulacrosPorUsuario(usuarioId: String): List<SimulacroResponse> {
        if (usuarioId.isBlank()) {
            throw IllegalArgumentException("El usuario es obligatorio")
        }

        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            NoSuchElementException("Usuario no encontrado")
        }

        if (usuario.rol == "ADMINISTRADOR") {
            return listarSimulacros()
        }

        if (usuario.facultadesIds.isEmpty()) {
            return emptyList()
        }

        // Obtener nombres de las facultades para los IDs que el usuario tenga
        val facultadesDelUsuarioEntidades = facultadRepository.findAllById(usuario.facultadesIds)
        val nombresFacultadesUsuario = facultadesDelUsuarioEntidades.map { it.nombre }
        
        // Combinar IDs y nombres para la normalización
        val valoresParaNormalizar = (usuario.facultadesIds + nombresFacultadesUsuario).map { normalizarFacultad(it) }.distinct()

        return simulacroRepository.findAll()
            .filter { simulacro ->
                simulacro.programado &&
                    !simulacro.eliminado &&
                    simulacro.bancoId == null &&
                    coincideConFacultadesNormalizadas(simulacro, valoresParaNormalizar, usuario.facultadesIds)
            }
            .sortedByDescending { parsearFechaONull(it.horaInicio) ?: LocalDateTime.MIN }
            .map { mapearAResponse(it) }
    }

    fun obtenerSimulacroPorId(id: String): SimulacroResponse {
        val simulacro = simulacroRepository.findById(id).orElseThrow {
            NoSuchElementException("Simulacro no encontrado")
        }

        if (!simulacro.programado || simulacro.eliminado || simulacro.bancoId != null) {
            throw NoSuchElementException("Simulacro no encontrado")
        }

        return mapearAResponse(simulacro)
    }

    fun eliminarSimulacro(id: String) {
        val simulacro = simulacroRepository.findById(id).orElseThrow {
            NoSuchElementException("Simulacro no encontrado")
        }

        if (!simulacro.programado || simulacro.eliminado || simulacro.bancoId != null) {
            throw NoSuchElementException("Simulacro no encontrado")
        }

        simulacroRepository.save(simulacro.copy(eliminado = true))
    }

    fun estaActivo(id: String): Boolean {
        val simulacro = simulacroRepository.findById(id).orElse(null) ?: return false
        if (!simulacro.programado || simulacro.eliminado || simulacro.bancoId != null) return false
        return calcularEstado(simulacro) == EstadoSimulacro.ACTIVO
    }

    fun estaFinalizado(id: String): Boolean {
        val simulacro = simulacroRepository.findById(id).orElse(null) ?: return false
        if (!simulacro.programado || simulacro.eliminado || simulacro.bancoId != null) return false
        return calcularEstado(simulacro) == EstadoSimulacro.FINALIZADO
    }

    fun estaFinalizadoIncluyendoEliminados(id: String): Boolean {
        val simulacro = simulacroRepository.findById(id).orElse(null) ?: return true
        if (!simulacro.programado || simulacro.bancoId != null) return true
        return calcularEstado(simulacro) == EstadoSimulacro.FINALIZADO
    }

    private fun mapearAResponse(simulacro: Simulacro): SimulacroResponse {
        val zonaHoraria = normalizarZonaHoraria(simulacro.zonaHorariaCreador)
        val estado = calcularEstado(simulacro)

        return SimulacroResponse(
            id = simulacro.id,
            nombre = simulacro.nombre.ifBlank { "Simulacro" },
            bancoId = simulacro.bancoId,
            tiempo = obtenerTiempoLimiteMinutos(simulacro),
            tiempoLimiteMinutos = obtenerTiempoLimiteMinutos(simulacro),
            horaInicio = simulacro.horaInicio,
            horaFin = simulacro.horaFin,
            estado = estado,
            puntaje = simulacro.puntaje,
            preguntaIds = simulacro.preguntaIds,
            totalPreguntas = simulacro.preguntaIds.size,
            facultadId = simulacro.facultadId,
            facultadNombre = simulacro.facultadNombre,
            creadoPor = simulacro.creadoPor,
            zonaHorariaCreador = zonaHoraria,
            segundosRestantes = calcularSegundosRestantes(simulacro),
            programado = simulacro.programado,
            eliminado = simulacro.eliminado
        )
    }

    private fun coincideConFacultadesNormalizadas(
        simulacro: Simulacro,
        valoresUsuarioNormalizados: List<String>,
        idsUsuario: List<String>
    ): Boolean {
        val facultadIdSimulacro = simulacro.facultadId.orEmpty().trim()
        val facultadNombreSimulacro = simulacro.facultadNombre.orEmpty().trim()

        val idNormalizado = normalizarFacultad(facultadIdSimulacro)
        val nombreNormalizado = normalizarFacultad(facultadNombreSimulacro)

        return idsUsuario.contains(facultadIdSimulacro) ||
               valoresUsuarioNormalizados.any { it == idNormalizado || it == nombreNormalizado }
    }

    private fun normalizarFacultad(valor: String): String {
        return valor
            .trim()
            .lowercase()
            .replace("facultad de ", "")
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
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
        if (calcularEstado(simulacro) != EstadoSimulacro.ACTIVO) {
            return 0L
        }

        val fin = parsearFechaONull(simulacro.horaFin) ?: return 0L
        val ahora = ahoraSegunZona(simulacro.zonaHorariaCreador)
        val segundosHastaCierre = Duration.between(ahora, fin).seconds.coerceAtLeast(0L)
        val segundosPorLimitePrueba = obtenerTiempoLimiteMinutos(simulacro)
            .coerceAtLeast(1)
            .toLong() * 60L

        return minOf(segundosHastaCierre, segundosPorLimitePrueba)
    }

    private fun obtenerTiempoLimiteMinutos(simulacro: Simulacro): Int {
        return when {
            simulacro.tiempoLimiteMinutos > 0 -> simulacro.tiempoLimiteMinutos
            simulacro.tiempo > 0 -> simulacro.tiempo
            else -> 1
        }
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
