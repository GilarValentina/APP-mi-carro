package com.example.carro.ui.vehiculos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.domain.vehiculo.Vehiculo
import com.example.carro.ui.componentes.SkeletonCarga
import java.text.NumberFormat

/**
 * Listado de vehículos: principal (RF-04), edición (RF-02), eliminación (RF-03)
 * y archivo/restauración sin perder el historial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosScreen(
    viewModel: VehiculosViewModel,
    onCrear: () -> Unit,
    onEditar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeUsuario.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<Vehiculo?>(null) }
    var pestana by remember { mutableIntStateOf(0) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis vehículos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrear,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Registrar vehículo") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = pestana, containerColor = MaterialTheme.colorScheme.background) {
                Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = { Text("Activos (${state.vehiculos.size})") })
                Tab(selected = pestana == 1, onClick = { pestana = 1 }, text = { Text("Archivados (${state.archivados.size})") })
            }
            val lista = if (pestana == 0) state.vehiculos else state.archivados
            when {
                state.cargando -> SkeletonCarga(bloques = listOf(120.dp, 120.dp, 120.dp))

                lista.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            if (pestana == 0) Icons.Filled.DirectionsCar else Icons.Filled.Archive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = if (pestana == 0) {
                                "Aún no tienes vehículos activos. Registra el primero con el botón de abajo."
                            } else {
                                "No tienes vehículos archivados."
                            },
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 96.dp)
                ) {
                    items(lista, key = { it.id }) { vehiculo ->
                        VehiculoCard(
                            vehiculo = vehiculo,
                            onPrincipal = { viewModel.seleccionarPrincipal(vehiculo) },
                            onEditar = { onEditar(vehiculo.id) },
                            onArchivar = { if (vehiculo.archivado) viewModel.restaurar(vehiculo) else viewModel.archivar(vehiculo) },
                            onEliminar = { porEliminar = vehiculo }
                        )
                    }
                }
            }
        }
    }

    porEliminar?.let { vehiculo ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar ${vehiculo.placa}?") },
            text = {
                Text(
                    "Se borrarán también su kilometraje, plan de mantenimiento, " +
                        "servicios, repuestos, documentos, citas y alertas. Esta acción no se puede deshacer. " +
                        "Si solo quieres ocultarlo, usa Archivar."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(vehiculo)
                    porEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun VehiculoCard(
    vehiculo: Vehiculo,
    onPrincipal: () -> Unit,
    onEditar: () -> Unit,
    onArchivar: () -> Unit,
    onEliminar: () -> Unit
) {
    val km = remember(vehiculo.kilometrajeActual) {
        NumberFormat.getIntegerInstance().format(vehiculo.kilometrajeActual)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (vehiculo.tipo.esMoto) Icons.Filled.TwoWheeler else Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(vehiculo.placa, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(vehiculo.titulo, style = MaterialTheme.typography.bodyLarge)
                }
                if (vehiculo.esPrincipal) {
                    Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                        Text("Principal", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Text(
                buildString {
                    append("${vehiculo.tipo.etiqueta} · $km km")
                    vehiculo.ciudadMatricula?.let { append(" · Matrícula: $it") }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            vehiculo.secretariaTransito?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (!vehiculo.archivado) {
                    IconButton(onClick = onPrincipal) {
                        Icon(
                            if (vehiculo.esPrincipal) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = if (vehiculo.esPrincipal) "Vehículo principal" else "Marcar como principal",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
                IconButton(onClick = onArchivar) {
                    Icon(
                        if (vehiculo.archivado) Icons.Filled.Unarchive else Icons.Filled.Archive,
                        contentDescription = if (vehiculo.archivado) "Restaurar" else "Archivar"
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
