package com.deis.backend.service

import com.deis.backend.dto.RecompensaItemResponse
import com.deis.backend.dto.RecompensasUsuarioResponse
import com.deis.backend.model.RecompensaObtenida
import com.deis.backend.repository.IntentoSimulacroRepository
import com.deis.backend.repository.RecompensaObtenidaRepository
import com.deis.backend.repository.RecompensaRepository
import com.deis.backend.repository.PreuniversitarioRepository
import org.springframework.stereotype.Service

@Service
class RecompensaService(
    private val recompensaRepository: RecompensaRepository,
    private val recompensaObtenidaRepository: RecompensaObtenidaRepository,
    private val intentoSimulacroRepository: IntentoSimulacroRepository,
    private val preuniversitarioRepository: PreuniversitarioRepository
) {

    fun guardarRecompensasPorResultado(
        usuarioId: String,
        porcentaje: Int,
        tipoIntento: String
    ): List<RecompensaObtenida> {
        val nuevasRecompensas = mutableListOf<RecompensaObtenida>()

        fun intentarGuardar(codigo: String) {
            val recompensa = recompensaRepository.findByCodigo(codigo) ?: return

            val yaExiste = recompensaObtenidaRepository
                .existsByUsuarioIdAndRecompensaCodigo(usuarioId, codigo)

            if (!yaExiste) {
                val obtenida = RecompensaObtenida(
                    usuarioId = usuarioId,
                    recompensaCodigo = recompensa.codigo
                )

                recompensaObtenidaRepository.save(obtenida)
                nuevasRecompensas.add(obtenida)
            }
        }

        when (tipoIntento) {
            "PRACTICA" -> {
                intentarGuardar("TITULO_DELFIN_NOVATO")

                when {
                    porcentaje <= 39 -> intentarGuardar("RECOMPENSA_BAJA_PRACTICA")
                    porcentaje <= 79 -> intentarGuardar("RECOMPENSA_MEDIA_PRACTICA")
                    else -> intentarGuardar("RECOMPENSA_ALTA_PRACTICA")
                }
            }

            "SIMULACRO" -> {
                when {
                    porcentaje <= 39 -> intentarGuardar("RECOMPENSA_BAJA_SIMULACRO")
                    porcentaje <= 79 -> intentarGuardar("RECOMPENSA_MEDIA_SIMULACRO")
                    else -> intentarGuardar("RECOMPENSA_ALTA_SIMULACRO")
                }
            }
        }

        if (porcentaje >= 51) {
            intentarGuardar("MARCO_OLA")
        }

        if (porcentaje >= 80) {
            intentarGuardar("MARCO_CORAL")
            intentarGuardar("TITULO_DELFIN_ACADEMICO")
        }
        
        val diasConsecutivos = preuniversitarioRepository
          .findByUsuarioId(usuarioId)
          ?.racha
          ?.diasConsecutivos
          ?: 0

          val notaCercanaAVeintiocho = porcentaje in 25..30
          val tieneRachaMinima = diasConsecutivos >= 2

          if (tieneRachaMinima && notaCercanaAVeintiocho) {
            intentarGuardar("MEDALLA_DELFIN_OCULTO")
         }

        if (tipoIntento == "SIMULACRO") {
            val simulacrosAltosRegistrados = contarSimulacrosAltos(usuarioId)

            val cumpleCuatroSimulacrosAltos =
                simulacrosAltosRegistrados >= 4 ||
                    (porcentaje >= 80 && simulacrosAltosRegistrados >= 3)

            if (cumpleCuatroSimulacrosAltos) {
                intentarGuardar("MARCO_OCEANO_PROFUNDO")
                intentarGuardar("MEDALLA_DELFIN_DIAMANTE")
                intentarGuardar("TITULO_GUARDIAN_OCEANO")
            }
        }

        return nuevasRecompensas
    }

    fun guardarRecompensaPorRachaActiva(
        usuarioId: String,
        diasConsecutivos: Int
    ): List<RecompensaObtenida> {
        val nuevasRecompensas = mutableListOf<RecompensaObtenida>()

        fun intentarGuardar(codigo: String) {
            val recompensa = recompensaRepository.findByCodigo(codigo) ?: return

            val yaExiste = recompensaObtenidaRepository
                .existsByUsuarioIdAndRecompensaCodigo(usuarioId, codigo)

            if (!yaExiste) {
                val obtenida = RecompensaObtenida(
                    usuarioId = usuarioId,
                    recompensaCodigo = recompensa.codigo
                )

                recompensaObtenidaRepository.save(obtenida)
                nuevasRecompensas.add(obtenida)
            }
        }

        if (diasConsecutivos >= 2) {
            intentarGuardar("MEDALLA_ALETA_INICIAL")
            intentarGuardar("TITULO_NADADOR_CONSTANTE")
        }

        if (diasConsecutivos >= 3) {
            intentarGuardar("RECOMPENSA_RACHA")
        }

        if (diasConsecutivos >= 7) {
            intentarGuardar("MEDALLA_SALTO_SEMANAL")
            intentarGuardar("TITULO_EXPLORADOR_ARRECIFE")
        }

        if (diasConsecutivos >= 10) {
            intentarGuardar("MEDALLA_NADO_IMPARABLE")
        }

        return nuevasRecompensas
    }

    fun obtenerRecompensasUsuario(usuarioId: String): RecompensasUsuarioResponse {
        val obtenidas = recompensaObtenidaRepository.findByUsuarioId(usuarioId)
        val mapaRecompensas = recompensaRepository.findAll().associateBy { it.codigo }

        val recompensas = obtenidas.mapNotNull { obtenida ->
            val recompensa = mapaRecompensas[obtenida.recompensaCodigo] ?: return@mapNotNull null

            RecompensaItemResponse(
                codigo = recompensa.codigo,
                titulo = recompensa.titulo,
                descripcion = recompensa.descripcion,
                tipo = recompensa.tipo,
                fechaObtencion = obtenida.fechaObtencion.toString()
            )
        }

        return RecompensasUsuarioResponse(
            usuarioId = usuarioId,
            recompensas = recompensas.sortedByDescending { it.fechaObtencion }
        )
    }

    private fun contarSimulacrosAltos(usuarioId: String): Int {
        return intentoSimulacroRepository
            .findByUsuarioIdOrderByFechaDesc(usuarioId)
            .count { intento ->
                val totalPreguntas = intento.totalPreguntas.coerceAtLeast(1)
                val porcentaje = (intento.respuestasCorrectas * 100) / totalPreguntas

                porcentaje >= 80
            }
    }
}