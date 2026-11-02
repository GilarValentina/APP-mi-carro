package com.example.carro.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private const val DURACION_MS = 3_200L
private const val VUELTA_MS = 2_600

/**
 * Pantalla de bienvenida: un vehículo recorre un circuito mientras la app termina
 * de iniciar (RNF-15). El carro se dibuja en vectores para verse nítido en cualquier
 * densidad de pantalla.
 *
 * @param onTerminado se invoca una sola vez al finalizar la animación.
 */
@Composable
fun SplashScreen(onTerminado: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(DURACION_MS)
        onTerminado()
    }

    val transicion = rememberInfiniteTransition(label = "splash")
    val giro by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(VUELTA_MS, easing = LinearEasing)),
        label = "giro"
    )

    val colores = MaterialTheme.colorScheme

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .semantics { contentDescription = "Iniciando MiCarro" },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val radio = size.minDimension / 2f - 26.dp.toPx()
                    dibujarCircuito(
                        radio = radio,
                        asfalto = colores.surfaceVariant,
                        linea = colores.onSurfaceVariant,
                        meta = colores.primary
                    )

                    val radianes = Math.toRadians(giro.toDouble())
                    translate(
                        left = radio * cos(radianes).toFloat(),
                        top = radio * sin(radianes).toFloat()
                    ) {
                        rotate(degrees = giro + 180f, pivot = center) {
                            dibujarCarro(
                                centro = center,
                                largo = 44.dp.toPx(),
                                ancho = 26.dp.toPx(),
                                carroceria = colores.primary,
                                vidrios = colores.surface,
                                detalles = colores.onSurface,
                                luces = colores.tertiary
                            )
                        }
                    }
                }
            }

            Text(
                text = "MiCarro",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = "Tu mantenimiento al día",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** Asfalto circular con línea central discontinua y línea de meta. */
private fun DrawScope.dibujarCircuito(radio: Float, asfalto: Color, linea: Color, meta: Color) {
    drawCircle(color = asfalto, radius = radio, style = Stroke(width = 42.dp.toPx()))
    drawCircle(
        color = linea,
        radius = radio,
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(10.dp.toPx(), 10.dp.toPx())
            )
        )
    )
    drawLine(
        color = meta,
        start = Offset(center.x + radio - 21.dp.toPx(), center.y),
        end = Offset(center.x + radio + 21.dp.toPx(), center.y),
        strokeWidth = 4.dp.toPx()
    )
}

/**
 * Carro visto desde arriba: carrocería, techo con vidrios, espejos, ruedas y luces.
 * El eje largo apunta hacia arriba; quien lo use debe rotarlo en la dirección de avance.
 */
private fun DrawScope.dibujarCarro(
    centro: Offset,
    largo: Float,
    ancho: Float,
    carroceria: Color,
    vidrios: Color,
    detalles: Color,
    luces: Color
) {
    val izquierda = centro.x - ancho / 2f
    val arriba = centro.y - largo / 2f
    val radioRueda = largo * 0.07f

    // Ruedas (delanteras y traseras, a lado y lado).
    listOf(arriba + largo * 0.22f, arriba + largo * 0.76f).forEach { y ->
        listOf(izquierda - radioRueda, izquierda + ancho - radioRueda).forEach { x ->
            drawRoundRect(
                color = detalles,
                topLeft = Offset(x, y),
                size = Size(radioRueda * 2f, largo * 0.17f),
                cornerRadius = CornerRadius(radioRueda, radioRueda)
            )
        }
    }

    // Carrocería.
    drawRoundRect(
        color = carroceria,
        topLeft = Offset(izquierda, arriba),
        size = Size(ancho, largo),
        cornerRadius = CornerRadius(ancho * 0.34f, ancho * 0.34f)
    )

    // Parabrisas y vidrio trasero.
    drawRoundRect(
        color = vidrios,
        topLeft = Offset(izquierda + ancho * 0.16f, arriba + largo * 0.17f),
        size = Size(ancho * 0.68f, largo * 0.15f),
        cornerRadius = CornerRadius(ancho * 0.12f, ancho * 0.12f)
    )
    drawRoundRect(
        color = vidrios,
        topLeft = Offset(izquierda + ancho * 0.16f, arriba + largo * 0.63f),
        size = Size(ancho * 0.68f, largo * 0.14f),
        cornerRadius = CornerRadius(ancho * 0.12f, ancho * 0.12f)
    )

    // Techo.
    drawRoundRect(
        color = detalles.copy(alpha = 0.18f),
        topLeft = Offset(izquierda + ancho * 0.13f, arriba + largo * 0.34f),
        size = Size(ancho * 0.74f, largo * 0.27f),
        cornerRadius = CornerRadius(ancho * 0.1f, ancho * 0.1f)
    )

    // Espejos laterales.
    listOf(izquierda - ancho * 0.08f, izquierda + ancho * 0.96f).forEach { x ->
        drawRoundRect(
            color = carroceria,
            topLeft = Offset(x, arriba + largo * 0.33f),
            size = Size(ancho * 0.12f, largo * 0.06f),
            cornerRadius = CornerRadius(ancho * 0.05f, ancho * 0.05f)
        )
    }

    // Luces delanteras y stops.
    listOf(izquierda + ancho * 0.16f, izquierda + ancho * 0.64f).forEach { x ->
        drawRoundRect(
            color = luces,
            topLeft = Offset(x, arriba + largo * 0.04f),
            size = Size(ancho * 0.2f, largo * 0.05f),
            cornerRadius = CornerRadius(ancho * 0.06f, ancho * 0.06f)
        )
        drawRoundRect(
            color = luces.copy(alpha = 0.7f),
            topLeft = Offset(x, arriba + largo * 0.91f),
            size = Size(ancho * 0.2f, largo * 0.05f),
            cornerRadius = CornerRadius(ancho * 0.06f, ancho * 0.06f)
        )
    }
}
