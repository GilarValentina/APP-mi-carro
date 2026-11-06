package com.example.carro.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Campo de fecha que se diligencia desde un calendario (RNF-15).
 * El texto es de solo lectura para evitar formatos inválidos: el valor siempre
 * queda en formato ISO (AAAA-MM-DD) o vacío cuando la fecha es opcional.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    valor: String,
    onValorCambiado: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    permitirVacio: Boolean = false,
    esError: Boolean = false,
    textoApoyo: String? = null
) {
    var mostrandoCalendario by remember { mutableStateOf(false) }
    val seleccionada = remember(valor) { runCatching { LocalDate.parse(valor.trim()) }.getOrNull() }
    // Tocar cualquier parte del campo abre el calendario, no solo el ícono.
    val interaccion = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    androidx.compose.runtime.LaunchedEffect(interaccion) {
        interaccion.interactions.collect {
            if (it is androidx.compose.foundation.interaction.PressInteraction.Release) mostrandoCalendario = true
        }
    }

    OutlinedTextField(
        value = valor,
        onValueChange = {},
        readOnly = true,
        interactionSource = interaccion,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = esError,
        supportingText = textoApoyo?.let { { Text(it) } },
        trailingIcon = {
            IconButton(onClick = { mostrandoCalendario = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Abrir calendario")
            }
        },
        modifier = modifier.semantics { contentDescription = etiqueta }
    )

    if (mostrandoCalendario) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = (seleccionada ?: LocalDate.now()).aMillis()
        )
        DatePickerDialog(
            onDismissRequest = { mostrandoCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let { onValorCambiado(it.aFechaLocal().toString()) }
                        mostrandoCalendario = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                if (permitirVacio) {
                    TextButton(
                        onClick = {
                            onValorCambiado("")
                            mostrandoCalendario = false
                        }
                    ) { Text("Sin fecha") }
                } else {
                    TextButton(onClick = { mostrandoCalendario = false }) { Text("Cancelar") }
                }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}

private fun LocalDate.aMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.aFechaLocal(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
