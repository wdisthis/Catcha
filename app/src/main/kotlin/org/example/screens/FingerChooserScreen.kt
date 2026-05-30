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
import androidx.compose.ui.graphics.StrokeCap
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
import org.example.components.DoodleCard
import org.example.components.DoodleButton
import org.example.components.NotebookBackground
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
    
    var winnerColor by remember { mutableStateOf<Color?>(null) }

    // Childish Crayon Color list for fingers
    val colorsList = listOf(
        NeonCyan,
        NeonPurple,
        NeonPink,
        CrayonYellow,
        NeonGreen,
        CrayonOrange,
        CrayonRed
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
        // Assign unique colors to new touches
        activeTouches.keys.forEach { pointerId ->
            if (!touchColors.containsKey(pointerId)) {
                val usedColors = touchColors.values.toSet()
                val availableColors = colorsList.filter { it !in usedColors }
                val chosenColor = if (availableColors.isNotEmpty()) {
                    availableColors.random()
                } else {
                    colorsList.random()
                }
                touchColors[pointerId] = chosenColor
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
                    triggerVibration(50, 100)
                }
            }
        }
    }

    // Countdown and choosing logic
    LaunchedEffect(gameState) {
        if (gameState == FingerGameState.COUNTDOWN) {
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
                
                val winnersCount = minOf(targetWinners, keysList.size)
                val shuffledKeys = keysList.shuffled()
                val winners = shuffledKeys.take(winnersCount)
                winningPointers.addAll(winners)
                
                val firstWinner = winners.firstOrNull()
                if (firstWinner != null) {
                    winnerColor = touchColors[firstWinner]
                }
                
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
        targetValue = 1.5f,
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

    NotebookBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gameState) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes
                            
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
                    // 1. Draw winner full-screen crayon overlay with hollow paths for winners
                    val firstWinnerColor = winnerColor ?: touchColors[winningPointers.firstOrNull()] ?: NeonGreen
                    
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
                    
                    // Draw subtracted path (translucent crayon overlay)
                    drawPath(path = resultPath, color = firstWinnerColor.copy(alpha = 0.85f))
                    
                    // 2. Draw thick sketchy outlines around winning finger circles
                    winningPointers.forEach { winnerId ->
                        val pos = activeTouches[winnerId]
                        if (pos != null) {
                            // Outermost thick sketch outline
                            drawCircle(
                                color = BorderColor,
                                radius = 120f,
                                center = pos,
                                style = Stroke(width = 8f)
                            )
                            // Inner white border loop
                            drawCircle(
                                color = Color.White,
                                radius = 112f,
                                center = pos,
                                style = Stroke(width = 4f)
                            )
                        }
                    }
                } else {
                    // WAITING or COUNTDOWN: Draw hand-drawn concentric sketchy circles
                    activeTouches.forEach { (pointerId, position) ->
                        val color = touchColors[pointerId] ?: NeonCyan
                        
                        // Sketchy Ripple (animated scale & alpha)
                        val rippleRadius = 80f * rippleScale
                        drawCircle(
                            color = color.copy(alpha = rippleAlpha),
                            radius = rippleRadius + 3f,
                            center = Offset(position.x - 2f, position.y + 1f),
                            style = Stroke(width = 3f)
                        )
                        drawCircle(
                            color = color.copy(alpha = rippleAlpha * 0.8f),
                            radius = rippleRadius - 3f,
                            center = Offset(position.x + 1f, position.y - 2f),
                            style = Stroke(width = 2f)
                        )

                        // Middle sketchy loop
                        drawCircle(
                            color = color.copy(alpha = 0.5f),
                            radius = 60f + 2f,
                            center = Offset(position.x - 2f, position.y + 2f),
                            style = Stroke(width = 4f)
                        )
                        drawCircle(
                            color = color.copy(alpha = 0.4f),
                            radius = 60f - 2f,
                            center = Offset(position.x + 1f, position.y - 1f),
                            style = Stroke(width = 3f)
                        )

                        // Core filled center
                        drawCircle(
                            color = color,
                            radius = 45f,
                            center = position
                        )

                        // Draw shrinking countdown circular border if currently counting down
                        if (gameState == FingerGameState.COUNTDOWN) {
                            drawArc(
                                color = BorderColor,
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
            val isSelectedState = gameState == FingerGameState.SELECTED
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(if (isSelectedState) Alignment.BottomCenter else Alignment.Center)
                    .padding(horizontal = 32.dp)
                    .padding(bottom = if (isSelectedState) 40.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (gameState) {
                    FingerGameState.WAITING -> {
                        Text(
                            text = if (activeTouches.isEmpty()) "PLACE YOUR FINGERS!" else "Need ${minParticipants - activeTouches.size} more fingers!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Place your fingers on the screen to start selecting",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    FingerGameState.COUNTDOWN -> {
                        Text(
                            text = countdownText,
                            fontSize = 96.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "HOLD YOUR FINGERS!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = CrayonRed,
                            letterSpacing = 4.sp
                        )
                    }
                    FingerGameState.SELECTED -> {
                        Text(
                            text = "GOTCHA!",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A decision has been made!",
                            fontSize = 15.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Header and Settings Glass Bar (Doodle Card style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("◀", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }

                    // Config Panel or Play Again Button (matching the back button)
                    if (gameState != FingerGameState.SELECTED) {
                        DoodleCard(
                            backgroundColor = Color.White,
                            shadowOffset = 4.dp,
                            modifier = Modifier.width(235.dp)
                        ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Winners:",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.width(68.dp)
                                )
                                (1..4).forEach { count ->
                                    val isSelected = targetWinners == count
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                if (isSelected) NeonCyan else Color.Transparent, 
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                if (isSelected) 2.dp else 0.dp, 
                                                if (isSelected) BorderColor else Color.Transparent, 
                                                RoundedCornerShape(8.dp)
                                            )
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
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Fingers:",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.width(68.dp)
                                )
                                val minPossible = targetWinners + 1
                                (minPossible..6).forEach { count ->
                                    val isSelected = minParticipants == count
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                if (isSelected) NeonPurple else Color.Transparent, 
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                if (isSelected) 2.dp else 0.dp, 
                                                if (isSelected) BorderColor else Color.Transparent, 
                                                RoundedCornerShape(8.dp)
                                            )
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
                                            color = if (isSelected) Color.White else TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                    } else {
                        // Play Again Button (matching the back button on the left!)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    gameState = FingerGameState.WAITING
                                    winningPointers.clear()
                                    winnerColor = null
                                    triggerVibration(60, 150)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(
                                modifier = Modifier.size(22.dp)
                            ) {
                                val strokeWidthPx = 2.5.dp.toPx()
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val radius = size.minDimension * 0.35f
                                
                                val arcSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                                val topLeft = Offset(cx - radius, cy - radius)
                                
                                // Draw a clockwise circular arc with a gap at the top-right
                                drawArc(
                                    color = TextPrimary,
                                    startAngle = 0f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                )
                                
                                // Arrowhead at the end (top-center, pointing right)
                                val arrowSize = 4.dp.toPx()
                                val arrowPath = Path().apply {
                                    moveTo(cx, cy - radius - arrowSize)
                                    lineTo(cx + arrowSize * 1.2f, cy - radius)
                                    lineTo(cx, cy - radius + arrowSize)
                                    close()
                                }
                                drawPath(path = arrowPath, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
