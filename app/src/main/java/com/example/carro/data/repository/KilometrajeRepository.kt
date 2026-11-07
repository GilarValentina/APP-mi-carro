package com.example.carro.data.repository

import androidx.room.withTransaction
import com.example.carro.core.database.MiCarroDatabase
import com.example.carro.data.dao.KilometrajeDao
import com.example.carro.data.dao.VehiculoDao
import com.example.carro.data.entity.LecturaKilometrajeEntity
import com.example.carro.domain.kilometraje.KilometrajeValidator
import com.example.carro.domain.kilometraje.LecturaKilometraje
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Resultado de registrar una lectura de kilometraje. */
sealed interface ResultadoLectura {
    data object Guardada : ResultadoLectura
    data class Invalida(val mensaje: String) : ResultadoLectura
}

/**
 * Repositorio del módulo de kilometraje (RF-06 a RF-09).
 * Al registrar o borrar una lectura recalcula el odómetro del vehículo, del que dependen
 * el plan de mantenimiento y las barras de progreso.
 */
class KilometrajeRepository(
    private val database: MiCarroDatabase,
    private val kilometrajeDao: KilometrajeDao,
    private val vehiculoDao: VehiculoDao
) {

    fun observarLecturas(vehiculoId: Long): Flow<List<LecturaKilometraje>> =
        kilometrajeDao.observarPorVehiculo(vehiculoId).map { lista -> lista.map { it.toDomain() } }

    suspend fun registrar(
        vehiculoId: Long,
        valor: Long?,
        fecha: LocalDate,
        hoy: LocalDate = LocalDate.now()
    ): ResultadoLectura {
        val existentes = kilometrajeDao.obtenerPorVehiculo(vehiculoId).map { it.toDomain() }
        KilometrajeValidator.validar(valor, fecha, existentes, hoy)?.let { return ResultadoLectura.Invalida(it) }
        database.withTransaction {
            kilometrajeDao.insertar(
                LecturaKilometrajeEntity(vehiculoId = vehiculoId, fecha = fecha, valorOdometro = valor!!)
            )
            recalcularOdometro(vehiculoId)
        }
        return ResultadoLectura.Guardada
    }

    suspend fun eliminar(lectura: LecturaKilometraje) {
        database.withTransaction {
            kilometrajeDao.eliminar(lectura.toEntity())
            recalcularOdometro(lectura.vehiculoId)
        }
    }

    private suspend fun recalcularOdometro(vehiculoId: Long) {
        val maximo = kilometrajeDao.obtenerMaximoOdometro(vehiculoId) ?: return
        vehiculoDao.actualizarKilometraje(vehiculoId, maximo)
    }
}

private fun LecturaKilometrajeEntity.toDomain() = LecturaKilometraje(
    id = id,
    vehiculoId = vehiculoId,
    fecha = fecha,
    valor = valorOdometro
)

private fun LecturaKilometraje.toEntity() = LecturaKilometrajeEntity(
    id = id,
    vehiculoId = vehiculoId,
    fecha = fecha,
    valorOdometro = valor
)
