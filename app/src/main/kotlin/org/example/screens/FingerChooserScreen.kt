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
import kotlin.math.ln
import kotlin.math.exp
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

    LaunchedEffect(context) {
        org.example.audio.BubbleSoundPlayer.initialize(context)
    }

    var gameState by remember { mutableStateOf(FingerGameState.WAITING) }
    var targetWinners by remember { mutableStateOf(1) }
    var minParticipants by remember { mutableStateOf(2) }
    
    val activeTouches = remember { mutableStateMapOf<PointerId, Offset>() }
    val touchColors = remember { mutableStateMapOf<PointerId, Color>() }
    
    val winningPointers = remember { mutableStateListOf<PointerId>() }
    val winnerPositions = remember { mutableStateMapOf<PointerId, Offset>() }
    val winnerColors = remember { mutableStateMapOf<PointerId, Color>() }
    
    val countdownAnimatable = remember { Animatable(1f) }
    val winnerRevealProgress = remember { Animatable(0f) }
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
        if (!org.example.data.AppSettings.isVibrationEnabled) return
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
            countdownAnimatable.snapTo(1f)
            
            // Launch parallel coroutine to handle countdown text changes and vibrations
            val job = launch {
                countdownText = "4"
                triggerVibration(60, 120)
                delay(1000)
                if (gameState != FingerGameState.COUNTDOWN) return@launch
                
                countdownText = "3"
                triggerVibration(60, 120)
                delay(1000)
                if (gameState != FingerGameState.COUNTDOWN) return@launch
                
                countdownText = "2"
                triggerVibration(60, 120)
                delay(1000)
                if (gameState != FingerGameState.COUNTDOWN) return@launch
                
                countdownText = "1"
                triggerVibration(60, 120)
                delay(1000)
                if (gameState != FingerGameState.COUNTDOWN) return@launch
                
                countdownText = "GO!"
            }
            
            // Animate from 1f to 0f over 4 seconds smoothly
            countdownAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
            )
            
            job.join()
            
            if (gameState != FingerGameState.COUNTDOWN) return@LaunchedEffect
            
            // SELECT WINNERS
            val keysList = activeTouches.keys.toList()
            if (keysList.isNotEmpty()) {
                gameState = FingerGameState.SELECTED
                
                val winnersCount = minOf(targetWinners, keysList.size)
                val shuffledKeys = keysList.shuffled()
                val winners = shuffledKeys.take(winnersCount)
                winningPointers.addAll(winners)
                
                // Copy positions to the persistent winnerPositions map
                winners.forEach { winnerId ->
                    val pos = activeTouches[winnerId]
                    if (pos != null) {
                        winnerPositions[winnerId] = pos
                    }
                    val color = touchColors[winnerId]
                    if (color != null) {
                        winnerColors[winnerId] = color
                    }
                }
                
                val firstWinner = winners.firstOrNull()
                if (firstWinner != null) {
                    winnerColor = touchColors[firstWinner]
                }
                
                triggerVibration(400, 255)
                org.example.audio.BubbleSoundPlayer.playBigPop()
            } else {
                gameState = FingerGameState.WAITING
            }
        } else if (gameState == FingerGameState.WAITING) {
            countdownAnimatable.snapTo(1f)
            winnerPositions.clear()
            winningPointers.clear()
            winnerColors.clear()
            winnerColor = null
        }
    }

    // Winner reveal spotlight spring animation
    LaunchedEffect(gameState) {
        if (gameState == FingerGameState.SELECTED) {
            winnerRevealProgress.snapTo(0f)
            winnerRevealProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        } else {
            winnerRevealProgress.snapTo(0f)
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
    val winnerBeatScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "winnerBeatScale"
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
                                        if (!activeTouches.containsKey(change.id)) {
                                            org.example.audio.BubbleSoundPlayer.playSmallPop()
                                        }
                                        activeTouches[change.id] = change.position
                                    } else {
                                        if (activeTouches.containsKey(change.id)) {
                                            org.example.audio.BubbleSoundPlayer.playBigPop()
                                        }
                                        activeTouches.remove(change.id)
                                    }
                                }
                            } else {
                                // In selected state, track releases AND track movements of winning fingers so they move smoothly
                                changes.forEach { change ->
                                    if (!change.pressed) {
                                        if (activeTouches.containsKey(change.id)) {
                                            org.example.audio.BubbleSoundPlayer.playBigPop()
                                        }
                                        activeTouches.remove(change.id)
                                    } else if (winningPointers.contains(change.id)) {
                                        activeTouches[change.id] = change.position
                                        winnerPositions[change.id] = change.position
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
                    // Compute logarithmic/exponential iris-out radius to prevent negative numbers on spring overshoot
                    val maxDimension = maxOf(size.width, size.height)
                    val startRadius = maxDimension * 1.5f
                    val endRadius = 100f // 17% larger than 85f (85 * 1.176 = 100)
                    
                    val logStart = ln(startRadius.toDouble())
                    val logEnd = ln(endRadius.toDouble())
                    val logCurrent = logStart + (logEnd - logStart) * winnerRevealProgress.value.toDouble()
                    val baseRadius = exp(logCurrent).toFloat().coerceAtLeast(10f)
                    
                    // Apply beating scale smoothly as the reveal progress approaches completion
                    val beatFactor = 1f + (winnerBeatScale - 1f) * winnerRevealProgress.value
                    val currentRadius = baseRadius * beatFactor
                    
                    // 1. Divide screen into regions depending on number of winners
                    val winnersList = winningPointers.toList()
                    val numWinners = winnersList.size
                    
                    val regions = when (numWinners) {
                        1 -> listOf(
                            androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
                        )
                        2 -> listOf(
                            // Split vertically: left & right columns
                            androidx.compose.ui.geometry.Rect(0f, 0f, size.width / 2f, size.height),
                            androidx.compose.ui.geometry.Rect(size.width / 2f, 0f, size.width, size.height)
                        )
                        3 -> listOf(
                            // Split horizontally: top, middle, bottom rows
                            androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height / 3f),
                            androidx.compose.ui.geometry.Rect(0f, size.height / 3f, size.width, 2f * size.height / 3f),
                            androidx.compose.ui.geometry.Rect(0f, 2f * size.height / 3f, size.width, size.height)
                        )
                        4 -> listOf(
                            // Split into a 2x2 grid: top-left, top-right, bottom-left, bottom-right
                            androidx.compose.ui.geometry.Rect(0f, 0f, size.width / 2f, size.height / 2f),
                            androidx.compose.ui.geometry.Rect(size.width / 2f, 0f, size.width, size.height / 2f),
                            androidx.compose.ui.geometry.Rect(0f, size.height / 2f, size.width / 2f, size.height),
                            androidx.compose.ui.geometry.Rect(size.width / 2f, size.height / 2f, size.width, size.height)
                        )
                        else -> {
                            // Fallback for > 4 winners: split horizontally into equal rows
                            val bandHeight = size.height / numWinners
                            (0 until numWinners).map { idx ->
                                androidx.compose.ui.geometry.Rect(0f, idx * bandHeight, size.width, (idx + 1) * bandHeight)
                            }
                        }
                    }
                    
                    // Draw each region minus all the spotlight hollow circles
                    winnersList.forEachIndexed { index, winnerId ->
                        val regionRect = regions.getOrElse(index) {
                            androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
                        }
                        
                        val regionPath = Path().apply {
                            addRect(regionRect)
                        }
                        
                        var subtractedRegionPath = regionPath
                        winnersList.forEach { wId ->
                            val pos = winnerPositions[wId]
                            if (pos != null) {
                                val circlePath = Path().apply {
                                    addOval(androidx.compose.ui.geometry.Rect(center = pos, radius = currentRadius))
                                }
                                subtractedRegionPath = Path.combine(
                                    operation = PathOperation.Difference,
                                    path1 = subtractedRegionPath,
                                    path2 = circlePath
                                )
                            }
                        }
                        
                        val wColor = winnerColors[winnerId] ?: touchColors[winnerId] ?: NeonGreen
                        val overlayColor = wColor.copy(alpha = (0.85f * winnerRevealProgress.value).coerceIn(0f, 1f))
                        drawPath(path = subtractedRegionPath, color = overlayColor)
                    }
                    
                    // 2. Draw thick sketchy outlines around winning finger circles that shrink with the spotlight
                    winningPointers.forEach { winnerId ->
                        val pos = winnerPositions[winnerId]
                        if (pos != null) {
                            // Outermost thick sketch outline
                            drawCircle(
                                color = BorderColor,
                                radius = currentRadius,
                                center = pos,
                                style = Stroke(width = 8f)
                            )
                            // Inner white border loop
                            drawCircle(
                                color = Color.White,
                                radius = (currentRadius - 8f).coerceAtLeast(1f),
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
                        val rippleRadius = 110f * rippleScale
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
                            radius = 85f + 2f,
                            center = Offset(position.x - 2f, position.y + 2f),
                            style = Stroke(width = 4f)
                        )
                        drawCircle(
                            color = color.copy(alpha = 0.4f),
                            radius = 85f - 2f,
                            center = Offset(position.x + 1f, position.y - 1f),
                            style = Stroke(width = 3f)
                        )

                        // Core filled center
                        drawCircle(
                            color = color,
                            radius = 65f,
                            center = position
                        )

                        // Draw shrinking countdown circular border if currently counting down
                        if (gameState == FingerGameState.COUNTDOWN) {
                            drawArc(
                                color = BorderColor,
                                startAngle = -90f,
                                sweepAngle = 360f * countdownAnimatable.value,
                                useCenter = false,
                                style = Stroke(width = 8f),
                                size = androidx.compose.ui.geometry.Size(200f, 200f),
                                topLeft = Offset(position.x - 100f, position.y - 100f)
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
                    .padding(horizontal = 32.dp),
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
                            .clickable {
                                org.example.audio.BubbleSoundPlayer.playSmallPop()
                                onBack()
                            },
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
                                                    org.example.audio.BubbleSoundPlayer.playSmallPop()
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
                                                    org.example.audio.BubbleSoundPlayer.playSmallPop()
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
                                    org.example.audio.BubbleSoundPlayer.playSmallPop()
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
