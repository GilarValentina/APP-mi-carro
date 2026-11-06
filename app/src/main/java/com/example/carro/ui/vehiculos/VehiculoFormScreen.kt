package com.example.carro.ui.vehiculos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.domain.model.TipoVehiculo
import com.example.carro.domain.vehiculo.CampoVehiculo
import com.example.carro.domain.vehiculo.CatalogoFormulario
import com.example.carro.domain.vehiculo.VehiculoCatalog
import com.example.carro.domain.vehiculo.VehiculoValidator
import com.example.carro.ui.componentes.SelectorDesplegable
import com.example.carro.ui.theme.Positive
import java.time.LocalDate

/**
 * Formulario de registro y edición de vehículos (RF-01, RF-02, RF-05).
 * Casi todos los campos son selectores para evitar errores de digitación;
 * la placa se valida mientras se escribe según el tipo de vehículo.
 */
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
    val anios = remember { CatalogoFormulario.anios(LocalDate.now().year) }

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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Seccion("Identificación") {
                Text("Tipo de vehículo", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoVehiculo.entries.forEach { tipo ->
                        FilterChip(
                            selected = f.tipo == tipo,
                            onClick = {
                                viewModel.actualizar(CampoVehiculo.PLACA) {
                                    it.copy(
                                        tipo = tipo,
                                        placa = VehiculoValidator.filtrarEntradaPlaca(it.placa, tipo),
                                        cilindraje = it.cilindraje.takeIf { c -> c in CatalogoFormulario.cilindrajes(tipo) }.orEmpty()
                                    )
                                }
                            },
                            label = { Text(tipo.etiqueta) }
                        )
                    }
                }
                val placaValida = VehiculoValidator.placaValida(f.placa, f.tipo)
                OutlinedTextField(
                    value = f.placa,
                    onValueChange = { v ->
                        // Transformar el texto mientras el teclado compone hace que se pierdan letras:
                        // si la entrada ya es válida se guarda tal cual y solo se corrige cuando trae
                        // caracteres no permitidos. Las mayúsculas se muestran con visualTransformation.
                        viewModel.actualizar(CampoVehiculo.PLACA) {
                            val filtrada = VehiculoValidator.filtrarEntradaPlaca(v, it.tipo)
                            it.copy(placa = if (filtrada.equals(v, ignoreCase = true)) v else filtrada)
                        }
                    },
                    visualTransformation = MayusculasTransformation,
                    label = { Text("Placa *") },
                    placeholder = { Text(f.tipo.ejemploPlaca) },
                    isError = errores[CampoVehiculo.PLACA] != null,
                    supportingText = {
                        Text(errores[CampoVehiculo.PLACA] ?: "Formato para ${f.tipo.etiqueta.lowercase()}: ${f.tipo.ejemploPlaca}")
                    },
                    trailingIcon = {
                        if (placaValida) Icon(Icons.Filled.CheckCircle, contentDescription = "Placa válida", tint = Positive)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )
                SelectorDesplegable(
                    etiqueta = "Marca *",
                    buscable = true,
                    opciones = VehiculoCatalog.marcas,
                    seleccionado = f.marca.ifBlank { null },
                    onSeleccion = { marca ->
                        viewModel.actualizar(CampoVehiculo.MARCA) { it.copy(marca = marca, linea = "") }
                    },
                    error = errores[CampoVehiculo.MARCA],
                    modifier = Modifier.fillMaxWidth()
                )
                SelectorDesplegable(
                    etiqueta = "Línea *",
                    buscable = true,
                    opciones = VehiculoCatalog.lineasDeMarca(f.marca),
                    seleccionado = f.linea.ifBlank { null },
                    onSeleccion = { linea -> viewModel.actualizar(CampoVehiculo.LINEA) { it.copy(linea = linea) } },
                    habilitado = f.marca.isNotBlank(),
                    error = errores[CampoVehiculo.LINEA],
                    ayuda = if (f.marca.isBlank()) "Selecciona primero la marca" else null,
                    modifier = Modifier.fillMaxWidth()
                )
                SelectorDesplegable(
                    etiqueta = "Modelo (año) *",
                    opciones = anios,
                    seleccionado = f.anio.toIntOrNull(),
                    onSeleccion = { anio -> viewModel.actualizar(CampoVehiculo.ANIO) { it.copy(anio = anio.toString()) } },
                    error = errores[CampoVehiculo.ANIO],
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = f.kilometraje,
                    onValueChange = { v ->
                        viewModel.actualizar(CampoVehiculo.KILOMETRAJE) { it.copy(kilometraje = v.filter(Char::isDigit).take(7)) }
                    },
                    label = { Text("Kilometraje actual *") },
                    suffix = { Text("km") },
                    isError = errores[CampoVehiculo.KILOMETRAJE] != null,
                    supportingText = (errores[CampoVehiculo.KILOMETRAJE]
                        ?: if (state.editando) "Se actualiza desde el módulo de kilometraje" else null)?.let { { Text(it) } },
                    enabled = !state.editando,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Seccion("Matrícula y tránsito") {
                SelectorDesplegable(
                    etiqueta = "Ciudad de matrícula",
                    buscable = true,
                    opciones = CatalogoFormulario.ciudades,
                    seleccionado = f.ciudadMatricula.ifBlank { null },
                    onSeleccion = { ciudad ->
                        viewModel.actualizar {
                            it.copy(
                                ciudadMatricula = ciudad,
                                secretariaTransito = CatalogoFormulario.secretarias(ciudad).first()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                SelectorDesplegable(
                    etiqueta = "Secretaría de tránsito",
                    opciones = CatalogoFormulario.secretarias(f.ciudadMatricula),
                    seleccionado = f.secretariaTransito.ifBlank { null },
                    onSeleccion = { s -> viewModel.actualizar { it.copy(secretariaTransito = s) } },
                    habilitado = f.ciudadMatricula.isNotBlank(),
                    ayuda = if (f.ciudadMatricula.isBlank()) "Selecciona primero la ciudad de matrícula" else null,
                    modifier = Modifier.fillMaxWidth()
                )
                SelectorDesplegable(
                    etiqueta = "Ciudad donde circula (pico y placa)",
                    buscable = true,
                    opciones = CatalogoFormulario.ciudades,
                    seleccionado = f.ciudadPicoPlaca.ifBlank { null },
                    onSeleccion = { ciudad ->
                        viewModel.actualizar {
                            it.copy(
                                ciudadPicoPlaca = ciudad,
                                picoYPlaca = VehiculoCatalog.descripcionPicoPlaca(ciudad, it.tipo)
                            )
                        }
                    },
                    ayuda = "El panel principal mostrará el pico y placa del día",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Seccion("Datos opcionales") {
                Text("Color", style = MaterialTheme.typography.labelLarge)
                SelectorColor(
                    seleccionado = f.color,
                    onSeleccion = { nombre ->
                        viewModel.actualizar { it.copy(color = if (it.color == nombre) "" else nombre) }
                    }
                )
                Text("Combustible", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatalogoFormulario.combustibles.forEach { combustible ->
                        FilterChip(
                            selected = f.combustible == combustible,
                            onClick = {
                                viewModel.actualizar {
                                    it.copy(
                                        combustible = combustible.takeIf { c -> c != it.combustible },
                                        cilindraje = if (combustible == "Eléctrico") "" else it.cilindraje
                                    )
                                }
                            },
                            label = { Text(combustible) }
                        )
                    }
                }
                SelectorDesplegable(
                    etiqueta = "Cilindraje",
                    opciones = CatalogoFormulario.cilindrajes(f.tipo),
                    seleccionado = f.cilindraje.ifBlank { null },
                    onSeleccion = { c -> viewModel.actualizar { it.copy(cilindraje = c) } },
                    texto = { "$it cc" },
                    habilitado = f.combustible != "Eléctrico",
                    ayuda = if (f.combustible == "Eléctrico") "No aplica para vehículos eléctricos" else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = f.vin,
                    onValueChange = { v ->
                        viewModel.actualizar(CampoVehiculo.VIN) {
                            it.copy(vin = v.uppercase().filter(Char::isLetterOrDigit).take(17))
                        }
                    },
                    label = { Text("VIN") },
                    isError = errores[CampoVehiculo.VIN] != null,
                    supportingText = { Text(errores[CampoVehiculo.VIN] ?: "Número de identificación de 17 caracteres (${f.vin.length}/17)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = viewModel::guardar,
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) { Text(if (state.editando) "Guardar cambios" else "Registrar vehículo") }
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

/** Muestras de color circulares: el usuario toca el color en lugar de escribirlo. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectorColor(seleccionado: String, onSeleccion: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CatalogoFormulario.colores.forEach { opcion ->
            val color = Color(opcion.argb)
            val activo = seleccionado.equals(opcion.nombre, ignoreCase = true)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color, CircleShape)
                        .border(
                            BorderStroke(
                                if (activo) 3.dp else 1.dp,
                                if (activo) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                            ),
                            CircleShape
                        )
                        .clickable { onSeleccion(opcion.nombre) }
                        .semantics { contentDescription = "Color ${opcion.nombre}" },
                    contentAlignment = Alignment.Center
                ) {
                    if (activo) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = if (color.luminance() > 0.5f) Color.Black else Color.White
                        )
                    }
                }
                Text(opcion.nombre, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/** Muestra el texto en mayúsculas sin modificar el valor que edita el teclado. */
private object MayusculasTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString) =
        androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(text.text.uppercase()),
            androidx.compose.ui.text.input.OffsetMapping.Identity
        )
}
