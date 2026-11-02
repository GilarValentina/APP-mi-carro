package com.example.carro.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carro.domain.historial.FiltroHistorial
import com.example.carro.domain.model.TipoMantenimiento
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Panel de filtros del historial (RF-35): rango de fechas, categoría, tipo,
 * taller y rango de costo. Los criterios solo se aplican al confirmar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltroHistorialSheet(
    filtroActual: FiltroHistorial,
    categorias: List<String>,
    talleres: List<String>,
    costoMaximo: Double,
    onAplicar: (FiltroHistorial) -> Unit,
    onLimpiar: () -> Unit,
    onCerrar: () -> Unit
) {
    val estadoSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var desde by remember { mutableStateOf(filtroActual.desde?.format(FORMATO_FECHA).orEmpty()) }
    var hasta by remember { mutableStateOf(filtroActual.hasta?.format(FORMATO_FECHA).orEmpty()) }
    var seleccionCategorias by remember { mutableStateOf(filtroActual.categorias) }
    var tipo by remember { mutableStateOf(filtroActual.tipo) }
    var taller by remember { mutableStateOf(filtroActual.taller) }
    var costoMin by remember { mutableStateOf(filtroActual.costoMinimo?.toLong()?.toString().orEmpty()) }
    var costoMax by remember { mutableStateOf(filtroActual.costoMaximo?.toLong()?.toString().orEmpty()) }

    val fechaDesde = desde.aFecha()
    val fechaHasta = hasta.aFecha()
    val errorDesde = desde.isNotBlank() && fechaDesde == null
    val errorHasta = hasta.isNotBlank() && fechaHasta == null
    val rangoInvertido = fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)
    val montoMin = costoMin.toDoubleOrNull()
    val montoMax = costoMax.toDoubleOrNull()
    val costoInvertido = montoMin != null && montoMax != null && montoMin > montoMax
    val puedeAplicar = !errorDesde && !errorHasta && !rangoInvertido && !costoInvertido

    ModalBottomSheet(onDismissRequest = onCerrar, sheetState = estadoSheet) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Filtrar historial", style = MaterialTheme.typography.titleLarge)

            Text("Rango de fechas", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = desde,
                    onValueChange = { desde = it },
                    label = { Text("Desde") },
                    placeholder = { Text("dd/mm/aaaa") },
                    isError = errorDesde || rangoInvertido,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = hasta,
                    onValueChange = { hasta = it },
                    label = { Text("Hasta") },
                    placeholder = { Text("dd/mm/aaaa") },
                    isError = errorHasta || rangoInvertido,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            if (errorDesde || errorHasta) {
                TextoError("Usa el formato dd/mm/aaaa.")
            } else if (rangoInvertido) {
                TextoError("La fecha inicial no puede ser posterior a la final.")
            }

            if (categorias.isNotEmpty()) {
                Text("Categoría", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categorias.forEach { categoria ->
                        FilterChip(
                            selected = categoria in seleccionCategorias,
                            onClick = {
                                seleccionCategorias = if (categoria in seleccionCategorias) {
                                    seleccionCategorias - categoria
                                } else {
                                    seleccionCategorias + categoria
                                }
                            },
                            label = { Text(categoria) }
                        )
                    }
                }
            }

            Text("Tipo de mantenimiento", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoMantenimiento.entries.forEach { opcion ->
                    FilterChip(
                        selected = tipo == opcion,
                        onClick = { tipo = if (tipo == opcion) null else opcion },
                        label = { Text(opcion.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (talleres.isNotEmpty()) {
                Text("Taller o responsable", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    talleres.forEach { opcion ->
                        FilterChip(
                            selected = taller == opcion,
                            onClick = { taller = if (taller == opcion) null else opcion },
                            label = { Text(opcion) }
                        )
                    }
                }
            }

            Text(
                "Rango de costo (hasta ${Formatos.moneda(costoMaximo)})",
                style = MaterialTheme.typography.labelLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = costoMin,
                    onValueChange = { costoMin = it.filter(Char::isDigit) },
                    label = { Text("Mínimo") },
                    isError = costoInvertido,
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = costoMax,
                    onValueChange = { costoMax = it.filter(Char::isDigit) },
                    label = { Text("Máximo") },
                    isError = costoInvertido,
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            if (costoInvertido) {
                TextoError("El costo mínimo no puede superar al máximo.")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onLimpiar, modifier = Modifier.weight(1f)) {
                    Text("Limpiar")
                }
                Button(
                    onClick = {
                        onAplicar(
                            FiltroHistorial(
                                texto = filtroActual.texto,
                                desde = fechaDesde,
                                hasta = fechaHasta,
                                categorias = seleccionCategorias,
                                tipo = tipo,
                                taller = taller,
                                costoMinimo = montoMin,
                                costoMaximo = montoMax
                            )
                        )
                    },
                    enabled = puedeAplicar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aplicar")
                }
            }
        }
    }
}

@Composable
private fun TextoError(texto: String) {
    Text(texto, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}

private fun String.aFecha(): LocalDate? =
    takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it, FORMATO_FECHA) }.getOrNull() }
