package com.deis.backend.service

import com.deis.backend.model.RecompensaObtenida
import com.deis.backend.repository.RecompensaObtenidaRepository
import com.deis.backend.repository.RecompensaRepository
import org.springframework.stereotype.Service

@Service
class RecompensaService(
    private val recompensaRepository: RecompensaRepository,
    private val recompensaObtenidaRepository: RecompensaObtenidaRepository
) {

    fun guardarRecompensasPorResultado(
        usuarioId: String,
        porcentaje: Int
    ): List<RecompensaObtenida> {
        val nuevasRecompensas = mutableListOf<RecompensaObtenida>()

        fun intentarGuardar(codigo: String) {
            val recompensa = recompensaRepository.findByCodigo(codigo) ?: return

            val yaExiste = recompensaObtenidaRepository
                .existsByUsuarioIdAndRecompensaCodigo(usuarioId, codigo)

            if (!yaExiste) {
                val obtenida = RecompensaObtenida(
                    usuarioId = usuarioId,
                    recompensaCodigo = codigo
                )
                recompensaObtenidaRepository.save(obtenida)
                nuevasRecompensas.add(obtenida)
            }
        }

        when {
            porcentaje <= 39 -> intentarGuardar("RECOMPENSA_BAJA")
            porcentaje <= 79 -> intentarGuardar("RECOMPENSA_MEDIA")
            else -> intentarGuardar("RECOMPENSA_ALTA")
        }

        return nuevasRecompensas
    }

    fun guardarRecompensaPorRachaActiva(
        usuarioId: String,
        diasConsecutivos: Int
    ): List<RecompensaObtenida> {
        val nuevasRecompensas = mutableListOf<RecompensaObtenida>()

        if (diasConsecutivos < 3) {
            return nuevasRecompensas
        }

        val codigo = "RECOMPENSA_RACHA"
        val recompensa = recompensaRepository.findByCodigo(codigo) ?: return nuevasRecompensas

        val yaExiste = recompensaObtenidaRepository
            .existsByUsuarioIdAndRecompensaCodigo(usuarioId, codigo)

        if (!yaExiste) {
            val obtenida = RecompensaObtenida(
                usuarioId = usuarioId,
                recompensaCodigo = codigo
            )
            recompensaObtenidaRepository.save(obtenida)
            nuevasRecompensas.add(obtenida)
        }

        return nuevasRecompensas
    }
}