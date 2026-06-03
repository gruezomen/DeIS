package com.deis.backend.controller

import com.deis.backend.dto.CrearBancoRequest
import com.deis.backend.service.BancoPreguntaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bancos-preguntas")
class BancoPreguntaController(
    private val bancoPreguntaService: BancoPreguntaService
) {

    @PostMapping
    fun crearBanco(@RequestBody request: CrearBancoRequest): ResponseEntity<Any> {
        return try {
            val bancoCreado = bancoPreguntaService.crearBanco(request)
            ResponseEntity.status(HttpStatus.CREATED).body(bancoCreado)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("mensaje" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("mensaje" to "Error interno"))
        }
    }

    @GetMapping
    fun obtenerBancosPreguntas(): ResponseEntity<Any> {
        return ResponseEntity.ok(bancoPreguntaService.obtenerTodosLosBancos())
    }

    @GetMapping("/{id}")
    fun obtenerBancoPorId(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(bancoPreguntaService.obtenerBancoPorId(id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun eliminarBanco(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            bancoPreguntaService.eliminarBanco(id)
            ResponseEntity.ok(mapOf("mensaje" to "Banco de preguntas eliminado correctamente"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("mensaje" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("mensaje" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("mensaje" to "Error interno"))
        }
    }
}