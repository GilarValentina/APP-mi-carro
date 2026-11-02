package com.example.carro.domain.historial

import com.example.carro.domain.model.TipoMantenimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Pruebas de la exportación compartible del historial (RF-38, RNF-13). */
class ExportadorCsvTest {

    private val entradas = listOf(
        EntradaHistorial(
            mantenimientoId = 1,
            vehiculoId = 1,
            fecha = LocalDate.of(2025, 1, 10),
            kilometraje = 50_000,
            tipo = TipoMantenimiento.PREVENTIVO,
            descripcion = "Cambio de aceite, filtro",
            categoria = "Aceite",
            taller = "Taller \"El Rápido\"",
            costoManoObra = 50_000.0,
            costoRepuestos = 100_000.0,
            costoTotal = 150_000.0,
            cantidadRepuestos = 2
        ),
        EntradaHistorial(
            mantenimientoId = 2,
            vehiculoId = 1,
            fecha = LocalDate.of(2024, 12, 1),
            kilometraje = 45_000,
            tipo = TipoMantenimiento.CORRECTIVO,
            descripcion = "Frenos",
            categoria = "Frenos",
            costoTotal = 250_000.0
        )
    )

    @Test
    fun `incluye titulo encabezados y una fila por mantenimiento`() {
        val lineas = ExportadorCsv.generar("ABC123 - Mazda 3", entradas).trim().lines()

        assertEquals(5, lineas.size)
        assertEquals("\"Historial de ABC123 - Mazda 3\"", lineas[0])
        assertTrue(lineas[1].startsWith("\"Fecha\",\"Kilometraje\""))
    }

    @Test
    fun `ordena las filas cronologicamente de forma ascendente`() {
        val lineas = ExportadorCsv.generar("ABC123", entradas).trim().lines()

        assertTrue(lineas[2].startsWith("\"2024-12-01\""))
        assertTrue(lineas[3].startsWith("\"2025-01-10\""))
    }

    @Test
    fun `escapa comillas y conserva las comas dentro del campo (RFC 4180)`() {
        val csv = ExportadorCsv.generar("ABC123", entradas)

        assertTrue(csv.contains("\"Taller \"\"El Rápido\"\"\""))
        assertTrue(csv.contains("\"Cambio de aceite, filtro\""))
    }

    @Test
    fun `agrega una fila final con el total gastado`() {
        val ultima = ExportadorCsv.generar("ABC123", entradas).trim().lines().last()

        assertTrue(ultima.startsWith("\"TOTAL\""))
        assertTrue(ultima.contains("\"400000.00\""))
    }

    @Test
    fun `un historial vacio conserva encabezados y total en cero`() {
        val lineas = ExportadorCsv.generar("ABC123", emptyList()).trim().lines()

        assertEquals(3, lineas.size)
        assertTrue(lineas.last().contains("\"0.00\""))
    }

    @Test
    fun `el nombre de archivo descarta caracteres invalidos`() {
        assertEquals("historial_ABC123.csv", ExportadorCsv.nombreArchivo("ABC-123"))
        assertEquals("historial_vehiculo.csv", ExportadorCsv.nombreArchivo("///"))
    }
}
