package com.example.carro.domain.agenda

import com.example.carro.domain.model.TipoCita
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Cita de la agenda del vehículo. La fecha y la hora siempre vienen de selectores. */
data class Cita(
    val id: Long = 0,
    val vehiculoId: Long,
    val tipo: TipoCita,
    val titulo: String,
    val lugar: String? = null,
    val fecha: LocalDate,
    val hora: LocalTime,
    val notas: String? = null,
    val anticipacionDias: Int = 1,
    val completada: Boolean = false
) {
    val momento: LocalDateTime get() = LocalDateTime.of(fecha, hora)

    /** Momento del recordatorio: N días antes a la misma hora de la cita. */
    val momentoRecordatorio: LocalDateTime get() = momento.minusDays(anticipacionDias.toLong())
}

/** Validación pura de una cita (RNF-16). */
object CitaValidator {
    fun validar(cita: Cita, ahora: LocalDateTime): String? = when {
        cita.titulo.isBlank() -> "Escribe un título para la cita."
        !cita.completada && cita.momento.isBefore(ahora) -> "La cita debe programarse en una fecha y hora futuras."
        else -> null
    }
}
