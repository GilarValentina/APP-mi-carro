package com.example.carro.domain.vehiculo

import com.example.carro.domain.model.TipoVehiculo

object VehiculoCatalog {
    private val lineasCarros: Map<String, List<String>> = mapOf(
        "Chevrolet" to listOf(
            "Spark", "Spark GT", "Spark Life", "Beat", "Onix", "Onix Turbo", "Sail", "Aveo",
            "Joy", "Tracker", "Captiva", "Equinox", "Groove", "Blazer", "Traverse", "Montana",
            "D-Max", "Colorado", "N300", "N400", "Camaro", "Corvette"
        ),
        "Renault" to listOf(
            "Twingo", "Kwid", "Kwid E-Tech", "Logan", "Sandero", "Sandero Stepway", "Stepway",
            "Symbol", "Clio", "Megane", "Fluence", "Duster", "Duster Oroch", "Captur", "Koleos",
            "Kardian", "Alaskan", "Kangoo"
        ),
        "Mazda" to listOf(
            "Mazda 2", "Mazda 2 Sport", "Mazda 3", "Mazda 3 Sedán", "Mazda 3 Sport", "Mazda 6",
            "CX-3", "CX-30", "CX-5", "CX-50", "CX-60", "CX-90", "BT-50", "MX-5"
        ),
        "Toyota" to listOf(
            "Yaris", "Yaris Cross", "Corolla", "Corolla Cross", "Raize", "RAV4", "C-HR",
            "Hilux", "Fortuner", "SW4", "Prado", "Land Cruiser", "Hiace", "Prius", "4Runner"
        ),
        "Kia" to listOf(
            "Picanto", "Rio", "K3", "Cerato", "Forte", "Soluto", "Stonic", "Seltos", "Sonet",
            "Sportage", "Sorento", "Carens", "Carnival", "Niro", "EV5", "Frontier", "K2500"
        ),
        "Nissan" to listOf(
            "March", "Versa", "Sentra", "Kicks", "Juke", "Qashqai", "X-Trail", "Murano",
            "Pathfinder", "Frontier", "Navara", "NP300", "Patrol", "Urvan"
        ),
        "Hyundai" to listOf(
            "Grand i10", "i20", "Accent", "Elantra", "Venue", "Creta", "Creta Grand", "Tucson",
            "Santa Fe", "Palisade", "Kona", "Staria", "H1", "Porter"
        ),
        "Suzuki" to listOf(
            "Alto", "Celerio", "Swift", "Baleno", "Ciaz", "Ignis", "S-Presso", "Fronx",
            "Vitara", "Grand Vitara", "S-Cross", "Jimny", "XL7", "Ertiga"
        ),
        "Volkswagen" to listOf(
            "Gol", "Polo", "Virtus", "Voyage", "Jetta", "Vento", "Golf", "Passat",
            "T-Cross", "Nivus", "Taos", "Tiguan", "Teramont", "Amarok", "Saveiro"
        ),
        "Ford" to listOf(
            "Fiesta", "Focus", "Escape", "Territory", "Bronco", "Bronco Sport", "Explorer",
            "Edge", "Expedition", "Ranger", "Maverick", "F-150", "Transit", "Mustang"
        ),
        "Honda" to listOf(
            "Fit", "City", "Civic", "Accord", "HR-V", "CR-V", "BR-V", "WR-V", "Pilot", "ZR-V"
        ),
        "Mitsubishi" to listOf(
            "Mirage", "Lancer", "ASX", "Eclipse Cross", "Outlander", "Xpander", "Xforce",
            "Montero", "Montero Sport", "L200", "Triton"
        ),
        "Fiat" to listOf(
            "Mobi", "Argo", "Cronos", "Uno", "Palio", "Pulse", "Fastback", "Toro", "Strada", "Ducato"
        ),
        "Jeep" to listOf(
            "Renegade", "Compass", "Commander", "Cherokee", "Grand Cherokee", "Wrangler", "Gladiator"
        ),
        "Peugeot" to listOf(
            "208", "2008", "301", "308", "3008", "408", "5008", "Partner", "Expert", "Landtrek"
        ),
        "Citroën" to listOf(
            "C3", "C3 Aircross", "C4 Cactus", "C4", "C5 Aircross", "Berlingo", "Jumpy", "Jumper"
        ),
        "Subaru" to listOf(
            "Impreza", "XV", "Crosstrek", "Forester", "Outback", "Legacy", "WRX", "BRZ"
        ),
        "Chery" to listOf(
            "QQ", "Arrizo 5", "Arrizo 6", "Tiggo 2", "Tiggo 3", "Tiggo 4", "Tiggo 5X",
            "Tiggo 7 Pro", "Tiggo 8 Pro", "Omoda 5"
        ),
        "JAC" to listOf(
            "S2", "S3", "S4", "S7", "T6", "T8", "E10X", "E-JS1", "Sunray", "X200"
        ),
        "BYD" to listOf(
            "Dolphin", "Dolphin Mini", "Seagull", "Yuan Plus", "Yuan Pro", "Song Plus",
            "Han", "Tang", "Seal", "Shark"
        ),
        "Changan" to listOf(
            "Alsvin", "Eado", "CS15", "CS35 Plus", "CS55 Plus", "CS75 Plus", "Hunter", "Star Truck"
        ),
        "Great Wall" to listOf(
            "Haval H6", "Haval Jolion", "Haval H2", "Poer", "Wingle 5", "Wingle 7", "Ora 03", "Tank 300"
        ),
        "Foton" to listOf(
            "Tunland", "Sauvana", "Aumark", "Gratour", "Toano", "View", "Terracota"
        ),
        "SsangYong" to listOf(
            "Tivoli", "Korando", "Rexton", "Musso", "Actyon", "Torres"
        ),
        "Dodge" to listOf(
            "Attitude", "Journey", "Durango", "Charger", "Challenger", "Hornet"
        ),
        "RAM" to listOf(
            "700", "1000", "1200", "1500", "2500", "3500", "Rampage"
        ),
        "Volvo" to listOf(
            "XC40", "XC60", "XC90", "S60", "S90", "V60", "C40", "EX30"
        ),
        "Land Rover" to listOf(
            "Defender", "Discovery", "Discovery Sport", "Range Rover", "Range Rover Sport",
            "Range Rover Evoque", "Range Rover Velar"
        ),
        "Tesla" to listOf(
            "Model 3", "Model Y", "Model S", "Model X", "Cybertruck"
        ),
        "BMW" to listOf(
            "Serie 1", "Serie 2", "Serie 3", "Serie 4", "Serie 5", "Serie 7",
            "X1", "X2", "X3", "X4", "X5", "X6", "X7", "iX", "i4"
        ),
        "Mercedes-Benz" to listOf(
            "Clase A", "Clase B", "Clase C", "Clase E", "Clase S", "CLA", "GLA", "GLB",
            "GLC", "GLE", "GLS", "Clase G", "Sprinter", "Vito"
        ),
        "Audi" to listOf(
            "A1", "A3", "A4", "A5", "A6", "A7", "Q2", "Q3", "Q5", "Q7", "Q8", "e-tron", "TT"
        )
    )

