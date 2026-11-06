package com.example.carro.data.repository

import android.content.Context
import com.example.carro.data.dao.AlertaDao
import com.example.carro.data.dao.DocumentoDao
import com.example.carro.data.entity.AlertaEntity
import com.example.carro.data.entity.DocumentoEntity
import com.example.carro.domain.alerta.Alerta
import com.example.carro.domain.alerta.CalculadorVencimiento
import com.example.carro.domain.alerta.ConfiguracionAlertas
import com.example.carro.domain.alerta.Documento
import com.example.carro.domain.alerta.DocumentoConEstado
import com.example.carro.domain.alerta.EstadoVencimiento
import com.example.carro.domain.model.TipoAlerta
import com.example.carro.domain.model.TipoDocumento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Repositorio de alertas y documentos legales (RF-28 a RF-33).
 * Gestiona los documentos con vencimiento, el cálculo de su estado, las alertas
 * locales y la configuración de anticipación del usuario (RF-29).
 */
class AlertasRepository(
    context: Context,
    private val documentoDao: DocumentoDao,
    private val alertaDao: AlertaDao
) {

    private val prefs = context.applicationContext
        .getSharedPreferences("alertas_config", Context.MODE_PRIVATE)

    private val configuracionFlow = MutableStateFlow(leerConfiguracion())

    val configuracion: Flow<ConfiguracionAlertas> = configuracionFlow

    private fun leerConfiguracion(): ConfiguracionAlertas = ConfiguracionAlertas(
        alertasActivas = prefs.getBoolean("activas", true),
        anticipacionDias = prefs.getInt("anticipacion", 15)
    )

    fun actualizarConfiguracion(config: ConfiguracionAlertas) {
        prefs.edit()
            .putBoolean("activas", config.alertasActivas)
            .putInt("anticipacion", config.anticipacionDias)
            .apply()
        configuracionFlow.value = config
    }

    fun configuracionActual(): ConfiguracionAlertas = configuracionFlow.value

    // ---- Documentos (RF-32, RF-33) ----

    fun observarDocumentos(vehiculoId: Long): Flow<List<DocumentoConEstado>> =
        combine(
            documentoDao.observarPorVehiculo(vehiculoId),
            configuracionFlow
        ) { documentos, config ->
            val hoy = LocalDate.now()
            documentos
                .map { CalculadorVencimiento.evaluar(it.toDomain(), hoy, it.anticipacionDias ?: config.anticipacionDias) }
                .sortedBy { it.diasRestantes }
        }

    suspend fun obtenerDocumento(id: Long): Documento? =
        documentoDao.obtenerPorId(id)?.toDomain()

    suspend fun guardarDocumento(documento: Documento): Long {
        val id = if (documento.id == 0L) {
            documentoDao.insertar(documento.toEntity())
        } else {
            documentoDao.actualizar(documento.toEntity())
            documento.id
        }
        sincronizarAlertaDocumento(documento.copy(id = id))
        return id
    }

    suspend fun eliminarDocumento(documento: Documento) {
        alertaDao.eliminarPorDocumento(documento.id)
        documentoDao.eliminar(documento.toEntity())
    }

    // ---- Alertas (RF-28, RF-30, RF-31) ----

    fun observarAlertasActivas(): Flow<List<Alerta>> =
        alertaDao.observarActivas().map { lista -> lista.map { it.toDomain() } }

    /** RF-31 y RN-08: pospone el aviso sin alterar el vencimiento real del documento. */
    suspend fun posponerAlerta(alerta: Alerta, dias: Int) {
        val nuevaFecha = LocalDate.now().plusDays(dias.toLong())
        alertaDao.actualizar(alerta.copy(pospuestaHasta = nuevaFecha).toEntity())
    }

    suspend fun marcarAtendida(alerta: Alerta) {
        alertaDao.actualizar(alerta.copy(atendida = true, activa = false).toEntity())
    }

    /**
     * Sincroniza las alertas de todos los documentos con su estado actual (RF-28, RF-33).
     * Crea o conserva alertas para documentos próximos o vencidos y elimina las de documentos vigentes.
     * Devuelve los documentos que requieren aviso, para notificar.
     */
    suspend fun sincronizarAlertas(): List<DocumentoConEstado> {
        val hoy = LocalDate.now()
        val anticipacion = configuracionActual().anticipacionDias
        val pendientes = mutableListOf<DocumentoConEstado>()
        documentoDao.obtenerTodos().forEach { entity ->
            val evaluado = CalculadorVencimiento.evaluar(entity.toDomain(), hoy, entity.anticipacionDias ?: anticipacion)
            if (evaluado.estado == EstadoVencimiento.VIGENTE) {
                alertaDao.eliminarPorDocumento(entity.id)
            } else {
                sincronizarAlertaDocumento(entity.toDomain(), evaluado.estado, hoy)
                // RF-31 / RN-08: no se notifica mientras el aviso siga pospuesto a futuro.
                val alerta = alertaDao.obtenerPorDocumento(entity.id)?.toDomain()
                if (alerta == null || !alerta.fechaEfectiva.isAfter(hoy)) {
                    pendientes += evaluado
                }
            }
        }
        return pendientes
    }

    private suspend fun sincronizarAlertaDocumento(
        documento: Documento,
        estadoPrevio: EstadoVencimiento? = null,
        hoy: LocalDate = LocalDate.now()
    ) {
        val estado = estadoPrevio
            ?: CalculadorVencimiento.estado(
                documento.fechaVencimiento,
                hoy,
                documento.anticipacionDias ?: configuracionActual().anticipacionDias
            )
        if (estado == EstadoVencimiento.VIGENTE) {
            alertaDao.eliminarPorDocumento(documento.id)
            return
        }
        val existente = alertaDao.obtenerPorDocumento(documento.id)
        val causa = if (estado == EstadoVencimiento.VENCIDO) {
            "${documento.titulo} está vencido (venció el ${documento.fechaVencimiento})."
        } else {
            "${documento.titulo} vence el ${documento.fechaVencimiento}."
        }
        if (existente == null) {
            alertaDao.insertar(
                AlertaEntity(
                    vehiculoId = documento.vehiculoId,
                    documentoId = documento.id,
                    tipo = TipoAlerta.DOCUMENTO.name,
                    causa = causa,
                    fechaAviso = hoy
                )
            )
        } else {
            alertaDao.actualizar(existente.copy(causa = causa, activa = true))
        }
    }
}

