package com.deis.backend.service

import com.deis.backend.dto.CrearBancoRequest
import com.deis.backend.model.BancoPregunta
import com.deis.backend.repository.BancoPreguntaRepository
import com.deis.backend.repository.FacultadRepository
import com.deis.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class BancoPreguntaService(
    private val bancoPreguntaRepository: BancoPreguntaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val facultadRepository: FacultadRepository
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

    fun obtenerBancosPorFacultades(facultadesIds: List<String>): List<BancoPregunta> {
        return bancoPreguntaRepository.findByFacultadIdIn(facultadesIds)
    }

    fun obtenerBancosPorUsuario(usuarioId: String): List<BancoPregunta> {
        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }

        if (usuario.rol == "ADMINISTRADOR") {
            return bancoPreguntaRepository.findAll()
        }

        if (usuario.facultadesIds.isEmpty()) {
            return emptyList()
        }

        // Obtener nombres de las facultades para los IDs que el usuario tenga
        val facultadesDelUsuario = facultadRepository.findAllById(usuario.facultadesIds)
        val nombresFacultadesUsuario = facultadesDelUsuario.map { it.nombre }
        
        // Combinar IDs y nombres para la normalización (soporta tanto lo antiguo como lo nuevo)
        val valoresParaNormalizar = (usuario.facultadesIds + nombresFacultadesUsuario).map { normalizarTexto(it) }.distinct()

        val todosLosBancos = bancoPreguntaRepository.findAll()

        return todosLosBancos.filter { banco ->
            val bancoFacultadId = banco.facultadId.trim()
            val bancoFacultadNormalizada = normalizarTexto(bancoFacultadId)
            
            // Coincidencia exacta de ID, coincidencia de nombre normalizado
            usuario.facultadesIds.contains(bancoFacultadId) || 
            valoresParaNormalizar.any { it == bancoFacultadNormalizada }
        }
    }

    private fun normalizarTexto(texto: String): String {
        return texto.lowercase()
            .trim()
            .replace("facultad de ", "")
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
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
