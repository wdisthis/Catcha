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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.components.DoodleCard
import org.example.components.NotebookBackground
import org.example.theme.*
import kotlin.math.cos
import kotlin.math.sin

enum class OrderGameState {
    WAITING,
    COUNTDOWN,
    REVEAL,       // Sequential reveal animation in progress
    COMPLETED     // All orders have been revealed
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun FingerOrderScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(context) {
        org.example.audio.BubbleSoundPlayer.initialize(context)
    }

    var gameState by remember { mutableStateOf(OrderGameState.WAITING) }
    var minFingers by remember { mutableStateOf(2) }

    val activeTouches = remember { mutableStateMapOf<PointerId, Offset>() }
    val touchColors = remember { mutableStateMapOf<PointerId, Color>() }

    // Order assignment: PointerId -> order number (1-based)
    val fingerOrders = remember { mutableStateMapOf<PointerId, Int>() }
    val fingerPositions = remember { mutableStateMapOf<PointerId, Offset>() }

    // Sequential reveal tracking
    var revealedCount by remember { mutableStateOf(0) }
    var totalFingers by remember { mutableStateOf(0) }

    val countdownAnimatable = remember { Animatable(1f) }
    val revealProgress = remember { Animatable(0f) }
    var countdownText by remember { mutableStateOf("") }

    // Colors for order numbers - each order gets a unique color
    val orderColors = listOf(
        NeonCyan,       // 1st
        NeonPink,       // 2nd
        CrayonYellow,   // 3rd
        NeonPurple,     // 4th
        NeonGreen,      // 5th
        CrayonOrange,   // 6th
        CrayonRed,      // 7th
        Color(0xFF5DADE2), // 8th
        Color(0xFFF39C12), // 9th
        Color(0xFF2ECC71)  // 10th
    )

    val availableFingerColors = listOf(NeonCyan, NeonPurple, NeonPink, CrayonYellow, NeonGreen, CrayonOrange, CrayonRed)

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

    fun getUniqueColorForTouch(id: PointerId): Color {
        val existingColors = touchColors.values.toSet()
        val unusedColor = availableFingerColors.firstOrNull { it !in existingColors }
        return unusedColor ?: availableFingerColors[id.value.toInt() % availableFingerColors.size]
    }

    // Transition between WAITING and COUNTDOWN based on finger count
    LaunchedEffect(activeTouches.size) {
        if (gameState != OrderGameState.REVEAL && gameState != OrderGameState.COMPLETED) {
            if (activeTouches.size >= minFingers) {
                if (gameState == OrderGameState.WAITING) {
                    gameState = OrderGameState.COUNTDOWN
                    triggerVibration(100, 150)
                }
            } else {
                if (gameState == OrderGameState.COUNTDOWN) {
                    gameState = OrderGameState.WAITING
                    triggerVibration(50, 100)
                }
            }
        }
    }

