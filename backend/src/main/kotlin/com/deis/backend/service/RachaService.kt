package com.deis.backend.service

import com.deis.backend.model.Racha
import com.deis.backend.repository.PreuniversitarioRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import com.deis.backend.model.Facultad
import com.deis.backend.model.Preuniversitario

@Service
class RachaService(
    private val preuniversitarioRepository: PreuniversitarioRepository
) {

   fun obtenerRacha(usuarioId: String): Racha {
    val preuniversitario = obtenerOCrearPreuniversitario(usuarioId)

    return preuniversitario.racha
}

    fun registrarPracticaDiaria(
        usuarioId: String,
        fechaActual: LocalDate = LocalDate.now()
    ): Racha {
        val preuniversitario = obtenerOCrearPreuniversitario(usuarioId)

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