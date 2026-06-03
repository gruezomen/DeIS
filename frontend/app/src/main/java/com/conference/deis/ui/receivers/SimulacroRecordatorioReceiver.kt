package com.conference.deis.ui.receivers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.conference.deis.MainActivity
import com.conference.deis.R

class SimulacroRecordatorioReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val nombreSimulacro = intent.getStringExtra("nombreSimulacro") ?: "Simulacro"
        val horaInicio = intent.getStringExtra("horaInicio") ?: ""

        val channelId = "recordatorios_simulacros"
        crearCanalSiHaceFalta(context, channelId)

        val abrirAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            abrirAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.delfin)
            .setContentTitle("Tu simulacro empieza pronto")
            .setContentText("El simulacro \"$nombreSimulacro\" inicia en 5 minutos.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "El simulacro \"$nombreSimulacro\" inicia en 5 minutos. Hora de inicio: $horaInicio"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun crearCanalSiHaceFalta(
        context: Context,
        channelId: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de simulacros",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones locales para recordar simulacros próximos"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}