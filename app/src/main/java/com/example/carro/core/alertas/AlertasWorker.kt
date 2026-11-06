package com.example.carro.core.alertas

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.carro.MiCarroApplication
import com.example.carro.domain.model.EstadoActividad

/**
 * Trabajo en segundo plano que revisa los vencimientos y publica alertas locales (RF-28, RF-33).
 * Se ejecuta de forma periódica mediante WorkManager, sin requerir conexión (RNF offline-first).
 */
class AlertasWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as MiCarroApplication).container
        val repository = container.alertasRepository
        if (!repository.configuracionActual().alertasActivas) return Result.success()
        return try {
            val pendientes = repository.sincronizarAlertas()
            NotificacionesAlertas.notificar(applicationContext, pendientes)

            // Mantenimientos próximos o vencidos de los vehículos activos.
            val mantenimientos = container.planMantenimientoRepository.pendientesDeAviso(
                kilometrajeDe = { id -> container.vehiculoDao.obtenerPorId(id)?.kilometrajeActual }
            )
            NotificacionesAlertas.notificarRecordatorios(
                applicationContext,
                mantenimientos.map { item ->
                    val estado = if (item.estado == EstadoActividad.VENCIDA) "vencido" else "próximo"
                    "Mantenimiento $estado" to item.actividad.nombre
                }
            )

            // Respaldo de los recordatorios de citas por si el sistema canceló algún trabajo.
            container.citasRepository.obtenerPendientes().forEach {
                ProgramadorCitas.programar(applicationContext, it)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
