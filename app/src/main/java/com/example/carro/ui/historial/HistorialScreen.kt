package com.example.carro.ui.historial

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.domain.historial.EntradaHistorial
import com.example.carro.domain.vehiculo.Vehiculo

/**
 * Historial cronológico, búsqueda, filtros y resumen de gastos de un vehículo
 * (RF-34, RF-35, RF-36 y RF-38).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    vehiculo: Vehiculo,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeUsuario.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var pestania by remember { mutableIntStateOf(0) }
    var mostrarFiltros by remember { mutableStateOf(false) }

    // RF-38: el usuario elige dónde guardar el CSV mediante el selector del sistema.
    val exportar = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { destino -> destino?.let(viewModel::exportar) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Historial y resumen")
                        Text(
                            "${vehiculo.placa} · ${vehiculo.titulo}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { exportar.launch(viewModel.nombreArchivoExportacion()) },
                        enabled = state.entradas.isNotEmpty()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Exportar historial a CSV")
                    }
                    BadgedBox(
                        badge = {
                            if (state.filtro.activo) Badge { Text("${state.filtro.cantidadCriterios}") }
                        }
                    ) {
                        IconButton(onClick = { mostrarFiltros = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar historial")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = pestania) {
                Tab(
                    selected = pestania == 0,
                    onClick = { pestania = 0 },
                    text = { Text("Historial") }
                )
                Tab(
                    selected = pestania == 1,
                    onClick = { pestania = 1 },
                    text = { Text("Resumen") }
                )
            }

            if (pestania == 0) {
                BuscadorHistorial(
                    texto = state.filtro.texto,
                    onTextoCambiado = viewModel::buscar
                )
                if (state.filtro.activo) {
                    ResumenFiltroAplicado(
                        cantidad = state.entradas.size,
                        total = state.totalSinFiltrar,
                        onLimpiar = viewModel::limpiarFiltro
                    )
                }
                when {
                    state.vacio -> MensajeCentral(
                        "Aún no hay mantenimientos registrados para este vehículo."
                    )
                    state.sinResultados -> MensajeCentral(
                        "Ningún mantenimiento coincide con los filtros aplicados."
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.entradas, key = { it.mantenimientoId }) { entrada ->
                            TarjetaHistorial(entrada)
                        }
                    }
                }
            } else {
                ResumenGastosContenido(
                    resumen = state.resumen,
                    periodoSeleccionado = state.periodo,
                    onPeriodoCambiado = viewModel::cambiarPeriodo
                )
            }
        }
    }

    if (mostrarFiltros) {
        FiltroHistorialSheet(
            filtroActual = state.filtro,
            categorias = state.categoriasDisponibles,
            talleres = state.talleresDisponibles,
            costoMaximo = state.costoMaximoHistorial,
            onAplicar = {
                viewModel.aplicarFiltro(it)
                mostrarFiltros = false
            },
            onLimpiar = {
                viewModel.limpiarFiltro()
                mostrarFiltros = false
            },
            onCerrar = { mostrarFiltros = false }
        )
    }
}

@Composable
private fun BuscadorHistorial(texto: String, onTextoCambiado: (String) -> Unit) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoCambiado,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        label = { Text("Buscar en el historial") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onTextoCambiado("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Borrar búsqueda")
                }
            }
        }
    )
}

@Composable
private fun ResumenFiltroAplicado(cantidad: Int, total: Int, onLimpiar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Mostrando $cantidad de $total mantenimientos",
            style = MaterialTheme.typography.bodySmall
        )
        AssistChip(onClick = onLimpiar, label = { Text("Quitar filtros") })
    }
}

@Composable
private fun TarjetaHistorial(entrada: EntradaHistorial) {
    val descripcionAccesible = "${entrada.descripcion}, ${Formatos.fecha(entrada.fecha)}, " +
        "${Formatos.moneda(entrada.costoTotal)}"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = descripcionAccesible }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(Formatos.fecha(entrada.fecha), style = MaterialTheme.typography.labelLarge)
                Text(
                    Formatos.moneda(entrada.costoTotal),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(entrada.descripcion, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${entrada.categoria} · ${entrada.tipo.name.lowercase().replaceFirstChar { it.uppercase() }} · ${Formatos.kilometraje(entrada.kilometraje)}",
                style = MaterialTheme.typography.bodySmall
            )
            entrada.taller?.takeIf { it.isNotBlank() }?.let {
                Text("Taller: $it", style = MaterialTheme.typography.bodySmall)
            }
            if (entrada.cantidadRepuestos > 0) {
                Text(
                    "${entrada.cantidadRepuestos} repuesto(s) · mano de obra ${Formatos.moneda(entrada.costoManoObra)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MensajeCentral(texto: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
        Text(texto, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}
