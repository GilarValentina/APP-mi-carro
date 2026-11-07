package com.example.carro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Vehículo registrado por el usuario (RF-01, RF-02).
 * Es la entidad raíz: el resto de módulos la referencian por [id].
 */
@Entity(
    tableName = "vehiculos",
    indices = [Index(value = ["placa"], unique = true)]
)
data class VehiculoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val placa: String,
    val tipo: String,
    val marca: String,
    val linea: String,
    val modelo: String,
    val anio: Int,
    val kilometrajeActual: Long,
    val color: String? = null,
    val vin: String? = null,
    val combustible: String? = null,
    val cilindraje: String? = null,
    val fotoUri: String? = null,
    val ciudadPicoPlaca: String? = null,
    val picoYPlaca: String? = null,
    val ciudadMatricula: String? = null,
    val secretariaTransito: String? = null,
    val esPrincipal: Boolean = false,
    val archivado: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis()
)
