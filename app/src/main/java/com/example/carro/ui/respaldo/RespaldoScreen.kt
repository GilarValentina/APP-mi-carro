package com.example.carro.ui.respaldo

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

private const val PALABRA_CONFIRMACION = "ELIMINAR"

/**
 * Copia de respaldo, restauración y borrado definitivo de los datos (RF-39, RF-40).
 * Las acciones destructivas exigen confirmación explícita del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespaldoScreen(
    viewModel: RespaldoViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var confirmarRestauracion by remember { mutableStateOf<android.net.Uri?>(null) }
    var confirmarBorrado by remember { mutableStateOf(false) }

    val crearRespaldo = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { destino -> destino?.let(viewModel::crearRespaldo) }

    val elegirRespaldo = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { origen -> origen?.let { confirmarRestauracion = it } }

    LaunchedEffect(state.mensaje, state.requiereReinicio) {
        val texto = state.mensaje ?: return@LaunchedEffect
        snackbar.showSnackbar(texto)
        viewModel.mensajeMostrado()
        if (state.requiereReinicio) {
            delay(500)
            reiniciarAplicacion(contexto)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Respaldo y datos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.procesando) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            TarjetaAccion(
                titulo = "Crear copia de respaldo",
                descripcion = "Guarda todos tus vehículos, mantenimientos, repuestos y documentos " +
                    "en un archivo que tú eliges dónde almacenar.",
                textoBoton = "Crear respaldo",
                habilitado = !state.procesando,
                onAccion = { crearRespaldo.launch(viewModel.nombreSugerido()) }
            )

            TarjetaAccion(
                titulo = "Restaurar respaldo",
                descripcion = "Reemplaza los datos actuales con los de un archivo de respaldo. " +
                    "La aplicación se reiniciará al terminar.",
                textoBoton = "Elegir archivo",
                habilitado = !state.procesando,
                onAccion = { elegirRespaldo.launch(arrayOf("*/*")) }
            )

            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Eliminar todos los datos", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Borra de forma definitiva los vehículos, el plan, el historial, los " +
                            "repuestos, los documentos y las alertas de este dispositivo. " +
                            "Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { confirmarBorrado = true },
                        enabled = !state.procesando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Eliminar todo")
                    }
                }
            }
        }
    }

    confirmarRestauracion?.let { uri ->
        AlertDialog(
            onDismissRequest = { confirmarRestauracion = null },
            title = { Text("¿Restaurar el respaldo?") },
            text = {
                Text(
                    "Los datos actuales se reemplazarán por los del archivo seleccionado y la " +
                        "aplicación se reiniciará."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRestauracion = null
                    viewModel.restaurarRespaldo(uri)
                }) { Text("Restaurar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRestauracion = null }) { Text("Cancelar") }
            }
        )
    }

    if (confirmarBorrado) {
        DialogoBorrarTodo(
            onConfirmar = {
                confirmarBorrado = false
                viewModel.eliminarTodo()
            },
            onCancelar = { confirmarBorrado = false }
        )
    }
}

/** RF-40: la confirmación exige escribir la palabra clave para evitar borrados accidentales. */
@Composable
private fun DialogoBorrarTodo(onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    var texto by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿Eliminar todos los datos?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Se borrarán definitivamente todos tus registros. Si quieres conservarlos, " +
                        "crea primero una copia de respaldo."
                )
                Text("Escribe $PALABRA_CONFIRMACION para confirmar:")
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it.uppercase() },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                enabled = texto == PALABRA_CONFIRMACION
            ) { Text("Eliminar todo") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun TarjetaAccion(
    titulo: String,
    descripcion: String,
    textoBoton: String,
    habilitado: Boolean,
    onAccion: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(descripcion, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onAccion, enabled = habilitado) { Text(textoBoton) }
        }
    }
}

/**
 * Reinicia la aplicación tras restaurar un respaldo: la base de datos se cerró para
 * reemplazar el archivo, así que debe volver a abrirse desde cero.
 */
private fun reiniciarAplicacion(contexto: Context) {
    val intent = contexto.packageManager.getLaunchIntentForPackage(contexto.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    contexto.startActivity(intent)
    Runtime.getRuntime().exit(0)
}
