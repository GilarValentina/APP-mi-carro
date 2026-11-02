package com.example.carro.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carro.core.di.AppContainer
import com.example.carro.domain.historial.EntradaHistorial
import com.example.carro.domain.model.EstadoActividad
import com.example.carro.domain.plan.ActividadConEstado
import com.example.carro.ui.historial.Formatos
import com.example.carro.ui.navigation.Destinos

/**
 * Inicio de la aplicación (RF-37): muestra los vehículos con alertas, los
 * mantenimientos próximos o vencidos y los gastos recientes, además del acceso
 * a cada módulo.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    container: AppContainer,
    onNavegar: (String) -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("MiCarro") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!state.cargando && !state.hayVehiculos) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavegar(Destinos.VEHICULOS) }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Comienza registrando tu vehículo", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Con un vehículo registrado podrás programar su plan y llevar el historial.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (state.hayVehiculos) {
                SeccionVehiculos(state.vehiculosConAlertas) { onNavegar(Destinos.ALERTAS) }
                SeccionProximos(state.proximosMantenimientos) { onNavegar(Destinos.PLAN_MANTENIMIENTO) }
                SeccionGastos(
                    recientes = state.mantenimientosRecientes,
                    gastoTrimestre = state.gastoUltimosTresMeses,
                    onVerHistorial = { onNavegar(Destinos.HISTORIAL) }
                )
            }

            Text("Módulos", style = MaterialTheme.typography.titleMedium)
            val modulos = listOf(
                ModuloInicio("Vehículos", Icons.Filled.DirectionsCar, Destinos.VEHICULOS),
                ModuloInicio("Kilometraje", Icons.Filled.Speed, null),
                ModuloInicio("Plan", Icons.AutoMirrored.Filled.EventNote, Destinos.PLAN_MANTENIMIENTO),
                ModuloInicio("Mantenimientos", Icons.Filled.Build, Destinos.MANTENIMIENTOS),
                ModuloInicio("Alertas", Icons.Filled.Notifications, Destinos.ALERTAS),
                ModuloInicio("Historial", Icons.Filled.History, Destinos.HISTORIAL),
                ModuloInicio("Respaldo", Icons.Filled.Backup, Destinos.RESPALDO)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                modulos.forEach { modulo ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 96.dp)
                            .then(
                                if (modulo.ruta != null) {
                                    Modifier.clickable { onNavegar(modulo.ruta) }
                                } else {
                                    Modifier
                                }
                            )
                            .semantics { contentDescription = modulo.titulo }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = modulo.icono, contentDescription = null)
                            Text(text = modulo.titulo, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                if (modulos.size % 2 != 0) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/** Acceso a un módulo desde el inicio: título, icono y destino de navegación. */
private data class ModuloInicio(
    val titulo: String,
    val icono: ImageVector,
    val ruta: String?
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeccionVehiculos(vehiculos: List<VehiculoConAlertas>, onVerAlertas: () -> Unit) {
    Text("Tus vehículos", style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        vehiculos.forEach { item ->
            val etiqueta = if (item.alertasActivas > 0) {
                "${item.vehiculo.placa} · ${item.alertasActivas} alerta(s)"
            } else {
                "${item.vehiculo.placa} · al día"
            }
            AssistChip(
                onClick = onVerAlertas,
                label = { Text(etiqueta) },
                modifier = Modifier.semantics { contentDescription = etiqueta }
            )
        }
    }
}

@Composable
private fun SeccionProximos(actividades: List<ActividadConEstado>, onVerPlan: () -> Unit) {
    Text("Próximos mantenimientos", style = MaterialTheme.typography.titleMedium)
    if (actividades.isEmpty()) {
        Text(
            "No hay mantenimientos próximos ni vencidos.",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }
    actividades.take(4).forEach { item ->
        val vencida = item.estado == EstadoActividad.VENCIDA
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onVerPlan() },
            colors = if (vencida) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            } else {
                CardDefaults.cardColors()
            }
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.actividad.nombre, style = MaterialTheme.typography.bodyLarge)
                Text(
                    buildString {
                        append(if (vencida) "Vencida" else "Próxima")
                        item.actividad.proximaFecha?.let { append(" · ${Formatos.fecha(it)}") }
                        item.actividad.proximoKm?.let { append(" · ${Formatos.kilometraje(it)}") }
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SeccionGastos(
    recientes: List<EntradaHistorial>,
    gastoTrimestre: Double,
    onVerHistorial: () -> Unit
) {
    Text("Gastos recientes", style = MaterialTheme.typography.titleMedium)
    Card(modifier = Modifier.fillMaxWidth().clickable { onVerHistorial() }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Últimos 3 meses", style = MaterialTheme.typography.labelLarge)
            Text(Formatos.moneda(gastoTrimestre), style = MaterialTheme.typography.headlineSmall)
            if (recientes.isEmpty()) {
                Text("Aún no has registrado mantenimientos.", style = MaterialTheme.typography.bodyMedium)
            } else {
                recientes.forEach { entrada ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${Formatos.fecha(entrada.fecha)} · ${entrada.descripcion}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            Formatos.moneda(entrada.costoTotal),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
