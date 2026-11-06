package com.example.carro.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.domain.model.EstadoActividad
import com.example.carro.domain.plan.ActividadConEstado
import com.example.carro.domain.plan.ProgresoCalculator
import com.example.carro.ui.componentes.BarraProgreso
import com.example.carro.ui.componentes.SkeletonCarga
import com.example.carro.ui.componentes.colorEstado
import com.example.carro.ui.historial.Formatos

/**
 * Plan de mantenimiento (RF-14, RF-15, RF-16) con barras de progreso por kilometraje:
 * verde al día, amarillo próximo y rojo vencido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanMantenimientoScreen(
    viewModel: PlanMantenimientoViewModel,
    onCrear: () -> Unit,
    onEditar: (Long) -> Unit,
    onVolver: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeUsuario.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<ActividadConEstado?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan de mantenimiento") },
                navigationIcon = {
                    onVolver?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::aplicarPlanBase) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Aplicar plan preventivo recomendado")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrear) {
                Icon(Icons.Default.Add, contentDescription = "Nueva actividad")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            state.cargando -> SkeletonCarga(Modifier.padding(padding))

            state.actividades.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Filled.Build, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(56.dp))
                    Text(
                        "Aún no hay actividades. Aplica el plan preventivo recomendado " +
                            "(cada 5.000, 10.000 y 20.000 km) o crea las tuyas con el botón +.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = viewModel::aplicarPlanBase) { Text("Aplicar plan preventivo") }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { ResumenEstados(state.actividades, viewModel.kilometrajeActual) }
                // Primero lo urgente: vencidas, luego próximas, al día y sin programación.
                items(
                    state.actividades.sortedBy { it.estado.ordenUrgencia() },
                    key = { it.actividad.id }
                ) { item ->
                    ActividadCard(
                        item = item,
                        kmActual = viewModel.kilometrajeActual,
                        onEditar = { onEditar(item.actividad.id) },
                        onPausarReactivar = { viewModel.pausarReactivar(item) },
                        onEliminar = { porEliminar = item }
                    )
                }
            }
        }
    }

    porEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar \"${item.actividad.nombre}\"?") },
            text = { Text("La actividad dejará de generar alertas. Los mantenimientos ya registrados se conservan.") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminar(item); porEliminar = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

private fun EstadoActividad.ordenUrgencia(): Int = when (this) {
    EstadoActividad.VENCIDA -> 0
    EstadoActividad.PROXIMA -> 1
    EstadoActividad.AL_DIA -> 2
    EstadoActividad.SIN_PROGRAMACION -> 3
}

@Composable
private fun ResumenEstados(actividades: List<ActividadConEstado>, kmActual: Long) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Odómetro: ${Formatos.kilometraje(kmActual)}", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(EstadoActividad.AL_DIA, EstadoActividad.PROXIMA, EstadoActividad.VENCIDA).forEach { estado ->
                    Column {
                        Text(
                            actividades.count { it.estado == estado }.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorEstado(estado)
                        )
                        Text(estado.etiqueta(), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

private fun EstadoActividad.etiqueta(): String = when (this) {
    EstadoActividad.AL_DIA -> "Al día"
    EstadoActividad.PROXIMA -> "Próximo"
    EstadoActividad.VENCIDA -> "Vencido"
    EstadoActividad.SIN_PROGRAMACION -> "Sin programación"
}

@Composable
private fun ActividadCard(
    item: ActividadConEstado,
    kmActual: Long,
    onEditar: () -> Unit,
    onPausarReactivar: () -> Unit,
    onEliminar: () -> Unit
) {
    val progreso = ProgresoCalculator.calcular(item.actividad, kmActual)
    val color = colorEstado(item.estado)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.actividad.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                EstadoBadge(item.estado)
            }
            if (progreso != null) {
                BarraProgreso(progreso.fraccion, color)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${Formatos.kilometraje(progreso.recorridoEnIntervalo.coerceAtMost(progreso.intervalo))} / " +
                            Formatos.kilometraje(progreso.intervalo),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        if (progreso.restantes >= 0) "Faltan ${Formatos.kilometraje(progreso.restantes)}"
                        else "Excedido por ${Formatos.kilometraje(-progreso.restantes)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (progreso.restantes < 0) color else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (progreso == null && item.actividad.programarPorKm) {
                item.actividad.proximoKm?.let {
                    Text(
                        "Próximo a los ${Formatos.kilometraje(it)} (odómetro: ${Formatos.kilometraje(kmActual)})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            item.actividad.proximaFecha?.takeIf { item.actividad.programarPorFecha }?.let {
                Text("Próxima fecha: ${Formatos.fecha(it)}", style = MaterialTheme.typography.bodySmall)
            }
            item.actividad.descripcion?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!item.actividad.activa) {
                Text("Pausada", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onPausarReactivar) {
                    Icon(
                        if (item.actividad.activa) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Pausar o reactivar"
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

/** Insignia visual del estado de la actividad (RF-14). */
@Composable
private fun EstadoBadge(estado: EstadoActividad) {
    Badge(containerColor = colorEstado(estado), contentColor = if (estado == EstadoActividad.PROXIMA) Color.Black else Color.White) {
        Text(estado.etiqueta(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}
