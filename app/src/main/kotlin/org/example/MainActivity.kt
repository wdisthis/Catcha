package org.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import org.example.screens.CoinFlipScreen
import org.example.screens.FingerChooserScreen
import org.example.screens.MainMenuScreen
import org.example.screens.RouletteScreen
import org.example.theme.CatchaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable standard edge-to-edge status and navigation bars drawing
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CatchaTheme {
                var currentScreen by remember { mutableStateOf("menu") }

                when (currentScreen) {
                    "menu" -> MainMenuScreen(
                        onNavigateToFingerChooser = { currentScreen = "finger" },
                        onNavigateToRoulette = { currentScreen = "roulette" },
                        onNavigateToCoinFlip = { currentScreen = "coin" }
                    )
                    "finger" -> FingerChooserScreen(
                        onBack = { currentScreen = "menu" }
                    )
                    "roulette" -> RouletteScreen(
                        onBack = { currentScreen = "menu" }
                    )
                    "coin" -> CoinFlipScreen(
                        onBack = { currentScreen = "menu" }
                    )
                }
            }
        }
    }
}
