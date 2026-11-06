package com.example.carro.ui.perfil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.carro.core.alertas.NotificacionesAlertas
import com.example.carro.core.di.AppContainer
import com.example.carro.domain.model.Anticipaciones
import com.example.carro.domain.perfil.CampoPerfil
import com.example.carro.domain.perfil.PerfilValidator
import com.example.carro.ui.componentes.SelectorAnticipacion
import kotlinx.coroutines.launch

/**
 * Ajustes: perfil del usuario, notificaciones (reactivación si se rechazaron),
 * anticipación general de recordatorios y acceso al respaldo de datos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    container: AppContainer,
    onRespaldo: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val perfilRepository = container.perfilRepository
    val alertasRepository = container.alertasRepository
    val perfilGuardado by perfilRepository.perfil.collectAsStateWithLifecycle()
    val configuracion by alertasRepository.configuracion.collectAsStateWithLifecycle(alertasRepository.configuracionActual())
    var perfil by remember(perfilGuardado) { mutableStateOf(perfilGuardado) }
    var errores by remember { mutableStateOf<Map<CampoPerfil, String>>(emptyMap()) }
    var push by remember { mutableStateOf(perfilRepository.pushHabilitado) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Se vuelve a consultar el permiso al regresar de los ajustes del sistema.
    var revision by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { revision++ }
    }
    val permitido = remember(revision) { NotificacionesAlertas.tienePermiso(context) }
    val permisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        revision++
        if (!concedido) abrirAjustesNotificaciones(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Seccion("Mi perfil", "Modifica tu información personal") {
                FormularioPerfil(perfil, errores) { campo, nuevo ->
                    perfil = nuevo
                    errores = errores - campo
                }
                Button(
                    onClick = {
                        errores = PerfilValidator.validar(perfil)
                        if (errores.isEmpty()) {
                            perfilRepository.guardar(perfil)
                            scope.launch { snackbar.showSnackbar("Perfil actualizado") }
                        }
                    },
                    enabled = perfil != perfilGuardado,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text("Guardar cambios") }
            }

            Seccion("Notificaciones", TEXTO_PERMISO_NOTIFICACIONES) {
                FilaInterruptor(
                    titulo = "Notificaciones locales",
                    detalle = if (permitido) "Permitidas en este dispositivo" else "Desactivadas: toca para activarlas",
                    activo = permitido,
                    onCambio = {
                        val permiso = permisoNotificaciones
                        if (!permitido && permiso != null) permisoLauncher.launch(permiso) else abrirAjustesNotificaciones(context)
                    }
                )
                HorizontalDivider()
                FilaInterruptor(
                    titulo = "Alertas de vencimientos y mantenimientos",
                    detalle = "Revisión diaria en segundo plano",
                    activo = configuracion.alertasActivas,
                    onCambio = { alertasRepository.actualizarConfiguracion(configuracion.copy(alertasActivas = it)) }
                )
                HorizontalDivider()
                FilaInterruptor(
                    titulo = "Notificaciones push",
                    detalle = "Preparadas para una próxima versión con servidor de mensajería",
                    activo = push,
                    onCambio = {
                        push = it
                        perfilRepository.pushHabilitado = it
                    }
                )
                Text(
                    "Avisarme de documentos con anticipación de:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                SelectorAnticipacion(
                    opciones = Anticipaciones.opcionesDias,
                    seleccionado = configuracion.anticipacionDias,
                    onSeleccion = { alertasRepository.actualizarConfiguracion(configuracion.copy(anticipacionDias = it)) }
                )
                if (!permitido) {
                    OutlinedButton(onClick = { abrirAjustesNotificaciones(context) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Abrir ajustes del sistema")
                    }
                }
            }

            Seccion("Datos", "Respaldo, restauración y borrado de la información") {
                TextButton(onClick = onRespaldo, modifier = Modifier.fillMaxWidth()) {
                    Text("Respaldo y restauración", modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun Seccion(titulo: String, subtitulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(titulo.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { contenido() }
        }
    }
}

@Composable
private fun FilaInterruptor(titulo: String, detalle: String, activo: Boolean, onCambio: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = activo, onCheckedChange = onCambio)
    }
}
