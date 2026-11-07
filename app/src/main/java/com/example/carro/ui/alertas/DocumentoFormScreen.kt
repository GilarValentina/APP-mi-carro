package com.example.carro.ui.alertas

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.carro.domain.model.Anticipaciones
import com.example.carro.ui.componentes.SelectorAnticipacion
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carro.domain.alerta.Documento
import com.example.carro.domain.model.TipoDocumento
import com.example.carro.ui.componentes.CampoFecha
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentoFormScreen(
    viewModel: AlertasViewModel,
    documentoId: Long?,
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    val editando = documentoId != null && documentoId != 0L

    var tipo by remember { mutableStateOf(TipoDocumento.SOAT) }
    var nombre by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now().plusMonths(1).toString()) }
    var expandido by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fechaExpedicion by remember { mutableStateOf("") }
    var adjunto by remember { mutableStateOf<String?>(null) }
    var anticipacion by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // Foto o PDF del documento. Se guarda solo el URI con permiso persistente, no se copia el archivo.
    val selectorAdjunto = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            adjunto = uri.toString()
        }
    }

    LaunchedEffect(documentoId) {
        if (editando) {
            viewModel.obtenerDocumento(documentoId!!)?.let { d ->
                fechaExpedicion = d.fechaExpedicion?.toString().orEmpty()
                adjunto = d.adjuntoUri
                anticipacion = d.anticipacionDias
                tipo = d.tipo
                nombre = d.nombre.orEmpty()
                fecha = d.fechaVencimiento.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editando) "Editar documento" else "Nuevo documento") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = it }
            ) {
                OutlinedTextField(
                    value = tipo.etiqueta,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de documento *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    TipoDocumento.entries.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion.etiqueta) },
                            onClick = {
                                tipo = opcion
                                expandido = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(if (tipo == TipoDocumento.OTRO) "Nombre *" else "Nombre (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            CampoFecha(
                valor = fecha,
                onValorCambiado = { fecha = it; error = null },
                etiqueta = "Fecha de vencimiento *",
                esError = error != null,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            CampoFecha(
                valor = fechaExpedicion,
                onValorCambiado = { fechaExpedicion = it; error = null },
                etiqueta = "Fecha de expedición (opcional)",
                permitirVacio = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            Text(
                "Recordarme con anticipación de:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            FilterChip(
                selected = anticipacion == null,
                onClick = { anticipacion = null },
                label = { Text("Configuración general") }
            )
            SelectorAnticipacion(
                opciones = Anticipaciones.opcionesDias,
                seleccionado = anticipacion,
                onSeleccion = { anticipacion = it }
            )

            Text(
                "Adjunto",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            if (adjunto == null) {
                OutlinedButton(
                    onClick = { selectorAdjunto.launch(arrayOf("image/*", "application/pdf")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AttachFile, contentDescription = null)
                    Text(" Adjuntar foto o PDF")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { abrirAdjunto(context, adjunto!!) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null)
                        Text(" Ver adjunto")
                    }
                    TextButton(onClick = { adjunto = null }) { Text("Quitar") }
                }
            }

            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    val fechaParseada = runCatching { LocalDate.parse(fecha.trim()) }.getOrNull()
                    if (fechaParseada == null) {
                        error = "La fecha debe tener el formato AAAA-MM-DD."
                        return@Button
                    }
                    val documento = Documento(
                        id = documentoId ?: 0L,
                        vehiculoId = 0L,
                        tipo = tipo,
                        nombre = nombre.ifBlank { null },
                        fechaVencimiento = fechaParseada,
                        fechaExpedicion = runCatching { LocalDate.parse(fechaExpedicion) }.getOrNull(),
                        adjuntoUri = adjunto,
                        anticipacionDias = anticipacion
                    )
                    if (documento.fechaExpedicion?.isAfter(fechaParseada) == true) {
                        error = "La fecha de expedición no puede ser posterior al vencimiento."
                        return@Button
                    }
                    val resultado = viewModel.validar(documento)
                    if (!resultado.esValido) {
                        error = resultado.errores.joinToString("\n")
                        return@Button
                    }
                    viewModel.guardarDocumento(documento)
                    onGuardado()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Text("Guardar")
            }
        }
    }
}

/** Abre el adjunto con la app que el usuario tenga para imágenes o PDF. */
fun abrirAdjunto(context: Context, uri: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
