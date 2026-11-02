package com.example.carro.domain.historial

/**
 * Generación del archivo compartible con el historial de un vehículo (RF-38).
 * Produce texto CSV puro, sin dependencias de Android, para poder probarlo (RNF-13).
 */
object ExportadorCsv {

    private val ENCABEZADOS = listOf(
        "Fecha",
        "Kilometraje",
        "Tipo",
        "Categoria",
        "Actividad",
        "Descripcion",
        "Taller",
        "Costo mano de obra",
        "Costo repuestos",
        "Costo total",
        "Repuestos"
    )

    /**
     * @param titulo encabezado descriptivo del vehículo, por ejemplo "ABC123 - Mazda 3 2019".
     * @return contenido CSV con una fila por mantenimiento y una fila final de totales.
     */
    fun generar(titulo: String, entradas: List<EntradaHistorial>): String {
        val filas = StringBuilder()
        filas.append(escapar("Historial de $titulo")).append("\n")
        filas.append(ENCABEZADOS.joinToString(",") { escapar(it) }).append("\n")
        entradas.sortedBy { it.fecha }.forEach { e ->
            filas.append(
                listOf(
                    e.fecha.toString(),
                    e.kilometraje.toString(),
                    e.tipo.name,
                    e.categoria,
                    e.actividad.orEmpty(),
                    e.descripcion,
                    e.taller.orEmpty(),
                    numero(e.costoManoObra),
                    numero(e.costoRepuestos),
                    numero(e.costoTotal),
                    e.cantidadRepuestos.toString()
                ).joinToString(",") { escapar(it) }
            ).append("\n")
        }
        val total = entradas.sumOf { it.costoTotal }
        filas.append(
            listOf("TOTAL", "", "", "", "", "", "", "", "", numero(total), "")
                .joinToString(",") { escapar(it) }
        ).append("\n")
        return filas.toString()
    }

    /** Nombre de archivo seguro, sin caracteres inválidos en el sistema de archivos. */
    fun nombreArchivo(placa: String): String =
        "historial_${placa.filter { it.isLetterOrDigit() }.ifEmpty { "vehiculo" }}.csv"

    private fun numero(valor: Double): String = "%.2f".format(java.util.Locale.US, valor)

    /** Entrecomilla el campo y duplica las comillas internas, según RFC 4180. */
    private fun escapar(valor: String): String =
        "\"" + valor.replace("\"", "\"\"").replace("\r\n", " ").replace("\n", " ") + "\""
}
