package com.example.carro.ui.perfil

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carro.domain.perfil.CampoPerfil
import com.example.carro.domain.perfil.PerfilUsuario
import com.example.carro.domain.vehiculo.CatalogoFormulario
import com.example.carro.ui.componentes.SelectorDesplegable

/** Campos del perfil compartidos por el onboarding y la pantalla de ajustes. */
@Composable
fun FormularioPerfil(
    perfil: PerfilUsuario,
    errores: Map<CampoPerfil, String>,
    onCambio: (CampoPerfil, PerfilUsuario) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CampoPerfilTexto(
            valor = perfil.nombre,
            etiqueta = "Nombre(s) *",
            error = errores[CampoPerfil.NOMBRE],
            capitalizacion = KeyboardCapitalization.Words,
            onCambio = { onCambio(CampoPerfil.NOMBRE, perfil.copy(nombre = it)) }
        )
        CampoPerfilTexto(
            valor = perfil.apellido,
            etiqueta = "Apellido(s) *",
            error = errores[CampoPerfil.APELLIDO],
            capitalizacion = KeyboardCapitalization.Words,
            onCambio = { onCambio(CampoPerfil.APELLIDO, perfil.copy(apellido = it)) }
        )
        SelectorDesplegable(
            etiqueta = "Ciudad *",
            buscable = true,
            opciones = CatalogoFormulario.ciudades,
            seleccionado = perfil.ciudad.ifBlank { null },
            onSeleccion = { onCambio(CampoPerfil.CIUDAD, perfil.copy(ciudad = it)) },
            error = errores[CampoPerfil.CIUDAD],
            modifier = Modifier.fillMaxWidth()
        )
        CampoPerfilTexto(
            valor = perfil.correo,
            etiqueta = "Correo electrónico *",
            error = errores[CampoPerfil.CORREO],
            teclado = KeyboardType.Email,
            capitalizacion = KeyboardCapitalization.None,
            onCambio = { onCambio(CampoPerfil.CORREO, perfil.copy(correo = it.trim())) }
        )
        CampoPerfilTexto(
            valor = perfil.telefono,
            etiqueta = "Teléfono (opcional)",
            error = errores[CampoPerfil.TELEFONO],
            teclado = KeyboardType.Phone,
            capitalizacion = KeyboardCapitalization.None,
            onCambio = { v -> onCambio(CampoPerfil.TELEFONO, perfil.copy(telefono = v.filter(Char::isDigit).take(10))) }
        )
    }
}

@Composable
private fun CampoPerfilTexto(
    valor: String,
    etiqueta: String,
    error: String?,
    onCambio: (String) -> Unit,
    teclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.Sentences
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalizacion,
            keyboardType = teclado,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Permiso que se pide para notificaciones locales; null en Android 12 o anterior (no hace falta). */
val permisoNotificaciones: String? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null

/** Abre los ajustes de notificaciones de la app, para reactivarlas si el usuario las rechazó. */
fun abrirAjustesNotificaciones(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

const val TEXTO_PERMISO_NOTIFICACIONES =
    "Activa las notificaciones para recibir alertas sobre mantenimientos, documentos y eventos importantes de tu vehículo."
