package com.example.carro.domain.historial

import com.example.carro.domain.model.TipoMantenimiento
import java.time.LocalDate

/** Categoría mostrada cuando el mantenimiento no está ligado a una actividad del plan. */
const val CATEGORIA_SIN_ASIGNAR = "Sin categoría"

/**
 * Entrada del historial cronológico de un vehículo (RF-34).
 * Aplana el mantenimiento con los datos que el usuario necesita para consultar y filtrar (RF-35).
 */
data class EntradaHistorial(
    val mantenimientoId: Long,
    val vehiculoId: Long,
    val fecha: LocalDate,
    val kilometraje: Long,
    val tipo: TipoMantenimiento,
    val descripcion: String,
    val categoria: String = CATEGORIA_SIN_ASIGNAR,
    val actividad: String? = null,
    val taller: String? = null,
    val costoManoObra: Double = 0.0,
    val costoRepuestos: Double = 0.0,
    val costoTotal: Double = 0.0,
    val cantidadRepuestos: Int = 0
)

/**
 * Criterios de búsqueda y filtrado del historial (RF-35).
 * Todos los campos son opcionales: un filtro vacío devuelve el historial completo.
 */
data class FiltroHistorial(
    val texto: String = "",
    val desde: LocalDate? = null,
    val hasta: LocalDate? = null,
    val categorias: Set<String> = emptySet(),
    val tipo: TipoMantenimiento? = null,
    val taller: String? = null,
    val costoMinimo: Double? = null,
    val costoMaximo: Double? = null
) {
    /** Indica si hay algún criterio aplicado, para mostrarlo en la interfaz. */
    val activo: Boolean
        get() = texto.isNotBlank() || desde != null || hasta != null ||
            categorias.isNotEmpty() || tipo != null || taller != null ||
            costoMinimo != null || costoMaximo != null

    /** Cantidad de criterios aplicados, para el indicador del botón de filtros. */
    val cantidadCriterios: Int
        get() = listOf(
            texto.isNotBlank(),
            desde != null,
            hasta != null,
            categorias.isNotEmpty(),
            tipo != null,
            taller != null,
            costoMinimo != null || costoMaximo != null
        ).count { it }
}

/** Periodo sobre el que se calcula el resumen de gastos (RF-36). */
enum class PeriodoResumen(val etiqueta: String, val meses: Long?) {
    ULTIMO_MES("Último mes", 1),
    ULTIMOS_3_MESES("Últimos 3 meses", 3),
    ULTIMO_ANIO("Último año", 12),
    TODO("Todo el historial", null)
}

/** Total acumulado de una agrupación del resumen (por categoría o por mes). */
data class GastoAgrupado(
    val etiqueta: String,
    val total: Double,
    val cantidad: Int
)

/**
 * Resumen de gastos de un vehículo en un periodo (RF-36).
 * Los totales se calculan sobre las entradas ya filtradas.
 */
data class ResumenGastos(
    val periodo: PeriodoResumen = PeriodoResumen.TODO,
    val total: Double = 0.0,
    val totalManoObra: Double = 0.0,
    val totalRepuestos: Double = 0.0,
    val cantidadMantenimientos: Int = 0,
    val promedioPorMantenimiento: Double = 0.0,
    val porCategoria: List<GastoAgrupado> = emptyList(),
    val porMes: List<GastoAgrupado> = emptyList(),
    val masCostoso: EntradaHistorial? = null
) {
    val vacio: Boolean get() = cantidadMantenimientos == 0
}
