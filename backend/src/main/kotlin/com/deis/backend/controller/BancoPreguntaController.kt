package com.deis.backend.controller

import com.deis.backend.repository.BancoPreguntaRepository
import com.deis.backend.repository.PreguntaRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bancos-preguntas")
class BancoPreguntaController(
    private val bancoPreguntaRepository: BancoPreguntaRepository,
    private val preguntaRepository: PreguntaRepository
) {

    @GetMapping
    fun obtenerBancosPreguntas(): ResponseEntity<Any> {
        val bancos = bancoPreguntaRepository.findAll()
        return ResponseEntity.ok(bancos)
    }

    @GetMapping("/{id}")
    fun obtenerBancoPreguntaPorId(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return try {
            val banco = bancoPreguntaRepository.findById(id).orElseThrow {
                IllegalArgumentException("Banco de preguntas no encontrado")
            }

            ResponseEntity.ok(banco)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf("mensaje" to (e.message ?: "Banco de preguntas no encontrado"))
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf("mensaje" to "Error interno al obtener el banco de preguntas")
            )
        }
    }

    @GetMapping("/{id}/preguntas")
    fun obtenerPreguntasDelBanco(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return try {
            val banco = bancoPreguntaRepository.findById(id).orElseThrow {
                IllegalArgumentException("Banco de preguntas no encontrado")
            }

            val preguntas = banco.preguntaIds.mapNotNull { preguntaId ->
                preguntaRepository.findById(preguntaId).orElse(null)
            }

            ResponseEntity.ok(preguntas)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf("mensaje" to (e.message ?: "Banco de preguntas no encontrado"))
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf("mensaje" to "Error interno al obtener las preguntas del banco")
            )
        }
    }
}