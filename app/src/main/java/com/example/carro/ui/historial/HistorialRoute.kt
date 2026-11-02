package com.example.carro.ui.historial

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carro.core.di.AppContainer
import com.example.carro.ui.vehiculos.ConVehiculoPrincipal

/**
 * Punto de entrada del módulo de consultas y costos (RF-34 a RF-38).
 * Trabaja sobre el vehículo principal (RF-04).
 */
@Composable
fun HistorialRoute(
    container: AppContainer,
    onRegistrarVehiculo: () -> Unit,
    onVolver: () -> Unit
) {
    ConVehiculoPrincipal(
        container = container,
        mensajeSinVehiculo = "El historial necesita un vehículo registrado.",
        onRegistrarVehiculo = onRegistrarVehiculo
    ) { vehiculo ->
        HistorialScreen(
            viewModel = viewModel(
                key = "historial_${vehiculo.id}",
                factory = HistorialViewModel.factory(container, vehiculo)
            ),
            vehiculo = vehiculo,
            onVolver = onVolver
        )
    }
}
