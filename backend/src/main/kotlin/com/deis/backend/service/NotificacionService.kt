package com.deis.backend.service

import com.deis.backend.model.Notificacion
import com.deis.backend.repository.NotificacionRepository
import com.deis.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class NotificacionService(
    private val notificacionRepository: NotificacionRepository,
    private val usuarioRepository: UsuarioRepository
) {

    fun obtenerNotificacionesUsuario(usuarioId: String): List<Notificacion> {
        if (usuarioId.isBlank()) {
            throw IllegalArgumentException("El usuario es obligatorio")
        }

        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
    }

    fun marcarComoLeida(id: String): Notificacion {
        val notificacion = notificacionRepository.findById(id).orElseThrow {
            NoSuchElementException("Notificación no encontrada")
        }

        return notificacionRepository.save(notificacion.copy(leida = true))
    }

    fun crearNotificacionesPorFacultad(
        simulacroId: String?,
        nombreSimulacro: String,
        facultadId: String?,
        facultadNombre: String?,
        fechaInicio: String,
        fechaFin: String,
        creadoPor: String?
    ) {
        val usuarios = usuarioRepository.findAll().filter { usuario ->
            val esAdmin = usuario.rol.equals("ADMINISTRADOR", ignoreCase = true)
            if (esAdmin) return@filter false

            coincideConFacultadesUsuario(
                facultadesUsuario = usuario.facultadesIds,
                facultadIdSimulacro = facultadId,
                facultadNombreSimulacro = facultadNombre
            )
        }

        val titulo = "Nuevo simulacro disponible"
        val mensaje = buildString {
            append("Se programó \"$nombreSimulacro\"")
            if (!facultadNombre.isNullOrBlank()) {
                append(" para $facultadNombre")
            }
            append(". Inicio: $fechaInicio. Fin: $fechaFin.")
        }

        usuarios.forEach { usuario ->
            val usuarioId = usuario.id ?: return@forEach

            if (!creadoPor.isNullOrBlank() && usuarioId == creadoPor) {
                return@forEach
            }

            notificacionRepository.save(
                Notificacion(
                    usuarioId = usuarioId,
                    titulo = titulo,
                    mensaje = mensaje,
                    tipo = "SIMULACRO",
                    referenciaId = simulacroId
                )
            )
        }
    }

    private fun coincideConFacultadesUsuario(
        facultadesUsuario: List<String>,
        facultadIdSimulacro: String?,
        facultadNombreSimulacro: String?
    ): Boolean {
        val idSimulacro = facultadIdSimulacro.orEmpty().trim()
        val nombreSimulacro = facultadNombreSimulacro.orEmpty().trim()

        return facultadesUsuario.any { facultadUsuario ->
            val valor = facultadUsuario.trim()

            valor.equals(idSimulacro, ignoreCase = true) ||
                valor.equals(nombreSimulacro, ignoreCase = true) ||
                normalizarFacultad(valor) == normalizarFacultad(nombreSimulacro) ||
                normalizarFacultad(valor) == normalizarFacultad(idSimulacro)
        }
    }

    private fun normalizarFacultad(valor: String): String {
        return valor
            .trim()
            .lowercase()
            .replace("facultad de ", "")
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
    }
}