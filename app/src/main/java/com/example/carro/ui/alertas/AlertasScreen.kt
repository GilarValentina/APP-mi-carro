package com.example.carro.ui.alertas

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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.domain.alerta.Alerta
import com.example.carro.domain.alerta.ConfiguracionAlertas
import com.example.carro.domain.alerta.DocumentoConEstado
import com.example.carro.domain.alerta.EstadoVencimiento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    viewModel: AlertasViewModel,
    onCrearDocumento: () -> Unit,
    onEditarDocumento: (Long) -> Unit,
    onRevisarAhora: () -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeUsuario.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var pestana by remember { mutableIntStateOf(0) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas y documentos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (pestana == 0) {
                FloatingActionButton(onClick = onCrearDocumento) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo documento")
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = pestana) {
                Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = { Text("Documentos") })
                Tab(selected = pestana == 1, onClick = { pestana = 1 }, text = { Text("Alertas") })
                Tab(selected = pestana == 2, onClick = { pestana = 2 }, text = { Text("Ajustes") })
            }
            when (pestana) {
                0 -> DocumentosTab(
                    items = state.documentos,
                    onEditar = onEditarDocumento,
                    onEliminar = { viewModel.eliminarDocumento(it.documento) }
                )
                1 -> AlertasTab(
                    alertas = state.alertas,
                    onPosponer = { alerta, dias -> viewModel.posponerAlerta(alerta, dias) },
                    onAtender = { viewModel.marcarAtendida(it) }
                )
                else -> AjustesTab(
                    configuracion = state.configuracion,
                    onConfiguracionCambiada = { viewModel.actualizarConfiguracion(it) },
                    onRevisarAhora = onRevisarAhora
                )
            }
        }
    }
}

@Composable
private fun DocumentosTab(
    items: List<DocumentoConEstado>,
    onEditar: (Long) -> Unit,
    onEliminar: (DocumentoConEstado) -> Unit
) {
    if (items.isEmpty()) {
        MensajeVacio("Aún no hay documentos. Registra SOAT, tecnomecánica, seguro y otros con el botón +.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.documento.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(item.documento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        EstadoChip(item.estado)
                    }
                    Text("Vence: ${item.documento.fechaVencimiento}", style = MaterialTheme.typography.bodySmall)
                    item.documento.fechaExpedicion?.let {
                        Text("Expedido: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(textoDias(item), style = MaterialTheme.typography.bodySmall)
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Row(Modifier.fillMaxWidth(), Arrangement.End) {
                        item.documento.adjuntoUri?.let { uri ->
                            IconButton(onClick = { abrirAdjunto(context, uri) }) {
                                Icon(Icons.Filled.AttachFile, contentDescription = "Ver adjunto")
                            }
                        }
                        IconButton(onClick = { onEditar(item.documento.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { onEliminar(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertasTab(
    alertas: List<Alerta>,
    onPosponer: (Alerta, Int) -> Unit,
    onAtender: (Alerta) -> Unit
) {
    if (alertas.isEmpty()) {
        MensajeVacio("No hay alertas activas. Todo está al día.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(alertas, key = { it.id }) { alerta ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Aviso", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Text(alerta.causa, style = MaterialTheme.typography.bodyMedium)
                    alerta.pospuestaHasta?.let {
                        Text("Pospuesta hasta: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onPosponer(alerta, 7) }) { Text("Posponer 7 d") }
                        OutlinedButton(onClick = { onPosponer(alerta, 30) }) { Text("30 d") }
                        IconButton(onClick = { onAtender(alerta) }) {
                            Icon(Icons.Default.Check, contentDescription = "Marcar atendida")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AjustesTab(
    configuracion: ConfiguracionAlertas,
    onConfiguracionCambiada: (ConfiguracionAlertas) -> Unit,
    onRevisarAhora: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Alertas activas (RF-29)", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = configuracion.alertasActivas,
                onCheckedChange = { onConfiguracionCambiada(configuracion.copy(alertasActivas = it)) }
            )
        }
        Text("Anticipación del aviso: ${configuracion.anticipacionDias} días", style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            listOf(7, 15, 30, 60).forEach { dias ->
                val seleccionado = configuracion.anticipacionDias == dias
                OutlinedButton(onClick = { onConfiguracionCambiada(configuracion.copy(anticipacionDias = dias)) }) {
                    Text(if (seleccionado) "• $dias" else "$dias")
                }
            }
        }
        OutlinedButton(onClick = onRevisarAhora, modifier = Modifier.fillMaxWidth()) {
            Text("Revisar vencimientos ahora")
        }
    }
}

@Composable
private fun EstadoChip(estado: EstadoVencimiento) {
    val (texto, color) = when (estado) {
        EstadoVencimiento.VENCIDO -> "Vencido" to MaterialTheme.colorScheme.error
        EstadoVencimiento.PROXIMO -> "Próximo" to Color(0xFFF57C00)
        EstadoVencimiento.VIGENTE -> "Vigente" to Color(0xFF2E7D32)
    }
    AssistChip(
        onClick = {},
        label = { Text(texto) },
        colors = AssistChipDefaults.assistChipColors(labelColor = color)
    )
}

@Composable
private fun textoDias(item: DocumentoConEstado): String = when {
    item.diasRestantes < 0 -> "Venció hace ${-item.diasRestantes} día(s)"
    item.diasRestantes == 0L -> "Vence hoy"
    else -> "Faltan ${item.diasRestantes} día(s)"
}

@Composable
private fun MensajeVacio(texto: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
