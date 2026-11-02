package com.example.carro.domain.historial

import java.time.LocalDate

/**
 * Cálculo del resumen de gastos por vehículo, categoría y periodo (RF-36).
 * Funciones puras para poder probarlas sin Android (RNF-13).
 */
object ResumenCalculator {

    private val MESES = listOf(
        "ene", "feb", "mar", "abr", "may", "jun",
        "jul", "ago", "sep", "oct", "nov", "dic"
    )

    /** Entradas que caen dentro del periodo, tomando [hoy] como referencia. */
    fun enPeriodo(
        entradas: List<EntradaHistorial>,
        periodo: PeriodoResumen,
        hoy: LocalDate = LocalDate.now()
    ): List<EntradaHistorial> {
        val meses = periodo.meses ?: return entradas
        val limite = hoy.minusMonths(meses)
        return entradas.filter { !it.fecha.isBefore(limite) }
    }

    /**
     * Calcula los totales del periodo: gasto acumulado, promedio, desglose por categoría
     * y evolución mensual (RF-36).
     */
    fun calcular(
        entradas: List<EntradaHistorial>,
        periodo: PeriodoResumen = PeriodoResumen.TODO,
        hoy: LocalDate = LocalDate.now()
    ): ResumenGastos {
        val delPeriodo = enPeriodo(entradas, periodo, hoy)
        if (delPeriodo.isEmpty()) return ResumenGastos(periodo = periodo)

        val total = delPeriodo.sumOf { it.costoTotal }
        return ResumenGastos(
            periodo = periodo,
            total = total,
            totalManoObra = delPeriodo.sumOf { it.costoManoObra },
            totalRepuestos = delPeriodo.sumOf { it.costoRepuestos },
            cantidadMantenimientos = delPeriodo.size,
            promedioPorMantenimiento = total / delPeriodo.size,
            porCategoria = agrupar(delPeriodo) { it.categoria }
                .sortedByDescending { it.total },
            porMes = agrupar(delPeriodo) { etiquetaMes(it.fecha) }
                .sortedByDescending { it.etiqueta },
            masCostoso = delPeriodo.maxByOrNull { it.costoTotal }
        )
    }

    private fun agrupar(
        entradas: List<EntradaHistorial>,
        clave: (EntradaHistorial) -> String
    ): List<GastoAgrupado> =
        entradas.groupBy(clave).map { (etiqueta, lista) ->
            GastoAgrupado(
                etiqueta = etiqueta,
                total = lista.sumOf { it.costoTotal },
                cantidad = lista.size
            )
        }

    /** Etiqueta ordenable y legible del mes, por ejemplo "2025-03 · mar 2025". */
    fun etiquetaMes(fecha: LocalDate): String =
        "%04d-%02d · %s %d".format(fecha.year, fecha.monthValue, MESES[fecha.monthValue - 1], fecha.year)

    /** Porcentaje que representa un grupo frente al total, entre 0 y 1. */
    fun proporcion(grupo: GastoAgrupado, total: Double): Float =
        if (total <= 0.0) 0f else (grupo.total / total).toFloat().coerceIn(0f, 1f)
}
