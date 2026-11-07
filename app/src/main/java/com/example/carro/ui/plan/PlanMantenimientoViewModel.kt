package com.example.carro.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.carro.core.di.AppContainer
import com.example.carro.data.entity.CategoriaEntity
import com.example.carro.data.repository.PlanMantenimientoRepository
import com.example.carro.domain.plan.Actividad
import com.example.carro.domain.plan.ActividadConEstado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de UI del plan de mantenimiento (RF-10 a RF-16). */
data class PlanUiState(
    val actividades: List<ActividadConEstado> = emptyList(),
    val categorias: List<CategoriaEntity> = emptyList(),
    val cargando: Boolean = true
)

/**
 * ViewModel del plan de mantenimiento. Expone las actividades de un vehículo
 * con su estado calculado y gestiona el ciclo de vida de cada actividad (RF-15).
 */
class PlanMantenimientoViewModel(
    private val repository: PlanMantenimientoRepository,
    private val vehiculoId: Long,
    val kilometrajeActual: Long
) : ViewModel() {

    /** Aplica el plan preventivo base de 5.000 / 10.000 / 20.000 km al vehículo. */
    fun aplicarPlanBase() {
        viewModelScope.launch {
            val creadas = repository.aplicarPlanBase(vehiculoId, kilometrajeActual)
            mensaje.value = if (creadas > 0) {
                "Se agregaron $creadas actividades del plan preventivo"
            } else {
                "El plan preventivo ya estaba aplicado"
            }
        }
    }

    private val mensaje = MutableStateFlow<String?>(null)
    val mensajeUsuario: StateFlow<String?> = mensaje.asStateFlow()

    val uiState: StateFlow<PlanUiState> =
        combine(
            repository.observarActividades(vehiculoId, kilometrajeActual),
            repository.observarCategorias()
        ) { actividades, categorias ->
            PlanUiState(actividades = actividades, categorias = categorias, cargando = false)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlanUiState()
        )

    init {
        viewModelScope.launch { repository.sembrarCategoriasSiVacio() }
    }

    fun guardar(actividad: Actividad) {
        viewModelScope.launch {
            repository.guardar(actividad.copy(vehiculoId = vehiculoId))
            mensaje.value = "Actividad guardada"
        }
    }

    fun pausarReactivar(item: ActividadConEstado) {
        viewModelScope.launch {
            val nuevoEstado = !item.actividad.activa
            repository.cambiarActiva(item.actividad, nuevoEstado)
            mensaje.value = if (nuevoEstado) "Actividad reactivada" else "Actividad pausada"
        }
    }

    fun eliminar(item: ActividadConEstado) {
        viewModelScope.launch {
            repository.eliminar(item.actividad)
            mensaje.value = "Actividad eliminada"
        }
    }

    fun mensajeMostrado() {
        mensaje.value = null
    }

    suspend fun obtenerActividad(id: Long): Actividad? = repository.obtenerActividad(id)

    companion object {
        fun factory(
            container: AppContainer,
            vehiculoId: Long,
            kilometrajeActual: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                PlanMantenimientoViewModel(
                    container.planMantenimientoRepository,
                    vehiculoId,
                    kilometrajeActual
                ) as T
        }
    }
}
