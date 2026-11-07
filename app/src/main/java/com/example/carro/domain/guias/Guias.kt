package com.example.carro.domain.guias

/** Paso de una guía de emergencia: título corto y explicación desplegable. */
data class PasoGuia(val titulo: String, val detalle: String)

/** Número de emergencia que se puede marcar desde la app (abre el marcador, no llama solo). */
data class NumeroEmergencia(val entidad: String, val numero: String, val descripcion: String)

enum class GuiaEmergencia(val id: String, val titulo: String, val resumen: String) {
    ACCIDENTE("accidente", "Accidente de tránsito", "Qué hacer en los primeros minutos"),
    PINCHAZO("pinchazo", "Pinchazo", "Cambia la llanta de forma segura"),
    BATERIA("bateria", "Batería descargada", "Arranque con cables paso a paso"),
    SOBRECALENTAMIENTO("sobrecalentamiento", "Sobrecalentamiento", "Si el indicador de temperatura sube"),
    VARADO("varado", "Vehículo varado", "Señaliza y pide asistencia"),
    NUMEROS("numeros", "Números de emergencia", "Líneas de atención en Colombia");

    companion object {
        fun desde(id: String?): GuiaEmergencia = entries.firstOrNull { it.id == id } ?: ACCIDENTE
    }
}

/**
 * Contenido de las guías. Son recomendaciones generales de seguridad vial;
 * no reemplazan las instrucciones de las autoridades ni del fabricante del vehículo.
 */
object ContenidoGuias {

    val numeros = listOf(
        NumeroEmergencia("Línea única de emergencias", "123", "Policía, ambulancias y bomberos"),
        NumeroEmergencia("Policía de Tránsito y Carreteras", "#767", "Accidentes y emergencias en vía"),
        NumeroEmergencia("Bomberos", "119", "Incendios y rescates"),
        NumeroEmergencia("Cruz Roja", "132", "Atención prehospitalaria"),
        NumeroEmergencia("Defensa Civil", "144", "Emergencias y desastres")
    )

    fun pasos(guia: GuiaEmergencia): List<PasoGuia> = when (guia) {
        GuiaEmergencia.ACCIDENTE -> listOf(
            PasoGuia("Mantén la calma y verifica que todos estén bien", "Revisa si hay personas heridas. Si alguien está lesionado, no lo muevas y llama de inmediato al 123."),
            PasoGuia("Señaliza la zona", "Enciende las luces de parqueo y ubica los conos o triángulos a unos 30 metros detrás del vehículo."),
            PasoGuia("Toma fotografías", "Registra la posición de los vehículos, las placas, los daños y el entorno antes de moverlos."),
            PasoGuia("Ubica los vehículos en la orilla", "Si no hay heridos y los vehículos pueden moverse, despeja la vía para evitar otro accidente y posibles multas."),
            PasoGuia("Intercambia datos", "Anota nombre, documento, teléfono, placa, aseguradora y SOAT del otro conductor."),
            PasoGuia("¿Necesitas tránsito o asistencia médica?", "Si hay heridos o no hay acuerdo, comunícate con la Policía de Tránsito (#767) y espera el informe de accidente.")
        )
        GuiaEmergencia.PINCHAZO -> listOf(
            PasoGuia("Detente en un lugar seguro", "Reduce la velocidad sin frenar bruscamente y estaciona en una zona plana, lejos del tráfico."),
            PasoGuia("Señaliza", "Activa las luces de parqueo, coloca el freno de mano y ubica los triángulos de seguridad."),
            PasoGuia("Afloja los pernos", "Con el vehículo aún en el suelo, afloja los pernos media vuelta con la llave de pernos."),
            PasoGuia("Levanta el vehículo", "Ubica el gato en el punto indicado en el manual y eleva hasta que la llanta quede libre."),
            PasoGuia("Cambia la llanta", "Retira los pernos, monta la llanta de repuesto y aprieta en cruz."),
            PasoGuia("Baja y verifica", "Baja el vehículo, aprieta los pernos por completo y revisa la presión del repuesto en cuanto puedas.")
        )
        GuiaEmergencia.BATERIA -> listOf(
            PasoGuia("Confirma que es la batería", "Si las luces del tablero encienden débil o el motor no gira, probablemente es la batería."),
            PasoGuia("Ubica otro vehículo", "Estaciónalo cerca, sin que los vehículos se toquen, y apaga ambos motores."),
            PasoGuia("Conecta los cables en orden", "Rojo al positivo (+) descargado, rojo al positivo (+) cargado, negro al negativo (–) cargado y negro a una parte metálica del motor descargado."),
            PasoGuia("Arranca", "Enciende el vehículo que ayuda, espera unos minutos y luego intenta arrancar el tuyo."),
            PasoGuia("Retira los cables al revés", "Desconéctalos en orden inverso y mantén el motor encendido al menos 20 minutos."),
            PasoGuia("Revisa la batería", "Si vuelve a pasar, lleva el vehículo a revisión: la batería o el alternador pueden estar fallando.")
        )
        GuiaEmergencia.SOBRECALENTAMIENTO -> listOf(
            PasoGuia("Detente y apaga el motor", "Si el indicador de temperatura sube o sale vapor, estaciona en un lugar seguro y apaga el vehículo."),
            PasoGuia("No abras el radiador en caliente", "El líquido está a presión y puede causar quemaduras graves. Espera al menos 30 minutos."),
            PasoGuia("Revisa el nivel de refrigerante", "Con el motor frío, verifica el depósito de expansión y completa con refrigerante o agua si es necesario."),
            PasoGuia("Busca fugas", "Observa si hay charcos o mangueras rotas debajo del vehículo."),
            PasoGuia("Continúa con precaución", "Si la temperatura se normaliza, conduce despacio hasta un taller. Si vuelve a subir, pide asistencia.")
        )
        GuiaEmergencia.VARADO -> listOf(
            PasoGuia("Sal de la vía", "Intenta orillarte por completo y enciende las luces de parqueo."),
            PasoGuia("Señaliza", "Ubica los triángulos de seguridad y usa el chaleco reflectivo si sales del vehículo."),
            PasoGuia("Ponte a salvo", "En carretera, espera detrás de la barrera de seguridad, nunca entre el vehículo y el tráfico."),
            PasoGuia("Pide asistencia", "Comunícate con la asistencia de tu seguro o del SOAT, o con la Policía de Carreteras (#767)."),
            PasoGuia("Comparte tu ubicación", "Indica el kilómetro, la vía y puntos de referencia cercanos.")
        )
        GuiaEmergencia.NUMEROS -> emptyList()
    }
}
