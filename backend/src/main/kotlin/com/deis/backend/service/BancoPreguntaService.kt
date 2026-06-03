package com.deis.backend.service

import com.deis.backend.dto.CrearBancoRequest
import com.deis.backend.model.BancoPregunta
import com.deis.backend.repository.BancoPreguntaRepository
import com.deis.backend.repository.UsuarioRepository
import com.deis.backend.repository.FacultadRepository
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

    fun obtenerBancosPorUsuario(usuarioId: String): List<BancoPregunta> {
        if (usuarioId.isBlank()) {
            throw IllegalArgumentException("El usuario es obligatorio")
        }

        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            NoSuchElementException("Usuario no encontrado")
        }

        // Si es administrador, ve todo. Usamos ignoreCase para evitar errores de ROL vs rol.
        if (usuario.rol.equals("ADMINISTRADOR", ignoreCase = true) || usuario.rol.equals("ADMIN", ignoreCase = true)) {
            return bancoPreguntaRepository.findAll()
        }

        // Obtener mapa de ID -> Nombre de facultades
        val todasLasFacultades = facultadRepository.findAll()
        val mapaFacultades = todasLasFacultades.associate { it.id to it.nombre }

        // Convertir facultades del usuario (IDs) a nombres si existen, si no, mantener ID
        val facultadesUsuarioNombres = usuario.facultadesIds
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { id -> mapaFacultades[id] ?: id }

        if (facultadesUsuarioNombres.isEmpty()) {
            return emptyList()
        }

        val todosLosBancos = bancoPreguntaRepository.findAll()

        return todosLosBancos.filter { banco ->
            val facultadIdBanco = banco.facultadId.trim()
            val normalizadoBanco = normalizarFacultad(facultadIdBanco)

            facultadesUsuarioNombres.any { facultadUsuarioNombre ->
                val valor = facultadUsuarioNombre.trim()
                val normalizadoUsuario = normalizarFacultad(valor)

                // Coincidencia exacta o coincidencia de nombres normalizados (sin acentos, sin "Facultad de")
                valor.equals(facultadIdBanco, ignoreCase = true) ||
                    normalizadoUsuario == normalizadoBanco ||
                    normalizadoUsuario.contains(normalizadoBanco) ||
                    normalizadoBanco.contains(normalizadoUsuario)
            }
        }
    }

    private fun normalizarFacultad(valor: String): String {
        return valor
            .lowercase()
            .trim()
            .replace("facultad de ", "")
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .replace(Regex("[^a-z0-9 ]"), "") // Quita cualquier caracter especial restante
            .replace(Regex("\\s+"), " ")      // Colapsa múltiples espacios
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
