package com.example.carro.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.carro.domain.historial.GastoAgrupado
import com.example.carro.domain.historial.PeriodoResumen
import com.example.carro.domain.historial.ResumenCalculator
import com.example.carro.domain.historial.ResumenGastos

/**
 * Resumen de gastos del vehículo por categoría y por periodo (RF-36).
 * Se calcula sobre las entradas ya filtradas en la pestaña de historial.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumenGastosContenido(
    resumen: ResumenGastos,
    periodoSeleccionado: PeriodoResumen,
    onPeriodoCambiado: (PeriodoResumen) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodoResumen.entries.forEach { periodo ->
                    FilterChip(
                        selected = periodo == periodoSeleccionado,
                        onClick = { onPeriodoCambiado(periodo) },
                        label = { Text(periodo.etiqueta) }
                    )
                }
            }
        }

        if (resumen.vacio) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    Text(
                        "No hay gastos registrados en ${periodoSeleccionado.etiqueta.lowercase()}.",
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@LazyColumn
        }

        item { TarjetaTotales(resumen) }

        if (resumen.porCategoria.isNotEmpty()) {
            item { Text("Gasto por categoría", style = MaterialTheme.typography.titleMedium) }
            items(resumen.porCategoria, key = { "cat_${it.etiqueta}" }) { grupo ->
                BarraGasto(grupo, resumen.total)
            }
        }

        if (resumen.porMes.isNotEmpty()) {
            item { Text("Gasto por mes", style = MaterialTheme.typography.titleMedium) }
            items(resumen.porMes, key = { "mes_${it.etiqueta}" }) { grupo ->
                BarraGasto(grupo.copy(etiqueta = grupo.etiqueta.substringAfter("· ")), resumen.total)
            }
        }

        resumen.masCostoso?.let { mayor ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Mantenimiento más costoso", style = MaterialTheme.typography.labelLarge)
                        Text(mayor.descripcion, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${Formatos.fecha(mayor.fecha)} · ${Formatos.moneda(mayor.costoTotal)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaTotales(resumen: ResumenGastos) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "Gasto total ${Formatos.moneda(resumen.total)} en ${resumen.cantidadMantenimientos} mantenimientos"
            }
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Gasto total del periodo", style = MaterialTheme.typography.labelLarge)
            Text(Formatos.moneda(resumen.total), style = MaterialTheme.typography.headlineSmall)
            FilaDato("Mantenimientos", resumen.cantidadMantenimientos.toString())
            FilaDato("Promedio por servicio", Formatos.moneda(resumen.promedioPorMantenimiento))
            FilaDato("Mano de obra", Formatos.moneda(resumen.totalManoObra))
            FilaDato("Repuestos", Formatos.moneda(resumen.totalRepuestos))
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BarraGasto(grupo: GastoAgrupado, total: Double) {
    val proporcion = ResumenCalculator.proporcion(grupo, total)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "${grupo.etiqueta}: ${Formatos.moneda(grupo.total)}, ${(proporcion * 100).toInt()} por ciento"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${grupo.etiqueta} (${grupo.cantidad})", style = MaterialTheme.typography.bodyMedium)
            Text(Formatos.moneda(grupo.total), style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { proporcion },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
