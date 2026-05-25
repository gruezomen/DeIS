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

data class CrearIntentoSimulacroRequest(
    val usuarioId: String,
    val bancoId: String,
    val tipo: String = "SIMULACRO",
    val puntaje: Int,
    val totalPreguntas: Int,
    val respuestasCorrectas: Int,
    val respuestasIncorrectas: Int
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

enum class EstadoRendimiento {
    MEJORO,
    SE_MANTUVO_IGUAL,
    DISMINUYO,
    SIN_DATOS,
    SIN_COMPARACION
}

data class ComparacionRendimientoResponse(
    val usuarioId: String,
    val ultimoResultado: Double?,
    val resultadoAnterior: Double?,
    val diferencia: Double?,
    val estado: EstadoRendimiento,
    val mensaje: String,
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
    fun guardarIntento(
        @RequestBody request: CrearIntentoSimulacroRequest
    ): ResponseEntity<Any> {
        if (request.usuarioId.isBlank()) {
            return ResponseEntity.badRequest().body(
                mapOf("mensaje" to "El usuario es obligatorio")
            )
        }

        if (request.bancoId.isBlank()) {
            return ResponseEntity.badRequest().body(
                mapOf("mensaje" to "El banco o práctica es obligatorio")
            )
        }

        if (request.totalPreguntas <= 0) {
            return ResponseEntity.badRequest().body(
                mapOf("mensaje" to "El total de preguntas debe ser mayor a cero")
            )
        }

        if (request.puntaje < 0) {
            return ResponseEntity.badRequest().body(
                mapOf("mensaje" to "El puntaje no puede ser negativo")
            )
        }

        if (request.respuestasCorrectas < 0 || request.respuestasIncorrectas < 0) {
            return ResponseEntity.badRequest().body(
                mapOf("mensaje" to "Las respuestas correctas e incorrectas no pueden ser negativas")
            )
        }

        if (request.respuestasCorrectas + request.respuestasIncorrectas != request.totalPreguntas) {
            return ResponseEntity.badRequest().body(
                mapOf("mensaje" to "La suma de respuestas correctas e incorrectas debe coincidir con el total de preguntas")
            )
        }

        val intento = IntentoSimulacro(
            usuarioId = request.usuarioId,
            bancoId = request.bancoId,
            tipo = request.tipo,
            puntaje = request.puntaje,
            totalPreguntas = request.totalPreguntas,
            respuestasCorrectas = request.respuestasCorrectas,
            respuestasIncorrectas = request.respuestasIncorrectas
        )

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
            redondear(intentos.map { calcularNota(it) }.average())
        }

        return ResponseEntity.ok(
            PromedioGeneralResponse(
                usuarioId = usuarioId,
                promedioGeneral = promedio,
                totalIntentos = intentos.size
            )
        )
    }

    @GetMapping("/intentos/usuario/{usuarioId}/comparacion-rendimiento")
    fun compararRendimiento(
        @PathVariable usuarioId: String
    ): ResponseEntity<ComparacionRendimientoResponse> {
        val intentos = intentoSimulacroRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)

        if (intentos.isEmpty()) {
            return ResponseEntity.ok(
                ComparacionRendimientoResponse(
                    usuarioId = usuarioId,
                    ultimoResultado = null,
                    resultadoAnterior = null,
                    diferencia = null,
                    estado = EstadoRendimiento.SIN_DATOS,
                    mensaje = "El estudiante aún no tiene intentos registrados.",
                    totalIntentos = 0
                )
            )
        }

        val ultimoResultado = calcularNota(intentos[0])

        if (intentos.size == 1) {
            return ResponseEntity.ok(
                ComparacionRendimientoResponse(
                    usuarioId = usuarioId,
                    ultimoResultado = ultimoResultado,
                    resultadoAnterior = null,
                    diferencia = null,
                    estado = EstadoRendimiento.SIN_COMPARACION,
                    mensaje = "El estudiante solo tiene un intento registrado. No existe un resultado anterior para comparar.",
                    totalIntentos = 1
                )
            )
        }

        val resultadoAnterior = calcularNota(intentos[1])
        val diferencia = redondear(ultimoResultado - resultadoAnterior)

        val estado = when {
            diferencia > 0.0 -> EstadoRendimiento.MEJORO
            diferencia < 0.0 -> EstadoRendimiento.DISMINUYO
            else -> EstadoRendimiento.SE_MANTUVO_IGUAL
        }

        val mensaje = when (estado) {
            EstadoRendimiento.MEJORO -> "El rendimiento del estudiante mejoró."
            EstadoRendimiento.SE_MANTUVO_IGUAL -> "El rendimiento del estudiante se mantuvo igual."
            EstadoRendimiento.DISMINUYO -> "El rendimiento del estudiante disminuyó."
            EstadoRendimiento.SIN_DATOS -> "El estudiante aún no tiene intentos registrados."
            EstadoRendimiento.SIN_COMPARACION -> "No existe un resultado anterior para comparar."
        }

        return ResponseEntity.ok(
            ComparacionRendimientoResponse(
                usuarioId = usuarioId,
                ultimoResultado = ultimoResultado,
                resultadoAnterior = resultadoAnterior,
                diferencia = diferencia,
                estado = estado,
                mensaje = mensaje,
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
