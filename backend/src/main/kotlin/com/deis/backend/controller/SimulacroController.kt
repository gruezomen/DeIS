package com.deis.backend.controller

import com.deis.backend.model.IntentoSimulacro
import com.deis.backend.model.Simulacro
import com.deis.backend.repository.IntentoSimulacroRepository
import com.deis.backend.repository.SimulacroRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import kotlin.math.round

data class CrearSimulacroRequest(
    val bancoId: String? = null,
    val tiempo: Int,
    val preguntaIds: List<String> = emptyList()
)

data class ResultadoHistoricoResponse(
    val id: String?,
    val bancoId: String,
    val puntaje: Int,
    val totalPreguntas: Int,
    val nota: Double,
    val fecha: String
)

data class PromedioGeneralResponse(
    val usuarioId: String,
    val promedioGeneral: Double,
    val totalIntentos: Int
)

@RestController
@RequestMapping("/api/simulacros")
class SimulacroController(
    private val intentoSimulacroRepository: IntentoSimulacroRepository,
    private val simulacroRepository: SimulacroRepository
) {

    @PostMapping
    fun crearSimulacro(@RequestBody request: CrearSimulacroRequest): ResponseEntity<Any> {
        if (request.tiempo <= 0) {
            return ResponseEntity.badRequest().body(
                mapOf("mensaje" to "El tiempo del simulacro debe ser mayor a cero")
            )
        }

        val horaInicio = LocalDateTime.now()
        val horaFin = horaInicio.plusMinutes(request.tiempo.toLong())

        val simulacro = Simulacro(
            bancoId = request.bancoId,
            tiempo = request.tiempo,
            horaInicio = horaInicio.toString(),
            horaFin = horaFin.toString(),
            preguntaIds = request.preguntaIds
        )

        val guardado = simulacroRepository.save(simulacro)
        return ResponseEntity.ok(guardado)
    }

    @GetMapping("/{id}")
    fun obtenerSimulacroPorId(@PathVariable id: String): ResponseEntity<Any> {
        val simulacro = simulacroRepository.findById(id)

        return if (simulacro.isPresent) {
            ResponseEntity.ok(simulacro.get())
        } else {
            ResponseEntity.status(404).body(
                mapOf("mensaje" to "Simulacro no encontrado")
            )
        }
    }

    @PostMapping("/intentos")
    fun guardarIntento(@RequestBody intento: IntentoSimulacro): ResponseEntity<IntentoSimulacro> {
        val guardado = intentoSimulacroRepository.save(intento)
        return ResponseEntity.ok(guardado)
    }

    @GetMapping("/intentos/usuario/{usuarioId}")
    fun obtenerIntentosPorUsuario(
        @PathVariable usuarioId: String
    ): ResponseEntity<List<IntentoSimulacro>> {
        val intentos = intentoSimulacroRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
        return ResponseEntity.ok(intentos)
    }

    @GetMapping("/intentos/usuario/{usuarioId}/historial")
    fun obtenerHistorialResultados(
        @PathVariable usuarioId: String
    ): ResponseEntity<List<ResultadoHistoricoResponse>> {
        val historial = intentoSimulacroRepository
            .findByUsuarioIdOrderByFechaDesc(usuarioId)
            .map { intento ->
                ResultadoHistoricoResponse(
                    id = intento.id,
                    bancoId = intento.bancoId,
                    puntaje = intento.puntaje,
                    totalPreguntas = intento.totalPreguntas,
                    nota = calcularNota(intento),
                    fecha = intento.fecha.toString()
                )
            }

        return ResponseEntity.ok(historial)
    }

    @GetMapping("/intentos/usuario/{usuarioId}/promedio-general")
    fun calcularPromedioGeneral(
        @PathVariable usuarioId: String
    ): ResponseEntity<PromedioGeneralResponse> {
        val intentos = intentoSimulacroRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)

        val promedio = if (intentos.isEmpty()) {
            0.0
        } else {
            redondear(
                intentos.map { calcularNota(it) }.average()
            )
        }

        return ResponseEntity.ok(
            PromedioGeneralResponse(
                usuarioId = usuarioId,
                promedioGeneral = promedio,
                totalIntentos = intentos.size
            )
        )
    }

    private fun calcularNota(intento: IntentoSimulacro): Double {
        if (intento.totalPreguntas <= 0) return 0.0

        val nota = (intento.puntaje.toDouble() / intento.totalPreguntas.toDouble()) * 100.0
        return redondear(nota)
    }

    private fun redondear(valor: Double): Double {
        return round(valor * 100.0) / 100.0
    }
}
