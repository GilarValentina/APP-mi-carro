package com.example.carro.ui.vehiculos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.carro.core.di.AppContainer
import com.example.carro.data.repository.VehiculoRepository
import com.example.carro.domain.vehiculo.Vehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de UI del listado de vehículos (RF-01 a RF-05). */
data class VehiculosUiState(
    val vehiculos: List<Vehiculo> = emptyList(),
    val archivados: List<Vehiculo> = emptyList(),
    val cargando: Boolean = true
)

/** ViewModel del listado: selección del principal (RF-04) y eliminación (RF-03). */
class VehiculosViewModel(
    private val repository: VehiculoRepository
) : ViewModel() {

    private val mensaje = MutableStateFlow<String?>(null)
    val mensajeUsuario: StateFlow<String?> = mensaje.asStateFlow()

    val uiState: StateFlow<VehiculosUiState> =
        combine(repository.observarVehiculos(), repository.observarArchivados()) { activos, archivados ->
            VehiculosUiState(vehiculos = activos, archivados = archivados, cargando = false)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = VehiculosUiState()
            )

    fun seleccionarPrincipal(vehiculo: Vehiculo) {
        if (vehiculo.esPrincipal) return
        viewModelScope.launch {
            repository.seleccionarPrincipal(vehiculo.id)
            mensaje.value = "${vehiculo.placa} es ahora el vehículo principal"
        }
    }

    fun eliminar(vehiculo: Vehiculo) {
        viewModelScope.launch {
            repository.eliminar(vehiculo.id)
            mensaje.value = "Vehículo ${vehiculo.placa} eliminado"
        }
    }

    fun archivar(vehiculo: Vehiculo) {
        viewModelScope.launch {
            repository.archivar(vehiculo.id)
            mensaje.value = "${vehiculo.placa} archivado. Puedes restaurarlo en la pestaña Archivados."
        }
    }

    fun restaurar(vehiculo: Vehiculo) {
        viewModelScope.launch {
            repository.restaurar(vehiculo.id)
            mensaje.value = "${vehiculo.placa} restaurado"
        }
    }

    fun mensajeMostrado() { mensaje.value = null }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                    VehiculosViewModel(container.vehiculoRepository) as T
            }
    }
}
