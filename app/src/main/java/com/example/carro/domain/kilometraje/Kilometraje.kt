package com.example.carro.domain.kilometraje

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Lectura del odómetro (RF-06, RF-07). */
data class LecturaKilometraje(
    val id: Long = 0,
    val vehiculoId: Long,
    val fecha: LocalDate,
    val valor: Long
)

/** Estadísticas del uso del vehículo calculadas a partir del historial (RF-08, RF-09). */
data class EstadisticasKilometraje(
    val actual: Long,
    val ultimaActualizacion: LocalDate?,
    val totalRecorrido: Long,
    /** Promedio de km por día entre la primera y la última lectura; null si no hay suficientes datos. */
    val promedioDiario: Double?,
    val promedioMensual: Double?,
    val diasSinActualizar: Long?
) {
    /** RF-09: se sugiere actualizar el odómetro si pasan más de [DIAS_RECORDATORIO] días. */
    val requiereActualizacion: Boolean get() = (diasSinActualizar ?: 0) > DIAS_RECORDATORIO

    companion object {
        const val DIAS_RECORDATORIO = 15
    }
}

/** Cálculos puros del módulo de kilometraje (RNF-13). */
object KilometrajeCalculator {

    fun estadisticas(lecturas: List<LecturaKilometraje>, hoy: LocalDate): EstadisticasKilometraje {
        if (lecturas.isEmpty()) {
            return EstadisticasKilometraje(0, null, 0, null, null, null)
        }
        val ordenadas = lecturas.sortedWith(compareBy({ it.fecha }, { it.valor }))
        val primera = ordenadas.first()
        val ultima = ordenadas.last()
        val maximo = ordenadas.maxOf { it.valor }
        val dias = ChronoUnit.DAYS.between(primera.fecha, ultima.fecha)
        val recorrido = maximo - primera.valor
        val promedioDiario = if (dias > 0) recorrido.toDouble() / dias else null
        return EstadisticasKilometraje(
            actual = maximo,
            ultimaActualizacion = ultima.fecha,
            totalRecorrido = recorrido,
            promedioDiario = promedioDiario,
            promedioMensual = promedioDiario?.times(30),
            diasSinActualizar = ChronoUnit.DAYS.between(ultima.fecha, hoy)
        )
    }
}

/**
 * Validación de una nueva lectura (RF-06, RN-06):
 * - no puede ser negativa ni con fecha futura;
 * - no puede ser menor que una lectura anterior o igual de fecha;
 * - no puede ser mayor que una lectura posterior (registros retroactivos).
 */
object KilometrajeValidator {

    const val SALTO_MAXIMO_DIARIO = 2_000L

    fun validar(
        valor: Long?,
        fecha: LocalDate,
        existentes: List<LecturaKilometraje>,
        hoy: LocalDate
    ): String? {
        if (valor == null) return "Ingresa el kilometraje del odómetro."
        if (valor < 0) return "El kilometraje no puede ser negativo."
        if (fecha.isAfter(hoy)) return "La fecha no puede ser futura."
        val anterior = existentes.filter { !it.fecha.isAfter(fecha) }.maxByOrNull { it.valor }
        if (anterior != null && valor < anterior.valor) {
            return "Debe ser mayor o igual a ${anterior.valor} km (lectura del ${anterior.fecha})."
        }
        val posterior = existentes.filter { it.fecha.isAfter(fecha) }.minByOrNull { it.valor }
        if (posterior != null && valor > posterior.valor) {
            return "Debe ser menor o igual a ${posterior.valor} km (lectura posterior del ${posterior.fecha})."
        }
        if (anterior != null) {
            val dias = ChronoUnit.DAYS.between(anterior.fecha, fecha).coerceAtLeast(1)
            if (valor - anterior.valor > SALTO_MAXIMO_DIARIO * dias) {
                return "El aumento parece demasiado alto (${valor - anterior.valor} km en $dias día(s)). Revisa el valor."
            }
        }
        return null
    }
}
