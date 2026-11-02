package com.example.carro.ui.historial

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formatos de presentación compartidos por las pantallas de historial y resumen.
 * La moneda y la fecha siguen la configuración regional del dispositivo (RNF-17).
 */
object Formatos {

    private val formatoFecha: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    private val formatoMoneda: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 0
        }

    fun moneda(valor: Double): String = formatoMoneda.format(valor)

    fun fecha(fecha: LocalDate): String = fecha.format(formatoFecha)

    fun kilometraje(valor: Long): String =
        "${NumberFormat.getIntegerInstance(Locale.getDefault()).format(valor)} km"
}
