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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
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
    var minParticipants by remember { mutableStateOf(2) }
    
    val activeTouches = remember { mutableStateMapOf<PointerId, Offset>() }
    val touchColors = remember { mutableStateMapOf<PointerId, Color>() }
    
    val winningPointers = remember { mutableStateListOf<PointerId>() }
    
    var countdownProgress by remember { mutableStateOf(1f) } // 1.0 down to 0.0
    var countdownText by remember { mutableStateOf("") }

    // Color list for fingers
    val colorsList = listOf(
        Color(0xFF06B6D4), // Cyan
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFFFFD700), // Gold
        Color(0xFF10B981), // Green
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

    // Auto-adjust minParticipants if targetWinners changes
    LaunchedEffect(targetWinners) {
        if (minParticipants < targetWinners + 1) {
            minParticipants = targetWinners + 1
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
            if (activeTouches.size >= minParticipants) {
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
                            // In selected state, track releases AND track movements of winning fingers so they move smoothly
                            changes.forEach { change ->
                                if (!change.pressed) {
                                    activeTouches.remove(change.id)
                                } else if (winningPointers.contains(change.id)) {
                                    activeTouches[change.id] = change.position
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // Touch Rings & Interactive Dials Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val isSelectedState = gameState == FingerGameState.SELECTED

            if (isSelectedState && winningPointers.isNotEmpty()) {
                // 1. Draw winner full-screen background overlay with holes for active winners
                val firstWinnerColor = touchColors[winningPointers.firstOrNull()] ?: NeonGreen
                
                val fullScreenPath = Path().apply {
                    addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                }
                
                var resultPath = fullScreenPath
                winningPointers.forEach { winnerId ->
                    val pos = activeTouches[winnerId]
                    if (pos != null) {
                        val circlePath = Path().apply {
                            addOval(androidx.compose.ui.geometry.Rect(center = pos, radius = 120f))
                        }
                        resultPath = Path.combine(
                            operation = PathOperation.Difference,
                            path1 = resultPath,
                            path2 = circlePath
                        )
                    }
                }
                
                // Draw the subtracted path (winner color covers everything except hollow circles)
                drawPath(path = resultPath, color = firstWinnerColor)
                
                // 2. Draw white circular borders for active winners
                winningPointers.forEach { winnerId ->
                    val pos = activeTouches[winnerId]
                    if (pos != null) {
                        drawCircle(
                            color = Color.White,
                            radius = 120f,
                            center = pos,
                            style = Stroke(width = 8f)
                        )
                    }
                }
            } else {
                // WAITING or COUNTDOWN state: standard touch rings
                activeTouches.forEach { (pointerId, position) ->
                    val color = touchColors[pointerId] ?: NeonCyan
                    
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
                        text = if (activeTouches.isEmpty()) "TEMPELKAN JARI" else "Butuh ${minParticipants - activeTouches.size} jari lagi!",
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GOTCHA!",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Keputusan telah diambil",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Glassmorphic Ulang Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .clickable {
                                        gameState = FingerGameState.WAITING
                                        winningPointers.clear()
                                        triggerVibration(60, 150)
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "MAINKAN LAGI",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
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
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurface.copy(alpha = 0.85f))
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("<", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                // Config Panel for winners and min fingers
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianSurface.copy(alpha = 0.85f))
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Pemenang:",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(68.dp)
                            )
                            (1..4).forEach { count ->
                                val isSelected = targetWinners == count
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
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
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Min Jari:",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(68.dp)
                            )
                            val minPossible = targetWinners + 1
                            (minPossible..6).forEach { count ->
                                val isSelected = minParticipants == count
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonPurple else Color.Transparent)
                                        .clickable {
                                            if (gameState == FingerGameState.WAITING) {
                                                minParticipants = count
                                                triggerVibration(30, 100)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = count.toString(),
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
