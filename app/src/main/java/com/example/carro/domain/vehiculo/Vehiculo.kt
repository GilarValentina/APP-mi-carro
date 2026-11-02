package com.example.carro.domain.vehiculo

import com.example.carro.domain.model.TipoVehiculo

/**
 * Vehículo registrado por el usuario (RF-01, RF-02).
 * Modelo de dominio independiente de Room (RNF-12).
 *
 * Siguiendo la convención colombiana, la **línea** es el nombre comercial
 * (p. ej. "Spark GT") y el **modelo** es el año del vehículo.
 */
data class Vehiculo(
    val id: Long = 0,
    val placa: String,
    val tipo: TipoVehiculo,
    val marca: String,
    val linea: String,
    val anio: Int,
    val kilometrajeActual: Long,
    val color: String? = null,
    val vin: String? = null,
    val combustible: String? = null,
    val cilindraje: String? = null,
    val ciudadPicoPlaca: String? = null,
    val picoYPlaca: String? = null,
    val esPrincipal: Boolean = false
) {
    /** Nombre corto para mostrar en listas y encabezados. */
    val titulo: String get() = "$marca $linea $anio"
    /** Alias de compatibilidad para la preferencia/favorito del vehículo. */
    val esFavorito: Boolean get() = esPrincipal
}
