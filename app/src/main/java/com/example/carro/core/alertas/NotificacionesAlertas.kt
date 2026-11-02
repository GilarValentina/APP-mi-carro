package com.example.carro.core.alertas

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.carro.R
import com.example.carro.domain.alerta.DocumentoConEstado
import com.example.carro.domain.alerta.EstadoVencimiento

/**
 * Publicación de notificaciones locales de vencimientos (RF-28, RF-33).
 * Funciona 100% sin conexión: todo se genera en el dispositivo.
 */
object NotificacionesAlertas {

    private const val CANAL_ID = "alertas_vencimientos"
    private const val CANAL_NOMBRE = "Alertas de vencimientos"

    fun crearCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_ID,
            CANAL_NOMBRE,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos de mantenimientos y documentos próximos o vencidos"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(canal)
    }

    fun tienePermiso(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    /** Publica una notificación por cada documento próximo o vencido. */
    fun notificar(context: Context, pendientes: List<DocumentoConEstado>) {
        if (pendientes.isEmpty() || !tienePermiso(context)) return
        crearCanal(context)
        val manager = NotificationManagerCompat.from(context)
        pendientes.forEach { item ->
            val titulo = if (item.estado == EstadoVencimiento.VENCIDO) {
                "Documento vencido"
            } else {
                "Documento próximo a vencer"
            }
            val texto = "${item.documento.titulo}: vence el ${item.documento.fechaVencimiento}"
            val notificacion = NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            manager.notify(item.documento.id.toInt(), notificacion)
        }
    }

    fun notificarMantenimiento(
        context: Context,
        titulo: String,
        mensaje: String,
        id: Int = 9001
    ) {
        if (!tienePermiso(context)) return
        crearCanal(context)
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(id, notificacion)
    }

    fun notificarRecordatorios(
        context: Context,
        recordatorios: List<Pair<String, String>>
    ) {
        if (recordatorios.isEmpty() || !tienePermiso(context)) return
        crearCanal(context)
        val manager = NotificationManagerCompat.from(context)
        recordatorios.forEachIndexed { index, (titulo, texto) ->
            val notificacion = NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            manager.notify(9000 + index, notificacion)
        }
    }
}
