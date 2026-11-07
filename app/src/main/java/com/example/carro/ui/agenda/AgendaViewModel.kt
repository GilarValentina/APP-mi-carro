package com.example.carro.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.carro.core.di.AppContainer
import com.example.carro.data.repository.CitasRepository
import com.example.carro.domain.agenda.Cita
import com.example.carro.domain.agenda.CitaValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class AgendaUiState(
    val proximas: List<Cita> = emptyList(),
    val pasadas: List<Cita> = emptyList(),
    val cargando: Boolean = true
)

/** Agenda de citas del vehículo principal: taller, tecnomecánica, SOAT, seguros y mantenimiento. */
class AgendaViewModel(
    private val repository: CitasRepository,
    private val vehiculoId: Long
) : ViewModel() {

    private val mensaje = MutableStateFlow<String?>(null)
    val mensajeUsuario: StateFlow<String?> = mensaje.asStateFlow()

    val uiState: StateFlow<AgendaUiState> =
        repository.observarCitas(vehiculoId).map { citas ->
            val ahora = LocalDateTime.now()
            val (proximas, pasadas) = citas.partition { !it.completada && !it.momento.isBefore(ahora) }
            AgendaUiState(
                proximas = proximas.sortedBy { it.momento },
                pasadas = pasadas.sortedByDescending { it.momento },
                cargando = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgendaUiState())

    suspend fun obtener(id: Long): Cita? = repository.obtener(id)

    /** Devuelve el error de validación o null si se guardó. */
    fun guardar(cita: Cita, onGuardada: () -> Unit): String? {
        val completa = cita.copy(vehiculoId = vehiculoId)
        CitaValidator.validar(completa, LocalDateTime.now())?.let { return it }
        viewModelScope.launch {
            repository.guardar(completa)
            mensaje.value = "Cita guardada. Te recordaremos ${if (completa.anticipacionDias == 1) "1 día" else "${completa.anticipacionDias} días"} antes."
            onGuardada()
        }
        return null
    }

    fun completar(cita: Cita) {
        viewModelScope.launch {
            repository.marcarCompletada(cita, !cita.completada)
            mensaje.value = if (cita.completada) "Cita reabierta" else "Cita marcada como realizada"
        }
    }

    fun eliminar(cita: Cita) {
        viewModelScope.launch {
            repository.eliminar(cita)
            mensaje.value = "Cita eliminada"
        }
    }

    fun mensajeMostrado() { mensaje.value = null }

    companion object {
        fun factory(container: AppContainer, vehiculoId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                    AgendaViewModel(container.citasRepository, vehiculoId) as T
            }
    }
}
