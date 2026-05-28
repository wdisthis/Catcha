package org.example.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.components.ConfettiState
import org.example.components.SparkleConfetti
import org.example.theme.*
import kotlin.random.Random

enum class FingerGameState {
    WAITING,
    COUNTDOWN,
    SELECTED
}

@Composable
fun FingerChooserScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val scope = rememberCoroutineScope()

    var gameState by remember { mutableStateOf(FingerGameState.WAITING) }
    var targetWinners by remember { mutableStateOf(1) }
    
    val activeTouches = remember { mutableStateMapOf<PointerId, Offset>() }
    val touchColors = remember { mutableStateMapOf<PointerId, Color>() }
    
    val winningPointers = remember { mutableStateListOf<PointerId>() }
    
    var countdownProgress by remember { mutableStateOf(1f) } // 1.0 down to 0.0
    var countdownText by remember { mutableStateOf("") }
    
    val confettiState = remember { ConfettiState() }

    // Color list for fingers
    val colorsList = listOf(
        Color(0xFF00F0FF), // Cyan
        Color(0xFF9D4EDD), // Purple
        Color(0xFFFF007F), // Pink
        Color(0xFFFFD700), // Gold
        Color(0xFF39FF14), // Green
        Color(0xFFFF5722)  // Orange
    )

    // Trigger device haptics
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

    // Double check when touch counts change
    LaunchedEffect(activeTouches.size) {
        // Assign colors to new touches
        activeTouches.keys.forEach { pointerId ->
            if (!touchColors.containsKey(pointerId)) {
                touchColors[pointerId] = colorsList[Random.nextInt(colorsList.size)]
            }
        }
        
        // Remove colors for lifted touches
        val iterator = touchColors.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (!activeTouches.containsKey(item.key)) {
                iterator.remove()
            }
        }

        // Logic depending on count
        if (gameState != FingerGameState.SELECTED) {
            if (activeTouches.size >= targetWinners + 1 || (targetWinners == 1 && activeTouches.size >= 2)) {
                // We have enough fingers to start a countdown!
                if (gameState == FingerGameState.WAITING) {
                    gameState = FingerGameState.COUNTDOWN
                    triggerVibration(100, 150)
                }
            } else {
                // Not enough fingers
                if (gameState == FingerGameState.COUNTDOWN) {
                    gameState = FingerGameState.WAITING
                    // Quick alert double-buzz to notify user of cancellation
                    triggerVibration(50, 100)
                }
            }
        } else {
            // SELECTED state, once all fingers are lifted, reset to waiting
            if (activeTouches.isEmpty()) {
                gameState = FingerGameState.WAITING
                winningPointers.clear()
            }
        }
    }

    // Countdown and choosing logic
    LaunchedEffect(gameState) {
        if (gameState == FingerGameState.COUNTDOWN) {
            // Count down from 3 seconds
            countdownProgress = 1f
            countdownText = "3"
            triggerVibration(60, 120)
            
            delay(1000)
            if (gameState != FingerGameState.COUNTDOWN) return@LaunchedEffect
            countdownProgress = 0.66f
            countdownText = "2"
            triggerVibration(60, 120)
            
            delay(1000)
            if (gameState != FingerGameState.COUNTDOWN) return@LaunchedEffect
            countdownProgress = 0.33f
            countdownText = "1"
            triggerVibration(60, 120)
            
            delay(1000)
            if (gameState != FingerGameState.COUNTDOWN) return@LaunchedEffect
            countdownProgress = 0f
            countdownText = "GO!"
            
            // SELECT WINNERS
            val keysList = activeTouches.keys.toList()
            if (keysList.isNotEmpty()) {
                gameState = FingerGameState.SELECTED
                
                // Pick random winners
                val winnersCount = minOf(targetWinners, keysList.size)
                val shuffledKeys = keysList.shuffled()
                val winners = shuffledKeys.take(winnersCount)
                winningPointers.addAll(winners)
                
                // Spawn confetti at winning locations
                winners.forEach { winnerId ->
                    val pos = activeTouches[winnerId]
                    if (pos != null) {
                        confettiState.spawn(pos.x, pos.y, 40)
                    }
                }
                
                // Vibration celebration
                triggerVibration(400, 255)
            } else {
                gameState = FingerGameState.WAITING
            }
        }
    }

    // Infinite animation for pulses
    val infiniteTransition = rememberInfiniteTransition(label = "fingerRipples")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .pointerInput(gameState) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        
                        // We only track touches if not currently showing selected winners
                        if (gameState != FingerGameState.SELECTED) {
                            changes.forEach { change ->
                                if (change.pressed) {
                                    activeTouches[change.id] = change.position
                                } else {
                                    activeTouches.remove(change.id)
                                }
                            }
                        } else {
                            // In selected state, we only track releases to clear out dead pointer touches
                            changes.forEach { change ->
                                if (!change.pressed) {
                                    activeTouches.remove(change.id)
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // Touch Rings & Interactive Dials Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            activeTouches.forEach { (pointerId, position) ->
                val color = touchColors[pointerId] ?: NeonCyan
                val isWinner = winningPointers.contains(pointerId)
                val isSelectedState = gameState == FingerGameState.SELECTED

                if (isSelectedState) {
                    if (isWinner) {
                        // Draw winner ring with pulsing glow
                        drawCircle(
                            color = NeonGreen.copy(alpha = 0.2f),
                            radius = 160f,
                            center = position
                        )
                        drawCircle(
                            color = NeonGreen,
                            radius = 70f,
                            center = position
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 60f,
                            center = position,
                            style = Stroke(width = 8f)
                        )
                    } else {
                        // Non-winners fade away with transparent gray
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.2f),
                            radius = 50f,
                            center = position
                        )
                    }
                } else {
                    // Standard Touch Node & Ripples
                    drawCircle(
                        color = color.copy(alpha = rippleAlpha),
                        radius = 80f * rippleScale,
                        center = position,
                        style = Stroke(width = 4f)
                    )
                    drawCircle(
                        color = color.copy(alpha = 0.4f),
                        radius = 60f,
                        center = position
                    )
                    drawCircle(
                        color = color,
                        radius = 45f,
                        center = position
                    )

                    // Draw shrinking countdown circular border if currently counting down
                    if (gameState == FingerGameState.COUNTDOWN) {
                        drawArc(
                            color = Color.White,
                            startAngle = -90f,
                            sweepAngle = 360f * countdownProgress,
                            useCenter = false,
                            style = Stroke(width = 8f),
                            size = androidx.compose.ui.geometry.Size(180f, 180f),
                            topLeft = Offset(position.x - 90f, position.y - 90f)
                        )
                    }
                }
            }
        }

        // Particle Celebrations Layer
        SparkleConfetti(state = confettiState)

        // Status Messages Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (gameState) {
                FingerGameState.WAITING -> {
                    Text(
                        text = if (activeTouches.isEmpty()) "TEMPELKAN JARI" else "Butuh ${targetWinners + 1 - activeTouches.size} jari lagi!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Letakkan jari kalian di layar untuk memulai pilihan",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                FingerGameState.COUNTDOWN -> {
                    Text(
                        text = countdownText,
                        fontSize = 90.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TAHAN JARI ANDA!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPink,
                        letterSpacing = 4.sp
                    )
                }
                FingerGameState.SELECTED -> {
                    Text(
                        text = "GOTCHA!",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen,
                        textAlign = TextAlign.Center,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Angkat semua jari untuk mengulang",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Header and Settings Glass Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
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

                // Config Panel for winners count (choose 1 to 5 people)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianSurface.copy(alpha = 0.85f))
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pilih:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        (1..4).forEach { count ->
                            val isSelected = targetWinners == count
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonCyan else Color.Transparent)
                                    .clickable {
                                        if (gameState == FingerGameState.WAITING) {
                                            targetWinners = count
                                            triggerVibration(30, 100)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count.toString(),
                                    color = if (isSelected) ObsidianBg else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Orang",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
