package com.deis.backend.controller

import com.deis.backend.dto.RegistroUsuarioRequest
import com.deis.backend.dto.RegistroUsuarioResponse
import com.deis.backend.dto.LoginUsuarioRequest
import com.deis.backend.dto.LoginUsuarioResponse
import com.deis.backend.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    private val usuarioService: UsuarioService
) {

    @PostMapping("/registro")
    fun registrarUsuario(
        @Valid @RequestBody request: RegistroUsuarioRequest
    ): ResponseEntity<RegistroUsuarioResponse> {
        val response = usuarioService.registrarUsuario(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    fun loginUsuario(
        @Valid @RequestBody request: LoginUsuarioRequest
    ): ResponseEntity<LoginUsuarioResponse> {
        val response = usuarioService.loginUsuario(request)
        return ResponseEntity.ok(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun manejarErroresValidacion(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errores = ex.bindingResult.fieldErrors.associate { error ->
            error.field to (error.defaultMessage ?: "Valor inválido")
        }

        return ResponseEntity.badRequest().body(
            mapOf(
                "mensaje" to "Error de validación",
                "errores" to errores
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun manejarIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(
            mapOf("mensaje" to (ex.message ?: "Solicitud inválida"))
        )
    }
}
