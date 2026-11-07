package com.example.carro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.carro.data.entity.VehiculoEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a datos de vehículos (RF-01 a RF-05). */
@Dao
interface VehiculoDao {

    @Query("SELECT * FROM vehiculos WHERE archivado = 0 ORDER BY esPrincipal DESC, marca ASC")
    fun observarActivos(): Flow<List<VehiculoEntity>>

    @Query("SELECT * FROM vehiculos ORDER BY creadoEn DESC")
    fun observarTodos(): Flow<List<VehiculoEntity>>

    @Query("SELECT * FROM vehiculos WHERE id = :id")
    fun observarPorId(id: Long): Flow<VehiculoEntity?>

    @Query("SELECT * FROM vehiculos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): VehiculoEntity?

    /** RF-04: vehículo principal; si ninguno está marcado, el más antiguo activo. */
    @Query("SELECT * FROM vehiculos WHERE archivado = 0 ORDER BY esPrincipal DESC, creadoEn ASC LIMIT 1")
    fun observarPrincipal(): Flow<VehiculoEntity?>

    @Query("SELECT * FROM vehiculos WHERE archivado = 0 ORDER BY esPrincipal DESC, creadoEn ASC LIMIT 1")
    suspend fun obtenerPrincipal(): VehiculoEntity?

    @Query("SELECT COUNT(*) FROM vehiculos WHERE archivado = 0")
    suspend fun contarActivos(): Int

    @Query("SELECT COUNT(*) FROM vehiculos WHERE placa = :placa")
    suspend fun contarPorPlaca(placa: String): Int

    /** RF-05: placas iguales en otro vehículo distinto del que se edita. */
    @Query("SELECT COUNT(*) FROM vehiculos WHERE placa = :placa AND id != :excluirId")
    suspend fun contarPorPlacaExcluyendo(placa: String, excluirId: Long): Int

    @Query("UPDATE vehiculos SET esPrincipal = 0 WHERE esPrincipal = 1")
    suspend fun quitarPrincipal()

    @Query("UPDATE vehiculos SET esPrincipal = 1 WHERE id = :id")
    suspend fun marcarPrincipal(id: Long)

    @Query("SELECT * FROM vehiculos WHERE archivado = 1 ORDER BY marca ASC")
    fun observarArchivados(): Flow<List<VehiculoEntity>>

    /** Archiva o restaura un vehículo; un vehículo archivado deja de ser principal. */
    @Query("UPDATE vehiculos SET archivado = :archivado, esPrincipal = CASE WHEN :archivado THEN 0 ELSE esPrincipal END WHERE id = :id")
    suspend fun cambiarArchivado(id: Long, archivado: Boolean)

    /** RF-06: el odómetro del vehículo refleja siempre la lectura más alta registrada. */
    @Query("UPDATE vehiculos SET kilometrajeActual = :km WHERE id = :id")
    suspend fun actualizarKilometraje(id: Long, km: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(vehiculo: VehiculoEntity): Long

    @Update
    suspend fun actualizar(vehiculo: VehiculoEntity)

    @Delete
    suspend fun eliminar(vehiculo: VehiculoEntity)
}
