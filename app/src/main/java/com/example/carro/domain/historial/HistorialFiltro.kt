package com.example.carro.domain.historial

/**
 * Búsqueda y filtrado del historial (RF-35).
 * Funciones puras, sin dependencias de Android, para poder probarlas (RNF-13).
 */
object HistorialFiltro {

    /**
     * Aplica todos los criterios del filtro y devuelve las entradas en orden cronológico
     * descendente (RF-34). Los criterios se combinan con AND.
     */
    fun aplicar(entradas: List<EntradaHistorial>, filtro: FiltroHistorial): List<EntradaHistorial> =
        entradas
            .filter { cumple(it, filtro) }
            .sortedWith(compareByDescending<EntradaHistorial> { it.fecha }.thenByDescending { it.mantenimientoId })

    private fun cumple(entrada: EntradaHistorial, filtro: FiltroHistorial): Boolean {
        val texto = filtro.texto.trim()
        if (texto.isNotEmpty() && !coincideTexto(entrada, texto)) return false
        if (filtro.desde != null && entrada.fecha.isBefore(filtro.desde)) return false
        if (filtro.hasta != null && entrada.fecha.isAfter(filtro.hasta)) return false
        if (filtro.categorias.isNotEmpty() && entrada.categoria !in filtro.categorias) return false
        if (filtro.tipo != null && entrada.tipo != filtro.tipo) return false
        if (filtro.taller != null && !entrada.taller.equals(filtro.taller, ignoreCase = true)) return false
        if (filtro.costoMinimo != null && entrada.costoTotal < filtro.costoMinimo) return false
        if (filtro.costoMaximo != null && entrada.costoTotal > filtro.costoMaximo) return false
        return true
    }

    private fun coincideTexto(entrada: EntradaHistorial, texto: String): Boolean {
        val campos = listOfNotNull(
            entrada.descripcion,
            entrada.categoria,
            entrada.actividad,
            entrada.taller,
            entrada.kilometraje.toString()
        )
        return campos.any { it.contains(texto, ignoreCase = true) }
    }

    /** Categorías presentes en el historial, para ofrecerlas como opciones de filtro. */
    fun categoriasDisponibles(entradas: List<EntradaHistorial>): List<String> =
        entradas.map { it.categoria }.distinct().sorted()

    /** Talleres o responsables registrados, para ofrecerlos como opciones de filtro. */
    fun talleresDisponibles(entradas: List<EntradaHistorial>): List<String> =
        entradas.mapNotNull { it.taller?.trim()?.takeIf(String::isNotEmpty) }
            .distinctBy { it.lowercase() }
            .sorted()

    /** Costo máximo del historial, para acotar el selector de rango de costo. */
    fun costoMaximo(entradas: List<EntradaHistorial>): Double =
        entradas.maxOfOrNull { it.costoTotal } ?: 0.0
}
