package com.example.carro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.carro.data.entity.MantenimientoEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a datos de mantenimientos realizados (RF-17 a RF-23, RF-34). */
@Dao
interface MantenimientoDao {

    @Query("SELECT * FROM mantenimientos WHERE vehiculoId = :vehiculoId ORDER BY fecha DESC, id DESC")
    fun observarPorVehiculo(vehiculoId: Long): Flow<List<MantenimientoEntity>>

    @Query("SELECT * FROM mantenimientos WHERE id = :id")
    fun observarPorId(id: Long): Flow<MantenimientoEntity?>

    /** RF-37: últimos mantenimientos de todos los vehículos, para el inicio. */
    @Query("SELECT * FROM mantenimientos ORDER BY fecha DESC, id DESC LIMIT :limite")
    fun observarRecientes(limite: Int): Flow<List<MantenimientoEntity>>

    @Query("SELECT * FROM mantenimientos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): MantenimientoEntity?

    @Query("SELECT SUM(costoTotal) FROM mantenimientos WHERE vehiculoId = :vehiculoId")
    suspend fun obtenerCostoTotal(vehiculoId: Long): Double?

    @Insert
    suspend fun insertar(mantenimiento: MantenimientoEntity): Long

    @Update
    suspend fun actualizar(mantenimiento: MantenimientoEntity)

    @Delete
    suspend fun eliminar(mantenimiento: MantenimientoEntity)
}
