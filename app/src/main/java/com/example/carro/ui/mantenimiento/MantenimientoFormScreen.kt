package com.example.carro.ui.mantenimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carro.domain.mantenimiento.Mantenimiento
import com.example.carro.domain.mantenimiento.Repuesto
import com.example.carro.domain.model.TipoMantenimiento
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val MANTENIMIENTOS_COMUNES = listOf(
    "Cambio de aceite",
    "Cambio de filtro de aceite",
    "Cambio de filtro de aire",
    "Cambio de filtro de combustible",
    "Revisión de frenos",
    "Balanceo",
    "Alineación",
    "Cambio de llantas",
    "Cambio de batería",
    "Cambio de bujías",
    "Revisión técnico-mecánica",
    "SOAT",
    "Lavado",
    "Otro"
)

/** Formulario de registro/edición de mantenimiento con repuestos (RF-17 a RF-27). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenimientoFormScreen(
    viewModel: MantenimientoViewModel,
    mantenimientoId: Long?,
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    val editando = mantenimientoId != null && mantenimientoId != 0L
    val scope = rememberCoroutineScope()

    var fecha by remember { mutableStateOf(LocalDate.now().toString()) }
    var kilometraje by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoMantenimiento.PREVENTIVO) }
    var descripcion by remember { mutableStateOf("") }
    var taller by remember { mutableStateOf("") }
    var manoObra by remember { mutableStateOf("") }
    var mantenimientoExpandido by remember { mutableStateOf(false) }
    val repuestos = remember { mutableStateListOf<Repuesto>() }

    var errores by remember { mutableStateOf<List<String>>(emptyList()) }
    var advertenciaPendiente by remember { mutableStateOf<Mantenimiento?>(null) }
    var advertenciasTexto by remember { mutableStateOf<List<String>>(emptyList()) }
    var editorRepuesto by remember { mutableStateOf<RepuestoEditState?>(null) }

    LaunchedEffect(mantenimientoId) {
        if (editando) {
            viewModel.obtenerMantenimiento(mantenimientoId!!)?.let { m ->
                fecha = m.fecha.toString()
                kilometraje = m.kilometraje.toString()
                tipo = m.tipo
                descripcion = m.descripcion
                taller = m.tallerResponsable.orEmpty()
                manoObra = if (m.costoManoObra > 0) m.costoManoObra.toString() else ""
                repuestos.clear()
                repuestos.addAll(m.repuestos)
            }
        }
    }

    val opcionDescripcion = if (descripcion in MANTENIMIENTOS_COMUNES) descripcion else "Otro"

    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    fun parseFecha(t: String): LocalDate? = runCatching { LocalDate.parse(t, formatter) }.getOrNull()

    fun construir(): Mantenimiento = Mantenimiento(
        id = if (editando) mantenimientoId!! else 0L,
        vehiculoId = 0L,
        fecha = parseFecha(fecha) ?: LocalDate.now(),
        kilometraje = kilometraje.toLongOrNull() ?: 0L,
        tipo = tipo,
        descripcion = descripcion.trim(),
        tallerResponsable = taller.trim().ifBlank { null },
        costoManoObra = manoObra.toDoubleOrNull() ?: 0.0,
        repuestos = repuestos.toList()
    )

    val totalEnVivo = (manoObra.toDoubleOrNull() ?: 0.0) + repuestos.sumOf { it.subtotal }

    fun intentarGuardar() {
        scope.launch {
            val m = construir()
            val r = viewModel.validar(m)
            if (!r.esValido) {
                errores = r.errores
                return@launch
            }
            errores = emptyList()
            if (r.advertencias.isNotEmpty()) {
                advertenciaPendiente = m
                advertenciasTexto = r.advertencias
            } else {
                viewModel.guardar(m)
                onGuardado()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editando) "Editar mantenimiento" else "Nuevo mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoMantenimiento.entries.forEach { opt ->
                    FilterChip(
                        selected = tipo == opt,
                        onClick = { tipo = opt },
                        label = { Text(opt.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Text("Tipo de mantenimiento", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = mantenimientoExpandido,
                onExpandedChange = { mantenimientoExpandido = it }
            ) {
                OutlinedTextField(
                    value = opcionDescripcion,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar mantenimiento") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mantenimientoExpandido) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = mantenimientoExpandido,
                    onDismissRequest = { mantenimientoExpandido = false }
                ) {
                    MANTENIMIENTOS_COMUNES.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                if (opcion == "Otro") {
                                    descripcion = ""
                                } else {
                                    descripcion = opcion
                                }
                                mantenimientoExpandido = false
                            }
                        )
                    }
                }
            }
            if (opcionDescripcion == "Otro") {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción personalizada *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OutlinedTextField(
                value = fecha, onValueChange = { fecha = it },
                label = { Text("Fecha (AAAA-MM-DD) *") }, singleLine = true,
                isError = parseFecha(fecha) == null, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = kilometraje, onValueChange = { kilometraje = it.filter(Char::isDigit) },
                label = { Text("Kilometraje *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = taller, onValueChange = { taller = it },
                label = { Text("Taller o responsable") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = manoObra, onValueChange = { manoObra = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Costo de mano de obra") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Text("Repuestos", style = MaterialTheme.typography.titleSmall)
                OutlinedButton(onClick = { editorRepuesto = RepuestoEditState() }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" Agregar")
                }
            }
            repuestos.forEachIndexed { index, r ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(r.nombre, fontWeight = FontWeight.Medium)
                            Text(
                                "x${r.cantidad} · ${formatearMoneda(r.valorUnitario)} = ${formatearMoneda(r.subtotal)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { editorRepuesto = RepuestoEditState.from(r, index) }) {
                            Icon(Icons.Default.Add, contentDescription = "Editar repuesto")
                        }
                        IconButton(onClick = { repuestos.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar repuesto")
                        }
                    }
                }
            }

            HorizontalDivider()
            Text(
                "Total: ${formatearMoneda(totalEnVivo)}",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )

            if (errores.isNotEmpty()) {
                errores.forEach { e ->
                    Text("• $e", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(onClick = { intentarGuardar() }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar")
            }
        }
    }

    // Diálogo de confirmación de advertencias (RN-06).
    advertenciaPendiente?.let { m ->
        AlertDialog(
            onDismissRequest = { advertenciaPendiente = null },
            title = { Text("Confirmar") },
            text = {
                Column { advertenciasTexto.forEach { Text("• $it") } }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.guardar(m)
                    advertenciaPendiente = null
                    onGuardado()
                }) { Text("Guardar de todas formas") }
            },
            dismissButton = {
                TextButton(onClick = { advertenciaPendiente = null }) { Text("Corregir") }
            }
        )
    }

    // Editor de repuesto (RF-24 a RF-27).
    editorRepuesto?.let { estado ->
        RepuestoDialog(
            estado = estado,
            onConfirmar = { repuesto, index ->
                if (index == null) repuestos.add(repuesto) else repuestos[index] = repuesto
                editorRepuesto = null
            },
            onCancelar = { editorRepuesto = null }
        )
    }
}

/** Estado editable de un repuesto dentro del diálogo. */
private data class RepuestoEditState(
    val index: Int? = null,
    val id: Long = 0,
    val nombre: String = "",
    val marca: String = "",
    val referencia: String = "",
    val cantidad: String = "1",
    val valorUnitario: String = "",
    val proveedor: String = "",
    val garantiaDias: String = "",
    val garantiaKm: String = "",
    val instalado: Boolean = true
) {
    companion object {
        fun from(r: Repuesto, index: Int) = RepuestoEditState(
            index = index,
            id = r.id,
            nombre = r.nombre,
            marca = r.marca.orEmpty(),
            referencia = r.referencia.orEmpty(),
            cantidad = r.cantidad.toString(),
            valorUnitario = if (r.valorUnitario > 0) r.valorUnitario.toString() else "",
            proveedor = r.proveedor.orEmpty(),
            garantiaDias = r.garantiaDias?.toString().orEmpty(),
            garantiaKm = r.garantiaKm?.toString().orEmpty(),
            instalado = r.instaladoActualmente
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepuestoDialog(
    estado: RepuestoEditState,
    onConfirmar: (Repuesto, Int?) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(estado.nombre) }
    var marca by remember { mutableStateOf(estado.marca) }
    var referencia by remember { mutableStateOf(estado.referencia) }
    var cantidad by remember { mutableStateOf(estado.cantidad) }
    var valorUnitario by remember { mutableStateOf(estado.valorUnitario) }
    var proveedor by remember { mutableStateOf(estado.proveedor) }
    var garantiaDias by remember { mutableStateOf(estado.garantiaDias) }
    var garantiaKm by remember { mutableStateOf(estado.garantiaKm) }
    var instalado by remember { mutableStateOf(estado.instalado) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (estado.index == null) "Agregar repuesto" else "Editar repuesto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(marca, { marca = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(referencia, { referencia = it }, label = { Text("Referencia") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    cantidad, { cantidad = it.filter(Char::isDigit) }, label = { Text("Cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    valorUnitario, { valorUnitario = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Valor unitario") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(proveedor, { proveedor = it }, label = { Text("Proveedor") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    garantiaDias, { garantiaDias = it.filter(Char::isDigit) }, label = { Text("Garantía (días)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    garantiaKm, { garantiaKm = it.filter(Char::isDigit) }, label = { Text("Garantía (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Instalado actualmente")
                    Switch(checked = instalado, onCheckedChange = { instalado = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank() && (cantidad.toIntOrNull() ?: 0) > 0,
                onClick = {
                    onConfirmar(
                        Repuesto(
                            id = estado.id,
                            nombre = nombre.trim(),
                            marca = marca.trim().ifBlank { null },
                            referencia = referencia.trim().ifBlank { null },
                            cantidad = cantidad.toIntOrNull() ?: 1,
                            valorUnitario = valorUnitario.toDoubleOrNull() ?: 0.0,
                            proveedor = proveedor.trim().ifBlank { null },
                            fechaInstalacion = if (instalado) LocalDate.now() else null,
                            garantiaDias = garantiaDias.toIntOrNull(),
                            garantiaKm = garantiaKm.toLongOrNull(),
                            instaladoActualmente = instalado
                        ),
                        estado.index
                    )
                }
            ) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
