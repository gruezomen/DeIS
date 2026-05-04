package com.deis.backend.service

import com.deis.backend.dto.CrearPreguntaRequest
import com.deis.backend.model.Categoria
import com.deis.backend.model.Dificultad
import com.deis.backend.model.Opcion
import com.deis.backend.model.Pregunta
import com.deis.backend.repository.BancoPreguntaRepository
import com.deis.backend.repository.PreguntaRepository
import org.springframework.stereotype.Service

@Service
class PreguntaService(
    private val preguntaRepository: PreguntaRepository,
    private val bancoPreguntaRepository: BancoPreguntaRepository
) {
    private val categoriasPermitidas = setOf(
        "Matematicas",
        "Fisica",
        "Quimica",
        "Biologia"
    )

    fun eliminarPregunta(id: String) {
        if (id.isBlank()) {
            throw IllegalArgumentException("El id de la pregunta es obligatorio")
        }

        if (!preguntaRepository.existsById(id)) {
            throw NoSuchElementException("Pregunta no encontrada")
        }

        bancoPreguntaRepository.findAll()
            .filter { banco -> banco.preguntaIds.contains(id) }
            .forEach { banco ->
                val bancoActualizado = banco.copy(
                    preguntaIds = banco.preguntaIds.filterNot { preguntaId -> preguntaId == id }
                )

                bancoPreguntaRepository.save(bancoActualizado)
            }

        preguntaRepository.deleteById(id)
    }

    fun crearPregunta(request: CrearPreguntaRequest): Pregunta {
        validarRequest(request)

        val pregunta = Pregunta(
            enunciado = request.enunciado.trim(),
            solucion = request.solucion.trim(),
            video = "",
            dificultad = Dificultad.valueOf(request.dificultad.trim().uppercase()),
            categoria = Categoria(
                nombre = request.categoria.trim(),
                descripcion = ""
            ),
            opciones = request.opciones.mapIndexed { index, texto ->
                Opcion(
                    texto = texto.trim(),
                    esCorrecta = index == request.indiceCorrecta
                )
            }
        )

        return preguntaRepository.save(pregunta)
    }

    fun obtenerTodasLasPreguntas(): List<Pregunta> {
        return preguntaRepository.findAll()
    }

    fun obtenerPreguntaPorId(id: String): Pregunta {
        return preguntaRepository.findById(id).orElseThrow {
            IllegalArgumentException("Pregunta no encontrada")
        }
    }

    fun actualizarPregunta(id: String, request: CrearPreguntaRequest): Pregunta {
        val preguntaExistente = preguntaRepository.findById(id).orElseThrow {
            IllegalArgumentException("Pregunta no encontrada")
        }

        validarRequest(request)

        val preguntaActualizada = preguntaExistente.copy(
            enunciado = request.enunciado.trim(),
            solucion = request.solucion.trim(),
            dificultad = Dificultad.valueOf(request.dificultad.trim().uppercase()),
            categoria = Categoria(
                nombre = request.categoria.trim(),
                descripcion = ""
            ),
            opciones = request.opciones.mapIndexed { index, texto ->
                Opcion(
                    texto = texto.trim(),
                    esCorrecta = index == request.indiceCorrecta
                )
            }
        )

        return preguntaRepository.save(preguntaActualizada)
    }

    fun asociarPreguntaABanco(preguntaId: String, bancoPreguntaId: String): Pregunta {
        if (preguntaId.isBlank()) {
            throw IllegalArgumentException("El id de la pregunta es obligatorio")
        }

        if (bancoPreguntaId.isBlank()) {
            throw IllegalArgumentException("El id del banco de preguntas es obligatorio")
        }

        val pregunta = preguntaRepository.findById(preguntaId).orElseThrow {
            IllegalArgumentException("Pregunta no encontrada")
        }

        val bancoSeleccionado = bancoPreguntaRepository.findById(bancoPreguntaId).orElseThrow {
            IllegalArgumentException("Banco de preguntas no encontrado")
        }

        bancoPreguntaRepository.findAll()
            .filter { banco ->
                banco.id != bancoPreguntaId && banco.preguntaIds.contains(preguntaId)
            }
            .forEach { banco ->
                val bancoActualizado = banco.copy(
                    preguntaIds = banco.preguntaIds.filterNot { id -> id == preguntaId }
                )

                bancoPreguntaRepository.save(bancoActualizado)
            }

        if (!bancoSeleccionado.preguntaIds.contains(preguntaId)) {
            val bancoActualizado = bancoSeleccionado.copy(
                preguntaIds = bancoSeleccionado.preguntaIds + preguntaId
            )

            bancoPreguntaRepository.save(bancoActualizado)
        }

        return pregunta
    }

    private fun validarRequest(request: CrearPreguntaRequest) {
        if (request.enunciado.isBlank()) {
            throw IllegalArgumentException("El enunciado es obligatorio")
        }

        if (request.solucion.isBlank()) {
            throw IllegalArgumentException("La explicacion es obligatoria")
        }

        if (request.categoria.isBlank()) {
            throw IllegalArgumentException("La categoria es obligatoria")
        }

        if (request.categoria.trim() !in categoriasPermitidas) {
            throw IllegalArgumentException("La categoria no es valida")
        }

        val dificultadNormalizada = request.dificultad.trim().uppercase()
        val dificultadesPermitidas = setOf("FACIL", "MEDIO", "DIFICIL")

        if (dificultadNormalizada !in dificultadesPermitidas) {
            throw IllegalArgumentException("La dificultad no es valida")
        }

        if (request.opciones.size != 4) {
            throw IllegalArgumentException("Debe enviar exactamente 4 opciones")
        }

        if (request.opciones.any { it.isBlank() }) {
            throw IllegalArgumentException("Todas las opciones deben tener texto")
        }

        if (request.indiceCorrecta !in 0..3) {
            throw IllegalArgumentException("La opcion correcta no es valida")
        }
    }
}