    private val lineasMotos: Map<String, List<String>> = mapOf(
        "Bajaj" to listOf(
            "Boxer CT 100", "Boxer BM 150", "Pulsar NS 125", "Pulsar NS 160", "Pulsar NS 200",
            "Pulsar N250", "Discover 125", "Dominar 400", "Platina", "Avenger"
        ),
        "Yamaha" to listOf(
            "FZ 2.0", "FZ-S", "FZ 25", "YBR 125", "Crypton", "BWS", "NMAX", "XTZ 125",
            "XTZ 150", "XTZ 250", "MT-03", "MT-07", "R3", "MT-09", "Calima"
        ),
        "AKT" to listOf(
            "AK 110", "AK 125 NKD", "AK 150", "AK 180 TT", "Dynamic 125", "Evo 125",
            "Flex 125", "Special 125", "TTR 180", "Jet 5"
        ),
        "Honda" to listOf(
            "CB 125F", "CB 160F", "CB 190R", "CB 300F", "CB 500F", "XR 150L", "XR 190L",
            "XRE 300", "Wave 110", "Dio 110", "Navi", "PCX 160", "ADV 160", "Africa Twin"
        ),
        "Suzuki" to listOf(
            "AX 100", "AX4", "Best 125", "Viva R", "GN 125", "Gixxer 150", "Gixxer 250",
            "DR 150", "DR 650", "V-Strom 250", "V-Strom 650", "Burgman Street", "GSX-S750"
        ),
        "Hero" to listOf(
            "Eco Deluxe", "Splendor", "Hunk 160R", "Xpulse 200", "Dash", "Ignitor", "Thriller 200R"
        ),
        "TVS" to listOf(
            "Sport 100", "Raider 125", "Apache RTR 160", "Apache RTR 200", "Apache RR 310",
            "NTorq 125", "Jupiter", "Stryker 125"
        ),
        "Victory" to listOf(
            "One", "Advance R", "MRX 125", "MRX 150", "Nitro 125", "Bomber 125", "Black 125", "Venom 400"
        ),
        "KTM" to listOf(
            "Duke 200", "Duke 250", "Duke 390", "RC 200", "RC 390", "Adventure 390", "Adventure 250"
        ),
        "Royal Enfield" to listOf(
            "Classic 350", "Hunter 350", "Meteor 350", "Himalayan", "Interceptor 650", "Continental GT 650"
        ),
        "Kawasaki" to listOf(
            "Ninja 400", "Ninja 300", "Z400", "Z650", "Z900", "Versys 650", "KLR 650", "Versys-X 300"
        ),
        "Triumph" to listOf(
            "Speed 400", "Scrambler 400 X", "Trident 660", "Tiger Sport 660", "Tiger 900",
            "Tiger 1200", "Street Triple", "Speed Triple", "Bonneville T100", "Bonneville T120",
            "Scrambler 1200", "Rocket 3", "Thruxton", "Speedmaster"
        ),
        "CFMoto" to listOf(
            "150 NK", "250 NK", "300 NK", "400 NK", "650 NK", "300 SR", "450 SR",
            "650 MT", "800 MT", "450 MT", "Papio", "Cforce 520", "Cforce 1000", "Uforce 1000"
        ),
        "BMW" to listOf(
            "G 310 R", "G 310 GS", "F 750 GS", "F 850 GS", "F 900 R", "R 1250 GS", "R 1300 GS", "S 1000 RR"
        ),
        "Ducati" to listOf(
            "Monster", "Scrambler", "Multistrada V2", "Multistrada V4", "Panigale V2", "Panigale V4", "DesertX"
        ),
        "Harley-Davidson" to listOf(
            "Nightster", "Sportster S", "Street Bob", "Fat Boy", "Pan America", "Road King", "Street Glide"
        )
    )

