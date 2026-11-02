package com.example.carro.domain.historial

import com.example.carro.domain.model.TipoMantenimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Pruebas del resumen de gastos por categoría y periodo (RF-36, RNF-13). */
class ResumenCalculatorTest {

    private val hoy = LocalDate.of(2025, 6, 15)

    private fun entrada(
        id: Long,
        fecha: LocalDate,
        categoria: String,
        manoObra: Double,
        repuestos: Double
    ) = EntradaHistorial(
        mantenimientoId = id,
        vehiculoId = 1,
        fecha = fecha,
        kilometraje = 10_000,
        tipo = TipoMantenimiento.PREVENTIVO,
        descripcion = "Servicio $id",
        categoria = categoria,
        costoManoObra = manoObra,
        costoRepuestos = repuestos,
        costoTotal = manoObra + repuestos
    )

    private val historial = listOf(
        entrada(1, LocalDate.of(2024, 2, 1), "Aceite", 50_000.0, 50_000.0),
        entrada(2, LocalDate.of(2025, 4, 10), "Frenos", 100_000.0, 200_000.0),
        entrada(3, LocalDate.of(2025, 6, 1), "Aceite", 60_000.0, 40_000.0)
    )

    @Test
    fun `el periodo descarta las entradas anteriores al limite (RF-36)`() {
        val recientes = ResumenCalculator.enPeriodo(historial, PeriodoResumen.ULTIMOS_3_MESES, hoy)

        assertEquals(listOf(2L, 3L), recientes.map { it.mantenimientoId })
    }

    @Test
    fun `el periodo todo conserva el historial completo`() {
        assertEquals(3, ResumenCalculator.enPeriodo(historial, PeriodoResumen.TODO, hoy).size)
    }

    @Test
    fun `suma totales y calcula el promedio del periodo`() {
        val resumen = ResumenCalculator.calcular(historial, PeriodoResumen.TODO, hoy)

        assertEquals(500_000.0, resumen.total, 0.001)
        assertEquals(210_000.0, resumen.totalManoObra, 0.001)
        assertEquals(290_000.0, resumen.totalRepuestos, 0.001)
        assertEquals(3, resumen.cantidadMantenimientos)
        assertEquals(500_000.0 / 3, resumen.promedioPorMantenimiento, 0.001)
        assertEquals(2L, resumen.masCostoso?.mantenimientoId)
    }

    @Test
    fun `agrupa por categoria de mayor a menor gasto`() {
        val resumen = ResumenCalculator.calcular(historial, PeriodoResumen.TODO, hoy)

        assertEquals(listOf("Frenos", "Aceite"), resumen.porCategoria.map { it.etiqueta })
        assertEquals(300_000.0, resumen.porCategoria.first().total, 0.001)
        assertEquals(2, resumen.porCategoria.last().cantidad)
    }

    @Test
    fun `agrupa por mes en orden descendente`() {
        val resumen = ResumenCalculator.calcular(historial, PeriodoResumen.TODO, hoy)

        assertEquals(3, resumen.porMes.size)
        assertTrue(resumen.porMes.first().etiqueta.startsWith("2025-06"))
        assertTrue(resumen.porMes.last().etiqueta.startsWith("2024-02"))
    }

    @Test
    fun `un periodo sin movimientos devuelve un resumen vacio`() {
        val resumen = ResumenCalculator.calcular(historial, PeriodoResumen.ULTIMO_MES, LocalDate.of(2030, 1, 1))

        assertTrue(resumen.vacio)
        assertEquals(0.0, resumen.total, 0.001)
        assertNull(resumen.masCostoso)
    }

    @Test
    fun `la proporcion se acota entre cero y uno`() {
        val grupo = GastoAgrupado("Aceite", 200_000.0, 2)

        assertEquals(0.5f, ResumenCalculator.proporcion(grupo, 400_000.0), 0.001f)
        assertEquals(0f, ResumenCalculator.proporcion(grupo, 0.0), 0.001f)
    }

    @Test
    fun `la etiqueta del mes es ordenable y legible`() {
        assertEquals("2025-03 · mar 2025", ResumenCalculator.etiquetaMes(LocalDate.of(2025, 3, 7)))
    }
}
