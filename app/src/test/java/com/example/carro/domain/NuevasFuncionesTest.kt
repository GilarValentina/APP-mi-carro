package com.example.carro.domain

import com.example.carro.domain.agenda.Cita
import com.example.carro.domain.agenda.CitaValidator
import com.example.carro.domain.kilometraje.KilometrajeCalculator
import com.example.carro.domain.kilometraje.KilometrajeValidator
import com.example.carro.domain.kilometraje.LecturaKilometraje
import com.example.carro.domain.model.TipoCita
import com.example.carro.domain.model.TipoVehiculo
import com.example.carro.domain.perfil.CampoPerfil
import com.example.carro.domain.perfil.PerfilUsuario
import com.example.carro.domain.perfil.PerfilValidator
import com.example.carro.domain.picoyplaca.ReglasLocalesPicoYPlaca
import com.example.carro.domain.plan.Actividad
import com.example.carro.domain.plan.PlanPreventivoBase
import com.example.carro.domain.plan.ProgresoCalculator
import com.example.carro.domain.vehiculo.CatalogoFormulario
import com.example.carro.domain.vehiculo.VehiculoValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Pruebas de la lógica agregada para la versión de presentación (RNF-13). */
class NuevasFuncionesTest {

    private val hoy = LocalDate.of(2026, 11, 3)

    // ---- Kilometraje ----

    private fun lectura(dias: Long, km: Long) = LecturaKilometraje(vehiculoId = 1, fecha = hoy.minusDays(dias), valor = km)

    @Test
    fun `lectura menor a la anterior es rechazada`() {
        val existentes = listOf(lectura(10, 10_000))
        assertNotNull(KilometrajeValidator.validar(9_000, hoy, existentes, hoy))
        assertNull(KilometrajeValidator.validar(10_500, hoy, existentes, hoy))
    }

    @Test
    fun `lectura futura o vacia es rechazada`() {
        assertNotNull(KilometrajeValidator.validar(100, hoy.plusDays(1), emptyList(), hoy))
        assertNotNull(KilometrajeValidator.validar(null, hoy, emptyList(), hoy))
    }

    @Test
    fun `lectura retroactiva no puede superar una posterior`() {
        val existentes = listOf(lectura(20, 1_000), lectura(0, 2_000))
        assertNotNull(KilometrajeValidator.validar(2_500, hoy.minusDays(10), existentes, hoy))
        assertNull(KilometrajeValidator.validar(1_500, hoy.minusDays(10), existentes, hoy))
    }

    @Test
    fun `salto irreal de kilometraje es rechazado`() {
        val existentes = listOf(lectura(1, 1_000))
        assertNotNull(KilometrajeValidator.validar(50_000, hoy, existentes, hoy))
    }

    @Test
    fun `estadisticas calculan promedio y dias sin actualizar`() {
        val stats = KilometrajeCalculator.estadisticas(listOf(lectura(30, 10_000), lectura(20, 11_000)), hoy)
        assertEquals(11_000, stats.actual)
        assertEquals(1_000, stats.totalRecorrido)
        assertEquals(100.0, stats.promedioDiario!!, 0.001)
        assertEquals(20L, stats.diasSinActualizar)
        assertTrue(stats.requiereActualizacion)
    }

    // ---- Plan preventivo y barras de progreso ----

    @Test
    fun `proximo hito usa el siguiente multiplo del intervalo`() {
        assertEquals(15_000, PlanPreventivoBase.proximoHito(12_300, 5_000))
        assertEquals(10_000, PlanPreventivoBase.proximoHito(5_000, 5_000))
        assertEquals(20_000, PlanPreventivoBase.proximoHito(0, 20_000))
    }

    @Test
    fun `progreso muestra recorrido y kilometros restantes`() {
        val actividad = Actividad(vehiculoId = 1, nombre = "Aceite", programarPorKm = true, intervaloKm = 5_000, proximoKm = 15_000)
        val progreso = ProgresoCalculator.calcular(actividad, 14_000)!!
        assertEquals(4_000, progreso.recorridoEnIntervalo)
        assertEquals(1_000, progreso.restantes)
        assertEquals(0.8f, progreso.fraccion, 0.001f)
        assertEquals(1f, ProgresoCalculator.calcular(actividad, 16_000)!!.fraccion, 0.001f)
    }

    @Test
    fun `plan base cubre los tres intervalos`() {
        val intervalos = PlanPreventivoBase.lista.mapNotNull { it.intervaloKm }.toSet()
        assertEquals(setOf(5_000L, 10_000L, 20_000L), intervalos)
        assertEquals(15, PlanPreventivoBase.lista.size)
    }

