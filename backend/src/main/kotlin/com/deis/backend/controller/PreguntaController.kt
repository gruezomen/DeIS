package com.deis.backend.controller

import com.deis.backend.dto.AsociarPreguntaBancoRequest
import com.deis.backend.dto.CrearPreguntaRequest
import com.deis.backend.model.Pregunta
import com.deis.backend.service.PreguntaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/preguntas")
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

    @GetMapping
    fun obtenerTodasLasPreguntas(): ResponseEntity<List<Pregunta>> {
        return ResponseEntity.ok(preguntaService.obtenerTodasLasPreguntas())
    }

    @GetMapping("/{id}")
    fun obtenerPreguntaPorId(@PathVariable("id") id: String): ResponseEntity<Any> {
        return try {
            val pregunta = preguntaService.obtenerPreguntaPorId(id)
            ResponseEntity.ok(pregunta)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf("mensaje" to "Pregunta no encontrada")
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

    @PatchMapping("/{id}/banco")
    fun asociarPreguntaABanco(
       @PathVariable id: String,
       @RequestBody request: AsociarPreguntaBancoRequest
       ): ResponseEntity<Any> {
          return try {
            val pregunta = preguntaService.asociarPreguntaABanco(id, request.bancoPreguntaId)

        ResponseEntity.ok(
            mapOf(
                "mensaje" to "Pregunta asociada al banco correctamente",
                "pregunta" to pregunta
            )
        )
          } catch (e: IllegalArgumentException) {
               ResponseEntity.badRequest().body(
            mapOf("mensaje" to (e.message ?: "Datos invalidos"))
         )
    } catch (e: Exception) {
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            mapOf("mensaje" to "Error interno al asociar la pregunta al banco")
        )
    }
  }
  @DeleteMapping("/{id}")
    fun eliminarPregunta(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            preguntaService.eliminarPregunta(id)

            ResponseEntity.ok(
                mapOf("mensaje" to "Pregunta eliminada correctamente")
            )
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf("mensaje" to (e.message ?: "Pregunta no encontrada"))
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf("mensaje" to (e.message ?: "Datos invalidos"))
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf("mensaje" to "Error interno al eliminar la pregunta")
            )
        }
    }
}
