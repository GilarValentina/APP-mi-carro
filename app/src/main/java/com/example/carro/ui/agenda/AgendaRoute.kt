package com.example.carro.ui.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carro.core.di.AppContainer
import com.example.carro.ui.kilometraje.KilometrajeScreen
import com.example.carro.ui.kilometraje.KilometrajeViewModel
import com.example.carro.ui.vehiculos.ConVehiculoPrincipal

private sealed interface PantallaAgenda {
    data object Lista : PantallaAgenda
    data class Formulario(val citaId: Long?) : PantallaAgenda
}

/** Punto de entrada de la agenda; trabaja sobre el vehículo principal (RF-04). */
@Composable
fun AgendaRoute(
    container: AppContainer,
    onRegistrarVehiculo: () -> Unit,
    onVolver: () -> Unit
) {
    ConVehiculoPrincipal(
        container = container,
        mensajeSinVehiculo = "La agenda necesita un vehículo registrado.",
        onRegistrarVehiculo = onRegistrarVehiculo
    ) { v ->
        val viewModel: AgendaViewModel = viewModel(key = "agenda_${v.id}", factory = AgendaViewModel.factory(container, v.id))
        var pantalla by remember { mutableStateOf<PantallaAgenda>(PantallaAgenda.Lista) }
        when (val actual = pantalla) {
            PantallaAgenda.Lista -> AgendaScreen(
                placa = v.placa,
                viewModel = viewModel,
                onCrear = { pantalla = PantallaAgenda.Formulario(null) },
                onEditar = { pantalla = PantallaAgenda.Formulario(it) },
                onVolver = onVolver
            )
            is PantallaAgenda.Formulario -> CitaFormScreen(
                viewModel = viewModel,
                citaId = actual.citaId,
                onListo = { pantalla = PantallaAgenda.Lista }
            )
        }
    }
}

/** Punto de entrada del módulo de kilometraje sobre el vehículo principal (RF-04, RF-06). */
@Composable
fun KilometrajeRoute(
    container: AppContainer,
    onRegistrarVehiculo: () -> Unit,
    onVolver: () -> Unit
) {
    ConVehiculoPrincipal(
        container = container,
        mensajeSinVehiculo = "El kilometraje necesita un vehículo registrado.",
        onRegistrarVehiculo = onRegistrarVehiculo
    ) { v ->
        val viewModel: KilometrajeViewModel = viewModel(
            key = "km_${v.id}",
            factory = KilometrajeViewModel.factory(container, v.id)
        )
        KilometrajeScreen(vehiculo = v, viewModel = viewModel, onVolver = onVolver)
    }
}
