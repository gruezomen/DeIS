package com.deis.backend.controller

import com.deis.backend.dto.CrearPreguntaRequest
import com.deis.backend.model.Pregunta
import com.deis.backend.service.PreguntaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/preguntas")
class PreguntaController(
    private val preguntaService: PreguntaService
) {

    @PostMapping
    fun crearPregunta(@RequestBody request: CrearPreguntaRequest): ResponseEntity<Any> {
        return try {
            val preguntaCreada: Pregunta = preguntaService.crearPregunta(request)
            ResponseEntity.status(HttpStatus.CREATED).body(preguntaCreada)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf("mensaje" to (e.message ?: "Datos invalidos"))
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf("mensaje" to "Error interno al crear la pregunta")
            )
        }
    }

    @PutMapping("/{id}")
    fun actualizarPregunta(
        @PathVariable id: String,
        @RequestBody request: CrearPreguntaRequest
    ): ResponseEntity<Any> {
        return try {
            val preguntaActualizada = preguntaService.actualizarPregunta(id, request)
            ResponseEntity.ok(preguntaActualizada)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf("mensaje" to (e.message ?: "Datos invalidos"))
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf("mensaje" to "Error interno al actualizar la pregunta")
            )
        }
    }
}