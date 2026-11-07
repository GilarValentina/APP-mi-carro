package com.example.carro.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = White,
    // Contenedores opacos: con transparencia, los FAB y chips se veían grises sobre las tarjetas.
    primaryContainer = Color(0xFFDCE5EE),
    onPrimaryContainer = NavyPrimary,
    secondary = Petroleum,
    onSecondary = White,
    secondaryContainer = Color(0xFFD9ECE8),
    onSecondaryContainer = SuccessDeep,
    tertiary = SkyBlue,
    onTertiary = NavyPrimary,
    tertiaryContainer = Color(0xFFE1EEF5),
    onTertiaryContainer = NavyPrimary,
    background = BackgroundSoft,
    onBackground = NavyPrimary,
    // Sin estos, menús, diálogos y selectores usan el lavanda por defecto de Material 3.
    surfaceContainerLowest = White,
    surfaceContainerLow = Color(0xFFF9FAFC),
    surfaceContainer = White,
    surfaceContainerHigh = White,
    surfaceContainerHighest = Color(0xFFEDF1F5),
    surfaceTint = NavyPrimary,
    surface = White,
    onSurface = NavyPrimary,
    surfaceVariant = Color(0xFFE8EEF2),
    onSurfaceVariant = SecondaryText,
    error = ErrorRed,
    onError = White,
    errorContainer = Color(0xFFFDE7E6),
    onErrorContainer = ErrorRed,
    outline = Color(0xFFD9E1E7),
    scrim = NavyPrimary.copy(alpha = 0.38f)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9DB9D1),
    onPrimary = NavyPrimary,
    primaryContainer = Color(0xFF23496E),
    onPrimaryContainer = Color(0xFFEAF4FF),
    secondary = Color(0xFF93BFC6),
    onSecondary = NavyPrimary,
    secondaryContainer = Color(0xFF1E4045),
    onSecondaryContainer = Color(0xFFE8F3F4),
    tertiary = Color(0xFFB0D9EC),
    onTertiary = NavyPrimary,
    tertiaryContainer = Color(0xFF2B5874),
    onTertiaryContainer = Color(0xFFEAF7FF),
    background = Color(0xFF0E1A2A),
    onBackground = Color(0xFFEAF3FF),
    surface = Color(0xFF15293D),
    onSurface = Color(0xFFEAF3FF),
    surfaceVariant = Color(0xFF1F3348),
    onSurfaceVariant = Color(0xFFB7C5D7),
    error = Color(0xFFF28B82),
    onError = NavyPrimary,
    errorContainer = Color(0xFF5F1E22),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF2C4461),
    scrim = Color(0xFF000000)
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

@Composable
fun CarroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivado: la app usa siempre la paleta institucional en lugar del color del fondo de pantalla (Android 12+).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}