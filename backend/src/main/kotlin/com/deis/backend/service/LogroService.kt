package com.deis.backend.service

import com.deis.backend.model.LogroDesbloqueado
import com.deis.backend.repository.LogroDesbloqueadoRepository
import com.deis.backend.repository.LogroRepository
import org.springframework.stereotype.Service

@Service
class LogroService(
    private val logroRepository: LogroRepository,
    private val logroDesbloqueadoRepository: LogroDesbloqueadoRepository
) {

    fun verificarLogrosPractica(
        usuarioId: String,
        totalPracticasCompletadas: Int,
        porcentajeAciertos: Int
    ): List<LogroDesbloqueado> {
        val nuevosLogros = mutableListOf<LogroDesbloqueado>()

        fun intentarDesbloquear(codigo: String) {
            println("Buscando logro con código: $codigo")

            val logro = logroRepository.findByCodigo(codigo)

            println("Resultado encontrado: $logro")

            if (logro == null) {
                return
            }

            val yaExiste = logroDesbloqueadoRepository
                .existsByUsuarioIdAndLogroCodigo(usuarioId, codigo)

            if (!yaExiste) {
                val desbloqueado = LogroDesbloqueado(
                    usuarioId = usuarioId,
                    logroCodigo = codigo
                )
                logroDesbloqueadoRepository.save(desbloqueado)
                nuevosLogros.add(desbloqueado)
            }
        }

        if (totalPracticasCompletadas >= 1) {
            intentarDesbloquear("PRIMERA_PRACTICA")
        }

        if (totalPracticasCompletadas >= 5) {
            intentarDesbloquear("CINCO_PRACTICAS")
        }

        if (porcentajeAciertos >= 90) {
            intentarDesbloquear("PRECISION_ALTA")
        }

        return nuevosLogros
    }
}