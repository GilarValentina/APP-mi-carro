package com.example.carro.ui.respaldo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.carro.core.di.AppContainer
import com.example.carro.data.repository.RespaldoRepository
import com.example.carro.data.repository.ResultadoRespaldo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla de respaldo y borrado de datos (RF-39, RF-40). */
data class RespaldoUiState(
    val procesando: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false,
    val requiereReinicio: Boolean = false
)

/**
 * ViewModel de copia de respaldo, restauración y borrado definitivo (RF-39, RF-40).
 * Toda operación informa su resultado al usuario, incluso si falla (RNF-15).
 */
class RespaldoViewModel(
    private val repository: RespaldoRepository
) : ViewModel() {

    private val estado = MutableStateFlow(RespaldoUiState())
    val uiState: StateFlow<RespaldoUiState> = estado.asStateFlow()

    fun nombreSugerido(): String = repository.nombreSugerido()

    fun crearRespaldo(destino: Uri) = ejecutar { repository.crearRespaldo(destino) }

    fun restaurarRespaldo(origen: Uri) =
        ejecutar(reinicioAlTerminar = true) { repository.restaurarRespaldo(origen) }

    fun eliminarTodo() = ejecutar { repository.eliminarTodo() }

    private fun ejecutar(
        reinicioAlTerminar: Boolean = false,
        operacion: suspend () -> ResultadoRespaldo
    ) {
        if (estado.value.procesando) return
        estado.update { it.copy(procesando = true, mensaje = null) }
        viewModelScope.launch {
            val resultado = operacion()
            estado.update {
                it.copy(
                    procesando = false,
                    mensaje = when (resultado) {
                        is ResultadoRespaldo.Exito -> resultado.mensaje
                        is ResultadoRespaldo.Error -> resultado.mensaje
                    },
                    esError = resultado is ResultadoRespaldo.Error,
                    requiereReinicio = reinicioAlTerminar && resultado is ResultadoRespaldo.Exito
                )
            }
        }
    }

    fun mensajeMostrado() {
        estado.update { it.copy(mensaje = null, esError = false) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                    RespaldoViewModel(container.respaldoRepository) as T
            }
    }
}
