package com.deis.backend.service

import com.deis.backend.dto.DetalleRespuestaIntentoResponse
import com.deis.backend.dto.RespuestaIntentoDetalleRequest
import com.deis.backend.model.DetalleRespuestaIntento
import com.deis.backend.repository.DetalleRespuestaIntentoRepository
import org.springframework.stereotype.Service
import com.deis.backend.dto.ErrorPreguntaCategoriaResponse
import com.deis.backend.dto.ErroresPorCategoriaResponse

@Service
class HistorialIntentoDetalleService(
    private val detalleRespuestaIntentoRepository: DetalleRespuestaIntentoRepository
) {

    fun registrarDetalles(
        intentoId: String?,
        detalles: List<RespuestaIntentoDetalleRequest>
    ): List<DetalleRespuestaIntentoResponse> {
        if (intentoId.isNullOrBlank() || detalles.isEmpty()) {
            return emptyList()
        }

        val registros = detalles.map { detalle ->
            DetalleRespuestaIntento(
                intentoId = intentoId,
                preguntaId = detalle.preguntaId,
                enunciado = detalle.enunciado,
                categoria = detalle.categoria,
                respuestaSeleccionada = detalle.respuestaSeleccionada,
                respuestaCorrecta = detalle.respuestaCorrecta,
                esCorrecta = detalle.esCorrecta,
                orden = detalle.orden
            )
        }

        return detalleRespuestaIntentoRepository
            .saveAll(registros)
            .map { it.toResponse() }
    }

    fun obtenerDetallesPorIntento(intentoId: String): List<DetalleRespuestaIntentoResponse> {
        return detalleRespuestaIntentoRepository
            .findByIntentoIdOrderByOrdenAsc(intentoId)
            .map { it.toResponse() }
    }

    private fun DetalleRespuestaIntento.toResponse(): DetalleRespuestaIntentoResponse {
        return DetalleRespuestaIntentoResponse(
            id = id,
            intentoId = intentoId,
            preguntaId = preguntaId,
            enunciado = enunciado,
            categoria = categoria,
            respuestaSeleccionada = respuestaSeleccionada,
            respuestaCorrecta = respuestaCorrecta,
            esCorrecta = esCorrecta,
            orden = orden
        )
    }
    fun obtenerErroresPorCategoria(
        intentoIds: List<String>
    ): List<ErroresPorCategoriaResponse> {
        if (intentoIds.isEmpty()) return emptyList()

        val errores = detalleRespuestaIntentoRepository
            .findByIntentoIdIn(intentoIds)
            .filter { !it.esCorrecta }

        return errores
            .groupBy { it.categoria }
            .map { (categoria, erroresCategoria) ->
                ErroresPorCategoriaResponse(
                    categoria = categoria,
                    totalErrores = erroresCategoria.size,
                    errores = erroresCategoria
                        .sortedBy { it.orden }
                        .map { error ->
                            ErrorPreguntaCategoriaResponse(
                                intentoId = error.intentoId,
                                preguntaId = error.preguntaId,
                                enunciado = error.enunciado,
                                categoria = error.categoria,
                                respuestaSeleccionada = error.respuestaSeleccionada,
                                respuestaCorrecta = error.respuestaCorrecta,
                                orden = error.orden
                            )
                        }
                )
            }
            .sortedByDescending { it.totalErrores }
    }
}