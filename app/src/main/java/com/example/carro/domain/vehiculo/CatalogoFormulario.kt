package com.example.carro.domain.vehiculo

import com.example.carro.domain.model.TipoVehiculo

/** Color seleccionable en el formulario: nombre guardado y color de muestra (ARGB). */
data class ColorVehiculo(val nombre: String, val argb: Long)

/**
 * Opciones de los selectores del formulario de vehículos.
 * El objetivo es que el usuario elija en lugar de escribir y así evitar errores (RNF-16).
 */
object CatalogoFormulario {

    const val ANIO_MINIMO = VehiculoValidator.ANIO_MINIMO

    /** Años disponibles, del más reciente (año siguiente, modelos nuevos) al más antiguo. */
    fun anios(anioActual: Int): List<Int> = ((anioActual + 1) downTo ANIO_MINIMO).toList()

    val combustibles = listOf("Gasolina", "Diésel", "Gas", "Híbrido", "Eléctrico")

    val colores = listOf(
        ColorVehiculo("Blanco", 0xFFFFFFFF),
        ColorVehiculo("Negro", 0xFF1B1B1B),
        ColorVehiculo("Gris", 0xFF8A8F98),
        ColorVehiculo("Plateado", 0xFFC9CED6),
        ColorVehiculo("Rojo", 0xFFD32F2F),
        ColorVehiculo("Azul", 0xFF1E5AA8),
        ColorVehiculo("Verde", 0xFF2E7D32),
        ColorVehiculo("Amarillo", 0xFFF9C80E),
        ColorVehiculo("Naranja", 0xFFF57C00),
        ColorVehiculo("Café", 0xFF6D4C41),
        ColorVehiculo("Beige", 0xFFD7C4A3),
        ColorVehiculo("Vinotinto", 0xFF7B1E3A)
    )

    private val cilindrajesMoto = listOf(
        "100", "110", "125", "150", "160", "180", "200", "250", "300", "400", "650", "800", "1000", "1200"
    )
    private val cilindrajesCarro = listOf(
        "800", "1000", "1200", "1300", "1400", "1500", "1600", "1800", "2000", "2200", "2400",
        "2500", "2700", "3000", "3500", "4000", "5000"
    )
    private val cilindrajesCamion = listOf("2800", "3000", "4000", "5000", "6000", "7000", "9000", "12000")

    /** Cilindrajes comunes (cc) según el tipo; "Eléctrico" no aplica cilindraje. */
    fun cilindrajes(tipo: TipoVehiculo): List<String> = when (tipo) {
        TipoVehiculo.MOTOCICLETA -> cilindrajesMoto
        TipoVehiculo.CAMION -> cilindrajesCamion
        else -> cilindrajesCarro
    }

    /** Ciudades principales de Colombia para matrícula y pico y placa. */
    val ciudades = listOf(
        "Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena", "Bucaramanga", "Pereira",
        "Manizales", "Cúcuta", "Ibagué", "Villavicencio", "Pasto", "Armenia", "Santa Marta",
        "Neiva", "Tunja", "Popayán", "Montería", "Valledupar", "Sincelejo", "Chía", "Envigado",
        "Itagüí", "Bello", "Soacha", "Floridablanca", "Palmira", "Otra"
    )

    private val secretariasEspecificas = mapOf(
        "Bogotá" to listOf("Secretaría Distrital de Movilidad de Bogotá"),
        "Medellín" to listOf("Secretaría de Movilidad de Medellín"),
        "Cali" to listOf("Secretaría de Movilidad de Santiago de Cali"),
        "Barranquilla" to listOf("Secretaría Distrital de Tránsito y Seguridad Vial de Barranquilla"),
        "Cartagena" to listOf("Departamento Administrativo de Tránsito y Transporte (DATT) de Cartagena"),
        "Bucaramanga" to listOf("Dirección de Tránsito de Bucaramanga"),
        "Chía" to listOf("Secretaría de Movilidad de Chía", "Sede Operativa Departamental de Cundinamarca"),
        "Soacha" to listOf("Secretaría de Movilidad de Soacha", "Sede Operativa Departamental de Cundinamarca"),
        "Envigado" to listOf("Secretaría de Movilidad de Envigado"),
        "Itagüí" to listOf("Secretaría de Movilidad de Itagüí"),
        "Bello" to listOf("Secretaría de Movilidad de Bello"),
        "Floridablanca" to listOf("Dirección de Tránsito y Transporte de Floridablanca")
    )

    /** Secretarías de tránsito disponibles para la ciudad de matrícula. */
    fun secretarias(ciudad: String?): List<String> {
        if (ciudad.isNullOrBlank()) return emptyList()
        val especificas = secretariasEspecificas[ciudad]
        return when {
            especificas != null -> especificas + "Otra"
            ciudad == "Otra" -> listOf("Otra")
            else -> listOf("Secretaría de Tránsito y Transporte de $ciudad", "Otra")
        }
    }
}
