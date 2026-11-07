package com.example.carro.domain.vehiculo

import com.example.carro.domain.model.TipoVehiculo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VehiculoCatalogTest {

    @Test
    fun motoSoloMuestraMarcasDeMoto() {
        val marcas = VehiculoCatalog.marcas(TipoVehiculo.MOTOCICLETA)
        assertTrue("Yamaha" in marcas)
        assertFalse("Renault" in marcas)
        assertFalse("Toyota" in marcas)
    }

    @Test
    fun automovilNoMuestraMarcasDeMoto() {
        val marcas = VehiculoCatalog.marcas(TipoVehiculo.AUTOMOVIL)
        assertTrue("Renault" in marcas)
        assertFalse("Bajaj" in marcas)
        assertFalse("AKT" in marcas)
    }

    @Test
    fun lineasDependenDelTipo() {
        assertTrue("Civic" in VehiculoCatalog.lineasDeMarca("Honda", TipoVehiculo.AUTOMOVIL))
        assertFalse("Civic" in VehiculoCatalog.lineasDeMarca("Honda", TipoVehiculo.MOTOCICLETA))
        assertTrue("NPR" in VehiculoCatalog.lineasDeMarca("Chevrolet", TipoVehiculo.CAMION))
        assertFalse("Spark" in VehiculoCatalog.lineasDeMarca("Chevrolet", TipoVehiculo.CAMION))
    }

    @Test
    fun otroIncluyeTodasLasMarcas() {
        val marcas = VehiculoCatalog.marcas(TipoVehiculo.OTRO)
        assertTrue(marcas.containsAll(listOf("Renault", "Yamaha", "Kenworth")))
        assertEquals("Otra", marcas.last())
        assertEquals(marcas.size, marcas.distinct().size)
    }

    @Test
    fun todosLosTiposTerminanEnOtra() {
        TipoVehiculo.entries.forEach { tipo ->
            assertEquals("Otra", VehiculoCatalog.marcas(tipo).last())
            assertEquals(listOf("Otra"), VehiculoCatalog.lineasDeMarca("Otra", tipo))
        }
    }
}
