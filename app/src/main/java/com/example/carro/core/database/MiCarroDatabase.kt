package com.example.carro.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.carro.data.dao.ActividadDao
import com.example.carro.data.dao.AlertaDao
import com.example.carro.data.dao.CategoriaDao
import com.example.carro.data.dao.DocumentoDao
import com.example.carro.data.dao.KilometrajeDao
import com.example.carro.data.dao.MantenimientoDao
import com.example.carro.data.dao.RepuestoDao
import com.example.carro.data.dao.VehiculoDao
import com.example.carro.data.entity.ActividadMantenimientoEntity
import com.example.carro.data.entity.AlertaEntity
import com.example.carro.data.entity.CategoriaEntity
import com.example.carro.data.entity.DocumentoEntity
import com.example.carro.data.entity.LecturaKilometrajeEntity
import com.example.carro.data.entity.MantenimientoEntity
import com.example.carro.data.entity.RepuestoEntity
import com.example.carro.data.entity.VehiculoEntity

/**
 * Base de datos local de MiCarro (RNF-04: operación offline).
 * Núcleo compartido por todos los módulos; cada feature añade su propio DAO.
 */
@Database(
    entities = [
        VehiculoEntity::class,
        LecturaKilometrajeEntity::class,
        CategoriaEntity::class,
        ActividadMantenimientoEntity::class,
        MantenimientoEntity::class,
        RepuestoEntity::class,
        DocumentoEntity::class,
        AlertaEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MiCarroDatabase : RoomDatabase() {

    abstract fun vehiculoDao(): VehiculoDao
    abstract fun kilometrajeDao(): KilometrajeDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun actividadDao(): ActividadDao
    abstract fun mantenimientoDao(): MantenimientoDao
    abstract fun repuestoDao(): RepuestoDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun alertaDao(): AlertaDao

    companion object {
        /** Nombre del archivo local de la base de datos (usado por el respaldo, RF-39). */
        const val NOMBRE_ARCHIVO = "micarro.db"

        /** Añade los datos de pico y placa conservando los vehículos ya registrados. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehiculos ADD COLUMN ciudadPicoPlaca TEXT")
                db.execSQL("ALTER TABLE vehiculos ADD COLUMN picoYPlaca TEXT")
            }
        }

        @Volatile
        private var INSTANCE: MiCarroDatabase? = null

        fun getInstance(context: Context): MiCarroDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MiCarroDatabase::class.java,
                    NOMBRE_ARCHIVO
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
