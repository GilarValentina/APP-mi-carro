package com.example.carro.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.carro.core.di.AppContainer
import com.example.carro.data.repository.AlertasRepository
import com.example.carro.data.repository.CitasRepository
import com.example.carro.data.repository.PerfilRepository
import com.example.carro.domain.agenda.Cita
import com.example.carro.domain.perfil.PerfilUsuario
import com.example.carro.domain.picoyplaca.EstadoPicoYPlaca
import com.example.carro.domain.picoyplaca.ReglasLocalesPicoYPlaca
import java.time.LocalDate
import java.time.LocalDateTime
import com.example.carro.data.repository.HistorialRepository
import com.example.carro.data.repository.PlanMantenimientoRepository
import com.example.carro.data.repository.VehiculoRepository
import com.example.carro.domain.historial.EntradaHistorial
import com.example.carro.domain.historial.PeriodoResumen
import com.example.carro.domain.historial.ResumenCalculator
import com.example.carro.domain.model.EstadoActividad
import com.example.carro.domain.model.TipoAlerta
import com.example.carro.domain.plan.ActividadConEstado
import com.example.carro.domain.vehiculo.Vehiculo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

private const val LIMITE_RECIENTES = 20
private const val RECIENTES_VISIBLES = 5

/** Vehículo con el número de alertas activas que lo afectan (RF-37). */
data class VehiculoConAlertas(
    val vehiculo: Vehiculo,
    val alertasActivas: Int
)

/** Estado del inicio: alertas, próximos mantenimientos y gastos recientes (RF-37). */
data class HomeUiState(
    val vehiculosConAlertas: List<VehiculoConAlertas> = emptyList(),
    val proximosMantenimientos: List<ActividadConEstado> = emptyList(),
    val mantenimientosRecientes: List<EntradaHistorial> = emptyList(),
    val gastoUltimosTresMeses: Double = 0.0,
    val alertasActivas: Int = 0,
    val documentosProximos: Int = 0,
    val perfil: PerfilUsuario = PerfilUsuario(),
    val principal: Vehiculo? = null,
    val picoYPlaca: EstadoPicoYPlaca? = null,
    val proximaCita: Cita? = null,
    val cargando: Boolean = true
) {
    val hayVehiculos: Boolean get() = vehiculosConAlertas.isNotEmpty()
    val vehiculosRegistrados: Int get() = vehiculosConAlertas.size
    val totalAlertas: Int get() = alertasActivas
}

/**
 * ViewModel del inicio (RF-37). Reúne lo que el usuario necesita ver de un vistazo:
 * qué vehículos tienen alertas, qué mantenimientos están próximos o vencidos y
 * cuánto se ha gastado recientemente.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    vehiculoRepository: VehiculoRepository,
    planRepository: PlanMantenimientoRepository,
    alertasRepository: AlertasRepository,
    historialRepository: HistorialRepository,
    perfilRepository: PerfilRepository,
    citasRepository: CitasRepository
) : ViewModel() {

    private val actividadesDelPrincipal =
        vehiculoRepository.observarPrincipal().flatMapLatest { vehiculo ->
            if (vehiculo == null) {
                flowOf(emptyList())
            } else {
                planRepository.observarActividades(vehiculo.id, vehiculo.kilometrajeActual)
            }
        }

    /** Perfil, vehículo principal y su próxima cita, para el encabezado del panel. */
    private val encabezado =
        combine(
            perfilRepository.perfil,
            vehiculoRepository.observarPrincipal(),
            citasRepository.observarPendientes()
        ) { perfil, principal, citas ->
            val ahora = LocalDateTime.now()
            Triple(
                perfil,
                principal,
                citas.filter { it.vehiculoId == principal?.id && !it.momento.isBefore(ahora) }.minByOrNull { it.momento }
            )
        }

    val uiState: StateFlow<HomeUiState> =
        combine(
            vehiculoRepository.observarVehiculos(),
            alertasRepository.observarAlertasActivas(),
            actividadesDelPrincipal,
            historialRepository.observarRecientes(LIMITE_RECIENTES)
        ) { vehiculos, alertas, actividades, recientes ->
            val alertasPorVehiculo = alertas.groupingBy { it.vehiculoId }.eachCount()
            val alertasActivas = alertas.size
            HomeUiState(
                vehiculosConAlertas = vehiculos
                    .map { VehiculoConAlertas(it, alertasPorVehiculo[it.id] ?: 0) }
                    .sortedByDescending { it.alertasActivas },
                proximosMantenimientos = actividades
                    .filter { it.estado == EstadoActividad.VENCIDA || it.estado == EstadoActividad.PROXIMA }
                    .sortedBy { it.estado != EstadoActividad.VENCIDA },
                mantenimientosRecientes = recientes.take(RECIENTES_VISIBLES),
                gastoUltimosTresMeses = ResumenCalculator
                    .enPeriodo(recientes, PeriodoResumen.ULTIMOS_3_MESES)
                    .sumOf { it.costoTotal },
                alertasActivas = alertasActivas,
                documentosProximos = alertas.count { it.tipo == TipoAlerta.DOCUMENTO },
                cargando = false
            )
        }.combine(encabezado) { estado, (perfil, principal, cita) ->
            estado.copy(
                perfil = perfil,
                principal = principal,
                picoYPlaca = principal?.let { v ->
                    // Ciudad de circulación del vehículo; si no tiene, la ciudad del perfil.
                    val ciudad = v.ciudadPicoPlaca ?: perfil.ciudad.ifBlank { null }
                    ciudad?.let { ReglasLocalesPicoYPlaca.consultar(it, v.placa, v.tipo, LocalDate.now()) }
                },
                proximaCita = cita
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                    HomeViewModel(
                        container.vehiculoRepository,
                        container.planMantenimientoRepository,
                        container.alertasRepository,
                        container.historialRepository,
                        container.perfilRepository,
                        container.citasRepository
                    ) as T
            }
    }
}
