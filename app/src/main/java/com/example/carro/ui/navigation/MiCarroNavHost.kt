package com.example.carro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.carro.MiCarroApplication
import com.example.carro.ui.alertas.AlertasRoute
import com.example.carro.ui.historial.HistorialRoute
import com.example.carro.ui.home.HomeScreen
import com.example.carro.ui.mantenimiento.MantenimientoRoute
import com.example.carro.ui.plan.PlanMantenimientoRoute
import com.example.carro.ui.respaldo.RespaldoScreen
import com.example.carro.ui.respaldo.RespaldoViewModel
import com.example.carro.ui.vehiculos.VehiculoFormScreen
import com.example.carro.ui.vehiculos.VehiculoFormViewModel
import com.example.carro.ui.vehiculos.VehiculosScreen
import com.example.carro.ui.vehiculos.VehiculosViewModel

/**
 * Grafo de navegación raíz. El destino inicial es la pantalla de inicio;
 * cada feature branch agregará sus propios `composable(...)` para cada módulo.
 */
@Composable
fun MiCarroNavHost(
    navController: NavHostController = rememberNavController()
) {
    val container = (LocalContext.current.applicationContext as MiCarroApplication).container
    val registrarVehiculo = { navController.navigate(Destinos.vehiculoForm()) }

    NavHost(
        navController = navController,
        startDestination = Destinos.INICIO
    ) {
        composable(Destinos.INICIO) {
            HomeScreen(
                container = container,
                onNavegar = { ruta -> navController.navigate(ruta) }
            )
        }
        composable(Destinos.VEHICULOS) {
            VehiculosScreen(
                viewModel = viewModel(factory = VehiculosViewModel.factory(container)),
                onCrear = registrarVehiculo,
                onEditar = { id -> navController.navigate(Destinos.vehiculoForm(id)) },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(
            route = Destinos.VEHICULO_FORM,
            arguments = listOf(
                navArgument(Destinos.ARG_VEHICULO_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val vehiculoId = entry.arguments?.getString(Destinos.ARG_VEHICULO_ID)?.toLongOrNull()
            VehiculoFormScreen(
                viewModel = viewModel(factory = VehiculoFormViewModel.factory(container, vehiculoId)),
                onGuardado = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Destinos.PLAN_MANTENIMIENTO) {
            PlanMantenimientoRoute(
                container = container,
                onRegistrarVehiculo = registrarVehiculo,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Destinos.MANTENIMIENTOS) {
            MantenimientoRoute(
                container = container,
                onRegistrarVehiculo = registrarVehiculo,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Destinos.ALERTAS) {
            AlertasRoute(
                container = container,
                onRegistrarVehiculo = registrarVehiculo,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Destinos.HISTORIAL) {
            HistorialRoute(
                container = container,
                onRegistrarVehiculo = registrarVehiculo,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Destinos.RESPALDO) {
            RespaldoScreen(
                viewModel = viewModel(factory = RespaldoViewModel.factory(container)),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
