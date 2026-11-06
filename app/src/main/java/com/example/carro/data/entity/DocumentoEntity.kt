package com.example.carro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Documento legal con vencimiento: SOAT, RTM, seguro, etc. (RF-32, RF-33). */
@Entity(
    tableName = "documentos",
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
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val tipo: String,
    val nombre: String? = null,
    val fechaVencimiento: LocalDate,
    val fechaExpedicion: LocalDate? = null,
    /** URI de contenido del archivo adjunto (foto o PDF) elegido por el usuario. */
    val adjuntoUri: String? = null,
    /** Días de anticipación propios del documento; null usa la configuración general. */
    val anticipacionDias: Int? = null,
    val creadoEn: Long = System.currentTimeMillis()
)
