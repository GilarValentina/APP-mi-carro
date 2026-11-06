package com.example.carro.domain.vehiculo

/** Campos del formulario de vehículo que pueden tener error. */
enum class CampoVehiculo { PLACA, MARCA, LINEA, ANIO, KILOMETRAJE, VIN }

/** Resultado de validar un vehículo: un mensaje por campo con error. */
data class ResultadoValidacionVehiculo(
    val errores: Map<CampoVehiculo, String> = emptyMap()
) {
    val esValido: Boolean get() = errores.isEmpty()
}

/**
 * Validaciones del registro de vehículos (RF-01, RF-02, RF-05, RNF-16).
 * Funciones puras para facilitar las pruebas unitarias (RNF-13).
 */
object VehiculoValidator {

    const val ANIO_MINIMO = 1950
    private val PLACA_AUTOMOVIL = Regex("^[A-Z]{3}[0-9]{3}$")
    private val PLACA_MOTO = Regex("^[A-Z]{3}[0-9]{2}[A-Z]$")
    private val VIN = Regex("^[A-HJ-NPR-Z0-9]{17}$")

    /**
     * Normaliza la placa para compararla y guardarla siempre igual:
     * mayúsculas, sin espacios ni guiones.
     */
    fun normalizarPlaca(placa: String): String =
        placa.uppercase().replace("-", "").filter { it.isLetterOrDigit() }

    /** Validación inmediata mientras se escribe: indica si la placa ya tiene un formato válido. */
    fun placaValida(placa: String, tipo: com.example.carro.domain.model.TipoVehiculo): Boolean {
        val normalizada = normalizarPlaca(placa)
        return if (tipo.esMoto) PLACA_MOTO.matches(normalizada) else PLACA_AUTOMOVIL.matches(normalizada)
    }

    /**
     * Restringe lo que se puede escribir en la placa al patrón del tipo:
     * tres letras, luego números y, solo en motos, una letra final.
     */
    fun filtrarEntradaPlaca(entrada: String, tipo: com.example.carro.domain.model.TipoVehiculo): String {
        val limpio = normalizarPlaca(entrada)
        val resultado = StringBuilder()
        for (c in limpio) {
            val i = resultado.length
            val aceptado = when {
                i < 3 -> c.isLetter()
                i < 5 -> c.isDigit()
                i == 5 -> if (tipo.esMoto) c.isLetter() else c.isDigit()
                else -> false
            }
            if (aceptado) resultado.append(c) else break
        }
        return resultado.toString()
    }

    /**
     * @param placaDuplicada si ya existe otro vehículo con la misma placa normalizada (RF-05).
     * @param anioActual año de referencia; se admite hasta el año siguiente (modelos nuevos).
     */
    fun validar(
        vehiculo: Vehiculo,
        placaDuplicada: Boolean,
        anioActual: Int
    ): ResultadoValidacionVehiculo {
        val errores = mutableMapOf<CampoVehiculo, String>()
        val placa = normalizarPlaca(vehiculo.placa)
        val patronPlaca = if (vehiculo.tipo.esMoto) PLACA_MOTO else PLACA_AUTOMOVIL

        when {
            placa.isEmpty() -> errores[CampoVehiculo.PLACA] = "La placa es obligatoria."
            !patronPlaca.matches(placa) -> {
                errores[CampoVehiculo.PLACA] =
                    "La placa no coincide con el formato requerido para ${vehiculo.tipo.etiqueta.lowercase()}. " +
                        "Ejemplo: ${vehiculo.tipo.ejemploPlaca}."
            }
            placaDuplicada -> errores[CampoVehiculo.PLACA] = "Ya existe un vehículo con la placa $placa."
        }
        if (vehiculo.marca.isBlank()) errores[CampoVehiculo.MARCA] = "La marca es obligatoria."
        if (vehiculo.linea.isBlank()) errores[CampoVehiculo.LINEA] = "La línea es obligatoria."
        if (vehiculo.anio !in ANIO_MINIMO..anioActual + 1) {
            errores[CampoVehiculo.ANIO] = "El modelo debe estar entre $ANIO_MINIMO y ${anioActual + 1}."
        }
        if (vehiculo.kilometrajeActual < 0) {
            errores[CampoVehiculo.KILOMETRAJE] = "Ingresa el kilometraje actual (0 o más)."
        }
        vehiculo.vin?.takeIf { it.isNotBlank() }?.let { vin ->
            if (!VIN.matches(vin.uppercase())) {
                errores[CampoVehiculo.VIN] = "El VIN debe tener 17 caracteres (sin I, O ni Q)."
            }
        }
        return ResultadoValidacionVehiculo(errores)
    }
}
