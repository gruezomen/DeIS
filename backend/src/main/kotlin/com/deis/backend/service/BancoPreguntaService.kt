package com.deis.backend.service

import com.deis.backend.dto.CrearBancoRequest
import com.deis.backend.model.BancoPregunta
import com.deis.backend.repository.BancoPreguntaRepository
import com.deis.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class BancoPreguntaService(
    private val bancoPreguntaRepository: BancoPreguntaRepository,
    private val usuarioRepository: UsuarioRepository
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

    fun obtenerBancosPorUsuario(usuarioId: String): List<BancoPregunta> {
        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }

        // Si es administrador, quizás debería ver todos los bancos, 
        // pero la solicitud específica dice "cuando el usuario se registra, selecciona las facultades... 
        // mostrar únicamente los bancos asociados a las facultades que el estudiante está postulando".
        // Generalmente los administradores no tienen facultades seleccionadas para postular.
        
        if (usuario.rol == "ADMIN") {
            return bancoPreguntaRepository.findAll()
        }

        if (usuario.facultadesIds.isEmpty()) {
            return emptyList()
        }

        return bancoPreguntaRepository.findByFacultadIdIn(usuario.facultadesIds)
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