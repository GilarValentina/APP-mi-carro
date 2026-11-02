package com.example.carro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.carro.data.entity.RepuestoEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a datos de repuestos (RF-24 a RF-27). */
@Dao
interface RepuestoDao {

    @Query("SELECT * FROM repuestos WHERE mantenimientoId = :mantenimientoId ORDER BY id ASC")
    fun observarPorMantenimiento(mantenimientoId: Long): Flow<List<RepuestoEntity>>

    @Query("SELECT * FROM repuestos WHERE mantenimientoId = :mantenimientoId ORDER BY id ASC")
    suspend fun obtenerPorMantenimiento(mantenimientoId: Long): List<RepuestoEntity>

    @Query("SELECT COUNT(*) FROM repuestos WHERE mantenimientoId = :mantenimientoId")
    suspend fun contarPorMantenimiento(mantenimientoId: Long): Int

    /** RF-34 y RF-36: repuestos de todos los mantenimientos de un vehículo. */
    @Query(
        "SELECT r.* FROM repuestos r " +
            "INNER JOIN mantenimientos m ON m.id = r.mantenimientoId " +
            "WHERE m.vehiculoId = :vehiculoId ORDER BY r.id ASC"
    )
    fun observarPorVehiculo(vehiculoId: Long): Flow<List<RepuestoEntity>>

    @Query("DELETE FROM repuestos WHERE mantenimientoId = :mantenimientoId")
    suspend fun eliminarPorMantenimiento(mantenimientoId: Long)

    @Query("SELECT * FROM repuestos WHERE instaladoActualmente = 1 ORDER BY fechaInstalacion DESC")
    fun observarInstalados(): Flow<List<RepuestoEntity>>

    @Insert
    suspend fun insertar(repuesto: RepuestoEntity): Long

    @Update
    suspend fun actualizar(repuesto: RepuestoEntity)

    @Delete
    suspend fun eliminar(repuesto: RepuestoEntity)
}
