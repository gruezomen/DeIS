package com.deis.backend.controller

import com.deis.backend.repository.BancoPreguntaRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bancos-preguntas")
class BancoPreguntaController(
    private val bancoPreguntaRepository: BancoPreguntaRepository
) {

    @GetMapping
    fun obtenerBancosPreguntas(): ResponseEntity<Any> {
        val bancos = bancoPreguntaRepository.findAll()
        return ResponseEntity.ok(bancos)
    }

    @GetMapping("/{id}")
    fun obtenerBancoPorId(@org.springframework.web.bind.annotation.PathVariable id: String): ResponseEntity<Any> {
        val banco = bancoPreguntaRepository.findById(id)
        return if (banco.isPresent) {
            ResponseEntity.ok(banco.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }
}