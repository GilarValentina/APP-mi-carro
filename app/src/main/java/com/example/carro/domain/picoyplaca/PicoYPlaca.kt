package com.example.carro.domain.picoyplaca

import com.example.carro.domain.model.TipoVehiculo
import java.time.DayOfWeek
import java.time.LocalDate

/** Resultado de consultar el pico y placa de un vehículo para una fecha. */
data class EstadoPicoYPlaca(
    val ciudad: String,
    val fecha: LocalDate,
    /** null cuando no hay información de la ciudad. */
    val tieneRestriccion: Boolean?,
    val digitosRestringidos: Set<Int>,
    val horario: String?,
    val nota: String
)

/**
 * Fuente de reglas de pico y placa. La app trae reglas locales de referencia;
 * en el futuro puede reemplazarse por una implementación que consulte un servicio
 * en línea por ciudad sin cambiar las pantallas.
 */
interface ProveedorPicoYPlaca {
    fun ciudadesSoportadas(): List<String>
    fun consultar(ciudad: String, placa: String, tipo: TipoVehiculo, fecha: LocalDate): EstadoPicoYPlaca
}

/** Regla de una ciudad: qué últimos dígitos tienen restricción en una fecha dada. */
private class ReglaCiudad(
    val horario: String,
    val aplicaMotos: Boolean,
    val digitos: (LocalDate) -> Set<Int>
)

/**
 * Reglas locales **de referencia** (no oficiales). Los municipios cambian la rotación
 * periódicamente, por eso la pantalla siempre recuerda verificar con la secretaría de tránsito.
 */
object ReglasLocalesPicoYPlaca : ProveedorPicoYPlaca {

    private const val NOTA_REFERENCIA =
        "Información de referencia. Verifica la regla vigente con la secretaría de tránsito de tu ciudad."

    private fun porDiaSemana(vararg pares: Pair<DayOfWeek, Set<Int>>): (LocalDate) -> Set<Int> {
        val mapa = pares.toMap()
        return { fecha -> mapa[fecha.dayOfWeek].orEmpty() }
    }

    private val reglas: Map<String, ReglaCiudad> = mapOf(
        // Bogotá: esquema par/impar según el día del mes.
        "Bogotá" to ReglaCiudad("6:00 a. m. – 9:00 p. m.", aplicaMotos = false) { fecha ->
            if (fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY) emptySet()
            else if (fecha.dayOfMonth % 2 == 0) setOf(6, 7, 8, 9, 0) else setOf(1, 2, 3, 4, 5)
        },
        "Medellín" to ReglaCiudad(
            "5:00 – 8:00 a. m. y 5:00 – 7:00 p. m.", aplicaMotos = true,
            porDiaSemana(
                DayOfWeek.MONDAY to setOf(1, 7), DayOfWeek.TUESDAY to setOf(0, 3),
                DayOfWeek.WEDNESDAY to setOf(4, 6), DayOfWeek.THURSDAY to setOf(5, 9),
                DayOfWeek.FRIDAY to setOf(2, 8)
            )
        ),
        "Cali" to ReglaCiudad(
            "6:00 – 10:00 a. m. y 5:00 – 8:00 p. m.", aplicaMotos = false,
            porDiaSemana(
                DayOfWeek.MONDAY to setOf(1, 2), DayOfWeek.TUESDAY to setOf(3, 4),
                DayOfWeek.WEDNESDAY to setOf(5, 6), DayOfWeek.THURSDAY to setOf(7, 8),
                DayOfWeek.FRIDAY to setOf(9, 0)
            )
        ),
        "Bucaramanga" to ReglaCiudad(
            "6:00 a. m. – 8:00 p. m.", aplicaMotos = false,
            porDiaSemana(
                DayOfWeek.MONDAY to setOf(1, 2), DayOfWeek.TUESDAY to setOf(3, 4),
                DayOfWeek.WEDNESDAY to setOf(5, 6), DayOfWeek.THURSDAY to setOf(7, 8),
                DayOfWeek.FRIDAY to setOf(9, 0)
            )
        ),
        "Pereira" to ReglaCiudad(
            "7:00 – 9:00 a. m. y 5:30 – 7:30 p. m.", aplicaMotos = false,
            porDiaSemana(
                DayOfWeek.MONDAY to setOf(1, 2), DayOfWeek.TUESDAY to setOf(3, 4),
                DayOfWeek.WEDNESDAY to setOf(5, 6), DayOfWeek.THURSDAY to setOf(7, 8),
                DayOfWeek.FRIDAY to setOf(9, 0)
            )
        ),
        "Cartagena" to ReglaCiudad(
            "7:00 – 9:00 a. m., 12:00 – 2:00 p. m. y 5:00 – 7:00 p. m.", aplicaMotos = false,
            porDiaSemana(
                DayOfWeek.MONDAY to setOf(1, 2), DayOfWeek.TUESDAY to setOf(3, 4),
                DayOfWeek.WEDNESDAY to setOf(5, 6), DayOfWeek.THURSDAY to setOf(7, 8),
                DayOfWeek.FRIDAY to setOf(9, 0)
            )
        )
    )

    override fun ciudadesSoportadas(): List<String> = reglas.keys.toList()

    override fun consultar(
        ciudad: String,
        placa: String,
        tipo: TipoVehiculo,
        fecha: LocalDate
    ): EstadoPicoYPlaca {
        val regla = reglas[ciudad.trim()]
        val digito = ultimoDigito(placa)
        if (regla == null || digito == null) {
            return EstadoPicoYPlaca(
                ciudad = ciudad,
                fecha = fecha,
                tieneRestriccion = null,
                digitosRestringidos = emptySet(),
                horario = null,
                nota = if (regla == null) {
                    "Aún no tenemos información de pico y placa para $ciudad."
                } else {
                    "Registra una placa válida para calcular el pico y placa."
                }
            )
        }
        val digitos = if (tipo.esMoto && !regla.aplicaMotos) emptySet() else regla.digitos(fecha)
        return EstadoPicoYPlaca(
            ciudad = ciudad,
            fecha = fecha,
            tieneRestriccion = digito in digitos,
            digitosRestringidos = digitos,
            horario = regla.horario,
            nota = NOTA_REFERENCIA
        )
    }

    /** Las placas de moto terminan en letra: se toma el último dígito numérico. */
    fun ultimoDigito(placa: String): Int? = placa.lastOrNull { it.isDigit() }?.digitToInt()
}
