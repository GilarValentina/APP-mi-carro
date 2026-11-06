package com.example.carro.data.repository

import android.content.Context
import com.example.carro.domain.perfil.PerfilUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Perfil del usuario y preferencias de la app, guardados localmente (SharedPreferences).
 * Se mantiene entre sesiones y no sale del dispositivo.
 */
class PerfilRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("perfil_usuario", Context.MODE_PRIVATE)

    private val perfilFlow = MutableStateFlow(leerPerfil())
    val perfil: StateFlow<PerfilUsuario> = perfilFlow.asStateFlow()

    private val onboardingFlow = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    /** true cuando el usuario ya terminó la configuración inicial. */
    val onboardingCompleto: StateFlow<Boolean> = onboardingFlow.asStateFlow()

    private fun leerPerfil() = PerfilUsuario(
        nombre = prefs.getString(KEY_NOMBRE, "").orEmpty(),
        apellido = prefs.getString(KEY_APELLIDO, "").orEmpty(),
        ciudad = prefs.getString(KEY_CIUDAD, "").orEmpty(),
        correo = prefs.getString(KEY_CORREO, "").orEmpty(),
        telefono = prefs.getString(KEY_TELEFONO, "").orEmpty()
    )

    fun guardar(perfil: PerfilUsuario) {
        val limpio = perfil.copy(
            nombre = perfil.nombre.trim(),
            apellido = perfil.apellido.trim(),
            ciudad = perfil.ciudad.trim(),
            correo = perfil.correo.trim(),
            telefono = perfil.telefono.filter(Char::isDigit)
        )
        prefs.edit()
            .putString(KEY_NOMBRE, limpio.nombre)
            .putString(KEY_APELLIDO, limpio.apellido)
            .putString(KEY_CIUDAD, limpio.ciudad)
            .putString(KEY_CORREO, limpio.correo)
            .putString(KEY_TELEFONO, limpio.telefono)
            .apply()
        perfilFlow.value = limpio
    }

    fun completarOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
        onboardingFlow.value = true
    }

    /**
     * Preferencia de notificaciones push. La app aún no tiene servidor de mensajería:
     * se guarda la elección para cuando se integre un proveedor (p. ej. Firebase Cloud Messaging).
     */
    var pushHabilitado: Boolean
        get() = prefs.getBoolean(KEY_PUSH, false)
        set(valor) { prefs.edit().putBoolean(KEY_PUSH, valor).apply() }

    private companion object {
        const val KEY_NOMBRE = "nombre"
        const val KEY_APELLIDO = "apellido"
        const val KEY_CIUDAD = "ciudad"
        const val KEY_CORREO = "correo"
        const val KEY_TELEFONO = "telefono"
        const val KEY_ONBOARDING = "onboarding_completo"
        const val KEY_PUSH = "push_habilitado"
    }
}
