package com.deis.backend.service

import com.deis.backend.dto.CrearBancoRequest
import com.deis.backend.model.BancoPregunta
import com.deis.backend.repository.BancoPreguntaRepository
import org.springframework.stereotype.Service

@Service
class BancoPreguntaService(
    private val bancoPreguntaRepository: BancoPreguntaRepository
) {

    fun crearBanco(request: CrearBancoRequest): BancoPregunta {
        if (request.facultadId.isBlank()) {
            throw IllegalArgumentException("El ID de la facultad es obligatorio")
        }
        if (request.administradorId.isBlank()) {
            throw IllegalArgumentException("El ID del administrador es obligatorio")
        }

        val nuevoBanco = BancoPregunta(
            facultadId = request.facultadId.trim(),
            administradorId = request.administradorId.trim(),
            preguntaIds = emptyList()
        )

        return bancoPreguntaRepository.save(nuevoBanco)
    }

    fun obtenerTodosLosBancos(): List<BancoPregunta> {
        return bancoPreguntaRepository.findAll()
    }

    fun obtenerBancoPorId(id: String): BancoPregunta {
        return bancoPreguntaRepository.findById(id).orElseThrow {
            IllegalArgumentException("Banco de preguntas no encontrado")
        }
    }

    fun eliminarBanco(id: String) {
        if (id.isBlank()) {
            throw IllegalArgumentException("El ID del banco es obligatorio")
        }
        if (!bancoPreguntaRepository.existsById(id)) {
            throw NoSuchElementException("Banco de preguntas no encontrado")
        }
        bancoPreguntaRepository.deleteById(id)
    }
}