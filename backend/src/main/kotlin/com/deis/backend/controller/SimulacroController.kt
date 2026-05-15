package com.deis.backend.controller

import com.deis.backend.model.IntentoSimulacro
import com.deis.backend.repository.IntentoSimulacroRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/simulacros")
class SimulacroController(
    private val intentoSimulacroRepository: IntentoSimulacroRepository
) {

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
