package com.deis.backend.service

import com.deis.backend.repository.DispositivoPushRepository
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service

@Service
class PushNotificationService(
    private val dispositivoPushRepository: DispositivoPushRepository
) {

    fun enviarPushNuevoSimulacro(
        usuarioIds: List<String>,
        nombreSimulacro: String,
        facultadNombre: String?,
        simulacroId: String?
    ) {
        if (usuarioIds.isEmpty()) return

        val tokens = dispositivoPushRepository
            .findByUsuarioIdInAndActivoTrue(usuarioIds)
            .map { it.tokenFcm }
            .distinct()

        if (tokens.isEmpty()) return

        val lotes = tokens.chunked(500)

        lotes.forEach { lote ->
            val mensaje = MulticastMessage.builder()
                .addAllTokens(lote)
                .setNotification(
                    Notification.builder()
                        .setTitle("Nuevo simulacro disponible")
                        .setBody(
                            if (!facultadNombre.isNullOrBlank()) {
                                "Se publicó \"$nombreSimulacro\" para $facultadNombre."
                            } else {
                                "Se publicó \"$nombreSimulacro\"."
                            }
                        )
                        .build()
                )
                .putData("tipo", "SIMULACRO")
                .putData("simulacroId", simulacroId.orEmpty())
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build()
                )
                .build()

            val respuesta = FirebaseMessaging.getInstance()
                .sendEachForMulticast(mensaje)

            respuesta.responses.forEachIndexed { index, envio ->
                if (!envio.isSuccessful) {
                    println("Error push token ${lote[index]}: ${envio.exception?.message}")
                }
            }
        }
    }
}