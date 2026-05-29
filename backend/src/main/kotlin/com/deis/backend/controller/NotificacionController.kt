package com.deis.backend.controller

import com.deis.backend.service.NotificacionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notificaciones")
class NotificacionController(
    private val notificacionService: NotificacionService
) {

    @GetMapping("/usuario/{usuarioId}")
    fun obtenerNotificaciones(@PathVariable usuarioId: String): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(notificacionService.obtenerNotificacionesUsuario(usuarioId))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf("mensaje" to (e.message ?: "Solicitud inválida"))
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf("mensaje" to "No se pudieron obtener las notificaciones")
            )
        }
    }

    @PatchMapping("/{id}/leer")
    fun marcarComoLeida(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(notificacionService.marcarComoLeida(id))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(
                mapOf("mensaje" to "Notificación no encontrada")
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf("mensaje" to "No se pudo actualizar la notificación")
            )
        }
    }
}