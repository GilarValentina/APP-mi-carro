package com.example.carro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carro.ui.navigation.MiCarroNavHost
import com.example.carro.ui.perfil.OnboardingScreen
import com.example.carro.ui.splash.SplashScreen
import com.example.carro.ui.theme.CarroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val perfilRepository = (application as MiCarroApplication).container.perfilRepository
        setContent {
            CarroTheme {
                var mostrandoSplash by rememberSaveable { mutableStateOf(true) }
                val onboardingCompleto by perfilRepository.onboardingCompleto.collectAsStateWithLifecycle()
                when {
                    mostrandoSplash -> SplashScreen(onTerminado = { mostrandoSplash = false })
                    // Primer inicio: datos del usuario y permiso de notificaciones.
                    !onboardingCompleto -> OnboardingScreen(perfilRepository = perfilRepository, onTerminado = {})
                    else -> MiCarroNavHost()
                }
            }
        }
    }
}
