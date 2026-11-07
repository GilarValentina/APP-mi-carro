package com.example.carro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Cita agendada para el vehículo: taller, tecnomecánica, SOAT, seguro o mantenimiento. */
@Entity(
    tableName = "citas",
    foreignKeys = [
        ForeignKey(
            entity = VehiculoEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehiculoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehiculoId")]
)
data class CitaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val tipo: String,
    val titulo: String,
    val lugar: String? = null,
    val fecha: LocalDate,
    /** Hora del día en minutos desde la medianoche (0..1439). */
    val minutoDelDia: Int,
    val notas: String? = null,
    val anticipacionDias: Int = 1,
    val completada: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis()
)
