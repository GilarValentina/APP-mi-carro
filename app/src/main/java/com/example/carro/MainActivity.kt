package com.example.carro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.carro.ui.navigation.MiCarroNavHost
import com.example.carro.ui.splash.SplashScreen
import com.example.carro.ui.theme.CarroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarroTheme {
                var mostrandoSplash by rememberSaveable { mutableStateOf(true) }
                if (mostrandoSplash) {
                    SplashScreen(onTerminado = { mostrandoSplash = false })
                } else {
                    MiCarroNavHost()
                }
            }
        }
    }
}