    // ---- Placas ----

    @Test
    fun `entrada de placa se limita al formato del tipo`() {
        assertEquals("ABC123", VehiculoValidator.filtrarEntradaPlaca("abc-1234", TipoVehiculo.AUTOMOVIL))
        assertEquals("ABC12D", VehiculoValidator.filtrarEntradaPlaca("abc12d9", TipoVehiculo.MOTOCICLETA))
        assertEquals("AB", VehiculoValidator.filtrarEntradaPlaca("AB1", TipoVehiculo.CAMION))
        assertTrue(VehiculoValidator.placaValida("XYZ12D", TipoVehiculo.MOTOCICLETA))
        assertFalse(VehiculoValidator.placaValida("XYZ123", TipoVehiculo.MOTOCICLETA))
        assertTrue(VehiculoValidator.placaValida("XYZ123", TipoVehiculo.CAMPERO))
    }

    @Test
    fun `cada tipo tiene ejemplo de placa y cilindrajes`() {
        TipoVehiculo.entries.forEach { tipo ->
            assertTrue(VehiculoValidator.placaValida(tipo.ejemploPlaca, tipo))
            assertTrue(CatalogoFormulario.cilindrajes(tipo).isNotEmpty())
        }
    }

    @Test
    fun `secretarias dependen de la ciudad`() {
        assertTrue(CatalogoFormulario.secretarias(null).isEmpty())
        assertTrue(CatalogoFormulario.secretarias("Bogotá").first().contains("Bogotá"))
        assertTrue(CatalogoFormulario.secretarias("Tunja").first().contains("Tunja"))
    }

    // ---- Pico y placa ----

    @Test
    fun `pico y placa de bogota alterna por dia par o impar`() {
        val lunesPar = LocalDate.of(2026, 11, 2) // lunes 2
        val martesImpar = LocalDate.of(2026, 11, 3)
        assertEquals(true, ReglasLocalesPicoYPlaca.consultar("Bogotá", "ABC126", TipoVehiculo.AUTOMOVIL, lunesPar).tieneRestriccion)
        assertEquals(false, ReglasLocalesPicoYPlaca.consultar("Bogotá", "ABC126", TipoVehiculo.AUTOMOVIL, martesImpar).tieneRestriccion)
    }

    @Test
    fun `fin de semana y ciudades sin regla`() {
        val sabado = LocalDate.of(2026, 11, 7)
        assertEquals(false, ReglasLocalesPicoYPlaca.consultar("Bogotá", "ABC121", TipoVehiculo.AUTOMOVIL, sabado).tieneRestriccion)
        assertNull(ReglasLocalesPicoYPlaca.consultar("Tunja", "ABC121", TipoVehiculo.AUTOMOVIL, hoy).tieneRestriccion)
    }

    @Test
    fun `placa de moto usa el ultimo digito numerico`() {
        assertEquals(2, ReglasLocalesPicoYPlaca.ultimoDigito("ABC12D"))
    }

    // ---- Perfil y citas ----

    @Test
    fun `perfil exige nombre apellido ciudad y correo valido`() {
        val errores = PerfilValidator.validar(PerfilUsuario(correo = "no-es-correo"))
        assertTrue(errores.keys.containsAll(listOf(CampoPerfil.NOMBRE, CampoPerfil.APELLIDO, CampoPerfil.CIUDAD, CampoPerfil.CORREO)))
        val valido = PerfilUsuario("Valentina María", "Castaño", "Medellín", "vale@correo.com", "3001234567")
        assertTrue(PerfilValidator.validar(valido).isEmpty())
        assertEquals("Valentina", valido.primerNombre)
    }

    @Test
    fun `cita debe ser futura y con titulo`() {
        val ahora = LocalDateTime.of(hoy, LocalTime.NOON)
        val cita = Cita(vehiculoId = 1, tipo = TipoCita.TALLER, titulo = "Taller", fecha = hoy.plusDays(2), hora = LocalTime.of(9, 0), anticipacionDias = 3)
        assertNull(CitaValidator.validar(cita, ahora))
        assertNotNull(CitaValidator.validar(cita.copy(fecha = hoy.minusDays(1)), ahora))
        assertNotNull(CitaValidator.validar(cita.copy(titulo = " "), ahora))
        assertEquals(LocalDateTime.of(hoy.minusDays(1), LocalTime.of(9, 0)), cita.momentoRecordatorio)
    }
}
