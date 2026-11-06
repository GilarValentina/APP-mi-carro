package com.example.carro.ui.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.domain.agenda.Cita
import com.example.carro.domain.model.Anticipaciones
import com.example.carro.domain.model.TipoCita
import com.example.carro.ui.componentes.CampoFecha
import com.example.carro.ui.componentes.CampoHora
import com.example.carro.ui.componentes.SelectorAnticipacion
import com.example.carro.ui.componentes.SkeletonCarga
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoDia = DateTimeFormatter.ofPattern("EEE d 'de' MMMM", Locale.forLanguageTag("es-CO"))
private val formatoHora = DateTimeFormatter.ofPattern("hh:mm a", Locale.forLanguageTag("es-CO"))

fun TipoCita.icono(): ImageVector = when (this) {
    TipoCita.TALLER -> Icons.Filled.Store
    TipoCita.TECNOMECANICA -> Icons.Filled.FactCheck
    TipoCita.SOAT -> Icons.Filled.HealthAndSafety
    TipoCita.SEGURO -> Icons.Filled.Shield
    TipoCita.MANTENIMIENTO -> Icons.Filled.Build
    TipoCita.OTRO -> Icons.Filled.Event
}

/** Lista de citas próximas y pasadas del vehículo principal. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    placa: String,
    viewModel: AgendaViewModel,
    onCrear: () -> Unit,
    onEditar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeUsuario.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<Cita?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let { snackbar.showSnackbar(it); viewModel.mensajeMostrado() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda · $placa") },
                navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onCrear, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nueva cita") })
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            state.cargando -> SkeletonCarga(Modifier.padding(padding))
            state.proximas.isEmpty() && state.pasadas.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Event, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(56.dp))
                    Text(
                        "No tienes citas programadas. Agenda el taller, la tecnomecánica, el SOAT o tu seguro y te lo recordaremos.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.proximas.isNotEmpty()) {
                    item { Text("Próximas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(state.proximas, key = { it.id }) { cita ->
                        CitaCard(cita, onEditar = { onEditar(cita.id) }, onCompletar = { viewModel.completar(cita) }, onEliminar = { porEliminar = cita })
                    }
                }
                if (state.pasadas.isNotEmpty()) {
                    item { Text("Realizadas o pasadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(state.pasadas, key = { it.id }) { cita ->
                        CitaCard(cita, onEditar = { onEditar(cita.id) }, onCompletar = { viewModel.completar(cita) }, onEliminar = { porEliminar = cita })
                    }
                }
            }
        }
    }

    porEliminar?.let { cita ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar la cita?") },
            text = { Text("${cita.titulo} · ${cita.fecha.format(formatoDia)}. Se cancelará su recordatorio.") },
            confirmButton = { TextButton(onClick = { viewModel.eliminar(cita); porEliminar = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CitaCard(cita: Cita, onEditar: () -> Unit, onCompletar: () -> Unit, onEliminar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (cita.completada) 0.dp else 2.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(cita.tipo.icono(), null, tint = MaterialTheme.colorScheme.onPrimary) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(cita.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${cita.fecha.format(formatoDia).replaceFirstChar { it.uppercase() }} · ${cita.hora.format(formatoHora)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                cita.lugar?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text(
                    if (cita.completada) "Realizada" else "${cita.tipo.etiqueta} · aviso ${cita.anticipacionDias} día(s) antes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onCompletar) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = if (cita.completada) "Reabrir cita" else "Marcar como realizada",
                            tint = if (cita.completada) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onEditar) { Icon(Icons.Filled.Edit, "Editar") }
                    IconButton(onClick = onEliminar) { Icon(Icons.Filled.Delete, "Eliminar") }
                }
            }
        }
    }
}

/** Formulario de cita: tipo, fecha (DatePicker), hora (TimePicker) y anticipación del aviso. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CitaFormScreen(
    viewModel: AgendaViewModel,
    citaId: Long?,
    onListo: () -> Unit
) {
    var tipo by remember { mutableStateOf(TipoCita.TALLER) }
    var titulo by remember { mutableStateOf(TipoCita.TALLER.etiqueta) }
    var tituloEditado by remember { mutableStateOf(false) }
    var lugar by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now().plusDays(7).toString()) }
    var hora by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var notas by remember { mutableStateOf("") }
    var anticipacion by remember { mutableStateOf(1) }
    var completada by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(citaId) {
        citaId?.let { id ->
            viewModel.obtener(id)?.let { c ->
                tipo = c.tipo; titulo = c.titulo; tituloEditado = true; lugar = c.lugar.orEmpty()
                fecha = c.fecha.toString(); hora = c.hora; notas = c.notas.orEmpty()
                anticipacion = c.anticipacionDias; completada = c.completada
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (citaId == null) "Nueva cita" else "Editar cita") },
                navigationIcon = { IconButton(onClick = onListo) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tipo de cita", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoCita.entries.forEach { opcion ->
                    FilterChip(
                        selected = tipo == opcion,
                        onClick = {
                            tipo = opcion
                            if (!tituloEditado) titulo = opcion.etiqueta
                        },
                        label = { Text(opcion.etiqueta) },
                        leadingIcon = { Icon(opcion.icono(), null, Modifier.size(18.dp)) }
                    )
                }
            }
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it; tituloEditado = true; error = null },
                label = { Text("Título *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text("Lugar o taller (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            CampoFecha(valor = fecha, onValorCambiado = { fecha = it; error = null }, etiqueta = "Fecha *", modifier = Modifier.fillMaxWidth())
            CampoHora(valor = hora, onValorCambiado = { hora = it; error = null }, etiqueta = "Hora *", modifier = Modifier.fillMaxWidth())
            Text("Recordarme con anticipación de:", style = MaterialTheme.typography.labelLarge)
            SelectorAnticipacion(Anticipaciones.opcionesDias, anticipacion, { anticipacion = it })
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas (opcional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            Button(
                onClick = {
                    error = viewModel.guardar(
                        Cita(
                            id = citaId ?: 0L,
                            vehiculoId = 0L,
                            tipo = tipo,
                            titulo = titulo,
                            lugar = lugar,
                            fecha = LocalDate.parse(fecha),
                            hora = hora,
                            notas = notas,
                            anticipacionDias = anticipacion,
                            completada = completada
                        ),
                        onGuardada = onListo
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar cita") }
        }
    }
}
