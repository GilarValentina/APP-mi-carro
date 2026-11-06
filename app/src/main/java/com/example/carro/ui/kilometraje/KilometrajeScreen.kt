package com.example.carro.ui.kilometraje

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.domain.kilometraje.EstadisticasKilometraje
import com.example.carro.domain.kilometraje.LecturaKilometraje
import com.example.carro.domain.vehiculo.Vehiculo
import com.example.carro.ui.componentes.CampoFecha
import com.example.carro.ui.componentes.SkeletonCarga
import com.example.carro.ui.historial.Formatos
import com.example.carro.ui.theme.WarningAmber
import java.time.LocalDate
import kotlin.math.roundToLong

/** Módulo de kilometraje: registro, historial, evolución y estadísticas (RF-06 a RF-09). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KilometrajeScreen(
    vehiculo: Vehiculo,
    viewModel: KilometrajeViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeUsuario.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var registrando by remember { mutableStateOf(false) }
    var porEliminar by remember { mutableStateOf<LecturaKilometraje?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kilometraje · ${vehiculo.placa}") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.limpiarError(); registrando = true },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Registrar lectura") }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.cargando) {
            SkeletonCarga(Modifier.padding(padding))
            return@Scaffold
        }
        val stats = state.estadisticas
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TarjetaActual(vehiculo, stats) }
            if (stats?.requiereActualizacion == true) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.18f))) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "Hace ${stats.diasSinActualizar} días no actualizas el odómetro. " +
                                    "Regístralo para que tus alertas de mantenimiento sean precisas.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            item { TarjetaEstadisticas(stats) }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Evolución del odómetro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (state.lecturas.size < 2) {
                            Text(
                                "Registra al menos dos lecturas para ver la gráfica.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            GraficaKilometraje(state.lecturas)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                val orden = state.lecturas.sortedBy { it.fecha }
                                Text(Formatos.fecha(orden.first().fecha), style = MaterialTheme.typography.labelSmall)
                                Text(Formatos.fecha(orden.last().fecha), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            item { Text("Historial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(state.lecturas, key = { it.id }) { lectura ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Speed, null, tint = MaterialTheme.colorScheme.secondary)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(Formatos.kilometraje(lectura.valor), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(Formatos.fecha(lectura.fecha), style = MaterialTheme.typography.bodySmall)
                        }
                        // La única lectura no se elimina: el vehículo siempre conserva un odómetro.
                        if (state.lecturas.size > 1) {
                            IconButton(onClick = { porEliminar = lectura }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar lectura")
                            }
                        }
                    }
                }
            }
        }
    }

    if (registrando) {
        DialogoLectura(
            actual = state.estadisticas?.actual ?: vehiculo.kilometrajeActual,
            error = error,
            onCancelar = { registrando = false },
            onGuardar = { valor, fecha -> viewModel.registrar(valor, fecha) { registrando = false } },
            onEditar = viewModel::limpiarError
        )
    }

    porEliminar?.let { lectura ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar lectura?") },
            text = { Text("${Formatos.kilometraje(lectura.valor)} del ${Formatos.fecha(lectura.fecha)}. El odómetro se recalculará.") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminar(lectura); porEliminar = null }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaActual(vehiculo: Vehiculo, stats: EstadisticasKilometraje?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Kilometraje actual", style = MaterialTheme.typography.labelLarge)
            Text(
                Formatos.kilometraje(stats?.actual ?: vehiculo.kilometrajeActual),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                stats?.ultimaActualizacion?.let { "Última actualización: ${Formatos.fecha(it)}" } ?: "Sin lecturas registradas",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TarjetaEstadisticas(stats: EstadisticasKilometraje?) {
    Row(
        modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Estadistica(
            "Promedio diario",
            stats?.promedioDiario?.let { "${it.roundToLong()} km" } ?: "—",
            Modifier.weight(1f)
        )
        Estadistica(
            "Promedio mensual",
            stats?.promedioMensual?.let { Formatos.kilometraje(it.roundToLong()) } ?: "—",
            Modifier.weight(1f)
        )
        Estadistica(
            "Recorrido total",
            stats?.let { Formatos.kilometraje(it.totalRecorrido) } ?: "—",
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun Estadistica(titulo: String, valor: String, modifier: Modifier) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/** Gráfica de línea del odómetro en el tiempo, dibujada con Canvas (sin librerías externas). */
@Composable
private fun GraficaKilometraje(lecturas: List<LecturaKilometraje>) {
    val ordenadas = remember(lecturas) { lecturas.sortedWith(compareBy({ it.fecha }, { it.valor })) }
    val linea = MaterialTheme.colorScheme.secondary
    val relleno = MaterialTheme.colorScheme.tertiary
    val guia = MaterialTheme.colorScheme.outline
    val descripcion = "Gráfica de kilometraje: de ${ordenadas.first().valor} a ${ordenadas.last().valor} km"
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .semantics { contentDescription = descripcion }
    ) {
        val primeraFecha = ordenadas.first().fecha.toEpochDay()
        val rangoDias = (ordenadas.last().fecha.toEpochDay() - primeraFecha).coerceAtLeast(1)
        val minKm = ordenadas.minOf { it.valor }
        val rangoKm = (ordenadas.maxOf { it.valor } - minKm).coerceAtLeast(1)
        val margen = 8.dp.toPx()
        fun punto(l: LecturaKilometraje) = Offset(
            x = margen + (l.fecha.toEpochDay() - primeraFecha).toFloat() / rangoDias * (size.width - 2 * margen),
            y = size.height - margen - (l.valor - minKm).toFloat() / rangoKm * (size.height - 2 * margen)
        )
        repeat(4) { i ->
            val y = margen + i * (size.height - 2 * margen) / 3
            drawLine(guia.copy(alpha = 0.4f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        val puntos = ordenadas.map(::punto)
        val trazo = Path().apply {
            moveTo(puntos.first().x, puntos.first().y)
            puntos.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val area = Path().apply {
            addPath(trazo)
            lineTo(puntos.last().x, size.height)
            lineTo(puntos.first().x, size.height)
            close()
        }
        drawPath(area, Brush.verticalGradient(listOf(relleno.copy(alpha = 0.35f), Color.Transparent)))
        drawPath(trazo, linea, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        puntos.forEach { drawCircle(linea, radius = 4.dp.toPx(), center = it) }
    }
}

@Composable
private fun DialogoLectura(
    actual: Long,
    error: String?,
    onCancelar: () -> Unit,
    onGuardar: (Long?, LocalDate) -> Unit,
    onEditar: () -> Unit
) {
    var valor by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now().toString()) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nueva lectura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Último registro: ${Formatos.kilometraje(actual)}", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = valor,
                    onValueChange = { valor = it.filter(Char::isDigit).take(7); onEditar() },
                    label = { Text("Kilometraje del odómetro") },
                    suffix = { Text("km") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                CampoFecha(valor = fecha, onValorCambiado = { fecha = it; onEditar() }, etiqueta = "Fecha de la lectura")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardar(valor.toLongOrNull(), runCatching { LocalDate.parse(fecha) }.getOrDefault(LocalDate.now()))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
