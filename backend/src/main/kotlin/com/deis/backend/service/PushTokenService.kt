package com.deis.backend.service

import com.deis.backend.dto.RegistrarPushTokenRequest
import com.deis.backend.dto.RegistrarPushTokenResponse
import com.deis.backend.model.DispositivoPush
import com.deis.backend.repository.DispositivoPushRepository
import org.springframework.stereotype.Service

@Service
class PushTokenService(
    private val dispositivoPushRepository: DispositivoPushRepository
) {

    fun registrarToken(request: RegistrarPushTokenRequest): RegistrarPushTokenResponse {
        val usuarioId = request.usuarioId.trim()
        val tokenFcm = request.tokenFcm.trim()

        require(usuarioId.isNotBlank()) { "El usuario es obligatorio" }
        require(tokenFcm.isNotBlank()) { "El token FCM es obligatorio" }

        val existente = dispositivoPushRepository.findByTokenFcm(tokenFcm)

        if (existente != null) {
            dispositivoPushRepository.save(
                existente.copy(
                    usuarioId = usuarioId,
                    plataforma = request.plataforma,
                    activo = true
                )
            )

            println("Token push actualizado para usuario: $usuarioId")
            println("Token: $tokenFcm")
        } else {
            dispositivoPushRepository.save(
                DispositivoPush(
                    usuarioId = usuarioId,
                    tokenFcm = tokenFcm,
                    plataforma = request.plataforma
                )
            )
            println("Token push guardado para usuario: $usuarioId")
            println("Token: $tokenFcm")
        }

        return RegistrarPushTokenResponse(
            mensaje = "Token push registrado correctamente"
        )
    }
}