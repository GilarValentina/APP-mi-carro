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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.font.FontWeight
import com.example.carro.ui.componentes.SkeletonCarga
import com.example.carro.ui.perfil.saludo
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    container: AppContainer,
    onNavegar: (String) -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            saludo(state.perfil),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Qué bueno verte de nuevo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = { onNavegar(Destinos.ALERTAS) }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Ver alertas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onNavegar(Destinos.AJUSTES) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Ajustes y perfil",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.cargando) {
                SkeletonCarga(Modifier.fillMaxWidth())
                return@Scaffold
            }

            if (!state.hayVehiculos) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavegar(Destinos.VEHICULOS) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Comienza registrando tu vehículo", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Con un vehículo registrado podrás programar mantenimientos, alertas y revisiones.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                SeccionGuias(onNavegar)
                return@Scaffold
            }

            state.principal?.let { principal ->
                TarjetaVehiculoPrincipal(
                    vehiculo = principal,
                    picoYPlaca = state.picoYPlaca,
                    onClick = { onNavegar(Destinos.VEHICULOS) }
                )
            }
            AccesosRapidos(onNavegar)
            state.proximaCita?.let { cita ->
                TarjetaProximaCita(cita) { onNavegar(Destinos.AGENDA) }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Resumen general", style = MaterialTheme.typography.labelLarge)
                    Text(
                        if (state.totalAlertas > 0 || state.proximosMantenimientos.isNotEmpty()) "Hay pendientes por revisar" else "Todo bajo control",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (state.totalAlertas == 1) "1 alerta activa" else if (state.totalAlertas > 1) "${state.totalAlertas} alertas activas" else "Sin alertas pendientes",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        AssistChip(
                            onClick = { onNavegar(Destinos.ALERTAS) },
                            label = { Text("Ver detalle") },
                            colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
                        )
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Vehículos",
                    value = state.vehiculosRegistrados.toString(),
                    subtitle = "Registrados",
                    icon = Icons.Filled.DirectionsCar,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { onNavegar(Destinos.VEHICULOS) }
                )
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Mantenimientos",
                    value = state.proximosMantenimientos.size.toString(),
                    subtitle = "Próximos",
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = { onNavegar(Destinos.PLAN_MANTENIMIENTO) }
                )
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Alertas",
                    value = state.alertasActivas.toString(),
                    subtitle = "Activas",
                    icon = Icons.Filled.Notifications,
                    tint = MaterialTheme.colorScheme.tertiary,
                    onClick = { onNavegar(Destinos.ALERTAS) }
                )
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Documentos",
                    value = state.documentosProximos.toString(),
                    subtitle = "Próximos a vencer",
                    icon = Icons.Filled.Schedule,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { onNavegar(Destinos.ALERTAS) }
                )
            }

            SeccionVehiculos(state.vehiculosConAlertas) { onNavegar(Destinos.ALERTAS) }
            SeccionProximos(state.proximosMantenimientos) { onNavegar(Destinos.PLAN_MANTENIMIENTO) }
            SeccionGastos(
                recientes = state.mantenimientosRecientes,
                gastoTrimestre = state.gastoUltimosTresMeses,
                onVerHistorial = { onNavegar(Destinos.HISTORIAL) }
            )

            Text("Módulos", style = MaterialTheme.typography.titleMedium)
            val modulos = listOf(
                ModuloInicio("Vehículos", Icons.Filled.DirectionsCar, Destinos.VEHICULOS),
                ModuloInicio("Kilometraje", Icons.Filled.Speed, Destinos.KILOMETRAJE),
                ModuloInicio("Agenda", Icons.Filled.Event, Destinos.AGENDA),
                ModuloInicio("Plan", Icons.AutoMirrored.Filled.EventNote, Destinos.PLAN_MANTENIMIENTO),
                ModuloInicio("Mantenimientos", Icons.Filled.Build, Destinos.MANTENIMIENTOS),
                ModuloInicio("Alertas", Icons.Filled.Notifications, Destinos.ALERTAS),
                ModuloInicio("Historial", Icons.Filled.History, Destinos.HISTORIAL),
                ModuloInicio("Respaldo", Icons.Filled.Backup, Destinos.RESPALDO),
                ModuloInicio("Ajustes", Icons.Filled.Settings, Destinos.AJUSTES)
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
                            .semantics { contentDescription = modulo.titulo },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = modulo.icono,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(text = modulo.titulo, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                if (modulos.size % 2 != 0) {
                    Spacer(Modifier.weight(1f))
                }
            }

            SeccionGuias(onNavegar)
        }
    }
}

private data class ModuloInicio(
    val titulo: String,
    val icono: ImageVector,
    val ruta: String?
)

@Composable
private fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.labelMedium)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint
                )
            }
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("No hay mantenimientos próximos ni vencidos.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    actividades.take(4).forEach { item ->
        val vencida = item.estado == EstadoActividad.VENCIDA
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onVerPlan() },
            colors = CardDefaults.cardColors(
                containerColor = if (vencida) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (vencida) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (vencida) Icons.Filled.Warning else Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = if (vencida) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
}

@Composable
private fun SeccionGastos(
    recientes: List<EntradaHistorial>,
    gastoTrimestre: Double,
    onVerHistorial: () -> Unit
) {
    Text("Gastos recientes", style = MaterialTheme.typography.titleMedium)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onVerHistorial() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Últimos 3 meses", style = MaterialTheme.typography.labelLarge)
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(Formatos.moneda(gastoTrimestre), style = MaterialTheme.typography.headlineSmall)
            if (recientes.isEmpty()) {
                Text("Aún no has registrado mantenimientos.", style = MaterialTheme.typography.bodyMedium)
            } else {
                recientes.forEach { entrada ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${Formatos.fecha(entrada.fecha)} · ${entrada.descripcion}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            Formatos.moneda(entrada.costoTotal),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
