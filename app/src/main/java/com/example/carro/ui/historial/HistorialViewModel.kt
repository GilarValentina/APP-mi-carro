package com.example.carro.ui.historial

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.carro.core.di.AppContainer
import com.example.carro.data.repository.HistorialRepository
import com.example.carro.data.repository.RespaldoRepository
import com.example.carro.data.repository.ResultadoRespaldo
import com.example.carro.domain.historial.EntradaHistorial
import com.example.carro.domain.historial.ExportadorCsv
import com.example.carro.domain.historial.FiltroHistorial
import com.example.carro.domain.historial.HistorialFiltro
import com.example.carro.domain.historial.PeriodoResumen
import com.example.carro.domain.historial.ResumenCalculator
import com.example.carro.domain.historial.ResumenGastos
import com.example.carro.domain.vehiculo.Vehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla de historial y resumen de un vehículo (RF-34 a RF-38). */
data class HistorialUiState(
    val entradas: List<EntradaHistorial> = emptyList(),
    val filtro: FiltroHistorial = FiltroHistorial(),
    val periodo: PeriodoResumen = PeriodoResumen.TODO,
    val resumen: ResumenGastos = ResumenGastos(),
    val categoriasDisponibles: List<String> = emptyList(),
    val talleresDisponibles: List<String> = emptyList(),
    val costoMaximoHistorial: Double = 0.0,
    val totalSinFiltrar: Int = 0,
    val cargando: Boolean = true
) {
    val vacio: Boolean get() = !cargando && totalSinFiltrar == 0
    val sinResultados: Boolean get() = !cargando && totalSinFiltrar > 0 && entradas.isEmpty()
}

/**
 * ViewModel del historial: aplica los filtros sobre las entradas del vehículo (RF-35),
 * calcula el resumen de gastos del periodo seleccionado (RF-36) y exporta el
 * historial a CSV (RF-38).
 */
class HistorialViewModel(
    private val repository: HistorialRepository,
    private val respaldoRepository: RespaldoRepository,
    private val vehiculo: Vehiculo
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroHistorial())
    private val periodo = MutableStateFlow(PeriodoResumen.TODO)

    private val mensaje = MutableStateFlow<String?>(null)
    val mensajeUsuario: StateFlow<String?> = mensaje.asStateFlow()

    val uiState: StateFlow<HistorialUiState> =
        combine(
            repository.observarHistorial(vehiculo.id),
            filtro,
            periodo
        ) { todas, filtroActual, periodoActual ->
            val filtradas = HistorialFiltro.aplicar(todas, filtroActual)
            HistorialUiState(
                entradas = filtradas,
                filtro = filtroActual,
                periodo = periodoActual,
                resumen = ResumenCalculator.calcular(filtradas, periodoActual),
                categoriasDisponibles = HistorialFiltro.categoriasDisponibles(todas),
                talleresDisponibles = HistorialFiltro.talleresDisponibles(todas),
                costoMaximoHistorial = HistorialFiltro.costoMaximo(todas),
                totalSinFiltrar = todas.size,
                cargando = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistorialUiState()
        )

    fun buscar(texto: String) = filtro.update { it.copy(texto = texto) }

    fun aplicarFiltro(nuevo: FiltroHistorial) { filtro.value = nuevo }

    fun limpiarFiltro() { filtro.value = FiltroHistorial() }

    fun cambiarPeriodo(nuevo: PeriodoResumen) { periodo.value = nuevo }

    /** Nombre sugerido del archivo al abrir el selector del sistema (RF-38). */
    fun nombreArchivoExportacion(): String = ExportadorCsv.nombreArchivo(vehiculo.placa)

    /** RF-38: exporta las entradas visibles, respetando los filtros aplicados. */
    fun exportar(destino: Uri) {
        viewModelScope.launch {
            val resultado = respaldoRepository.exportarHistorial(
                destino = destino,
                titulo = "${vehiculo.placa} - ${vehiculo.titulo}",
                entradas = uiState.value.entradas
            )
            mensaje.value = when (resultado) {
                is ResultadoRespaldo.Exito -> resultado.mensaje
                is ResultadoRespaldo.Error -> resultado.mensaje
            }
        }
    }

    fun mensajeMostrado() { mensaje.value = null }

    companion object {
        fun factory(container: AppContainer, vehiculo: Vehiculo): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                    HistorialViewModel(
                        container.historialRepository,
                        container.respaldoRepository,
                        vehiculo
                    ) as T
            }
    }
}
