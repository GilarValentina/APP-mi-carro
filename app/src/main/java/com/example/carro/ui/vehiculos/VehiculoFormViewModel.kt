package com.example.carro.ui.vehiculos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.carro.core.di.AppContainer
import com.example.carro.data.repository.ResultadoGuardarVehiculo
import com.example.carro.data.repository.VehiculoRepository
import com.example.carro.domain.model.TipoVehiculo
import com.example.carro.domain.vehiculo.CampoVehiculo
import com.example.carro.domain.vehiculo.Vehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Valores del formulario tal como los escribe el usuario. */
data class FormularioVehiculo(
    val placa: String = "",
    val tipo: TipoVehiculo = TipoVehiculo.AUTOMOVIL,
    val marca: String = "",
    val linea: String = "",
    val anio: String = "",
    val kilometraje: String = "",
    val color: String = "",
    val combustible: String? = null,
    val cilindraje: String = "",
    val vin: String = "",
    val ciudadPicoPlaca: String = "",
    val picoYPlaca: String = ""
)

/** Estado de UI del formulario de registro/edición (RF-01, RF-02, RF-05). */
data class VehiculoFormUiState(
    val formulario: FormularioVehiculo = FormularioVehiculo(),
    val errores: Map<CampoVehiculo, String> = emptyMap(),
    val editando: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false
)

class VehiculoFormViewModel(
    private val repository: VehiculoRepository,
    private val vehiculoId: Long?
) : ViewModel() {

    private val estado = MutableStateFlow(
        VehiculoFormUiState(editando = vehiculoId != null, cargando = vehiculoId != null)
    )
    val uiState: StateFlow<VehiculoFormUiState> = estado.asStateFlow()

    private var original: Vehiculo? = null

    init {
        if (vehiculoId != null) {
            viewModelScope.launch {
                val v = repository.obtener(vehiculoId)
                original = v
                estado.update {
                    it.copy(
                        cargando = false,
                        formulario = v?.let { veh ->
                            FormularioVehiculo(
                                placa = veh.placa,
                                tipo = veh.tipo,
                                marca = veh.marca,
                                linea = veh.linea,
                                anio = veh.anio.toString(),
                                kilometraje = veh.kilometrajeActual.toString(),
                                color = veh.color.orEmpty(),
                                combustible = veh.combustible,
                                cilindraje = veh.cilindraje.orEmpty(),
                                vin = veh.vin.orEmpty(),
                                ciudadPicoPlaca = veh.ciudadPicoPlaca.orEmpty(),
                                picoYPlaca = veh.picoYPlaca.orEmpty()
                            )
                        } ?: it.formulario
                    )
                }
            }
        }
    }

    /** Aplica un cambio del formulario y limpia el error del campo editado. */
    fun actualizar(campo: CampoVehiculo? = null, cambio: (FormularioVehiculo) -> FormularioVehiculo) {
        estado.update {
            it.copy(
                formulario = cambio(it.formulario),
                errores = if (campo != null) it.errores - campo else it.errores
            )
        }
    }

    fun guardar() {
        val actual = estado.value
        if (actual.guardando) return
        val f = actual.formulario
        val vehiculo = Vehiculo(
            id = vehiculoId ?: 0L,
            placa = f.placa,
            tipo = f.tipo,
            marca = f.marca,
            linea = f.linea,
            // Valores no numéricos llegan como inválidos para que el validador los reporte.
            anio = f.anio.toIntOrNull() ?: -1,
            kilometrajeActual = original?.kilometrajeActual ?: (f.kilometraje.toLongOrNull() ?: -1),
            color = f.color,
            vin = f.vin,
            combustible = f.combustible,
            cilindraje = f.cilindraje,
            ciudadPicoPlaca = f.ciudadPicoPlaca.ifBlank { null },
            picoYPlaca = f.picoYPlaca.ifBlank { null }
        )
        estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            when (val r = repository.guardar(vehiculo)) {
                is ResultadoGuardarVehiculo.Guardado ->
                    estado.update { it.copy(guardando = false, guardado = true) }
                is ResultadoGuardarVehiculo.Invalido ->
                    estado.update { it.copy(guardando = false, errores = r.validacion.errores) }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer, vehiculoId: Long?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                    VehiculoFormViewModel(container.vehiculoRepository, vehiculoId) as T
            }
    }
}
