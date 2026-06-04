package com.conference.deis.ui.receivers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.conference.deis.MainActivity
import com.conference.deis.R
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.RegistrarPushTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val usuarioId = UserSession.user?.id?.toString() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitInstance.api.registrarPushToken(
                    RegistrarPushTokenRequest(
                        usuarioId = usuarioId,
                        tokenFcm = token
                    )
                )
            } catch (_: Exception) {
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val titulo = message.notification?.title
            ?: message.data["title"]
            ?: "Nuevo simulacro disponible"

        val cuerpo = message.notification?.body
            ?: message.data["body"]
            ?: "Tienes una nueva notificación."

        val simulacroId = message.data["simulacroId"]

        mostrarNotificacion(
            titulo = titulo,
            cuerpo = cuerpo,
            simulacroId = simulacroId
        )
    }

    private fun mostrarNotificacion(
        titulo: String,
        cuerpo: String,
        simulacroId: String?
    ) {
        val channelId = "push_simulacros"
        crearCanalSiHaceFalta(channelId)

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("destino", "lista_simulacros")
            putExtra("simulacroId", simulacroId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            simulacroId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.delfin)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(this).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun crearCanalSiHaceFalta(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Push de simulacros",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones push de simulacros"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}