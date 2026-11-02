package com.example.carro.core.di

import android.content.Context
import com.example.carro.core.database.MiCarroDatabase
import com.example.carro.data.repository.AlertasRepository
import com.example.carro.data.repository.HistorialRepository
import com.example.carro.data.repository.MantenimientoRepository
import com.example.carro.data.repository.PlanMantenimientoRepository
import com.example.carro.data.repository.RespaldoRepository
import com.example.carro.data.repository.VehiculoRepository

/**
 * Contenedor de dependencias manual (RNF-12: separación de capas y dependencias inyectables).
 *
 * Expone la base de datos y sus DAOs. Cada módulo (feature branch) construye sus
 * repositorios a partir de los DAOs que aquí se publican, sin acoplarse entre sí.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: MiCarroDatabase = MiCarroDatabase.getInstance(context)

    val vehiculoDao get() = database.vehiculoDao()
    val kilometrajeDao get() = database.kilometrajeDao()
    val categoriaDao get() = database.categoriaDao()
    val actividadDao get() = database.actividadDao()
    val mantenimientoDao get() = database.mantenimientoDao()
    val repuestoDao get() = database.repuestoDao()
    val documentoDao get() = database.documentoDao()
    val alertaDao get() = database.alertaDao()

    // Repositorios (cada módulo publica el suyo aquí).
    val vehiculoRepository by lazy {
        VehiculoRepository(database, vehiculoDao, kilometrajeDao)
    }
    val planMantenimientoRepository by lazy {
        PlanMantenimientoRepository(actividadDao, categoriaDao)
    }
    val mantenimientoRepository by lazy {
        MantenimientoRepository(database, mantenimientoDao, repuestoDao, kilometrajeDao, vehiculoDao)
    }
    val alertasRepository: AlertasRepository by lazy {
        AlertasRepository(appContext, documentoDao, alertaDao)
    }
    val historialRepository by lazy {
        HistorialRepository(mantenimientoDao, repuestoDao, actividadDao, categoriaDao)
    }
    val respaldoRepository by lazy {
        RespaldoRepository(appContext, database)
    }
}
