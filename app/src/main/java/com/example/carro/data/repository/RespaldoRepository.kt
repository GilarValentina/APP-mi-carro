package com.example.carro.data.repository

import android.content.Context
import android.net.Uri
import com.example.carro.core.database.MiCarroDatabase
import com.example.carro.domain.historial.EntradaHistorial
import com.example.carro.domain.historial.ExportadorCsv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Resultado de una operación de respaldo o borrado, con un mensaje para el usuario (RNF-15). */
sealed interface ResultadoRespaldo {
    data class Exito(val mensaje: String) : ResultadoRespaldo
    data class Error(val mensaje: String) : ResultadoRespaldo
}

/**
 * Copia de respaldo, restauración y borrado definitivo de los datos locales
 * (RF-38, RF-39, RF-40 y RNF-09).
 *
 * El respaldo es una copia del archivo SQLite de la aplicación: el usuario elige
 * dónde guardarlo mediante el selector del sistema, de modo que los datos quedan
 * bajo su control y no salen de su dispositivo sin su intervención.
 */
class RespaldoRepository(
    context: Context,
    private val database: MiCarroDatabase
) {

    private val appContext = context.applicationContext

    /** Primeros bytes de todo archivo SQLite válido; evita restaurar un archivo cualquiera. */
    private val firmaSqlite = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    private val archivoBase: File get() = appContext.getDatabasePath(MiCarroDatabase.NOMBRE_ARCHIVO)

    /** Nombre sugerido para el archivo de respaldo. */
    fun nombreSugerido(): String =
        "micarro-respaldo-${java.time.LocalDate.now()}.db"

    /**
     * RF-39: escribe una copia consistente de la base de datos en el destino elegido.
     * Antes de copiar vuelca el diario WAL para no perder operaciones recientes (RNF-05).
     */
    suspend fun crearRespaldo(destino: Uri): ResultadoRespaldo = withContext(Dispatchers.IO) {
        try {
            consolidarDiario()
            appContext.contentResolver.openOutputStream(destino, "wt")?.use { salida ->
                archivoBase.inputStream().use { entrada -> entrada.copyTo(salida) }
            } ?: return@withContext ResultadoRespaldo.Error("No se pudo abrir el archivo de destino.")
            ResultadoRespaldo.Exito("Respaldo creado correctamente.")
        } catch (e: IOException) {
            ResultadoRespaldo.Error("No se pudo crear el respaldo: ${e.localizedMessage ?: "error de almacenamiento"}")
        } catch (e: SecurityException) {
            ResultadoRespaldo.Error("La aplicación no tiene acceso al archivo seleccionado.")
        }
    }

    /**
     * RF-39: reemplaza los datos actuales con los del respaldo seleccionado.
     *
     * Valida el archivo antes de sobrescribir y conserva una copia temporal de los
     * datos actuales para revertir si la escritura falla (RNF-05). Tras restaurar,
     * la aplicación debe reiniciarse para volver a abrir la base de datos.
     */
    suspend fun restaurarRespaldo(origen: Uri): ResultadoRespaldo = withContext(Dispatchers.IO) {
        val temporal = File(appContext.cacheDir, "restauracion.db")
        val seguridad = File(appContext.cacheDir, "previo.db")
        try {
            appContext.contentResolver.openInputStream(origen)?.use { entrada ->
                temporal.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return@withContext ResultadoRespaldo.Error("No se pudo abrir el archivo seleccionado.")

            if (!esRespaldoValido(temporal)) {
                return@withContext ResultadoRespaldo.Error(
                    "El archivo seleccionado no es un respaldo de MiCarro."
                )
            }

            consolidarDiario()
            archivoBase.copyTo(seguridad, overwrite = true)
            database.close()
            try {
                temporal.copyTo(archivoBase, overwrite = true)
                eliminarDiarios()
            } catch (e: IOException) {
                seguridad.copyTo(archivoBase, overwrite = true)
                return@withContext ResultadoRespaldo.Error(
                    "No se pudo restaurar el respaldo; los datos actuales se conservaron."
                )
            }
            ResultadoRespaldo.Exito("Respaldo restaurado. La aplicación se reiniciará.")
        } catch (e: IOException) {
            ResultadoRespaldo.Error("No se pudo leer el respaldo: ${e.localizedMessage ?: "error de almacenamiento"}")
        } catch (e: SecurityException) {
            ResultadoRespaldo.Error("La aplicación no tiene acceso al archivo seleccionado.")
        } finally {
            temporal.delete()
            seguridad.delete()
        }
    }

    /**
     * RF-40 y RNF-09: borra definitivamente todos los registros y la configuración local.
     * La estructura de la base de datos se conserva, de modo que la app queda como recién instalada.
     */
    suspend fun eliminarTodo(): ResultadoRespaldo = withContext(Dispatchers.IO) {
        try {
            database.clearAllTables()
            appContext.getSharedPreferences("alertas_config", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
            ResultadoRespaldo.Exito("Se eliminaron todos los datos.")
        } catch (e: RuntimeException) {
            ResultadoRespaldo.Error("No se pudieron eliminar los datos: ${e.localizedMessage ?: "error desconocido"}")
        }
    }

    /** RF-38: escribe el historial del vehículo como CSV en el destino elegido. */
    suspend fun exportarHistorial(
        destino: Uri,
        titulo: String,
        entradas: List<EntradaHistorial>
    ): ResultadoRespaldo = withContext(Dispatchers.IO) {
        try {
            val contenido = ExportadorCsv.generar(titulo, entradas)
            appContext.contentResolver.openOutputStream(destino, "wt")?.use { salida ->
                salida.write(contenido.toByteArray(Charsets.UTF_8))
            } ?: return@withContext ResultadoRespaldo.Error("No se pudo abrir el archivo de destino.")
            ResultadoRespaldo.Exito("Historial exportado (${entradas.size} registros).")
        } catch (e: IOException) {
            ResultadoRespaldo.Error("No se pudo exportar el historial: ${e.localizedMessage ?: "error de almacenamiento"}")
        } catch (e: SecurityException) {
            ResultadoRespaldo.Error("La aplicación no tiene acceso al archivo seleccionado.")
        }
    }

    /** Vuelca el contenido del diario WAL al archivo principal antes de copiarlo. */
    private fun consolidarDiario() {
        database.openHelper.writableDatabase
            .query("PRAGMA wal_checkpoint(FULL)")
            .use { it.moveToFirst() }
    }

    private fun eliminarDiarios() {
        listOf("-wal", "-shm").forEach { sufijo ->
            File(archivoBase.path + sufijo).delete()
        }
    }

    private fun esRespaldoValido(archivo: File): Boolean {
        if (archivo.length() < firmaSqlite.size) return false
        val cabecera = ByteArray(firmaSqlite.size)
        archivo.inputStream().use { it.read(cabecera) }
        return cabecera.contentEquals(firmaSqlite)
    }
}
