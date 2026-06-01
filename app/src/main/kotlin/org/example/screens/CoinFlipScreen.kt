package org.example.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.components.ConfettiState
import org.example.components.SparkleConfetti
import org.example.components.DoodleCard
import org.example.components.DoodleButton
import org.example.components.NotebookBackground
import org.example.theme.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinFlipScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        org.example.audio.BubbleSoundPlayer.initialize(context)
    }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val scope = rememberCoroutineScope()

    var optionHeads by remember { mutableStateOf("GO") }
    var optionTails by remember { mutableStateOf("BACK") }

    var isFlipping by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    
    // Animation properties
    val rotationXAnim = remember { Animatable(0f) }
    val translationYAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }
    
    val confettiState = remember { ConfettiState() }
    var showResultCard by remember { mutableStateOf(false) }

    // Haptics helper
    fun triggerVibration(duration: Long, amplitude: Int = -1) {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = if (amplitude != -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createOneShot(duration, amplitude)
                    } else {
                        VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun flipCoin() {
        if (isFlipping) return
        isFlipping = true
        showResultCard = false
        triggerVibration(40, 120)

        scope.launch {
            val landsOnHeads = Random.nextBoolean()
            val finalRotation = if (landsOnHeads) {
                7 * 360f // lands on Heads side
            } else {
                7 * 360f + 180f // lands on Tails side
            }

            launch {
                // Fly up to height -350px and fall back
                translationYAnim.animateTo(
                    targetValue = -350f,
                    animationSpec = tween(durationMillis = 800, easing = EaseOutQuad)
                )
                translationYAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 800, easing = EaseInQuad)
                )
            }

            launch {
                // Scale up when flying, scale down on land
                scaleAnim.animateTo(
                    targetValue = 1.3f,
                    animationSpec = tween(durationMillis = 800, easing = EaseOutQuad)
                )
                scaleAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 800, easing = EaseInQuad)
                )
            }

            // Spin rotation
            rotationXAnim.animateTo(
                targetValue = finalRotation,
                animationSpec = tween(durationMillis = 1600, easing = EaseInOutCubic)
            )

            isFlipping = false
            
            // Set result
            val outcome = if (landsOnHeads) optionHeads else optionTails
            resultText = outcome
            showResultCard = true

            // Celebration haptics and confetti
            triggerVibration(250, 180)
            confettiState.spawn(
                x = context.resources.displayMetrics.widthPixels / 2f,
                y = 900f,
                count = 35
            )
        }
    }

    NotebookBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button styled with custom retro comic border
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clickable {
                            org.example.audio.BubbleSoundPlayer.playSmallPop()
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀", 
                        color = TextPrimary, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = "COIN FLIP",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Coin Sketch Arena
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Sketch-styled Coin
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .graphicsLayer {
                            rotationX = rotationXAnim.value
                            translationY = translationYAnim.value
                            scaleX = scaleAnim.value
                            scaleY = scaleAnim.value
                            cameraDistance = 12f * density
                        }
                        .background(CrayonYellow, CircleShape)
                        .border(4.5.dp, BorderColor, CircleShape)
                        .clickable(enabled = !isFlipping) { flipCoin() },
                    contentAlignment = Alignment.Center
                ) {
                    // Determine which side is visible depending on rotation angle
                    val isHeadsVisible = ((rotationXAnim.value + 90f) % 360f) in 90f..270f
                    
                    if (isHeadsVisible) {
                        // Tails Face (Childish Sketch Star)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationX = 180f }
                        ) {
                            Text(
                                text = "★",
                                fontSize = 68.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = optionTails.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    } else {
                        // Heads Face (Childish Smiley Face ☺)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "☺",
                                fontSize = 68.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = optionHeads.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            // Options Input Row (Custom labels for heads/tails)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Customize Coin:",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Heads input
                    OutlinedTextField(
                        value = optionHeads,
                        onValueChange = { optionHeads = it.take(15) },
                        label = { Text("Coin Side A (Heads)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        enabled = !isFlipping,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BorderColor,
                            unfocusedBorderColor = BorderColor.copy(alpha = 0.4f),
                            focusedLabelColor = TextPrimary,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = TextPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(12.dp))
                    )

                    // Tails input
                    OutlinedTextField(
                        value = optionTails,
                        onValueChange = { optionTails = it.take(15) },
                        label = { Text("Coin Side B (Tails)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        enabled = !isFlipping,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BorderColor,
                            unfocusedBorderColor = BorderColor.copy(alpha = 0.4f),
                            focusedLabelColor = TextPrimary,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = TextPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Flip Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                DoodleButton(
                    onClick = { flipCoin() },
                    backgroundColor = NeonPink,
                    text = if (isFlipping) "FLIPPING..." else "FLIP COIN!",
                    textColor = Color.White,
                    enabled = !isFlipping,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Victory Confetti
        SparkleConfetti(state = confettiState)

        // Floating Victory Outcome Sobekan Kertas Card
        if (showResultCard) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showResultCard = false },
                contentAlignment = Alignment.Center
            ) {
                DoodleCard(
                    backgroundColor = Color.White,
                    shadowOffset = 8.dp,
                    modifier = Modifier
                        .width(310.dp)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DECISION RESULT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Highlight Outcome Box (Crayon Casing)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CrayonYellow.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .border(2.5.dp, BorderColor, RoundedCornerShape(16.dp))
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = resultText.uppercase(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        DoodleButton(
                            onClick = { showResultCard = false },
                            backgroundColor = NeonCyan,
                            text = "AGREED!",
                            textColor = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
