package com.example.carro.domain.model

/**
 * Tipos de vehículo soportados (RF-01).
 * El nombre del enum es el valor guardado en Room; no se debe renombrar.
 * [ejemploPlaca] es el formato que se muestra como guía en el formulario.
 */
enum class TipoVehiculo(val etiqueta: String, val ejemploPlaca: String) {
    AUTOMOVIL("Automóvil", "ABC123"),
    CAMIONETA("Camioneta", "ABC123"),
    CAMPERO("Campero", "ABC123"),
    MOTOCICLETA("Moto", "ABC12D"),
    CAMION("Camión", "ABC123"),
    OTRO("Otro", "ABC123");

    val esMoto: Boolean get() = this == MOTOCICLETA

    companion object {
        fun desde(valor: String): TipoVehiculo =
            entries.firstOrNull { it.name == valor } ?: AUTOMOVIL
    }
}

/** Estado de una actividad del plan de mantenimiento (RF-14, RN-01, RN-02). */
enum class EstadoActividad { AL_DIA, PROXIMA, VENCIDA, SIN_PROGRAMACION }

/** Naturaleza del mantenimiento registrado (RF-17). */
enum class TipoMantenimiento { PREVENTIVO, CORRECTIVO }

/** Tipos de documentos legales con vencimiento (RF-32). */
enum class TipoDocumento(val etiqueta: String) {
    SOAT("SOAT"),
    REVISION_TECNICO_MECANICA("Revisión técnico-mecánica"),
    SEGURO("Seguro"),
    OTRO("Otro");

    companion object {
        fun desde(valor: String): TipoDocumento =
            entries.firstOrNull { it.name == valor } ?: OTRO
    }
}

/** Origen de una alerta local (RF-28, RF-33). */
enum class TipoAlerta { MANTENIMIENTO, DOCUMENTO }

/** Tipo de cita de la agenda del vehículo. */
enum class TipoCita(val etiqueta: String) {
    TALLER("Taller"),
    TECNOMECANICA("Tecnomecánica"),
    SOAT("SOAT"),
    SEGURO("Seguro"),
    MANTENIMIENTO("Mantenimiento"),
    OTRO("Otro");

    companion object {
        fun desde(valor: String): TipoCita =
            entries.firstOrNull { it.name == valor } ?: OTRO
    }
}

/** Anticipaciones de recordatorio que el usuario puede elegir (en días). */
object Anticipaciones {
    val opcionesDias = listOf(1, 3, 7, 15, 30)
}
