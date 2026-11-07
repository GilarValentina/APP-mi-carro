package com.example.carro.domain.perfil

/** Datos del usuario capturados en el onboarding inicial. Se guardan solo en el dispositivo. */
data class PerfilUsuario(
    val nombre: String = "",
    val apellido: String = "",
    val ciudad: String = "",
    val correo: String = "",
    val telefono: String = ""
) {
    val completo: Boolean get() = nombre.isNotBlank() && apellido.isNotBlank() && ciudad.isNotBlank() && correo.isNotBlank()

    /** Primer nombre para saludos cortos ("Hola Valentina"). */
    val primerNombre: String get() = nombre.trim().substringBefore(' ')
}

/** Campos del perfil que pueden tener error de validación. */
enum class CampoPerfil { NOMBRE, APELLIDO, CIUDAD, CORREO, TELEFONO }

/** Validaciones puras del perfil (RNF-16). */
object PerfilValidator {

    private val CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validar(perfil: PerfilUsuario): Map<CampoPerfil, String> {
        val errores = mutableMapOf<CampoPerfil, String>()
        if (perfil.nombre.isBlank()) errores[CampoPerfil.NOMBRE] = "Escribe tu nombre."
        if (perfil.apellido.isBlank()) errores[CampoPerfil.APELLIDO] = "Escribe tu apellido."
        if (perfil.ciudad.isBlank()) errores[CampoPerfil.CIUDAD] = "Selecciona tu ciudad."
        if (!CORREO.matches(perfil.correo.trim())) errores[CampoPerfil.CORREO] = "Escribe un correo válido."
        val telefono = perfil.telefono.filter(Char::isDigit)
        if (perfil.telefono.isNotBlank() && telefono.length !in 7..10) {
            errores[CampoPerfil.TELEFONO] = "El teléfono debe tener entre 7 y 10 dígitos."
        }
        return errores
    }
}
