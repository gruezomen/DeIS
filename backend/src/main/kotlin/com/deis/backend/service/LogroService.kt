package com.deis.backend.service

import com.deis.backend.model.LogroDesbloqueado
import com.deis.backend.repository.LogroDesbloqueadoRepository
import com.deis.backend.repository.LogroRepository
import org.springframework.stereotype.Service
import com.deis.backend.dto.LogroItemResponse
import com.deis.backend.dto.LogrosUsuarioResponse

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

    fun verificarLogroRacha(
        usuarioId: String,
        diasConsecutivos: Int
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

        if (diasConsecutivos >= 3) {
            intentarDesbloquear("RACHA_3_DIAS")
        }

        return nuevosLogros
    }

    fun obtenerLogrosUsuario(usuarioId: String): LogrosUsuarioResponse {
        val logros = logroRepository.findAll()
        val desbloqueadosUsuario = logroDesbloqueadoRepository.findByUsuarioId(usuarioId)

        val mapaDesbloqueados = desbloqueadosUsuario.associateBy { it.logroCodigo }

        val desbloqueados = mutableListOf<LogroItemResponse>()
        val pendientes = mutableListOf<LogroItemResponse>()

        logros.forEach { logro ->
            val desbloqueado = mapaDesbloqueados[logro.codigo]

            val item = LogroItemResponse(
                codigo = logro.codigo,
                titulo = logro.titulo,
                descripcion = logro.descripcion,
                desbloqueado = desbloqueado != null,
                fechaDesbloqueo = desbloqueado?.fechaDesbloqueo?.toString()
            )

            if (desbloqueado != null) {
                desbloqueados.add(item)
            } else {
                pendientes.add(item)
            }
        }

        return LogrosUsuarioResponse(
            usuarioId = usuarioId,
            desbloqueados = desbloqueados,
            pendientes = pendientes
        )
    }

    fun verificarLogrosSimulacro(
        usuarioId: String,
        totalSimulacrosCompletados: Int
    ): List<LogroDesbloqueado> {
        val nuevosLogros = mutableListOf<LogroDesbloqueado>()

        fun intentarDesbloquear(codigo: String) {
            val logro = logroRepository.findByCodigo(codigo) ?: return

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

        if (totalSimulacrosCompletados >= 1) {
            intentarDesbloquear("PRIMER_SIMULACRO")
        }

        if (totalSimulacrosCompletados >= 3) {
            intentarDesbloquear("TRES_SIMULACROS")
        }

        return nuevosLogros
    }
}