    // Countdown and order assignment logic
    LaunchedEffect(gameState) {
        if (gameState == OrderGameState.COUNTDOWN) {
            countdownAnimatable.snapTo(1f)
            org.example.audio.BubbleSoundPlayer.playSmallPop()

            val job = launch {
                countdownText = "3"
                triggerVibration(60, 120)
                delay(1000)
                if (gameState != OrderGameState.COUNTDOWN) return@launch

                countdownText = "2"
                triggerVibration(60, 120)
                org.example.audio.BubbleSoundPlayer.playSmallPop()
                delay(1000)
                if (gameState != OrderGameState.COUNTDOWN) return@launch

                countdownText = "1"
                triggerVibration(60, 120)
                org.example.audio.BubbleSoundPlayer.playSmallPop()
                delay(1000)
                if (gameState != OrderGameState.COUNTDOWN) return@launch

                countdownText = "GO!"
            }

            countdownAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
            )

            job.join()

            if (gameState != OrderGameState.COUNTDOWN) return@LaunchedEffect

            // ASSIGN RANDOM ORDER
            val keysList = activeTouches.keys.toList()
            if (keysList.isNotEmpty()) {
                val shuffledKeys = keysList.shuffled()
                totalFingers = shuffledKeys.size
                revealedCount = 0

                shuffledKeys.forEachIndexed { index, pointerId ->
                    fingerOrders[pointerId] = index + 1
                    val pos = activeTouches[pointerId]
                    if (pos != null) {
                        fingerPositions[pointerId] = pos
                    }
                }

                gameState = OrderGameState.REVEAL
                triggerVibration(200, 180)
                org.example.audio.BubbleSoundPlayer.playBigPop()
            } else {
                gameState = OrderGameState.WAITING
            }
        } else if (gameState == OrderGameState.WAITING) {
            countdownAnimatable.snapTo(1f)
            fingerOrders.clear()
            fingerPositions.clear()
            revealedCount = 0
            totalFingers = 0
        } else if (gameState == OrderGameState.REVEAL) {
            // Sequential reveal: reveal one finger at a time
            val total = totalFingers
            for (i in 1..total) {
                if (gameState != OrderGameState.REVEAL) break
                revealedCount = i
                triggerVibration(80, 150)
                org.example.audio.BubbleSoundPlayer.playSmallPop()

                // Animate each reveal with a bouncy spring
                revealProgress.snapTo(0f)
                revealProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                if (i < total) {
                    delay(400) // Pause between reveals
                }
            }

            if (gameState == OrderGameState.REVEAL) {
                gameState = OrderGameState.COMPLETED
                triggerVibration(400, 255)
                org.example.audio.BubbleSoundPlayer.playBigPop()
            }
        } else if (gameState == OrderGameState.COMPLETED) {
            revealProgress.snapTo(0f)
            revealProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // Pulsing animation for revealed numbers
    val infiniteTransition = rememberInfiniteTransition(label = "orderPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    NotebookBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes

                            val isLocked = gameState == OrderGameState.REVEAL || gameState == OrderGameState.COMPLETED

                            changes.forEach { change ->
                                val id = change.id
                                if (change.pressed) {
                                    if (!isLocked) {
                                        if (!activeTouches.containsKey(id)) {
                                            activeTouches[id] = change.position
                                            touchColors[id] = getUniqueColorForTouch(id)
                                            triggerVibration(25, 80)
                                            org.example.audio.BubbleSoundPlayer.playSmallPop()
                                        } else {
                                            activeTouches[id] = change.position
                                        }
                                    } else {
                                        // Track movement of fingers in locked state
                                        if (fingerOrders.containsKey(id)) {
                                            activeTouches[id] = change.position
                                            fingerPositions[id] = change.position
                                        }
                                    }
                                } else {
                                    // Finger released
                                    if (activeTouches.containsKey(id)) {
                                        activeTouches.remove(id)
                                        if (!isLocked) {
                                            touchColors.remove(id)
                                            triggerVibration(15, 60)
                                            org.example.audio.BubbleSoundPlayer.playBigPop()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            // Canvas for all visual elements
            Canvas(modifier = Modifier.fillMaxSize()) {
                val isRevealing = gameState == OrderGameState.REVEAL || gameState == OrderGameState.COMPLETED

                if (!isRevealing) {
                    // WAITING / COUNTDOWN: Draw colored circles per finger
                    activeTouches.forEach { (pointerId, pos) ->
                        val fingerColor = touchColors[pointerId] ?: NeonCyan

                        // Pulsating circular glow
                        drawCircle(
                            color = fingerColor.copy(alpha = 0.2f),
                            radius = 65.dp.toPx(),
                            center = pos
                        )

                        // Outer retro dashed ring
                        drawCircle(
                            color = BorderColor,
                            radius = 52.dp.toPx(),
                            center = pos,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(15f, 15f), 0f
                                )
                            )
                        )

                        // Main inner solid dot with finger color
                        drawCircle(
                            color = fingerColor,
                            radius = 35.dp.toPx(),
                            center = pos
                        )

                        // Doodle sketchy border
                        drawCircle(
                            color = BorderColor,
                            radius = 35.dp.toPx(),
                            center = pos,
                            style = Stroke(width = 3.5.dp.toPx())
                        )

                        // Question mark placeholder
                        val qText = textMeasurer.measure(
                            text = AnnotatedString("?"),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        )
                        drawText(
                            textLayoutResult = qText,
                            topLeft = Offset(
                                x = pos.x - qText.size.width / 2f,
                                y = pos.y - qText.size.height / 2f
                            )
                        )
                    }
                } else {
                    // REVEAL / COMPLETED: Draw ordered circles with numbers
                    fingerPositions.forEach { (pointerId, pos) ->
                        val orderNum = fingerOrders[pointerId] ?: return@forEach
                        val isRevealed = orderNum <= revealedCount
                        val isCurrentlyRevealing = orderNum == revealedCount

                        if (!isRevealed) {
                            // Not yet revealed: show finger color with "?"
                            val fingerColor = touchColors[pointerId] ?: NeonCyan
                            drawCircle(color = fingerColor.copy(alpha = 0.2f), radius = 65.dp.toPx(), center = pos)
                            drawCircle(
                                color = BorderColor,
                                radius = 52.dp.toPx(),
                                center = pos,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                        floatArrayOf(15f, 15f), 0f
                                    )
                                )
                            )
                            drawCircle(color = fingerColor, radius = 35.dp.toPx(), center = pos)
                            drawCircle(
                                color = BorderColor,
                                radius = 35.dp.toPx(),
                                center = pos,
                                style = Stroke(width = 3.5.dp.toPx())
                            )
                            val qText = textMeasurer.measure(
                                text = AnnotatedString("?"),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            )
                            drawText(
                                textLayoutResult = qText,
                                topLeft = Offset(
                                    x = pos.x - qText.size.width / 2f,
                                    y = pos.y - qText.size.height / 2f
                                )
                            )
                        } else {
                            // Revealed: Show circle with order number (keep original finger color)
                            val fingerColor = touchColors[pointerId] ?: NeonCyan
                            val scale = if (isCurrentlyRevealing) {
                                revealProgress.value * pulseScale
                            } else {
                                pulseScale
                            }
                            val animRadius = 35.dp.toPx() * scale
                            val outerRadius = 52.dp.toPx() * (if (isCurrentlyRevealing) revealProgress.value else 1f)
                            val glowRadius = 70.dp.toPx() * (if (isCurrentlyRevealing) revealProgress.value else 1f)

                            // Glow aura
                            drawCircle(
                                color = fingerColor.copy(alpha = 0.25f),
                                radius = glowRadius,
                                center = pos
                            )

                            // Outer dashed ring with order color
                            drawCircle(
                                color = fingerColor,
                                radius = outerRadius,
                                center = pos,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                        floatArrayOf(12f, 8f), 0f
                                    )
                                )
                            )

                            // Main filled circle
                            drawCircle(
                                color = fingerColor,
                                radius = animRadius,
                                center = pos
                            )

                            // Sketchy border
                            drawCircle(
                                color = BorderColor,
                                radius = animRadius,
                                center = pos,
                                style = Stroke(width = 3.5.dp.toPx())
                            )

                            // Double border for extra sketch feel
                            drawCircle(
                                color = BorderColor,
                                radius = animRadius + 6.dp.toPx(),
                                center = pos,
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Order number text inside the circle
                            val numText = textMeasurer.measure(
                                text = AnnotatedString(orderNum.toString()),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            )
                            drawText(
                                textLayoutResult = numText,
                                topLeft = Offset(
                                    x = pos.x - numText.size.width / 2f,
                                    y = pos.y - numText.size.height / 2f
                                )
                            )

                            // Ordinal label bubble above the finger
                            val ordinalSuffix = when {
                                orderNum % 100 in 11..13 -> "th"
                                orderNum % 10 == 1 -> "st"
                                orderNum % 10 == 2 -> "nd"
                                orderNum % 10 == 3 -> "rd"
                                else -> "th"
                            }
                            val labelText = "${orderNum}${ordinalSuffix}"
                            val bubbleW = 58.dp.toPx() * (if (isCurrentlyRevealing) revealProgress.value else 1f)
                            val bubbleH = 24.dp.toPx() * (if (isCurrentlyRevealing) revealProgress.value else 1f)

                            if (bubbleW > 10f) {
                                val bubbleX = pos.x - bubbleW / 2f
                                val bubbleY = pos.y - animRadius - 32.dp.toPx()

                                // White card label with colored border
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(bubbleX, bubbleY),
                                    size = Size(bubbleW, bubbleH),
                                    cornerRadius = CornerRadius(8.dp.toPx())
                                )
                                drawRoundRect(
                                    color = fingerColor,
                                    topLeft = Offset(bubbleX, bubbleY),
                                    size = Size(bubbleW, bubbleH),
                                    cornerRadius = CornerRadius(8.dp.toPx()),
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                                drawRoundRect(
                                    color = BorderColor,
                                    topLeft = Offset(bubbleX - 1f, bubbleY - 1f),
                                    size = Size(bubbleW + 2f, bubbleH + 2f),
                                    cornerRadius = CornerRadius(8.dp.toPx()),
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                val textLayout = textMeasurer.measure(
                                    text = AnnotatedString(labelText),
                                    style = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                drawText(
                                    textLayoutResult = textLayout,
                                    topLeft = Offset(
                                        x = bubbleX + (bubbleW - textLayout.size.width) / 2f,
                                        y = bubbleY + (bubbleH - textLayout.size.height) / 2f
                                    )
                                )
                            }

                            // Draw connecting line to next order number (for visual flow)
                            if (orderNum < totalFingers) {
                                val nextPointerId = fingerOrders.entries.firstOrNull { it.value == orderNum + 1 }?.key
                                val nextPos = if (nextPointerId != null) fingerPositions[nextPointerId] else null
                                val nextRevealed = (orderNum + 1) <= revealedCount

                                if (nextPos != null && nextRevealed) {
                                    // Calculate edge-to-edge line (stop at circle borders)
                                    val dx = nextPos.x - pos.x
                                    val dy = nextPos.y - pos.y
                                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                    if (dist > 0f) {
                                        val dirX = dx / dist
                                        val dirY = dy / dist
                                        val edgeRadius = animRadius + 8.dp.toPx() // outer border offset
                                        val startEdge = Offset(pos.x + dirX * edgeRadius, pos.y + dirY * edgeRadius)
                                        val endEdge = Offset(nextPos.x - dirX * edgeRadius, nextPos.y - dirY * edgeRadius)

                                        // Draw black sketchy connecting dashed line
                                        val jitters = listOf(
                                            Offset(0f, 0f),
                                            Offset(-1f, 0.8f),
                                            Offset(1f, -0.8f)
                                        )
                                        jitters.forEachIndexed { jitterIdx, jitter ->
                                            drawLine(
                                                color = BorderColor.copy(alpha = if (jitterIdx == 0) 0.7f else 0.3f),
                                                start = startEdge + jitter,
                                                end = endEdge + jitter,
                                                strokeWidth = if (jitterIdx == 0) 3.dp.toPx() else 2.dp.toPx(),
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                                    floatArrayOf(12f, 10f), 0f
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Game status text overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (gameState) {
                    OrderGameState.WAITING -> {
                        Text(
                            text = "FINGER ORDER",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Place at least $minFingers fingers on the screen to get a random order!",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OrderGameState.COUNTDOWN -> {
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
                    OrderGameState.REVEAL -> {
                        Text(
                            text = "REVEALING...",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$revealedCount of $totalFingers",
                            fontSize = 18.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    OrderGameState.COMPLETED -> {
                        Text(
                            text = "ORDERED!",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Random order assigned to $totalFingers fingers!",
                            fontSize = 15.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Header Bar
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

                    // Config Panel or Reset Button
                    if (gameState != OrderGameState.REVEAL && gameState != OrderGameState.COMPLETED) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box {
                            // Dropdown trigger button
                            Box(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (gameState == OrderGameState.WAITING) {
                                            org.example.audio.BubbleSoundPlayer.playSmallPop()
                                            dropdownExpanded = true
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Min: $minFingers",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "▼",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            // Dropdown menu
                            androidx.compose.material3.DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .border(2.dp, BorderColor, RoundedCornerShape(8.dp))
                            ) {
                                (2..10).forEach { count ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "$count fingers",
                                                color = if (minFingers == count) CrayonOrange else TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = if (minFingers == count) FontWeight.Black else FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            org.example.audio.BubbleSoundPlayer.playSmallPop()
                                            minFingers = count
                                            triggerVibration(30, 100)
                                            dropdownExpanded = false
                                        },
                                        modifier = Modifier.background(
                                            if (minFingers == count) CrayonOrange.copy(alpha = 0.1f) else Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // Play Again Reset Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    org.example.audio.BubbleSoundPlayer.playSmallPop()
                                    gameState = OrderGameState.WAITING
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

                                val arcSize = Size(radius * 2, radius * 2)
                                val topLeft = Offset(cx - radius, cy - radius)

                                drawArc(
                                    color = TextPrimary,
                                    startAngle = 0f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(
                                        width = strokeWidthPx,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )

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
