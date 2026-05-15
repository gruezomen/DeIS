package com.deis.backend.controller

import com.deis.backend.model.IntentoSimulacro
import com.deis.backend.model.Simulacro
import com.deis.backend.repository.IntentoSimulacroRepository
import com.deis.backend.repository.SimulacroRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

data class CrearSimulacroRequest(
    val bancoId: String? = null,
    val tiempo: Int,
    val preguntaIds: List<String> = emptyList()
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
    fun obtenerIntentosPorUsuario(@PathVariable usuarioId: String): ResponseEntity<List<IntentoSimulacro>> {
        // En una implementación real, se filtraría en el repository.
        // Por simplicidad para esta tarea, filtramos aquí si no queremos añadir el método al repo todavía.
        val todos = intentoSimulacroRepository.findAll()
        val filtrados = todos.filter { it.usuarioId == usuarioId }
        return ResponseEntity.ok(filtrados)
    }
}
