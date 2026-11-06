package com.example.carro.data.repository

import com.example.carro.data.dao.ActividadDao
import com.example.carro.data.dao.CategoriaDao
import com.example.carro.data.entity.ActividadMantenimientoEntity
import com.example.carro.data.entity.CategoriaEntity
import com.example.carro.domain.model.EstadoActividad
import com.example.carro.domain.plan.Actividad
import com.example.carro.domain.plan.ActividadConEstado
import com.example.carro.domain.plan.CategoriasPredeterminadas
import com.example.carro.domain.plan.EstadoCalculator
import com.example.carro.domain.plan.PlanPreventivoBase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Repositorio del plan de mantenimiento (RF-10 a RF-16).
 * Traduce entre entidades Room y modelos de dominio, y calcula el estado de cada actividad.
 */
class PlanMantenimientoRepository(
    private val actividadDao: ActividadDao,
    private val categoriaDao: CategoriaDao
) {

    fun observarActividades(
        vehiculoId: Long,
        kilometrajeActual: Long,
        hoy: LocalDate = LocalDate.now()
    ): Flow<List<ActividadConEstado>> =
        actividadDao.observarPorVehiculo(vehiculoId).map { lista ->
            lista.map { entity ->
                val actividad = entity.toDomain()
                ActividadConEstado(
                    actividad = actividad,
                    estado = EstadoCalculator.calcular(actividad, hoy, kilometrajeActual)
                )
            }
        }

    fun observarCategorias(): Flow<List<CategoriaEntity>> = categoriaDao.observarTodas()

    suspend fun obtenerActividad(id: Long): Actividad? =
        actividadDao.obtenerPorId(id)?.toDomain()

    suspend fun guardar(actividad: Actividad): Long {
        val entity = actividad.toEntity()
        return if (actividad.id == 0L) {
            actividadDao.insertar(entity)
        } else {
            actividadDao.actualizar(entity)
            actividad.id
        }
    }

    /** RF-15: pausar o reactivar una actividad. */
    suspend fun cambiarActiva(actividad: Actividad, activa: Boolean) {
        actividadDao.actualizar(actividad.copy(activa = activa).toEntity())
    }

    /** RF-15: eliminar una actividad. */
    suspend fun eliminar(actividad: Actividad) {
        actividadDao.eliminar(actividad.toEntity())
    }

    /**
     * RF-16: aplica el plan preventivo base (5.000 / 10.000 / 20.000 km) al vehículo.
     * No duplica actividades que ya existan con el mismo nombre. Devuelve cuántas creó.
     */
    suspend fun aplicarPlanBase(vehiculoId: Long, kmActual: Long): Int {
        sembrarCategoriasSiVacio()
        val categorias = categoriaDao.obtenerTodas().associateBy { it.nombre }
        val existentes = actividadDao.obtenerPorVehiculo(vehiculoId).map { it.nombre.lowercase() }.toSet()
        var creadas = 0
        PlanPreventivoBase.lista
            .filter { it.nombre.lowercase() !in existentes }
            .forEach { plantilla ->
                val intervalo = plantilla.intervaloKm ?: return@forEach
                actividadDao.insertar(
                    Actividad(
                        vehiculoId = vehiculoId,
                        nombre = plantilla.nombre,
                        categoriaId = categorias[plantilla.categoria]?.id,
                        descripcion = "Cada ${"%,d".format(intervalo).replace(',', '.')} km",
                        programarPorKm = true,
                        intervaloKm = intervalo,
                        proximoKm = PlanPreventivoBase.proximoHito(kmActual, intervalo),
                        anticipacionKm = PlanPreventivoBase.anticipacionKm(intervalo)
                    ).toEntity()
                )
                creadas++
            }
        return creadas
    }

    /**
     * RF-28: actividades activas próximas o vencidas de todos los vehículos activos,
     * para la revisión diaria en segundo plano.
     */
    suspend fun pendientesDeAviso(
        kilometrajeDe: suspend (Long) -> Long?,
        hoy: LocalDate = LocalDate.now()
    ): List<ActividadConEstado> =
        actividadDao.obtenerActivasDeVehiculosActivos().mapNotNull { entity ->
            val actividad = entity.toDomain()
            val km = kilometrajeDe(actividad.vehiculoId) ?: return@mapNotNull null
            val estado = EstadoCalculator.calcular(actividad, hoy, km)
            if (estado == EstadoActividad.PROXIMA || estado == EstadoActividad.VENCIDA) {
                ActividadConEstado(actividad, estado)
            } else {
                null
            }
        }

    /** RF-12: siembra las categorías iniciales si la tabla está vacía. */
    suspend fun sembrarCategoriasSiVacio() {
        if (categoriaDao.contar() == 0) {
            categoriaDao.insertarTodas(
                CategoriasPredeterminadas.nombres.map {
                    CategoriaEntity(nombre = it, esPredeterminada = true)
                }
            )
        }
    }
}

private fun ActividadMantenimientoEntity.toDomain(): Actividad = Actividad(
    id = id,
    vehiculoId = vehiculoId,
    nombre = nombre,
    categoriaId = categoriaId,
    descripcion = descripcion,
    programarPorFecha = programarPorFecha,
    programarPorKm = programarPorKm,
    intervaloDias = intervaloDias,
    intervaloKm = intervaloKm,
    proximaFecha = proximaFecha,
    proximoKm = proximoKm,
    anticipacionDias = anticipacionDias ?: 0,
    anticipacionKm = anticipacionKm ?: 0,
    activa = activa
)

private fun Actividad.toEntity(): ActividadMantenimientoEntity = ActividadMantenimientoEntity(
    id = id,
    vehiculoId = vehiculoId,
    nombre = nombre,
    categoriaId = categoriaId,
    descripcion = descripcion,
    programarPorFecha = programarPorFecha,
    programarPorKm = programarPorKm,
    intervaloDias = intervaloDias,
    intervaloKm = intervaloKm,
    proximaFecha = proximaFecha,
    proximoKm = proximoKm,
    anticipacionDias = anticipacionDias,
    anticipacionKm = anticipacionKm,
    estado = EstadoActividad.SIN_PROGRAMACION.name,
    activa = activa
)
