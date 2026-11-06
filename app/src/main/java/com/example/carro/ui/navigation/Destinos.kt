package com.example.carro.ui.navigation

/** Rutas de navegación de la app. Cada módulo registra su pantalla en el NavHost. */
object Destinos {
    const val INICIO = "inicio"

    // Módulos (los feature branches conectarán sus pantallas a estas rutas).
    const val VEHICULOS = "vehiculos"
    const val KILOMETRAJE = "kilometraje"
    const val PLAN_MANTENIMIENTO = "plan_mantenimiento"
    const val MANTENIMIENTOS = "mantenimientos"
    const val ALERTAS = "alertas"
    const val HISTORIAL = "historial"
    const val AGENDA = "agenda"
    const val AJUSTES = "ajustes"

    // Guías de ayuda en carretera: una sola ruta parametrizada por el id de la guía.
    const val ARG_GUIA = "guia"
    const val GUIA = "guias/{$ARG_GUIA}"
    fun guia(id: String): String = "guias/$id"

    /** Respaldo, restauración y borrado definitivo de los datos (RF-39, RF-40). */
    const val RESPALDO = "respaldo"

    // Formulario de vehículo: sin id registra uno nuevo, con id lo edita.
    const val ARG_VEHICULO_ID = "vehiculoId"
    const val VEHICULO_FORM = "vehiculos/form?$ARG_VEHICULO_ID={$ARG_VEHICULO_ID}"
    fun vehiculoForm(id: Long? = null): String =
        if (id == null) "vehiculos/form" else "vehiculos/form?$ARG_VEHICULO_ID=$id"
}
