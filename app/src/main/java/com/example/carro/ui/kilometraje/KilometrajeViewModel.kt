package com.example.carro.ui.kilometraje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.carro.core.di.AppContainer
import com.example.carro.data.repository.KilometrajeRepository
import com.example.carro.data.repository.ResultadoLectura
import com.example.carro.domain.kilometraje.EstadisticasKilometraje
import com.example.carro.domain.kilometraje.KilometrajeCalculator
import com.example.carro.domain.kilometraje.LecturaKilometraje
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Estado del módulo de kilometraje (RF-06 a RF-09). */
data class KilometrajeUiState(
    val lecturas: List<LecturaKilometraje> = emptyList(),
    val estadisticas: EstadisticasKilometraje? = null,
    val cargando: Boolean = true
)

class KilometrajeViewModel(
    private val repository: KilometrajeRepository,
    private val vehiculoId: Long
) : ViewModel() {

    private val mensaje = MutableStateFlow<String?>(null)
    val mensajeUsuario: StateFlow<String?> = mensaje.asStateFlow()

    private val errorFormulario = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = errorFormulario.asStateFlow()

    val uiState: StateFlow<KilometrajeUiState> =
        repository.observarLecturas(vehiculoId)
            .map { lecturas ->
                KilometrajeUiState(
                    lecturas = lecturas,
                    estadisticas = KilometrajeCalculator.estadisticas(lecturas, LocalDate.now()),
                    cargando = false
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KilometrajeUiState())

    /** Registra la lectura; devuelve el resultado mediante [onListo] para cerrar el diálogo. */
    fun registrar(valor: Long?, fecha: LocalDate, onListo: () -> Unit) {
        viewModelScope.launch {
            when (val r = repository.registrar(vehiculoId, valor, fecha)) {
                ResultadoLectura.Guardada -> {
                    errorFormulario.value = null
                    mensaje.value = "Kilometraje actualizado"
                    onListo()
                }
                is ResultadoLectura.Invalida -> errorFormulario.value = r.mensaje
            }
        }
    }

    fun eliminar(lectura: LecturaKilometraje) {
        viewModelScope.launch {
            repository.eliminar(lectura)
            mensaje.value = "Lectura eliminada"
        }
    }

    fun limpiarError() { errorFormulario.value = null }
    fun mensajeMostrado() { mensaje.value = null }

    companion object {
        fun factory(container: AppContainer, vehiculoId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                    KilometrajeViewModel(container.kilometrajeRepository, vehiculoId) as T
            }
    }
}
