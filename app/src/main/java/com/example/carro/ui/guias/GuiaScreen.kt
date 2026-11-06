package com.example.carro.ui.guias

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carro.domain.guias.ContenidoGuias
import com.example.carro.domain.guias.GuiaEmergencia
import com.example.carro.domain.guias.PasoGuia

fun GuiaEmergencia.icono(): ImageVector = when (this) {
    GuiaEmergencia.ACCIDENTE -> Icons.Filled.CarCrash
    GuiaEmergencia.PINCHAZO -> Icons.Filled.TireRepair
    GuiaEmergencia.BATERIA -> Icons.Filled.BatteryAlert
    GuiaEmergencia.SOBRECALENTAMIENTO -> Icons.Filled.Thermostat
    GuiaEmergencia.VARADO -> Icons.Filled.Warning
    GuiaEmergencia.NUMEROS -> Icons.Filled.LocalPhone
}

/** Pantalla explicativa de una guía de ayuda en carretera, con pasos desplegables. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaScreen(guia: GuiaEmergencia, onVolver: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(guia.titulo.uppercase(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(
                if (guia == GuiaEmergencia.NUMEROS) "Toca un número para abrir el marcador de tu teléfono."
                else "${guia.resumen}. Recomendaciones que debes considerar en el momento.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (guia == GuiaEmergencia.NUMEROS) {
                NumerosEmergencia()
            } else {
                ContenidoGuias.pasos(guia).forEachIndexed { i, paso -> PasoDesplegable(i + 1, paso, guia.icono()) }
                Text(
                    "Estas recomendaciones son generales y no reemplazan las indicaciones de las autoridades ni del manual de tu vehículo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                NumerosEmergencia(compacto = true)
            }
        }
    }
}

@Composable
private fun PasoDesplegable(numero: Int, paso: PasoGuia, icono: ImageVector) {
    var abierto by remember { mutableStateOf(numero == 1) }
    val giro by animateFloatAsState(if (abierto) 180f else 0f, label = "flecha")
    Card(
        modifier = Modifier.fillMaxWidth().clickable { abierto = !abierto },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(44.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(icono, null, tint = MaterialTheme.colorScheme.primary) }
                Text("$numero. ${paso.titulo}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = if (abierto) "Ocultar" else "Ver más", modifier = Modifier.rotate(giro))
            }
            AnimatedVisibility(abierto) {
                Text(
                    paso.detalle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 56.dp, top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun NumerosEmergencia(compacto: Boolean = false) {
    val context = LocalContext.current
    val lista = if (compacto) ContenidoGuias.numeros.take(2) else ContenidoGuias.numeros
    lista.forEach { n ->
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(n.entidad, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(n.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // ACTION_DIAL solo abre el marcador: el usuario decide si llama.
                FilledTonalButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(n.numero)}")))
                }) {
                    Icon(Icons.Filled.Call, null, Modifier.size(18.dp))
                    Text(" ${n.numero}")
                }
            }
        }
    }
}
