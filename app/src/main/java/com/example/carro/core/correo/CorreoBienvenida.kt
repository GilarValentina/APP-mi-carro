package com.example.carro.core.correo

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.carro.BuildConfig
import com.example.carro.domain.perfil.PerfilUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Envía el correo de agradecimiento al registrarse, a través del Apps Script configurado
 * en local.properties (CORREO_URL y CORREO_TOKEN). Si no hay internet, WorkManager
 * espera la conexión y reintenta; el registro en la app nunca se bloquea por esto.
 */
object CorreoBienvenida {

    private const val TRABAJO = "correo_bienvenida"
    internal const val KEY_NOMBRE = "nombre"
    internal const val KEY_CORREO = "correo"

    val configurado: Boolean
        get() = BuildConfig.CORREO_URL.isNotBlank() && BuildConfig.CORREO_TOKEN.isNotBlank()

    fun programar(context: Context, perfil: PerfilUsuario) {
        if (!configurado) return
        val solicitud = OneTimeWorkRequestBuilder<CorreoBienvenidaWorker>()
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .setInputData(workDataOf(KEY_NOMBRE to perfil.primerNombre, KEY_CORREO to perfil.correo))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(TRABAJO, ExistingWorkPolicy.REPLACE, solicitud)
    }
}

class CorreoBienvenidaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val correo = inputData.getString(CorreoBienvenida.KEY_CORREO).orEmpty()
        if (!CorreoBienvenida.configurado || correo.isBlank()) return@withContext Result.failure()

        val cuerpo = JSONObject()
            .put("token", BuildConfig.CORREO_TOKEN)
            .put("nombre", inputData.getString(CorreoBienvenida.KEY_NOMBRE).orEmpty())
            .put("correo", correo)
            .toString()

        try {
            val conexion = (URL(BuildConfig.CORREO_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            try {
                conexion.outputStream.use { it.write(cuerpo.toByteArray(Charsets.UTF_8)) }
                val codigo = conexion.responseCode
                val respuesta = if (codigo in 200..299) {
                    conexion.inputStream.bufferedReader().use { it.readText() }.trim()
                } else {
                    ""
                }
                when {
                    // Apps Script responde con una redirección después de ejecutar el script.
                    codigo in 300..399 -> Result.success()
                    respuesta == "ok" || respuesta == "ya enviado" -> Result.success()
                    codigo in 200..299 -> Result.failure() // token o correo rechazados: reintentar no ayuda
                    codigo >= 500 && runAttemptCount < MAX_INTENTOS -> Result.retry()
                    else -> Result.failure()
                }
            } finally {
                conexion.disconnect()
            }
        } catch (e: IOException) {
            if (runAttemptCount < MAX_INTENTOS) Result.retry() else Result.failure()
        }
    }

    private companion object {
        const val MAX_INTENTOS = 5
    }
}
