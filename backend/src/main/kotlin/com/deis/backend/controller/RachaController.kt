package com.deis.backend.controller

import com.deis.backend.dto.RachaResponse
import com.deis.backend.model.Racha
import com.deis.backend.service.RachaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import com.deis.backend.service.LogroService

@RestController
@RequestMapping("/api/racha")
class RachaController(
    private val rachaService: RachaService,
    private val logroService: LogroService
) {

    @GetMapping("/{usuarioId}")
    fun obtenerRacha(@PathVariable usuarioId: String): ResponseEntity<Any> {
        return try {
            val racha = rachaService.obtenerRacha(usuarioId)

            ResponseEntity.ok(
                construirRespuesta(
                    racha = racha,
                    mensaje = "Racha obtenida correctamente"
                ) as Any
            )
        } catch (e: IllegalArgumentException) {
            respuestaError(
                HttpStatus.NOT_FOUND,
                e.message ?: "No se pudo obtener la racha"
            )
        } catch (e: Exception) {
            respuestaError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno al obtener la racha"
            )
        }
    }

    @PostMapping("/{usuarioId}/practica")
    fun registrarPracticaDiaria(@PathVariable usuarioId: String): ResponseEntity<Any> {
        return try {
            val fechaActual = LocalDate.now()
            val rachaAnterior = rachaService.obtenerRacha(usuarioId)
            val rachaActualizada = rachaService.registrarPracticaDiaria(usuarioId, fechaActual)

            val nuevosLogros = logroService.verificarLogroRacha(
                usuarioId = usuarioId,
                diasConsecutivos = rachaActualizada.diasConsecutivos
            )

            println("===== LOGROS RACHA =====")
            println("Usuario: $usuarioId")
            println("Días consecutivos: ${rachaActualizada.diasConsecutivos}")

            if (nuevosLogros.isEmpty()) {
                println("No se desbloqueó ningún logro nuevo por racha.")
            } else {
                nuevosLogros.forEach {
                    println("Logro desbloqueado por racha: ${it.logroCodigo}")
                }
            }

            println("========================")

            val mensaje = when {
                rachaAnterior.ultimaPractica == fechaActual ->
                    "Ya registraste una práctica hoy. La racha no aumentó."

                rachaActualizada.diasConsecutivos == 1 ->
                    "Racha iniciada correctamente."

                else ->
                    "Racha actualizada correctamente."
            }

            ResponseEntity.ok(
                construirRespuesta(
                    racha = rachaActualizada,
                    mensaje = mensaje
                ) as Any
            )
        } catch (e: IllegalArgumentException) {
            respuestaError(
                HttpStatus.NOT_FOUND,
                e.message ?: "No se pudo registrar la práctica diaria"
            )
        } catch (e: Exception) {
            respuestaError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno al registrar la práctica diaria"
            )
        }
    }

   private fun construirRespuesta(
    racha: Racha,
    mensaje: String
): RachaResponse {
    return RachaResponse(
        diasConsecutivos = racha.diasConsecutivos,
        ultimaPractica = racha.ultimaPractica?.toString(),
        estadoDelfin = obtenerEstadoDelfin(racha),
        mensaje = mensaje
    )
}

   private fun obtenerEstadoDelfin(racha: Racha): String {
    val fechaActual = LocalDate.now(java.time.ZoneId.of("America/La_Paz"))
    val ultimaPractica = racha.ultimaPractica ?: return "DORMIDO"

    return when {
        ultimaPractica != fechaActual -> "DORMIDO"
        racha.diasConsecutivos >= 7 -> "FELIZ"
        racha.diasConsecutivos > 0 -> "DESPIERTO"
        else -> "DORMIDO"
    }
}

    private fun respuestaError(
        status: HttpStatus,
        mensaje: String
    ): ResponseEntity<Any> {
        return ResponseEntity
            .status(status)
            .body(mapOf("mensaje" to mensaje) as Any)
    }
}