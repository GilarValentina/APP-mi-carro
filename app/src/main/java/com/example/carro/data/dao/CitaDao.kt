package com.example.carro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.carro.data.entity.CitaEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a datos de la agenda de citas. */
@Dao
interface CitaDao {

    @Query("SELECT * FROM citas WHERE vehiculoId = :vehiculoId ORDER BY completada ASC, fecha ASC, minutoDelDia ASC")
    fun observarPorVehiculo(vehiculoId: Long): Flow<List<CitaEntity>>

    @Query("SELECT * FROM citas WHERE completada = 0 ORDER BY fecha ASC, minutoDelDia ASC")
    fun observarPendientes(): Flow<List<CitaEntity>>

    @Query("SELECT * FROM citas WHERE completada = 0 ORDER BY fecha ASC, minutoDelDia ASC")
    suspend fun obtenerPendientes(): List<CitaEntity>

    @Query("SELECT * FROM citas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): CitaEntity?

    @Insert
    suspend fun insertar(cita: CitaEntity): Long

    @Update
    suspend fun actualizar(cita: CitaEntity)

    @Delete
    suspend fun eliminar(cita: CitaEntity)
}
