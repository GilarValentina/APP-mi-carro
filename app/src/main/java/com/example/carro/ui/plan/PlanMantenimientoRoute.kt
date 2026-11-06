package com.example.carro.ui.plan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carro.core.di.AppContainer
import com.example.carro.ui.vehiculos.ConVehiculoPrincipal

private sealed interface PantallaPlan {
    data object Lista : PantallaPlan
    data class Formulario(val actividadId: Long?) : PantallaPlan
}

/**
 * Punto de entrada del módulo de plan de mantenimiento.
 * Trabaja sobre el vehículo principal (RF-04) y alterna entre el listado y el formulario.
 */
@Composable
fun PlanMantenimientoRoute(
    container: AppContainer,
    onRegistrarVehiculo: () -> Unit,
    onVolver: () -> Unit
) {
    ConVehiculoPrincipal(
        container = container,
        mensajeSinVehiculo = "El plan de mantenimiento necesita un vehículo registrado.",
        onRegistrarVehiculo = onRegistrarVehiculo
    ) { v ->
        // La clave incluye el kilometraje para recalcular los estados cuando cambie el odómetro.
        val viewModel: PlanMantenimientoViewModel = viewModel(
            key = "plan_${v.id}_${v.kilometrajeActual}",
            factory = PlanMantenimientoViewModel.factory(container, v.id, v.kilometrajeActual)
        )

        var pantalla by remember { mutableStateOf<PantallaPlan>(PantallaPlan.Lista) }

        when (val actual = pantalla) {
            is PantallaPlan.Lista -> PlanMantenimientoScreen(
                viewModel = viewModel,
                onCrear = { pantalla = PantallaPlan.Formulario(null) },
                onEditar = { id -> pantalla = PantallaPlan.Formulario(id) },
                onVolver = onVolver
            )

            is PantallaPlan.Formulario -> ActividadFormScreen(
                viewModel = viewModel,
                actividadId = actual.actividadId,
                onGuardado = { pantalla = PantallaPlan.Lista },
                onVolver = { pantalla = PantallaPlan.Lista }
            )
        }
    }
}
