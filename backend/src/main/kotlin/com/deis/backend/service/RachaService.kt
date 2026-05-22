package com.deis.backend.service

import com.deis.backend.model.Facultad
import com.deis.backend.model.Preuniversitario
import com.deis.backend.model.Racha
import com.deis.backend.repository.PreuniversitarioRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId

@Service
class RachaService(
    private val preuniversitarioRepository: PreuniversitarioRepository
) {

    private val zonaRacha = ZoneId.of("America/La_Paz")

    fun obtenerRacha(usuarioId: String): Racha {
        val preuniversitario = obtenerOCrearPreuniversitario(usuarioId)

        val preuniversitarioNormalizado = normalizarRachaVencida(
            preuniversitario = preuniversitario,
            fechaActual = LocalDate.now(zonaRacha)
        )

        return preuniversitarioNormalizado.racha
    }

    fun registrarPracticaDiaria(
        usuarioId: String,
        fechaActual: LocalDate = LocalDate.now(zonaRacha)
    ): Racha {
        val preuniversitario = normalizarRachaVencida(
            preuniversitario = obtenerOCrearPreuniversitario(usuarioId),
            fechaActual = fechaActual
        )

        val rachaActual = preuniversitario.racha
        val ultimaPractica = rachaActual.ultimaPractica

        val nuevaRacha = when {
            ultimaPractica == fechaActual -> {
                rachaActual
            }

            ultimaPractica == null -> {
                Racha(
                    diasConsecutivos = 1,
                    ultimaPractica = fechaActual
                )
            }

            ultimaPractica == fechaActual.minusDays(1) -> {
                Racha(
                    diasConsecutivos = rachaActual.diasConsecutivos + 1,
                    ultimaPractica = fechaActual
                )
            }

            else -> {
                Racha(
                    diasConsecutivos = 1,
                    ultimaPractica = fechaActual
                )
            }
        }

        if (nuevaRacha == rachaActual) {
            return rachaActual
        }

        val preuniversitarioActualizado = preuniversitario.copy(
            racha = nuevaRacha
        )

        preuniversitarioRepository.save(preuniversitarioActualizado)

        return nuevaRacha
    }

    private fun normalizarRachaVencida(
        preuniversitario: Preuniversitario,
        fechaActual: LocalDate
    ): Preuniversitario {
        val ultimaPractica = preuniversitario.racha.ultimaPractica
            ?: return preuniversitario

        val rachaPerdida = ultimaPractica.isBefore(fechaActual.minusDays(1))

        if (!rachaPerdida) {
            return preuniversitario
        }

        val preuniversitarioActualizado = preuniversitario.copy(
            racha = Racha(
                diasConsecutivos = 0,
                ultimaPractica = null
            )
        )

        return preuniversitarioRepository.save(preuniversitarioActualizado)
    }

    private fun obtenerOCrearPreuniversitario(usuarioId: String): Preuniversitario {
        if (usuarioId.isBlank() || usuarioId == "usuario_anonimo") {
            throw IllegalArgumentException("Usuario inválido para registrar racha")
        }

        val preuniversitarioExistente = preuniversitarioRepository.findByUsuarioId(usuarioId)

        if (preuniversitarioExistente != null) {
            return preuniversitarioExistente
        }

        return preuniversitarioRepository.save(
            Preuniversitario(
                usuarioId = usuarioId,
                facultad = Facultad(
                    nombre = "Ciencias y Tecnología"
                )
            )
        )
    }
}