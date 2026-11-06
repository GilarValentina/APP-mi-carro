package com.example.carro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carro.domain.agenda.Cita
import com.example.carro.domain.guias.GuiaEmergencia
import com.example.carro.domain.picoyplaca.EstadoPicoYPlaca
import com.example.carro.domain.vehiculo.Vehiculo
import com.example.carro.ui.agenda.icono
import com.example.carro.ui.guias.icono
import com.example.carro.ui.historial.Formatos
import com.example.carro.ui.navigation.Destinos
import com.example.carro.ui.theme.ErrorRed
import com.example.carro.ui.theme.Positive
import com.example.carro.ui.theme.WarningAmber
import java.time.format.DateTimeFormatter

/** Tarjeta principal del panel: vehículo predeterminado, odómetro y pico y placa del día. */
@Composable
internal fun TarjetaVehiculoPrincipal(
    vehiculo: Vehiculo,
    picoYPlaca: EstadoPicoYPlaca?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(52.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (vehiculo.tipo.esMoto) Icons.Filled.TwoWheeler else Icons.Filled.DirectionsCar,
                        contentDescription = null
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("${vehiculo.marca} · ${vehiculo.placa}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${vehiculo.linea} ${vehiculo.anio}", style = MaterialTheme.typography.bodyMedium)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ver vehículos")
            }
            Text(
                "Odómetro: ${Formatos.kilometraje(vehiculo.kilometrajeActual)}",
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
            BloquePicoYPlaca(picoYPlaca)
        }
    }
}

@Composable
private fun BloquePicoYPlaca(estado: EstadoPicoYPlaca?) {
    val (icono, color, titulo) = when (estado?.tieneRestriccion) {
        true -> Triple(Icons.Filled.Block, ErrorRed, "Hoy tienes pico y placa")
        false -> Triple(Icons.Filled.CheckCircle, Positive, "Hoy puedes circular")
        null -> Triple(Icons.Filled.Event, WarningAmber, "Pico y placa")
    }
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(36.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            when {
                estado == null -> Text(
                    "Selecciona la ciudad donde circula el vehículo para ver la restricción del día.",
                    style = MaterialTheme.typography.bodySmall
                )
                estado.tieneRestriccion == null -> Text(estado.nota, style = MaterialTheme.typography.bodySmall)
                else -> {
                    Text(
                        buildString {
                            append(estado.ciudad)
                            if (estado.digitosRestringidos.isEmpty()) append(": sin restricción hoy")
                            else append(": placas terminadas en ${estado.digitosRestringidos.sorted().joinToString(", ")}")
                            estado.horario?.takeIf { estado.digitosRestringidos.isNotEmpty() }?.let { append(" · $it") }
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        estado.nota,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private data class AccesoRapido(val titulo: String, val icono: ImageVector, val ruta: String)

/** Accesos de un toque a lo que más se usa. */
@Composable
internal fun AccesosRapidos(onNavegar: (String) -> Unit) {
    val accesos = listOf(
        AccesoRapido("Kilometraje", Icons.Filled.Speed, Destinos.KILOMETRAJE),
        AccesoRapido("Agenda", Icons.Filled.Event, Destinos.AGENDA),
        AccesoRapido("Documentos", Icons.Filled.Description, Destinos.ALERTAS),
        AccesoRapido("Plan", Icons.Filled.Build, Destinos.PLAN_MANTENIMIENTO)
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        accesos.forEach { acceso ->
            Card(
                modifier = Modifier.weight(1f).clickable { onNavegar(acceso.ruta) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { Icon(acceso.icono, null, tint = Positive) }
                    Text(acceso.titulo, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private val formatoCita = DateTimeFormatter.ofPattern("dd/MM/yyyy · hh:mm a")

@Composable
internal fun TarjetaProximaCita(cita: Cita, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(cita.tipo.icono(), null, tint = MaterialTheme.colorScheme.secondary)
            Column(Modifier.weight(1f)) {
                Text("Próxima cita", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(cita.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(cita.momento.format(formatoCita), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ver agenda")
        }
    }
}

/** Sección inferior del panel: guías y ayuda en carretera. */
@Composable
internal fun SeccionGuias(onNavegar: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("GUÍAS Y AYUDA EN CARRETERA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text(
            "¿Problemas en la vía? Te ayudamos paso a paso.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GuiaEmergencia.entries.forEach { guia ->
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .height(196.dp)
                        .clickable { onNavegar(Destinos.guia(guia.id)) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) { Icon(guia.icono(), null, tint = MaterialTheme.colorScheme.tertiary) }
                        Text(guia.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(guia.resumen, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
