package com.example.carro.ui.componentes

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.carro.domain.model.EstadoActividad
import com.example.carro.ui.theme.ErrorRed
import com.example.carro.ui.theme.Positive
import com.example.carro.ui.theme.WarningAmber
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Selector desplegable de solo lectura: el usuario elige una opción en lugar de escribir (RNF-16).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorDesplegable(
    etiqueta: String,
    opciones: List<T>,
    seleccionado: T?,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    texto: (T) -> String = { it.toString() },
    habilitado: Boolean = true,
    error: String? = null,
    ayuda: String? = null,
    /** Listas largas (marcas, ciudades): se puede escribir para filtrar, pero solo se guarda una opción de la lista. */
    buscable: Boolean = false
) {
    var expandido by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }
    val textoSeleccionado = seleccionado?.let(texto).orEmpty()
    val filtradas = if (buscable && busqueda.isNotBlank()) {
        opciones.filter { texto(it).contains(busqueda.trim(), ignoreCase = true) }
    } else {
        opciones
    }
    ExposedDropdownMenuBox(
        expanded = expandido && habilitado,
        onExpandedChange = {
            if (habilitado) {
                expandido = it
                if (it) busqueda = ""
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (buscable && expandido) busqueda else textoSeleccionado,
            onValueChange = { if (buscable) { busqueda = it; expandido = true } },
            readOnly = !buscable,
            enabled = habilitado,
            label = { Text(etiqueta) },
            placeholder = if (buscable && expandido) { { Text(textoSeleccionado.ifBlank { "Escribe para buscar" }) } } else null,
            isError = error != null,
            supportingText = (error ?: ayuda)?.let { t -> { Text(t) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(
                    if (buscable) ExposedDropdownMenuAnchorType.PrimaryEditable else ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    habilitado
                )
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido && habilitado, onDismissRequest = { expandido = false }) {
            if (filtradas.isEmpty()) {
                DropdownMenuItem(text = { Text("Sin resultados") }, onClick = {}, enabled = false)
            }
            filtradas.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(texto(opcion)) },
                    onClick = {
                        onSeleccion(opcion)
                        busqueda = ""
                        expandido = false
                    }
                )
            }
        }
    }
}

private val formatoHora: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

/** Campo de hora diligenciado con el reloj de Material 3; nunca se escribe a mano. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoHora(
    valor: LocalTime,
    onValorCambiado: (LocalTime) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    var mostrando by remember { mutableStateOf(false) }
    val interaccion = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    androidx.compose.runtime.LaunchedEffect(interaccion) {
        interaccion.interactions.collect {
            if (it is androidx.compose.foundation.interaction.PressInteraction.Release) mostrando = true
        }
    }
    OutlinedTextField(
        value = valor.format(formatoHora),
        onValueChange = {},
        readOnly = true,
        interactionSource = interaccion,
        label = { Text(etiqueta) },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { mostrando = true }) {
                Icon(Icons.Filled.Schedule, contentDescription = "Abrir reloj")
            }
        },
        modifier = modifier.semantics { contentDescription = etiqueta }
    )
    if (mostrando) {
        val estado = rememberTimePickerState(initialHour = valor.hour, initialMinute = valor.minute, is24Hour = false)
        AlertDialog(
            onDismissRequest = { mostrando = false },
            confirmButton = {
                TextButton(onClick = {
                    onValorCambiado(LocalTime.of(estado.hour, estado.minute))
                    mostrando = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrando = false }) { Text("Cancelar") } },
            text = { TimePicker(state = estado) }
        )
    }
}

/** Chips de anticipación del recordatorio (1, 3, 7, 15 o 30 días). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorAnticipacion(
    opciones: List<Int>,
    seleccionado: Int?,
    onSeleccion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { dias ->
            FilterChip(
                selected = seleccionado == dias,
                onClick = { onSeleccion(dias) },
                label = { Text(if (dias == 1) "1 día" else "$dias días") }
            )
        }
    }
}

/** Color semántico del estado: verde al día, amarillo próximo, rojo vencido. */
fun colorEstado(estado: EstadoActividad): Color = when (estado) {
    EstadoActividad.AL_DIA -> Positive
    EstadoActividad.PROXIMA -> WarningAmber
    EstadoActividad.VENCIDA -> ErrorRed
    EstadoActividad.SIN_PROGRAMACION -> Color(0xFF9AA3AE)
}

/** Barra de progreso redondeada con el color del estado. */
@Composable
fun BarraProgreso(fraccion: Float, color: Color, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { fraccion },
        color = color,
        trackColor = color.copy(alpha = 0.18f),
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {},
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
    )
}

/** Bloques animados que ocupan el lugar del contenido mientras carga (skeleton loading). */
@Composable
fun SkeletonCarga(modifier: Modifier = Modifier, bloques: List<Dp> = listOf(140.dp, 72.dp, 72.dp, 120.dp)) {
    val transicion = rememberInfiniteTransition(label = "skeleton")
    val alfa by transicion.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alfa"
    )
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        bloques.forEach { alto ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(alto)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alfa))
            )
        }
    }
}
