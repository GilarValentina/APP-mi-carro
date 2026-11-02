package com.example.carro.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.example.carro.core.database.MiCarroDatabase
import com.example.carro.data.dao.KilometrajeDao
import com.example.carro.data.dao.VehiculoDao
import com.example.carro.data.entity.LecturaKilometrajeEntity
import com.example.carro.data.entity.VehiculoEntity
import com.example.carro.domain.model.TipoVehiculo
import com.example.carro.domain.vehiculo.CampoVehiculo
import com.example.carro.domain.vehiculo.ResultadoValidacionVehiculo
import com.example.carro.domain.vehiculo.Vehiculo
import com.example.carro.domain.vehiculo.VehiculoValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Resultado de intentar guardar un vehículo. */
sealed interface ResultadoGuardarVehiculo {
    data class Guardado(val id: Long) : ResultadoGuardarVehiculo
    data class Invalido(val validacion: ResultadoValidacionVehiculo) : ResultadoGuardarVehiculo
}

/**
 * Repositorio de vehículos (RF-01 a RF-05).
 * Es la única fuente del vehículo principal con el que trabajan los demás módulos.
 */
class VehiculoRepository(
    private val database: MiCarroDatabase,
    private val vehiculoDao: VehiculoDao,
    private val kilometrajeDao: KilometrajeDao
) {

    fun observarVehiculos(): Flow<List<Vehiculo>> =
        vehiculoDao.observarActivos().map { lista -> lista.map { it.toDomain() } }

    /** RF-04: vehículo principal, o null si no hay ninguno registrado. */
    fun observarPrincipal(): Flow<Vehiculo?> =
        vehiculoDao.observarPrincipal().map { it?.toDomain() }

    suspend fun obtener(id: Long): Vehiculo? = vehiculoDao.obtenerPorId(id)?.toDomain()

    suspend fun validar(vehiculo: Vehiculo, hoy: LocalDate = LocalDate.now()): ResultadoValidacionVehiculo {
        val placa = VehiculoValidator.normalizarPlaca(vehiculo.placa)
        val duplicada = vehiculoDao.contarPorPlacaExcluyendo(placa, vehiculo.id) > 0
        return VehiculoValidator.validar(vehiculo, duplicada, hoy.year)
    }

    /**
     * RF-01 / RF-02: registra o actualiza un vehículo.
     * - El primer vehículo registrado queda como principal.
     * - Al crear, el odómetro inicial se guarda como primera lectura de kilometraje (RF-06).
     * - Al editar, el kilometraje no se modifica aquí: se actualiza desde el módulo de kilometraje.
     */
    suspend fun guardar(vehiculo: Vehiculo): ResultadoGuardarVehiculo {
        val validacion = validar(vehiculo)
        if (!validacion.esValido) return ResultadoGuardarVehiculo.Invalido(validacion)

        val normalizado = vehiculo.copy(
            placa = VehiculoValidator.normalizarPlaca(vehiculo.placa),
            vin = vehiculo.vin?.trim()?.uppercase()?.ifBlank { null }
        )
        return try {
            val id = database.withTransaction {
                if (normalizado.id == 0L) insertar(normalizado) else actualizar(normalizado)
            }
            ResultadoGuardarVehiculo.Guardado(id)
        } catch (e: SQLiteConstraintException) {
            // Respaldo del índice único por si otra operación insertó la misma placa (RF-05).
            ResultadoGuardarVehiculo.Invalido(
                ResultadoValidacionVehiculo(
                    mapOf(CampoVehiculo.PLACA to "Ya existe un vehículo con la placa ${normalizado.placa}.")
                )
            )
        }
    }

    private suspend fun insertar(vehiculo: Vehiculo): Long {
        val esPrimero = vehiculoDao.contarActivos() == 0
        val id = vehiculoDao.insertar(vehiculo.toEntity().copy(esPrincipal = esPrimero))
        kilometrajeDao.insertar(
            LecturaKilometrajeEntity(
                vehiculoId = id,
                fecha = LocalDate.now(),
                valorOdometro = vehiculo.kilometrajeActual
            )
        )
        return id
    }

    private suspend fun actualizar(vehiculo: Vehiculo): Long {
        val actual = requireNotNull(vehiculoDao.obtenerPorId(vehiculo.id)) {
            "El vehículo ${vehiculo.id} no existe."
        }
        vehiculoDao.actualizar(
            vehiculo.toEntity().copy(
                kilometrajeActual = actual.kilometrajeActual,
                esPrincipal = actual.esPrincipal,
                archivado = actual.archivado,
                fotoUri = actual.fotoUri,
                creadoEn = actual.creadoEn
            )
        )
        return vehiculo.id
    }

    /** RF-04: deja un único vehículo marcado como principal. */
    suspend fun seleccionarPrincipal(id: Long) {
        database.withTransaction {
            vehiculoDao.quitarPrincipal()
            vehiculoDao.marcarPrincipal(id)
        }
    }

    /**
     * RF-03: elimina el vehículo y, en cascada, todo su historial.
     * Si era el principal, el siguiente vehículo más antiguo pasa a serlo.
     */
    suspend fun eliminar(id: Long) {
        database.withTransaction {
            val entity = vehiculoDao.obtenerPorId(id) ?: return@withTransaction
            vehiculoDao.eliminar(entity)
            if (entity.esPrincipal) {
                vehiculoDao.obtenerPrincipal()?.let { vehiculoDao.marcarPrincipal(it.id) }
            }
        }
    }
}

private fun VehiculoEntity.toDomain(): Vehiculo = Vehiculo(
    id = id,
    placa = placa,
    tipo = TipoVehiculo.desde(tipo),
    marca = marca,
    linea = linea,
    anio = anio,
    kilometrajeActual = kilometrajeActual,
    color = color,
    vin = vin,
    combustible = combustible,
    cilindraje = cilindraje,
    ciudadPicoPlaca = ciudadPicoPlaca,
    picoYPlaca = picoYPlaca,
    esPrincipal = esPrincipal
)

// La columna `modelo` guarda el año como texto (convención colombiana: modelo = año).
private fun Vehiculo.toEntity(): VehiculoEntity = VehiculoEntity(
    id = id,
    placa = placa,
    tipo = tipo.name,
    marca = marca.trim(),
    linea = linea.trim(),
    modelo = anio.toString(),
    anio = anio,
    kilometrajeActual = kilometrajeActual,
    color = color?.trim()?.ifBlank { null },
    vin = vin,
    combustible = combustible?.trim()?.ifBlank { null },
    cilindraje = cilindraje?.trim()?.ifBlank { null },
    ciudadPicoPlaca = ciudadPicoPlaca?.trim()?.ifBlank { null },
    picoYPlaca = picoYPlaca?.trim()?.ifBlank { null },
    esPrincipal = esPrincipal
)
