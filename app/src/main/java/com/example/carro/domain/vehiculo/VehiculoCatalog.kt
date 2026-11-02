package com.example.carro.domain.vehiculo

object VehiculoCatalog {
    val marcas: List<String> = listOf(
        "Toyota",
        "Chevrolet",
        "Renault",
        "Mazda",
        "Honda",
        "Ford",
        "Volkswagen",
        "Nissan",
        "Kia",
        "Hyundai",
        "BMW",
        "Mercedes-Benz",
        "Audi",
        "Suzuki",
        "Mitsubishi"
    )

    private val lineasPorMarca: Map<String, List<String>> = mapOf(
        "Toyota" to listOf("Corolla", "Yaris", "Hilux", "RAV4", "Prius"),
        "Chevrolet" to listOf("Spark", "Onix", "Tracker", "Camaro", "Equinox"),
        "Renault" to listOf("Logan", "Sandero", "Duster", "Captur", "Kwid"),
        "Mazda" to listOf("Mazda3", "CX-5", "BT-50", "Mazda2"),
        "Honda" to listOf("Civic", "CR-V", "Fit", "Accord"),
        "Ford" to listOf("Focus", "Escape", "Ranger", "Fiesta"),
        "Volkswagen" to listOf("Jetta", "Gol", "T-Cross", "Taos", "Golf"),
        "Nissan" to listOf("Sentra", "March", "X-Trail", "Frontier"),
        "Kia" to listOf("Rio", "Sportage", "Cerato", "Sorento"),
        "Hyundai" to listOf("Elantra", "Creta", "Accent", "Santa Fe"),
        "BMW" to listOf("Serie 3", "X3", "X5", "Serie 1"),
        "Mercedes-Benz" to listOf("Clase C", "GLA", "GLC", "Clase E"),
        "Audi" to listOf("A3", "A4", "Q5", "Q3"),
        "Suzuki" to listOf("Swift", "Vitara", "Jimny", "Baleno"),
        "Mitsubishi" to listOf("Lancer", "Outlander", "Montero", "ASX")
    )

    fun lineasDeMarca(marca: String?): List<String> =
        if (marca.isNullOrBlank()) emptyList() else lineasPorMarca[marca]?.sorted() ?: emptyList()

    fun descripcionPicoPlaca(ciudad: String, tipo: com.example.carro.domain.model.TipoVehiculo): String {
        val base = when (ciudad.trim()) {
            "Bogotá" -> "Restricción del pico y placa en Bogotá: horario restringido de lunes a viernes según el último dígito."
            "Medellín" -> "Restricción del pico y placa en Medellín: aplica en horas pico según el último dígito."
            "Cali" -> "Restricción del pico y placa en Cali: aplica en los horarios restringidos según el último dígito."
            "Barranquilla" -> "Restricción del pico y placa en Barranquilla: aplica con horario y últimos dígitos definidos por la ciudad."
            "Cartagena" -> "Restricción del pico y placa en Cartagena: aplica según el último dígito y el horario municipal."
            else -> "Pico y placa según la regulación municipal de la ciudad seleccionada."
        }
        return if (tipo == com.example.carro.domain.model.TipoVehiculo.MOTOCICLETA) {
            "$base (Motos: reglas particulares según ciudad y horario)."
        } else {
            base
        }
    }
}
