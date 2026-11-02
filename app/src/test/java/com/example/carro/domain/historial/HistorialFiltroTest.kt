package com.example.carro.domain.historial

import com.example.carro.domain.model.TipoMantenimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Pruebas de la búsqueda y los filtros del historial (RF-35, RNF-13). */
class HistorialFiltroTest {

    private val hoy = LocalDate.of(2025, 6, 15)

    private fun entrada(
        id: Long,
        fecha: LocalDate = hoy,
        categoria: String = "Aceite",
        tipo: TipoMantenimiento = TipoMantenimiento.PREVENTIVO,
        taller: String? = "Taller Centro",
        costo: Double = 100_000.0,
        descripcion: String = "Cambio de aceite"
    ) = EntradaHistorial(
        mantenimientoId = id,
        vehiculoId = 1,
        fecha = fecha,
        kilometraje = 10_000,
        tipo = tipo,
        descripcion = descripcion,
        categoria = categoria,
        taller = taller,
        costoTotal = costo
    )

    private val historial = listOf(
        entrada(1, fecha = LocalDate.of(2025, 1, 10), categoria = "Aceite", costo = 150_000.0),
        entrada(
            2,
            fecha = LocalDate.of(2025, 3, 20),
            categoria = "Frenos",
            tipo = TipoMantenimiento.CORRECTIVO,
            taller = "Frenos Express",
            costo = 450_000.0,
            descripcion = "Cambio de pastillas"
        ),
        entrada(3, fecha = LocalDate.of(2025, 6, 1), categoria = "Llantas", taller = null, costo = 900_000.0)
    )

    @Test
    fun `sin criterios devuelve todo en orden cronologico descendente`() {
        val resultado = HistorialFiltro.aplicar(historial, FiltroHistorial())

        assertEquals(listOf(3L, 2L, 1L), resultado.map { it.mantenimientoId })
    }

    @Test
    fun `el texto busca en descripcion categoria y taller`() {
        assertEquals(1, HistorialFiltro.aplicar(historial, FiltroHistorial(texto = "pastillas")).size)
        assertEquals(1, HistorialFiltro.aplicar(historial, FiltroHistorial(texto = "llantas")).size)
        assertEquals(1, HistorialFiltro.aplicar(historial, FiltroHistorial(texto = "express")).size)
    }

    @Test
    fun `el rango de fechas incluye los extremos`() {
        val filtro = FiltroHistorial(
            desde = LocalDate.of(2025, 1, 10),
            hasta = LocalDate.of(2025, 3, 20)
        )

        assertEquals(listOf(2L, 1L), HistorialFiltro.aplicar(historial, filtro).map { it.mantenimientoId })
    }

    @Test
    fun `filtra por varias categorias a la vez`() {
        val filtro = FiltroHistorial(categorias = setOf("Aceite", "Llantas"))

        assertEquals(listOf(3L, 1L), HistorialFiltro.aplicar(historial, filtro).map { it.mantenimientoId })
    }

    @Test
    fun `filtra por tipo de mantenimiento`() {
        val filtro = FiltroHistorial(tipo = TipoMantenimiento.CORRECTIVO)

        assertEquals(listOf(2L), HistorialFiltro.aplicar(historial, filtro).map { it.mantenimientoId })
    }

    @Test
    fun `filtra por taller sin distinguir mayusculas`() {
        val filtro = FiltroHistorial(taller = "taller centro")

        assertEquals(listOf(1L), HistorialFiltro.aplicar(historial, filtro).map { it.mantenimientoId })
    }

    @Test
    fun `el rango de costo incluye los limites`() {
        val filtro = FiltroHistorial(costoMinimo = 150_000.0, costoMaximo = 450_000.0)

        assertEquals(listOf(2L, 1L), HistorialFiltro.aplicar(historial, filtro).map { it.mantenimientoId })
    }

    @Test
    fun `los criterios se combinan con and`() {
        val filtro = FiltroHistorial(
            categorias = setOf("Aceite"),
            tipo = TipoMantenimiento.CORRECTIVO
        )

        assertTrue(HistorialFiltro.aplicar(historial, filtro).isEmpty())
    }

    @Test
    fun `las opciones disponibles omiten talleres vacios`() {
        assertEquals(listOf("Aceite", "Frenos", "Llantas"), HistorialFiltro.categoriasDisponibles(historial))
        assertEquals(listOf("Frenos Express", "Taller Centro"), HistorialFiltro.talleresDisponibles(historial))
        assertEquals(900_000.0, HistorialFiltro.costoMaximo(historial), 0.001)
    }

    @Test
    fun `un filtro vacio no se considera activo`() {
        assertEquals(false, FiltroHistorial().activo)
        assertEquals(2, FiltroHistorial(texto = "aceite", tipo = TipoMantenimiento.PREVENTIVO).cantidadCriterios)
    }
}
