package com.deis.backend.service

import com.deis.backend.dto.ActualizarRespuestaCategoriaRequest
import com.deis.backend.dto.RegistrarRespuestaCategoriaRequest
import com.deis.backend.dto.RendimientoCategoriaResponse
import com.deis.backend.dto.RespuestaCategoriaRequest
import com.deis.backend.model.RespuestaCategoria
import com.deis.backend.repository.RespuestaCategoriaRepository
import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class RendimientoCategoriaService(
    private val respuestaCategoriaRepository: RespuestaCategoriaRepository
) {

    fun crearRespuesta(request: RegistrarRespuestaCategoriaRequest): RespuestaCategoria {
        if (request.usuarioId.isBlank()) throw IllegalArgumentException("El usuario es obligatorio")
        if (request.bancoId.isBlank()) throw IllegalArgumentException("El banco es obligatorio")
        if (request.preguntaId.isBlank()) throw IllegalArgumentException("La pregunta es obligatoria")
        if (request.categoria.isBlank()) throw IllegalArgumentException("La categoría es obligatoria")

        val respuesta = RespuestaCategoria(
            usuarioId = request.usuarioId.trim(),
            intentoId = request.intentoId?.trim(),
            bancoId = request.bancoId.trim(),
            preguntaId = request.preguntaId.trim(),
            categoria = request.categoria.trim(),
            esCorrecta = request.esCorrecta,
            tipo = request.tipo.ifBlank { "PRACTICA" }.uppercase()
        )

        return respuestaCategoriaRepository.save(respuesta)
    }

    fun registrarRespuestasDeIntento(
        usuarioId: String,
        intentoId: String?,
        bancoId: String,
        tipo: String,
        respuestas: List<RespuestaCategoriaRequest>
    ): List<RespuestaCategoria> {
        if (respuestas.isEmpty()) return emptyList()

        val respuestasGuardables = respuestas.map { respuesta ->
            RespuestaCategoria(
                usuarioId = usuarioId.trim(),
                intentoId = intentoId,
                bancoId = bancoId.trim(),
                preguntaId = respuesta.preguntaId.trim(),
                categoria = respuesta.categoria.trim(),
                esCorrecta = respuesta.esCorrecta,
                tipo = tipo.ifBlank { "PRACTICA" }.uppercase()
            )
        }

        return respuestaCategoriaRepository.saveAll(respuestasGuardables)
    }

    fun listarRespuestasPorUsuario(usuarioId: String): List<RespuestaCategoria> {
        if (usuarioId.isBlank()) throw IllegalArgumentException("El usuario es obligatorio")
        return respuestaCategoriaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
    }

    fun calcularRendimientoPorCategoria(usuarioId: String): List<RendimientoCategoriaResponse> {
        val respuestas = listarRespuestasPorUsuario(usuarioId)

        return respuestas
            .groupBy { it.categoria }
            .map { (categoria, respuestasCategoria) ->
                val total = respuestasCategoria.size
                val correctas = respuestasCategoria.count { it.esCorrecta }
                val incorrectas = total - correctas
                val porcentaje = if (total > 0) {
                    redondear((correctas.toDouble() / total.toDouble()) * 100.0)
                } else {
                    0.0
                }

                RendimientoCategoriaResponse(
                    categoria = categoria,
                    totalPreguntas = total,
                    correctas = correctas,
                    incorrectas = incorrectas,
                    porcentaje = porcentaje,
                    estado = calcularEstado(porcentaje)
                )
            }
            .sortedBy { it.categoria }
    }

    fun actualizarRespuesta(
        id: String,
        request: ActualizarRespuestaCategoriaRequest
    ): RespuestaCategoria {
        val respuesta = respuestaCategoriaRepository.findById(id).orElseThrow {
            NoSuchElementException("Respuesta por categoría no encontrada")
        }

        val actualizada = respuesta.copy(
            categoria = request.categoria?.takeIf { it.isNotBlank() }?.trim()
                ?: respuesta.categoria,
            esCorrecta = request.esCorrecta ?: respuesta.esCorrecta
        )

        return respuestaCategoriaRepository.save(actualizada)
    }

    fun eliminarRespuesta(id: String) {
        if (!respuestaCategoriaRepository.existsById(id)) {
            throw NoSuchElementException("Respuesta por categoría no encontrada")
        }

        respuestaCategoriaRepository.deleteById(id)
    }

    private fun calcularEstado(porcentaje: Double): String {
        return when {
            porcentaje >= 70.0 -> "AREA_FUERTE"
            porcentaje <= 50.0 -> "AREA_DEBIL"
            else -> "EN_PROCESO"
        }
    }

    private fun redondear(valor: Double): Double {
        return round(valor * 100.0) / 100.0
    }
}