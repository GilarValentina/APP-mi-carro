package com.example.carro.ui.perfil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.carro.data.repository.PerfilRepository
import com.example.carro.domain.perfil.CampoPerfil
import com.example.carro.domain.perfil.PerfilUsuario
import com.example.carro.domain.perfil.PerfilValidator

private enum class PasoOnboarding { DATOS, NOTIFICACIONES, LISTO }

/**
 * Configuración inicial: datos del usuario y permiso de notificaciones.
 * Se muestra una sola vez; los datos se pueden editar después en Ajustes.
 */
@Composable
fun OnboardingScreen(
    perfilRepository: PerfilRepository,
    onTerminado: () -> Unit
) {
    var paso by remember { mutableStateOf(PasoOnboarding.DATOS) }
    var perfil by remember { mutableStateOf(perfilRepository.perfil.value) }
    var errores by remember { mutableStateOf<Map<CampoPerfil, String>>(emptyMap()) }
    var push by remember { mutableStateOf(perfilRepository.pushHabilitado) }

    val permisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        paso = PasoOnboarding.LISTO
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = paso,
            transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith fadeOut() },
            label = "onboarding"
        ) { actual ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IndicadorPasos(actual.ordinal, PasoOnboarding.entries.size)
                when (actual) {
                    PasoOnboarding.DATOS -> {
                        Encabezado(
                            titulo = "Te damos la bienvenida a MiCarro",
                            subtitulo = "Cuéntanos un poco de ti para personalizar tu experiencia. Tus datos se guardan solo en este teléfono."
                        ) { Icon(Icons.Filled.DirectionsCar, null, tint = MaterialTheme.colorScheme.onPrimary) }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(Modifier.padding(16.dp)) {
                                FormularioPerfil(perfil, errores) { campo, nuevo ->
                                    perfil = nuevo
                                    errores = errores - campo
                                }
                            }
                        }
                        Button(
                            onClick = {
                                errores = PerfilValidator.validar(perfil)
                                if (errores.isEmpty()) {
                                    perfilRepository.guardar(perfil)
                                    paso = PasoOnboarding.NOTIFICACIONES
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("Continuar") }
                    }

                    PasoOnboarding.NOTIFICACIONES -> {
                        Encabezado(
                            titulo = "Activa las notificaciones",
                            subtitulo = TEXTO_PERMISO_NOTIFICACIONES
                        ) { Icon(Icons.Filled.NotificationsActive, null, tint = MaterialTheme.colorScheme.onPrimary) }
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf(
                                    "Mantenimientos próximos o vencidos",
                                    "SOAT, tecnomecánica y seguros por vencer",
                                    "Recordatorios de tus citas",
                                    "Pico y placa del día"
                                ).forEach { beneficio ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(Modifier.size(10.dp))
                                        Text(beneficio, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Notificaciones push", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "Avisos desde internet (disponibles en una próxima versión).",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(checked = push, onCheckedChange = {
                                        push = it
                                        perfilRepository.pushHabilitado = it
                                    })
                                }
                            }
                        }
                        Button(
                            onClick = {
                                val permiso = permisoNotificaciones
                                if (permiso != null) permisoLauncher.launch(permiso) else paso = PasoOnboarding.LISTO
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("Activar notificaciones") }
                        TextButton(onClick = { paso = PasoOnboarding.LISTO }, modifier = Modifier.fillMaxWidth()) {
                            Text("Ahora no (puedes activarlas luego en Ajustes)")
                        }
                    }

                    PasoOnboarding.LISTO -> {
                        Spacer(Modifier.height(40.dp))
                        Encabezado(
                            titulo = "¡Hola, ${perfil.primerNombre}!",
                            subtitulo = "Todo listo. Registra tu vehículo y MiCarro te ayudará con mantenimientos, documentos y citas."
                        ) { Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.onPrimary) }
                        Button(
                            onClick = {
                                perfilRepository.completarOnboarding()
                                onTerminado()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("Comenzar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(titulo: String, subtitulo: String, icono: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) { icono() }
        Text(
            titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun IndicadorPasos(actual: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        repeat(total) { i ->
            Box(
                Modifier
                    .height(6.dp)
                    .size(width = if (i == actual) 28.dp else 10.dp, height = 6.dp)
                    .background(
                        if (i <= actual) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(50)
                    )
            )
        }
    }
}

/** Saludo para el inicio sin asumir género: "Hola, Valentina" + mensaje neutro. */
fun saludo(perfil: PerfilUsuario): String =
    if (perfil.primerNombre.isBlank()) "Hola" else "Hola, ${perfil.primerNombre}"
