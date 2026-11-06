package com.example.carro.data.repository

import android.content.Context
import com.example.carro.core.alertas.ProgramadorCitas
import com.example.carro.data.dao.CitaDao
import com.example.carro.data.entity.CitaEntity
import com.example.carro.domain.agenda.Cita
import com.example.carro.domain.model.TipoCita
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

/** Repositorio de la agenda; programa o cancela el recordatorio local de cada cita. */
class CitasRepository(
    private val context: Context,
    private val citaDao: CitaDao
) {

    fun observarCitas(vehiculoId: Long): Flow<List<Cita>> =
        citaDao.observarPorVehiculo(vehiculoId).map { lista -> lista.map { it.toDomain() } }

    fun observarPendientes(): Flow<List<Cita>> =
        citaDao.observarPendientes().map { lista -> lista.map { it.toDomain() } }

    suspend fun obtenerPendientes(): List<Cita> = citaDao.obtenerPendientes().map { it.toDomain() }

    suspend fun obtener(id: Long): Cita? = citaDao.obtenerPorId(id)?.toDomain()

    suspend fun guardar(cita: Cita): Long {
        val id = if (cita.id == 0L) {
            citaDao.insertar(cita.toEntity())
        } else {
            citaDao.actualizar(cita.toEntity())
            cita.id
        }
        val guardada = cita.copy(id = id)
        if (guardada.completada) ProgramadorCitas.cancelar(context, id) else ProgramadorCitas.programar(context, guardada)
        return id
    }

    suspend fun marcarCompletada(cita: Cita, completada: Boolean) {
        guardar(cita.copy(completada = completada))
    }

    suspend fun eliminar(cita: Cita) {
        ProgramadorCitas.cancelar(context, cita.id)
        citaDao.eliminar(cita.toEntity())
    }
}

private fun CitaEntity.toDomain() = Cita(
    id = id,
    vehiculoId = vehiculoId,
    tipo = TipoCita.desde(tipo),
    titulo = titulo,
    lugar = lugar,
    fecha = fecha,
    hora = LocalTime.of(minutoDelDia / 60, minutoDelDia % 60),
    notas = notas,
    anticipacionDias = anticipacionDias,
    completada = completada
)

private fun Cita.toEntity() = CitaEntity(
    id = id,
    vehiculoId = vehiculoId,
    tipo = tipo.name,
    titulo = titulo.trim(),
    lugar = lugar?.trim()?.ifBlank { null },
    fecha = fecha,
    minutoDelDia = hora.hour * 60 + hora.minute,
    notas = notas?.trim()?.ifBlank { null },
    anticipacionDias = anticipacionDias,
    completada = completada
)