    private val lineasCamiones: Map<String, List<String>> = mapOf(
        "Chevrolet" to listOf("NHR", "NKR", "NPR", "NQR", "FRR", "FTR", "FVR", "FVZ"),
        "Hino" to listOf("Serie 300 Dutro", "Serie 500 FC", "Serie 500 FG", "Serie 500 GH", "Serie 700"),
        "Isuzu" to listOf("ELF 100", "ELF 300", "ELF 400", "ELF 600", "Forward 800", "Forward 1100"),
        "Foton" to listOf("Aumark", "Auman", "BJ1039", "Ollin", "Miler"),
        "JAC" to listOf("X200", "1035", "1040", "1063", "1083", "Gallop"),
        "Hyundai" to listOf("HD65", "HD78", "HD120", "Mighty", "Xcient"),
        "Kia" to listOf("K2500", "K2700", "K3000", "K4000"),
        "JMC" to listOf("Carrying", "N720", "N800", "N900"),
        "Mercedes-Benz" to listOf("Accelo", "Atego", "Axor", "Actros", "Arocs"),
        "Volvo" to listOf("FH", "FM", "FMX", "VM"),
        "Scania" to listOf("Serie P", "Serie G", "Serie R", "Serie S"),
        "Kenworth" to listOf("T370", "T680", "T800", "T880", "W900"),
        "International" to listOf("CV", "MV", "HV", "LT", "Lonestar", "ProStar", "7600"),
        "Freightliner" to listOf("M2 106", "M2 112", "Cascadia", "Columbia", "114SD"),
        "Fuso" to listOf("Canter", "Fighter", "FI", "FJ"),
        "Dongfeng" to listOf("Captain", "Duolika", "KR", "KL"),
        "Iveco" to listOf("Daily", "Tector", "Stralis", "S-Way", "Trakker"),
        "Mack" to listOf("Granite", "Anthem", "Pinnacle", "Vision"),
        "Sinotruk" to listOf("Howo", "Sitrak", "Homan")
    )

    private const val OTRA = "Otra"

    private fun catalogo(tipo: TipoVehiculo): Map<String, List<String>> = when (tipo) {
        TipoVehiculo.AUTOMOVIL, TipoVehiculo.CAMIONETA, TipoVehiculo.CAMPERO -> lineasCarros
        TipoVehiculo.MOTOCICLETA -> lineasMotos
        TipoVehiculo.CAMION -> lineasCamiones
        TipoVehiculo.OTRO -> (lineasCarros.keys + lineasMotos.keys + lineasCamiones.keys).associateWith { marca ->
            listOf(lineasCarros, lineasMotos, lineasCamiones).flatMap { it[marca].orEmpty() }.distinct()
        }
    }

    /** Marcas que aplican al [tipo] de vehículo; siempre termina con "Otra". */
    fun marcas(tipo: TipoVehiculo): List<String> = catalogo(tipo).keys.toList() + OTRA

    fun lineasDeMarca(marca: String?, tipo: TipoVehiculo): List<String> = when {
        marca.isNullOrBlank() -> emptyList()
        marca == OTRA -> listOf(OTRA)
        else -> catalogo(tipo)[marca]?.sorted().orEmpty()
    }

    fun descripcionPicoPlaca(ciudad: String, tipo: TipoVehiculo): String {
        val base = when (ciudad.trim()) {
            "Bogotá" -> "Restricción del pico y placa en Bogotá: horario restringido de lunes a viernes según el último dígito."
            "Medellín" -> "Restricción del pico y placa en Medellín: aplica en horas pico según el último dígito."
            "Cali" -> "Restricción del pico y placa en Cali: aplica en los horarios restringidos según el último dígito."
            "Barranquilla" -> "Restricción del pico y placa en Barranquilla: aplica con horario y últimos dígitos definidos por la ciudad."
            "Cartagena" -> "Restricción del pico y placa en Cartagena: aplica según el último dígito y el horario municipal."
            else -> "Pico y placa según la regulación municipal de la ciudad seleccionada."
        }
        return if (tipo == TipoVehiculo.MOTOCICLETA) {
            "$base (Motos: reglas particulares según ciudad y horario)."
        } else {
            base
        }
    }
}
