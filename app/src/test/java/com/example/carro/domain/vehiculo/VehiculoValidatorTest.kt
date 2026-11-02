package com.example.carro.domain.vehiculo

import com.example.carro.domain.model.TipoVehiculo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de validación del registro de vehículos (RNF-13).
 * Cubre campos obligatorios (RF-01), placa duplicada (RF-05) y rangos (RNF-16).
 */
class VehiculoValidatorTest {

    private val anioActual = 2026

    private fun base(
        placa: String = "ABC123",
        marca: String = "Chevrolet",
        linea: String = "Spark GT",
        anio: Int = 2020,
        km: Long = 45_000,
        vin: String? = null
    ) = Vehiculo(
        placa = placa,
        tipo = TipoVehiculo.AUTOMOVIL,
        marca = marca,
        linea = linea,
        anio = anio,
        kilometrajeActual = km,
        vin = vin
    )

    private fun validar(v: Vehiculo, duplicada: Boolean = false) =
        VehiculoValidator.validar(v, duplicada, anioActual)

    @Test
    fun `vehiculo completo es valido`() {
        assertTrue(validar(base()).esValido)
    }

    @Test
    fun `placa se normaliza a mayusculas sin espacios ni guiones`() {
        assertEquals("ABC123", VehiculoValidator.normalizarPlaca(" abc-123 "))
        assertEquals("XYZ12D", VehiculoValidator.normalizarPlaca("xyz 12d"))
    }

    @Test
    fun `placa de moto con letra final es valida`() {
        assertTrue(validar(base(placa = "xyz12d").copy(tipo = TipoVehiculo.MOTOCICLETA)).esValido)
        assertTrue(CampoVehiculo.PLACA in validar(base(placa = "xyz12d")).errores)
    }

    @Test
    fun `placa vacia o con longitud invalida es error`() {
        assertTrue(CampoVehiculo.PLACA in validar(base(placa = "")).errores)
        assertTrue(CampoVehiculo.PLACA in validar(base(placa = "AB1")).errores)
        assertTrue(CampoVehiculo.PLACA in validar(base(placa = "ABCD12345")).errores)
    }

    @Test
    fun `placa duplicada es error (RF-05)`() {
        val r = validar(base(), duplicada = true)
        assertFalse(r.esValido)
        assertTrue(r.errores.getValue(CampoVehiculo.PLACA).contains("ABC123"))
    }

    @Test
    fun `marca y linea son obligatorias`() {
        val r = validar(base(marca = " ", linea = ""))
        assertTrue(CampoVehiculo.MARCA in r.errores)
        assertTrue(CampoVehiculo.LINEA in r.errores)
    }

    @Test
    fun `anio fuera de rango es error y se admite el modelo del anio siguiente`() {
        assertTrue(CampoVehiculo.ANIO in validar(base(anio = 1900)).errores)
        assertTrue(CampoVehiculo.ANIO in validar(base(anio = anioActual + 2)).errores)
        assertTrue(validar(base(anio = anioActual + 1)).esValido)
    }

    @Test
    fun `kilometraje negativo es error`() {
        assertTrue(CampoVehiculo.KILOMETRAJE in validar(base(km = -1)).errores)
    }

    @Test
    fun `vin opcional pero si se indica debe tener 17 caracteres validos`() {
        assertTrue(validar(base(vin = "")).esValido)
        assertTrue(validar(base(vin = "9gaxs48t5lb123456")).esValido)
        assertTrue(CampoVehiculo.VIN in validar(base(vin = "123")).errores)
        // Las letras I, O y Q no se usan en un VIN.
        assertTrue(CampoVehiculo.VIN in validar(base(vin = "9GAXS48T5LB12345O")).errores)
    }
}
