package com.deis.backend.controller

import com.deis.backend.dto.RegistrarPushTokenRequest
import com.deis.backend.service.PushTokenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/push")
class PushTokenController(
    private val pushTokenService: PushTokenService
) {

    @PostMapping("/token")
    fun registrarToken(
        @RequestBody request: RegistrarPushTokenRequest
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(pushTokenService.registrarToken(request))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf("mensaje" to (e.message ?: "Solicitud inválida"))
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf("mensaje" to "No se pudo registrar el token push")
            )
        }
    }
}