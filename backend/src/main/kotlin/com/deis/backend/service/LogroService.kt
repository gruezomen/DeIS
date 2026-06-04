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

    private fun intentarDesbloquear(
    usuarioId: String,
    codigo: String,
    nuevosLogros: MutableList<LogroDesbloqueado>
    ) {
        val logro = logroRepository.findByCodigo(codigo) ?: return

        val yaExiste = logroDesbloqueadoRepository
            .existsByUsuarioIdAndLogroCodigo(usuarioId, codigo)

        if (!yaExiste) {
            val desbloqueado = LogroDesbloqueado(
                usuarioId = usuarioId,
                logroCodigo = logro.codigo
            )

            logroDesbloqueadoRepository.save(desbloqueado)
            nuevosLogros.add(desbloqueado)
        }
    }

    private fun verificarLogroColeccionista(
        usuarioId: String,
        nuevosLogros: MutableList<LogroDesbloqueado>
    ) {
        val codigoColeccionista = "COLECCIONISTA"

        val yaExiste = logroDesbloqueadoRepository
            .existsByUsuarioIdAndLogroCodigo(usuarioId, codigoColeccionista)

        if (yaExiste) return

        val logroColeccionista = logroRepository.findByCodigo(codigoColeccionista) ?: return

        val logrosActuales = logroDesbloqueadoRepository.findByUsuarioId(usuarioId)

        val cantidadSinColeccionista = logrosActuales.count {
            it.logroCodigo != codigoColeccionista
        }

        if (cantidadSinColeccionista >= logroColeccionista.condicionValor - 1) {
            intentarDesbloquear(
                usuarioId = usuarioId,
                codigo = codigoColeccionista,
                nuevosLogros = nuevosLogros
            )
        }
    }
    fun verificarLogrosPractica(
        usuarioId: String,
        totalPracticasCompletadas: Int,
        porcentajeAciertos: Int
    ): List<LogroDesbloqueado> {
        val nuevosLogros = mutableListOf<LogroDesbloqueado>()

        if (totalPracticasCompletadas >= 1) {
            intentarDesbloquear(
                usuarioId = usuarioId,
                codigo = "PRIMERA_PRACTICA",
                nuevosLogros = nuevosLogros
            )
        }

        if (totalPracticasCompletadas >= 5) {
            intentarDesbloquear(
                usuarioId = usuarioId,
                codigo = "CINCO_PRACTICAS",
                nuevosLogros = nuevosLogros
            )
        }

        if (porcentajeAciertos >= 90) {
            intentarDesbloquear(
                usuarioId = usuarioId,
                codigo = "PRECISION_ALTA",
                nuevosLogros = nuevosLogros
            )
        }

        if (porcentajeAciertos == 100) {
            intentarDesbloquear(
                usuarioId = usuarioId,
                codigo = "SIN_ERRORES",
                nuevosLogros = nuevosLogros
            )
        }

        verificarLogroColeccionista(
            usuarioId = usuarioId,
            nuevosLogros = nuevosLogros
        )

        return nuevosLogros
    }

    fun verificarLogroRacha(
        usuarioId: String,
        diasConsecutivos: Int
    ): List<LogroDesbloqueado> {
        val nuevosLogros = mutableListOf<LogroDesbloqueado>()

        if (diasConsecutivos >= 3) {
            intentarDesbloquear(
                usuarioId = usuarioId,
                codigo = "RACHA_3_DIAS",
                nuevosLogros = nuevosLogros
            )
        }

        if (diasConsecutivos >= 7) {
            intentarDesbloquear(
                usuarioId = usuarioId,
                codigo = "RACHA_7_DIAS",
                nuevosLogros = nuevosLogros
            )
        }

        if (diasConsecutivos >= 15) {
            intentarDesbloquear(
                usuarioId = usuarioId,
                codigo = "RACHA_15_DIAS",
                nuevosLogros = nuevosLogros
            )
        }

        verificarLogroColeccionista(
            usuarioId = usuarioId,
            nuevosLogros = nuevosLogros
        )

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