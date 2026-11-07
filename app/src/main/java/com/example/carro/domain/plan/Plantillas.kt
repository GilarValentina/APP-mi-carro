package com.example.carro.domain.plan

/**
 * Categorías iniciales del plan de mantenimiento (RF-12).
 * Son editables por el usuario; estas solo siembran la base la primera vez.
 */
object CategoriasPredeterminadas {
    val nombres = listOf(
        "Aceite",
        "Filtros",
        "Frenos",
        "Llantas",
        "Batería",
        "Refrigeración",
        "Suspensión",
        "Transmisión",
        "Otros"
    )
}

/**
 * Plantilla de mantenimiento común para agilizar la creación del plan (RF-16).
 * Los intervalos son sugerencias editables al crear la actividad.
 */
data class PlantillaMantenimiento(
    val nombre: String,
    val categoria: String,
    val intervaloDias: Int? = null,
    val intervaloKm: Long? = null
)

object PlantillasPredeterminadas {
    val lista = listOf(
        PlantillaMantenimiento("Cambio de aceite", "Aceite", intervaloDias = 180, intervaloKm = 5_000),
        PlantillaMantenimiento("Cambio de filtro de aceite", "Filtros", intervaloDias = 180, intervaloKm = 5_000),
        PlantillaMantenimiento("Cambio de filtro de aire", "Filtros", intervaloDias = 365, intervaloKm = 15_000),
        PlantillaMantenimiento("Revisión de frenos", "Frenos", intervaloDias = 365, intervaloKm = 10_000),
        PlantillaMantenimiento("Rotación de llantas", "Llantas", intervaloKm = 10_000),
        PlantillaMantenimiento("Revisión de batería", "Batería", intervaloDias = 365),
        PlantillaMantenimiento("Cambio de refrigerante", "Refrigeración", intervaloDias = 730, intervaloKm = 40_000),
        PlantillaMantenimiento("Revisión de suspensión", "Suspensión", intervaloKm = 20_000),
        PlantillaMantenimiento("Cambio de aceite de transmisión", "Transmisión", intervaloKm = 40_000)
    )
}

/**
 * Plan preventivo base por kilometraje: rutinas cada 5.000, 10.000 y 20.000 km.
 * Se aplica con un toque y cada actividad queda editable como cualquier otra.
 */
object PlanPreventivoBase {
    val lista = listOf(
        PlantillaMantenimiento("Cambio de aceite", "Aceite", intervaloKm = 5_000),
        PlantillaMantenimiento("Cambio de filtro de aceite", "Filtros", intervaloKm = 5_000),
        PlantillaMantenimiento("Revisión de fluidos", "Otros", intervaloKm = 5_000),
        PlantillaMantenimiento("Revisión eléctrica", "Batería", intervaloKm = 5_000),
        PlantillaMantenimiento("Inspección de llantas", "Llantas", intervaloKm = 5_000),
        PlantillaMantenimiento("Cambio de filtro de aire", "Filtros", intervaloKm = 10_000),
        PlantillaMantenimiento("Cambio de filtro de combustible", "Filtros", intervaloKm = 10_000),
        PlantillaMantenimiento("Revisión de frenos", "Frenos", intervaloKm = 10_000),
        PlantillaMantenimiento("Alineación", "Llantas", intervaloKm = 10_000),
        PlantillaMantenimiento("Balanceo", "Llantas", intervaloKm = 10_000),
        PlantillaMantenimiento("Revisión del sistema de escape", "Otros", intervaloKm = 20_000),
        PlantillaMantenimiento("Revisión del sistema de refrigeración", "Refrigeración", intervaloKm = 20_000),
        PlantillaMantenimiento("Cambio de bujías", "Otros", intervaloKm = 20_000),
        PlantillaMantenimiento("Revisión de correas", "Transmisión", intervaloKm = 20_000),
        PlantillaMantenimiento("Revisión de suspensión", "Suspensión", intervaloKm = 20_000)
    )

    /** Margen "próximo" (amarillo): el 20 % final del intervalo. */
    fun anticipacionKm(intervaloKm: Long): Long = intervaloKm / 5

    /**
     * Próximo hito del intervalo según el odómetro: con 12.300 km y un intervalo
     * de 5.000, el siguiente cambio queda a los 15.000 km.
     */
    fun proximoHito(kmActual: Long, intervaloKm: Long): Long =
        (kmActual / intervaloKm + 1) * intervaloKm
}

/** Avance de una actividad programada por kilometraje dentro de su intervalo. */
data class ProgresoKm(
    val recorridoEnIntervalo: Long,
    val intervalo: Long,
    val restantes: Long
) {
    /** Fracción entre 0 y 1 para las barras de progreso. */
    val fraccion: Float get() = (recorridoEnIntervalo.toFloat() / intervalo).coerceIn(0f, 1f)
}

object ProgresoCalculator {
    /** null si la actividad no se programa por kilometraje o le falta información. */
    fun calcular(actividad: Actividad, kmActual: Long): ProgresoKm? {
        val intervalo = actividad.intervaloKm?.takeIf { it > 0 } ?: return null
        val proximo = actividad.proximoKm ?: return null
        if (!actividad.programarPorKm) return null
        val inicio = proximo - intervalo
        return ProgresoKm(
            recorridoEnIntervalo = (kmActual - inicio).coerceAtLeast(0),
            intervalo = intervalo,
            restantes = proximo - kmActual
        )
    }
}
