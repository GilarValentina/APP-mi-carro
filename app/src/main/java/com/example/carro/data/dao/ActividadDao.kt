package com.example.carro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.carro.data.entity.ActividadMantenimientoEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a datos del plan de mantenimiento (RF-10 a RF-16). */
@Dao
interface ActividadDao {

    @Query("SELECT * FROM actividades_mantenimiento WHERE vehiculoId = :vehiculoId ORDER BY creadoEn DESC")
    fun observarPorVehiculo(vehiculoId: Long): Flow<List<ActividadMantenimientoEntity>>

    @Query("SELECT * FROM actividades_mantenimiento WHERE vehiculoId = :vehiculoId")
    suspend fun obtenerPorVehiculo(vehiculoId: Long): List<ActividadMantenimientoEntity>

    /** Actividades activas de todos los vehículos no archivados (revisión diaria de alertas). */
    @Query(
        "SELECT a.* FROM actividades_mantenimiento a INNER JOIN vehiculos v ON v.id = a.vehiculoId " +
            "WHERE a.activa = 1 AND v.archivado = 0"
    )
    suspend fun obtenerActivasDeVehiculosActivos(): List<ActividadMantenimientoEntity>

    @Query("SELECT * FROM actividades_mantenimiento WHERE id = :id")
    fun observarPorId(id: Long): Flow<ActividadMantenimientoEntity?>

    @Query("SELECT * FROM actividades_mantenimiento WHERE id = :id")
    suspend fun obtenerPorId(id: Long): ActividadMantenimientoEntity?

    @Insert
    suspend fun insertar(actividad: ActividadMantenimientoEntity): Long

    @Update
    suspend fun actualizar(actividad: ActividadMantenimientoEntity)

    @Delete
    suspend fun eliminar(actividad: ActividadMantenimientoEntity)
}
