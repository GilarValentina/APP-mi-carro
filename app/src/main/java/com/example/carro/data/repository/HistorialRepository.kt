package com.example.carro.data.repository

import com.example.carro.data.dao.ActividadDao
import com.example.carro.data.dao.CategoriaDao
import com.example.carro.data.dao.MantenimientoDao
import com.example.carro.data.dao.RepuestoDao
import com.example.carro.data.entity.MantenimientoEntity
import com.example.carro.domain.historial.CATEGORIA_SIN_ASIGNAR
import com.example.carro.domain.historial.EntradaHistorial
import com.example.carro.domain.model.TipoMantenimiento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Repositorio de consultas del historial y los costos (RF-34 a RF-38).
 *
 * Solo lee: compone los mantenimientos con sus repuestos y con la categoría de la
 * actividad del plan a la que pertenecen, de modo que la capa de presentación
 * reciba entradas ya listas para filtrar y totalizar.
 */
class HistorialRepository(
    private val mantenimientoDao: MantenimientoDao,
    private val repuestoDao: RepuestoDao,
    private val actividadDao: ActividadDao,
    private val categoriaDao: CategoriaDao
) {

    /** RF-34: historial cronológico completo de un vehículo. */
    fun observarHistorial(vehiculoId: Long): Flow<List<EntradaHistorial>> =
        combine(
            mantenimientoDao.observarPorVehiculo(vehiculoId),
            repuestoDao.observarPorVehiculo(vehiculoId),
            actividadDao.observarPorVehiculo(vehiculoId),
            categoriaDao.observarTodas()
        ) { mantenimientos, repuestos, actividades, categorias ->
            val conteoRepuestos = repuestos.groupingBy { it.mantenimientoId }.eachCount()
            val nombreCategoria = categorias.associate { it.id to it.nombre }
            val actividadPorId = actividades.associateBy { it.id }
            mantenimientos.map { entity ->
                val actividad = entity.actividadId?.let { actividadPorId[it] }
                entity.aEntrada(
                    categoria = actividad?.categoriaId?.let { nombreCategoria[it] } ?: CATEGORIA_SIN_ASIGNAR,
                    actividad = actividad?.nombre,
                    cantidadRepuestos = conteoRepuestos[entity.id] ?: 0
                )
            }
        }

    /** RF-37: últimos mantenimientos registrados en cualquier vehículo. */
    fun observarRecientes(limite: Int = 5): Flow<List<EntradaHistorial>> =
        mantenimientoDao.observarRecientes(limite).map { lista ->
            lista.map { it.aEntrada() }
        }
}

private fun MantenimientoEntity.aEntrada(
    categoria: String = CATEGORIA_SIN_ASIGNAR,
    actividad: String? = null,
    cantidadRepuestos: Int = 0
): EntradaHistorial = EntradaHistorial(
    mantenimientoId = id,
    vehiculoId = vehiculoId,
    fecha = fecha,
    kilometraje = kilometraje,
    tipo = runCatching { TipoMantenimiento.valueOf(tipo) }.getOrDefault(TipoMantenimiento.PREVENTIVO),
    descripcion = descripcion,
    categoria = categoria,
    actividad = actividad,
    taller = tallerResponsable,
    costoManoObra = costoManoObra,
    costoRepuestos = costoRepuestos,
    costoTotal = costoTotal,
    cantidadRepuestos = cantidadRepuestos
)
