package com.example.carro.domain.alerta

import com.example.carro.domain.model.TipoDocumento
import java.time.LocalDate

/**
 * Documento legal del vehículo con fecha de vencimiento (RF-32, RF-33).
 * Modelo de dominio independiente de Room (RNF-12).
 */
data class Documento(
    val id: Long = 0,
    val vehiculoId: Long,
    val tipo: TipoDocumento,
    val nombre: String? = null,
    val fechaVencimiento: LocalDate,
    val fechaExpedicion: LocalDate? = null,
    /** URI de contenido del adjunto (foto o PDF) con permiso persistente. */
    val adjuntoUri: String? = null,
    /** Anticipación propia del recordatorio; null usa la configuración general (RF-29). */
    val anticipacionDias: Int? = null
) {
    /** Nombre a mostrar: el personalizado o la etiqueta del tipo. */
    val titulo: String get() = nombre?.takeIf { it.isNotBlank() } ?: tipo.etiqueta
}

/** Estado de vencimiento de un documento o actividad (RN-01, RN-02, RF-33). */
enum class EstadoVencimiento { VIGENTE, PROXIMO, VENCIDO }

/** Documento junto con su estado calculado y los días restantes para el vencimiento. */
data class DocumentoConEstado(
    val documento: Documento,
    val estado: EstadoVencimiento,
    val diasRestantes: Long
)
