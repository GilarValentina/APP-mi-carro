package com.example.carro.core.alertas

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.carro.MiCarroApplication
import com.example.carro.domain.agenda.Cita
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Programa un recordatorio local por cita con WorkManager (sin conexión).
 * Si el momento del recordatorio ya pasó pero la cita es futura, avisa de inmediato.
 */
object ProgramadorCitas {

    private const val KEY_CITA_ID = "cita_id"

    private fun nombreTrabajo(id: Long) = "recordatorio_cita_$id"

    fun programar(context: Context, cita: Cita, ahora: LocalDateTime = LocalDateTime.now()) {
        if (cita.completada || cita.momento.isBefore(ahora)) {
            cancelar(context, cita.id)
            return
        }
        val retraso = Duration.between(ahora, cita.momentoRecordatorio).toMillis().coerceAtLeast(0)
        val solicitud = OneTimeWorkRequestBuilder<RecordatorioCitaWorker>()
            .setInitialDelay(retraso, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_CITA_ID to cita.id))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(nombreTrabajo(cita.id), ExistingWorkPolicy.REPLACE, solicitud)
    }

    fun cancelar(context: Context, id: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(nombreTrabajo(id))
    }

    internal fun citaId(params: WorkerParameters): Long = params.inputData.getLong(KEY_CITA_ID, 0L)
}

/** Publica la notificación de una cita cuando llega su momento de recordatorio. */
class RecordatorioCitaWorker(
    context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as MiCarroApplication).container
        val cita = container.citasRepository.obtener(ProgramadorCitas.citaId(params)) ?: return Result.success()
        if (cita.completada) return Result.success()
        val cuando = cita.momento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"))
        NotificacionesAlertas.notificarMantenimiento(
            context = applicationContext,
            titulo = "Recordatorio: ${cita.titulo}",
            mensaje = buildString {
                append("${cita.tipo.etiqueta} el $cuando")
                cita.lugar?.let { append(" · $it") }
            },
            id = 20_000 + cita.id.toInt()
        )
        return Result.success()
    }
}
