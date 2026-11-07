package com.example.carro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.carro.data.entity.LecturaKilometrajeEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a datos de lecturas de kilometraje (RF-06 a RF-09). */
@Dao
interface KilometrajeDao {

    @Query("SELECT * FROM lecturas_kilometraje WHERE vehiculoId = :vehiculoId ORDER BY fecha DESC, id DESC")
    fun observarPorVehiculo(vehiculoId: Long): Flow<List<LecturaKilometrajeEntity>>

    @Query("SELECT MAX(valorOdometro) FROM lecturas_kilometraje WHERE vehiculoId = :vehiculoId")
    suspend fun obtenerMaximoOdometro(vehiculoId: Long): Long?

    @Query("SELECT * FROM lecturas_kilometraje WHERE vehiculoId = :vehiculoId ORDER BY fecha ASC, id ASC")
    suspend fun obtenerPorVehiculo(vehiculoId: Long): List<LecturaKilometrajeEntity>

    @Insert
    suspend fun insertar(lectura: LecturaKilometrajeEntity): Long

    @Delete
    suspend fun eliminar(lectura: LecturaKilometrajeEntity)
}
