package dev.dreamteam.a22200322_ito_22200350_huamani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.dreamteam.a22200322_ito_22200350_huamani.core.navigation.NasaApp
import dev.dreamteam.a22200322_ito_22200350_huamani.ui.theme._22200322ITO22200350HUAMANITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _22200322ITO22200350HUAMANITheme {
                NasaApp()
            }
        }
    }
}
