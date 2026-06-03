package com.conference.deis.ui.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.conference.deis.network.model.Simulacro
import com.conference.deis.ui.receivers.SimulacroRecordatorioReceiver
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun programarRecordatorioSimulacro(
    context: Context,
    simulacro: Simulacro
) {
    val horaInicioTexto = simulacro.horaInicio

    if (horaInicioTexto.isNullOrBlank()) {
        Toast.makeText(
            context,
            "No se pudo obtener la hora del simulacro",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    try {
        val horaInicio = LocalDateTime.parse(
            horaInicioTexto,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )

        val momentoRecordatorio = horaInicio.minusMinutes(5)

        val instanteRecordatorio = momentoRecordatorio
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (instanteRecordatorio <= System.currentTimeMillis()) {
            Toast.makeText(
                context,
                "Ya no se puede programar este recordatorio",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(context, SimulacroRecordatorioReceiver::class.java).apply {
            putExtra("simulacroId", simulacro.id)
            putExtra("nombreSimulacro", simulacro.nombre)
            putExtra("horaInicio", horaInicioTexto)
        }

        val requestCode = simulacro.id?.hashCode() ?: horaInicioTexto.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intentPermiso = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                context.startActivity(intentPermiso)

                Toast.makeText(
                    context,
                    "Debes permitir alarmas exactas para usar este recordatorio",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            instanteRecordatorio,
            pendingIntent
        )

        Toast.makeText(
            context,
            "Recordatorio programado 5 minutos antes",
            Toast.LENGTH_SHORT
        ).show()
    } catch (e: Exception) {
        e.printStackTrace()

        Toast.makeText(
            context,
            "No se pudo programar el recordatorio: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}