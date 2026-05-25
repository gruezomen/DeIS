package com.deis.backend.controller

import com.deis.backend.service.LogroService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/logros")
class LogroController(
    private val logroService: LogroService
) {

    @GetMapping("/usuario/{usuarioId}")
    fun obtenerLogrosUsuario(
        @PathVariable usuarioId: String
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(logroService.obtenerLogrosUsuario(usuarioId))
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(mapOf("mensaje" to "No se pudieron obtener los logros"))
        }
    }
}