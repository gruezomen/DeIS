package com.deis.backend.controller

import com.deis.backend.dto.ActualizarRespuestaCategoriaRequest
import com.deis.backend.dto.RegistrarRespuestaCategoriaRequest
import com.deis.backend.model.RespuestaCategoria
import com.deis.backend.service.RendimientoCategoriaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rendimiento-categorias")
class RendimientoCategoriaController(
    private val rendimientoCategoriaService: RendimientoCategoriaService
) {

    @PostMapping
    fun crearRespuestaCategoria(
        @RequestBody request: RegistrarRespuestaCategoriaRequest
    ): ResponseEntity<Any> {
        return try {
            val respuesta = rendimientoCategoriaService.crearRespuesta(request)
            ResponseEntity.ok(respuesta)
        } catch (error: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf("mensaje" to (error.message ?: "Datos inválidos"))
            )
        } catch (error: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf("mensaje" to "Error interno al registrar la respuesta por categoría")
            )
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    fun obtenerRendimientoPorCategoria(
        @PathVariable usuarioId: String
    ): ResponseEntity<Any> {
        return try {
            val rendimiento = rendimientoCategoriaService.calcularRendimientoPorCategoria(usuarioId)
            ResponseEntity.ok(rendimiento)
        } catch (error: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf("mensaje" to (error.message ?: "Usuario inválido"))
            )
        } catch (error: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf("mensaje" to "Error interno al obtener el rendimiento por categoría")
            )
        }
    }

    @GetMapping("/usuario/{usuarioId}/registros")
    fun listarRespuestasPorUsuario(
        @PathVariable usuarioId: String
    ): ResponseEntity<Any> {
        return try {
            val respuestas = rendimientoCategoriaService.listarRespuestasPorUsuario(usuarioId)
            ResponseEntity.ok(respuestas)
        } catch (error: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf("mensaje" to (error.message ?: "Usuario inválido"))
            )
        } catch (error: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf("mensaje" to "Error interno al listar respuestas por categoría")
            )
        }
    }

    @PutMapping("/{id}")
    fun actualizarRespuestaCategoria(
        @PathVariable id: String,
        @RequestBody request: ActualizarRespuestaCategoriaRequest
    ): ResponseEntity<Any> {
        return try {
            val actualizada = rendimientoCategoriaService.actualizarRespuesta(id, request)
            ResponseEntity.ok(actualizada)
        } catch (error: NoSuchElementException) {
            ResponseEntity.status(404).body(
                mapOf("mensaje" to (error.message ?: "Respuesta por categoría no encontrada"))
            )
        } catch (error: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf("mensaje" to (error.message ?: "Datos inválidos"))
            )
        } catch (error: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf("mensaje" to "Error interno al actualizar la respuesta por categoría")
            )
        }
    }

    @DeleteMapping("/{id}")
    fun eliminarRespuestaCategoria(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return try {
            rendimientoCategoriaService.eliminarRespuesta(id)
            ResponseEntity.ok(
                mapOf("mensaje" to "Respuesta por categoría eliminada correctamente")
            )
        } catch (error: NoSuchElementException) {
            ResponseEntity.status(404).body(
                mapOf("mensaje" to (error.message ?: "Respuesta por categoría no encontrada"))
            )
        } catch (error: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf("mensaje" to "Error interno al eliminar la respuesta por categoría")
            )
        }
    }
}