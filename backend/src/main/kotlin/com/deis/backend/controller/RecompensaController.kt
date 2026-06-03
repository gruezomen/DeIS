package com.deis.backend.controller

import com.deis.backend.service.RecompensaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.deis.backend.dto.EquipamientoRecompensaRequest
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody


@RestController
@RequestMapping("/api/recompensas")
class RecompensaController(
    private val recompensaService: RecompensaService
) {

    @GetMapping("/usuario/{usuarioId}")
    fun obtenerRecompensasUsuario(
        @PathVariable usuarioId: String
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(recompensaService.obtenerRecompensasUsuario(usuarioId))
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(mapOf("mensaje" to "No se pudieron obtener las recompensas"))
        }
    }
    @GetMapping("/usuario/{usuarioId}/equipamiento")
fun obtenerEquipamiento(
    @PathVariable usuarioId: String
) = recompensaService.obtenerEquipamiento(usuarioId)

@PutMapping("/usuario/{usuarioId}/equipamiento")
fun guardarEquipamiento(
    @PathVariable usuarioId: String,
    @RequestBody request: EquipamientoRecompensaRequest
) = recompensaService.guardarEquipamiento(usuarioId, request)
}