package com.deis.backend.controller

import com.deis.backend.model.Facultad
import com.deis.backend.repository.FacultadRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/facultades")
class FacultadController(
    private val facultadRepository: FacultadRepository
) {
    @GetMapping
    fun obtenerFacultades(): List<Facultad> {
        val facultades = facultadRepository.findAll()
        if (facultades.isEmpty()) {
            throw IllegalStateException("No hay facultades registradas en el sistema. Por favor, contacte al administrador.")
        }
        return facultades
    }

    @ExceptionHandler(IllegalStateException::class)
    fun manejarIllegalState(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            mapOf("mensaje" to (ex.message ?: "Error interno del sistema"))
        )
    }
}