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
import androidx.compose.ui.graphics.Brush
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
import org.example.theme.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinFlipScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val scope = rememberCoroutineScope()

    var optionHeads by remember { mutableStateOf("MAJU") }
    var optionTails by remember { mutableStateOf("MUNDUR") }

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
            // Decide final winner beforehand
            val landsOnHeads = Random.nextBoolean()
            
            // Total 3D rotations: at least 6 full loops (2160 degrees) or 6.5 loops (2340 degrees)
            val finalRotation = if (landsOnHeads) {
                7 * 360f // lands on Heads side
            } else {
                7 * 360f + 180f // lands on Tails side
            }

            // Launch concurrent 3D fly up / down, scale, and rotations
            launch {
                // Fly up to height -400px and fall back
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
                    targetValue = 1.4f,
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

            // Catch vibration and confetti
            triggerVibration(250, 180)
            confettiState.spawn(
                x = context.resources.displayMetrics.widthPixels / 2f,
                y = 900f,
                count = 35
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("<", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                Text(
                    text = "LEMPAR KOIN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Coin 3D Arena
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Interactive 3D Coin Model
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            rotationX = rotationXAnim.value
                            translationY = translationYAnim.value
                            scaleX = scaleAnim.value
                            scaleY = scaleAnim.value
                            cameraDistance = 12f * density
                        }
                        .shadow(16.dp, CircleShape, spotColor = NeonPink)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NeonPink, NeonPurple)
                            )
                        )
                        .border(6.dp, Color.White, CircleShape)
                        .clickable(enabled = !isFlipping) { flipCoin() },
                    contentAlignment = Alignment.Center
                ) {
                    // Determine which side to draw depending on angle
                    val isHeadsVisible = ((rotationXAnim.value + 90f) % 360f) in 90f..270f
                    
                    if (isHeadsVisible) {
                        // Tails Face
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationX = 180f } // flip text so it reads upright on the reverse side
                        ) {
                            Text(
                                text = "B",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    } else {
                        // Heads Face
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "A",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
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
                    text = "Kustomisasi Koin :",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
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
                        label = { Text("Koin A (Heads)", color = NeonPink, fontSize = 11.sp) },
                        singleLine = true,
                        enabled = !isFlipping,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = BorderColor
                        ),
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .background(ObsidianSurface, RoundedCornerShape(12.dp))
                    )

                    // Tails input
                    OutlinedTextField(
                        value = optionTails,
                        onValueChange = { optionTails = it.take(15) },
                        label = { Text("Koin B (Tails)", color = NeonPurple, fontSize = 11.sp) },
                        singleLine = true,
                        enabled = !isFlipping,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = BorderColor
                        ),
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .background(ObsidianSurface, RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Flip Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { flipCoin() },
                    enabled = !isFlipping,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = NeonPink)
                ) {
                    Text(
                        text = if (isFlipping) "MELAYANG..." else "LEMPAR KOIN!",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // Victory Confetti
        SparkleConfetti(state = confettiState)

        // Floating Victory Outcome Card
        if (showResultCard) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { showResultCard = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ObsidianSurface)
                        .border(2.dp, NeonPink, RoundedCornerShape(24.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HASIL KEPUTUSAN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Outcome Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NeonPink.copy(alpha = 0.15f))
                            .border(1.5.dp, NeonPink, RoundedCornerShape(16.dp))
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resultText.uppercase(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showResultCard = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SEPAKAT!", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