private fun DocumentoEntity.toDomain(): Documento = Documento(
    id = id,
    vehiculoId = vehiculoId,
    tipo = TipoDocumento.desde(tipo),
    nombre = nombre,
    fechaVencimiento = fechaVencimiento,
    fechaExpedicion = fechaExpedicion,
    adjuntoUri = adjuntoUri,
    anticipacionDias = anticipacionDias
)

private fun Documento.toEntity(): DocumentoEntity = DocumentoEntity(
    id = id,
    vehiculoId = vehiculoId,
    tipo = tipo.name,
    nombre = nombre,
    fechaVencimiento = fechaVencimiento,
    fechaExpedicion = fechaExpedicion,
    adjuntoUri = adjuntoUri,
    anticipacionDias = anticipacionDias
)

private fun AlertaEntity.toDomain(): Alerta = Alerta(
    id = id,
    vehiculoId = vehiculoId,
    documentoId = documentoId,
    actividadId = actividadId,
    tipo = runCatching { TipoAlerta.valueOf(tipo) }.getOrDefault(TipoAlerta.DOCUMENTO),
    causa = causa,
    fechaAviso = fechaAviso,
    pospuestaHasta = pospuestaHasta,
    activa = activa,
    atendida = atendida
)

private fun Alerta.toEntity(): AlertaEntity = AlertaEntity(
    id = id,
    vehiculoId = vehiculoId,
    documentoId = documentoId,
    actividadId = actividadId,
    tipo = tipo.name,
    causa = causa,
    fechaAviso = fechaAviso,
    pospuestaHasta = pospuestaHasta,
    activa = activa,
    atendida = atendida
)
