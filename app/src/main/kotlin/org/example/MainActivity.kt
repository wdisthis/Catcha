package org.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import org.example.screens.CoinFlipScreen
import org.example.screens.FingerChooserScreen
import org.example.screens.MainMenuScreen
import org.example.screens.RouletteScreen
import org.example.screens.SplashScreen
import org.example.theme.CatchaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable standard edge-to-edge status and navigation bars drawing
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CatchaTheme {
                var currentScreen by remember { mutableStateOf("splash") }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        // Custom slide & fade screen transitions for high-fidelity feel
                        if (targetState == "menu" && initialState == "splash") {
                            // Smooth fade-out of the splash screen
                            fadeIn(animationSpec = tween(600, easing = EaseInOutQuart)) togetherWith
                            fadeOut(animationSpec = tween(400, easing = EaseInOutQuart))
                        } else if (targetState == "menu") {
                            // When going back to menu, slide from left to right (reverse)
                            (slideInHorizontally(
                                initialOffsetX = { -it / 4 },
                                animationSpec = tween(400, easing = EaseInOutQuart)
                            ) + fadeIn(animationSpec = tween(400, easing = EaseInOutQuart))) togetherWith
                            (slideOutHorizontally(
                                targetOffsetX = { it / 4 },
                                animationSpec = tween(300, easing = EaseInOutQuart)
                            ) + fadeOut(animationSpec = tween(300, easing = EaseInOutQuart)))
                        } else {
                            // When navigating from menu to games, slide from right to left (forward)
                            (slideInHorizontally(
                                initialOffsetX = { it / 4 },
                                animationSpec = tween(400, easing = EaseInOutQuart)
                            ) + fadeIn(animationSpec = tween(400, easing = EaseInOutQuart))) togetherWith
                            (slideOutHorizontally(
                                targetOffsetX = { -it / 4 },
                                animationSpec = tween(300, easing = EaseInOutQuart)
                            ) + fadeOut(animationSpec = tween(300, easing = EaseInOutQuart)))
                        }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        "splash" -> SplashScreen(
                            onSplashFinished = { currentScreen = "menu" }
                        )
                        "menu" -> MainMenuScreen(
                            onNavigateToFingerChooser = { currentScreen = "finger" },
                            onNavigateToFingerGrouper = { currentScreen = "grouper" },
                            onNavigateToRoulette = { currentScreen = "roulette" },
                            onNavigateToCoinFlip = { currentScreen = "coin" }
                        )
                        "finger" -> {
                            BackHandler { currentScreen = "menu" }
                            FingerChooserScreen(
                                onBack = { currentScreen = "menu" }
                            )
                        }
                        "grouper" -> {
                            BackHandler { currentScreen = "menu" }
                            org.example.screens.FingerGrouperScreen(
                                onBack = { currentScreen = "menu" }
                            )
                        }
                        "roulette" -> {
                            BackHandler { currentScreen = "menu" }
                            RouletteScreen(
                                onBack = { currentScreen = "menu" }
                            )
                        }
                        "coin" -> {
                            BackHandler { currentScreen = "menu" }
                            CoinFlipScreen(
                                onBack = { currentScreen = "menu" }
                            )
                        }
                    }
                }
            }
        }
    }
}

