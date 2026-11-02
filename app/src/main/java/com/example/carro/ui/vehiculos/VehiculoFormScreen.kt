package com.example.carro.ui.vehiculos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.domain.model.TipoVehiculo
import com.example.carro.domain.vehiculo.CampoVehiculo
import com.example.carro.domain.vehiculo.VehiculoCatalog

private val COMBUSTIBLES = listOf("Gasolina", "Diésel", "Gas", "Híbrido", "Eléctrico")
private val CIUDADES_PICO_PLACA = listOf("Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena", "Pereira")

/** Formulario de registro y edición de vehículos (RF-01, RF-02, RF-05). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VehiculoFormScreen(
    viewModel: VehiculoFormViewModel,
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val f = state.formulario
    val errores = state.errores
    val marcas = VehiculoCatalog.marcas
    val lineas = VehiculoCatalog.lineasDeMarca(f.marca)
    var marcaExpandida by remember { mutableStateOf(false) }
    var lineaExpandida by remember { mutableStateOf(false) }
    var ciudadExpandida by remember { mutableStateOf(false) }

    LaunchedEffect(state.guardado) {
        if (state.guardado) onGuardado()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.editando) "Editar vehículo" else "Nuevo vehículo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (state.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tipo de vehículo", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoVehiculo.entries.forEach { tipo ->
                    FilterChip(
                        selected = f.tipo == tipo,
                        onClick = { viewModel.actualizar { it.copy(tipo = tipo) } },
                        label = { Text(tipo.etiqueta) }
                    )
                }
            }

            CampoTexto(
                valor = f.placa,
                etiqueta = "Placa *",
                error = errores[CampoVehiculo.PLACA],
                capitalizacion = KeyboardCapitalization.Characters,
                onCambio = { v ->
                    val normalizada = v.filter { it.isLetterOrDigit() }.uppercase()
                    viewModel.actualizar(CampoVehiculo.PLACA) { it.copy(placa = normalizada) }
                }
            )

            Text("Marca *", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = marcaExpandida,
                onExpandedChange = { marcaExpandida = it }
            ) {
                OutlinedTextField(
                    value = f.marca,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Marca") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = marcaExpandida) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    isError = errores[CampoVehiculo.MARCA] != null
                )
                ExposedDropdownMenu(
                    expanded = marcaExpandida,
                    onDismissRequest = { marcaExpandida = false }
                ) {
                    marcas.forEach { marca ->
                        DropdownMenuItem(
                            text = { Text(marca) },
                            onClick = {
                                viewModel.actualizar(CampoVehiculo.MARCA) {
                                    it.copy(marca = marca, linea = "")
                                }
                                marcaExpandida = false
                            }
                        )
                    }
                }
            }
            if (errores[CampoVehiculo.MARCA] != null) {
                Text(errores[CampoVehiculo.MARCA]!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Text("Línea *", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = lineaExpandida,
                onExpandedChange = { if (f.marca.isNotBlank()) lineaExpandida = it }
            ) {
                OutlinedTextField(
                    value = f.linea,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Línea") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lineaExpandida) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    enabled = f.marca.isNotBlank(),
                    isError = errores[CampoVehiculo.LINEA] != null,
                    supportingText = if (f.marca.isBlank()) { { Text("Seleccione primero la marca") } } else null
                )
                ExposedDropdownMenu(
                    expanded = lineaExpandida && f.marca.isNotBlank(),
                    onDismissRequest = { lineaExpandida = false }
                ) {
                    lineas.forEach { linea ->
                        DropdownMenuItem(
                            text = { Text(linea) },
                            onClick = {
                                viewModel.actualizar(CampoVehiculo.LINEA) { it.copy(linea = linea) }
                                lineaExpandida = false
                            }
                        )
                    }
                }
            }
            if (errores[CampoVehiculo.LINEA] != null) {
                Text(errores[CampoVehiculo.LINEA]!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            CampoTexto(
                valor = f.anio,
                etiqueta = "Modelo (año) *",
                error = errores[CampoVehiculo.ANIO],
                teclado = KeyboardType.Number,
                onCambio = { v ->
                    viewModel.actualizar(CampoVehiculo.ANIO) { it.copy(anio = v.filter(Char::isDigit).take(4)) }
                }
            )
            CampoTexto(
                valor = f.kilometraje,
                etiqueta = "Kilometraje actual *",
                ayuda = if (state.editando) "Se actualiza desde el módulo de kilometraje" else null,
                error = errores[CampoVehiculo.KILOMETRAJE],
                teclado = KeyboardType.Number,
                habilitado = !state.editando,
                onCambio = { v ->
                    viewModel.actualizar(CampoVehiculo.KILOMETRAJE) { it.copy(kilometraje = v.filter(Char::isDigit).take(7)) }
                }
            )

            Text("Pico y placa", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = ciudadExpandida,
                onExpandedChange = { ciudadExpandida = it }
            ) {
                OutlinedTextField(
                    value = f.ciudadPicoPlaca,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ciudad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ciudadExpandida) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = ciudadExpandida,
                    onDismissRequest = { ciudadExpandida = false }
                ) {
                    CIUDADES_PICO_PLACA.forEach { ciudad ->
                        DropdownMenuItem(
                            text = { Text(ciudad) },
                            onClick = {
                                val descripcion = VehiculoCatalog.descripcionPicoPlaca(ciudad, f.tipo)
                                viewModel.actualizar {
                                    it.copy(
                                        ciudadPicoPlaca = ciudad,
                                        picoYPlaca = descripcion
                                    )
                                }
                                ciudadExpandida = false
                            }
                        )
                    }
                }
            }
            if (f.picoYPlaca.isNotBlank()) {
                Text(
                    text = f.picoYPlaca,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text("Datos opcionales", style = MaterialTheme.typography.labelLarge)
            CampoTexto(
                valor = f.color,
                etiqueta = "Color",
                onCambio = { v -> viewModel.actualizar { it.copy(color = v) } }
            )
            Text("Combustible", style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                COMBUSTIBLES.forEach { combustible ->
                    FilterChip(
                        selected = f.combustible == combustible,
                        onClick = {
                            viewModel.actualizar {
                                it.copy(combustible = combustible.takeIf { c -> c != it.combustible })
                            }
                        },
                        label = { Text(combustible) }
                    )
                }
            }
            CampoTexto(
                valor = f.cilindraje,
                etiqueta = "Cilindraje (cc)",
                teclado = KeyboardType.Number,
                onCambio = { v -> viewModel.actualizar { it.copy(cilindraje = v.filter(Char::isDigit).take(5)) } }
            )
            CampoTexto(
                valor = f.vin,
                etiqueta = "VIN",
                ayuda = "Número de identificación de 17 caracteres",
                error = errores[CampoVehiculo.VIN],
                capitalizacion = KeyboardCapitalization.Characters,
                onCambio = { v -> viewModel.actualizar(CampoVehiculo.VIN) { it.copy(vin = v) } }
            )

            Button(
                onClick = viewModel::guardar,
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    etiqueta: String,
    onCambio: (String) -> Unit,
    error: String? = null,
    ayuda: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    habilitado: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = (error ?: ayuda)?.let { texto -> { Text(texto) } },
        singleLine = true,
        enabled = habilitado,
        keyboardOptions = KeyboardOptions(capitalization = capitalizacion, keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
}
