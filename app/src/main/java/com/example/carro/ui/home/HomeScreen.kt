package com.example.carro.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.carro.BuildConfig
import com.example.carro.ui.navigation.Destinos

/**
 * Pantalla de inicio provisional (RF-37). Lista los módulos del MVP.
 * Cada feature branch reemplazará las tarjetas por su contenido real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavegar: (String) -> Unit = {}) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("MiCarro") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Proyecto: ${BuildConfig.PROJECT_DIR_NAME}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "Rama: ${BuildConfig.APP_GIT_BRANCH}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = BuildConfig.APP_PRESENTATION_TAG,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Text(
                text = "Control de mantenimiento vehicular",
                style = MaterialTheme.typography.titleMedium
            )
            val modulos = listOf(
                "Vehículos" to Destinos.VEHICULOS,
                "Kilometraje" to null,
                "Plan de mantenimiento" to Destinos.PLAN_MANTENIMIENTO,
                "Mantenimientos y repuestos" to Destinos.MANTENIMIENTOS,
                "Alertas y documentos" to Destinos.ALERTAS,
                "Historial y resumen" to null
            )
            modulos.forEach { (modulo, ruta) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (ruta != null) Modifier.clickable { onNavegar(ruta) } else Modifier)
                ) {
                    Text(
                        text = modulo,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
