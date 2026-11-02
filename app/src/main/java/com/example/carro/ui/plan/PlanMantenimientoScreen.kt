package com.example.carro.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.domain.model.EstadoActividad
import com.example.carro.domain.plan.ActividadConEstado

/** Pantalla de listado del plan de mantenimiento (RF-14, RF-15). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanMantenimientoScreen(
    viewModel: PlanMantenimientoViewModel,
    onCrear: () -> Unit,
    onEditar: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeUsuario.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Plan de mantenimiento") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrear) {
                Icon(Icons.Default.Add, contentDescription = "Nueva actividad")
            }
        }
    ) { padding ->
        when {
            state.cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.actividades.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay actividades. Agrega la primera con el botón +.",
                    textAlign = TextAlign.Center
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.actividades, key = { it.actividad.id }) { item ->
                    ActividadCard(
                        item = item,
                        onEditar = { onEditar(item.actividad.id) },
                        onPausarReactivar = { viewModel.pausarReactivar(item) },
                        onEliminar = { viewModel.eliminar(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActividadCard(
    item: ActividadConEstado,
    onEditar: () -> Unit,
    onPausarReactivar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.actividad.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                EstadoBadge(item.estado)
            }
            item.actividad.descripcion?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            if (!item.actividad.activa) {
                Text("Pausada", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onPausarReactivar) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Pausar o reactivar")
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
    val (texto, color) = when (estado) {
        EstadoActividad.AL_DIA -> "Al día" to Color(0xFF2E7D32)
        EstadoActividad.PROXIMA -> "Próxima" to Color(0xFFF9A825)
        EstadoActividad.VENCIDA -> "Vencida" to Color(0xFFC62828)
        EstadoActividad.SIN_PROGRAMACION -> "Sin programación" to Color(0xFF757575)
    }
    Badge(containerColor = color, contentColor = Color.White) {
        Text(texto, modifier = Modifier.padding(horizontal = 6.dp))
    }
}
