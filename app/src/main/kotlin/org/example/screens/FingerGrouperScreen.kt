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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.components.DoodleCard
import org.example.components.NotebookBackground
import org.example.theme.*
import kotlin.math.sin
import kotlin.random.Random

enum class GrouperGameState {
    WAITING,
    COUNTDOWN,
    SELECTED
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun FingerGrouperScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(context) {
        org.example.audio.BubbleSoundPlayer.initialize(context)
    }

    var gameState by remember { mutableStateOf(GrouperGameState.WAITING) }
    var targetGroups by remember { mutableStateOf(2) } // 2, 3, or 4 groups
    var minParticipants by remember { mutableStateOf(3) } // Needs at least targetGroups + 1 (or 3) fingers

    // Make sure minParticipants is always at least targetGroups + 1
    LaunchedEffect(targetGroups) {
        if (minParticipants < targetGroups + 1) {
            minParticipants = targetGroups + 1
        }
    }

    val activeTouches = remember { mutableStateMapOf<PointerId, Offset>() }
    val touchColors = remember { mutableStateMapOf<PointerId, Color>() }

    // Persistent grouping states when selection is locked
    val winnerGroups = remember { mutableStateMapOf<PointerId, Int>() } // PointerId -> GroupIndex (1..targetGroups)
    val winnerPositions = remember { mutableStateMapOf<PointerId, Offset>() } // PointerId -> Offset

    val countdownAnimatable = remember { Animatable(1f) }
    val winnerRevealProgress = remember { Animatable(0f) }
    var countdownText by remember { mutableStateOf("") }

    // Unique neon colors representing each group
    val groupColors = listOf(
        NeonCyan,      // Group 1
        NeonPink,      // Group 2
        CrayonYellow,  // Group 3
        NeonPurple     // Group 4
    )

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

    // Assign colors to fingers dynamically
    val availableColors = listOf(NeonCyan, NeonPurple, NeonPink, CrayonYellow, NeonGreen, CrayonOrange, CrayonRed)
    fun getUniqueColorForTouch(id: PointerId): Color {
        val existingColors = touchColors.values.toSet()
        val unusedColor = availableColors.firstOrNull { it !in existingColors }
        return unusedColor ?: availableColors[id.value.toInt() % availableColors.size]
    }

    // 1. Transition between WAITING and COUNTDOWN based on finger count
    LaunchedEffect(activeTouches.size) {
        if (gameState != GrouperGameState.SELECTED) {
            if (activeTouches.size >= minParticipants) {
                if (gameState == GrouperGameState.WAITING) {
                    gameState = GrouperGameState.COUNTDOWN
                    triggerVibration(100, 150)
                }
            } else {
                if (gameState == GrouperGameState.COUNTDOWN) {
                    gameState = GrouperGameState.WAITING
                    triggerVibration(50, 100)
                }
            }
        }
    }

    // 2. Countdown and choosing logic when gameState is COUNTDOWN
    LaunchedEffect(gameState) {
        if (gameState == GrouperGameState.COUNTDOWN) {
            countdownAnimatable.snapTo(1f)
            org.example.audio.BubbleSoundPlayer.playSmallPop()
            
            // Launch parallel coroutine to handle countdown text changes and vibrations
            val job = launch {
                countdownText = "3"
                triggerVibration(60, 120)
                delay(1000)
                if (gameState != GrouperGameState.COUNTDOWN) return@launch
                
                countdownText = "2"
                triggerVibration(60, 120)
                org.example.audio.BubbleSoundPlayer.playSmallPop()
                delay(1000)
                if (gameState != GrouperGameState.COUNTDOWN) return@launch
                
                countdownText = "1"
                triggerVibration(60, 120)
                org.example.audio.BubbleSoundPlayer.playSmallPop()
                delay(1000)
                if (gameState != GrouperGameState.COUNTDOWN) return@launch
                
                countdownText = "GO!"
            }
            
            // Animate from 1f to 0f over 3 seconds smoothly
            countdownAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
            )
            
            job.join()
            
            if (gameState != GrouperGameState.COUNTDOWN) return@LaunchedEffect
            
            // SELECT GROUPS
            val keysList = activeTouches.keys.toList()
            if (keysList.isNotEmpty()) {
                gameState = GrouperGameState.SELECTED
                
                // Shuffle and divide evenly into targetGroups
                val shuffledKeys = keysList.shuffled()
                shuffledKeys.forEachIndexed { index, pointerId ->
                    val grp = (index % targetGroups) + 1
                    winnerGroups[pointerId] = grp
                    
                    val pos = activeTouches[pointerId]
                    if (pos != null) {
                        winnerPositions[pointerId] = pos
                    }
                }
                
                triggerVibration(400, 255)
                org.example.audio.BubbleSoundPlayer.playBigPop()
            } else {
                gameState = GrouperGameState.WAITING
            }
        } else if (gameState == GrouperGameState.WAITING) {
            countdownAnimatable.snapTo(1f)
            winnerPositions.clear()
            winnerGroups.clear()
        } else if (gameState == GrouperGameState.SELECTED) {
            winnerRevealProgress.snapTo(0f)
            winnerRevealProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    NotebookBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes
                            
                            val isSelected = gameState == GrouperGameState.SELECTED
                            
                            changes.forEach { change ->
                                val id = change.id
                                if (change.pressed) {
                                    if (!isSelected) {
                                        if (!activeTouches.containsKey(id)) {
                                            activeTouches[id] = change.position
                                            touchColors[id] = getUniqueColorForTouch(id)
                                            triggerVibration(25, 80)
                                            org.example.audio.BubbleSoundPlayer.playSmallPop()
                                        } else {
                                            activeTouches[id] = change.position
                                        }
                                    } else {
                                        // In selected state, if a winner finger moves, track it
                                        if (winnerGroups.containsKey(id)) {
                                            activeTouches[id] = change.position
                                            winnerPositions[id] = change.position
                                        }
                                    }
                                } else {
                                    // Finger released
                                    if (activeTouches.containsKey(id)) {
                                        activeTouches.remove(id)
                                        touchColors.remove(id)
                                        if (!isSelected) {
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
            // Canvas for all rich visual sketches
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw holding ring animations when fingers are placed down
                if (gameState != GrouperGameState.SELECTED) {
                    activeTouches.forEach { (_, pos) ->
                        // All pre-selection fingers are sketched in Crayon Gray!
                        val grayColor = Color(0xFFB0B0B0)
                        
                        // Pulsating circular glow
                        drawCircle(
                            color = grayColor.copy(alpha = 0.2f),
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
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                        )
                        
                        // Main inner solid color dot
                        drawCircle(
                            color = grayColor,
                            radius = 35.dp.toPx(),
                            center = pos
                        )
                        
                        // Doodle sketchy core border
                        drawCircle(
                            color = BorderColor,
                            radius = 35.dp.toPx(),
                            center = pos,
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                    }
                }

                // 2. Draw Group division connecting lines and outlines when SELECTED
                if (gameState == GrouperGameState.SELECTED) {
                    val progress = winnerRevealProgress.value
                    val currentRadius = (35.dp.toPx() + (3.dp.toPx() * sin(progress * Math.PI.toFloat()))).coerceAtLeast(0f)
                    
                    // Draw sketchy connecting paths for each group
                    (1..targetGroups).forEach { grpIdx ->
                        val grpColor = groupColors.getOrElse(grpIdx - 1) { NeonCyan }
                        
                        // Get all fingers belonging to this group
                        val grpFingers = winnerGroups.filter { it.value == grpIdx }.keys.toList()
                        val points = grpFingers.mapNotNull { winnerPositions[it] }
                        
                        if (points.size >= 2) {
                            // Draw hand-drawn connectors: Multiple jittery line draws to create a real sketched feel!
                            val strokeWidthVal = 5.dp.toPx() * progress
                            
                            val jitters = listOf(
                                Offset(0f, 0f),
                                Offset(-1.5f, 1f),
                                Offset(1.5f, -1f)
                            )
                            
                            jitters.forEachIndexed { jitterIdx, jitter ->
                                val path = Path().apply {
                                    val start = points.first() + jitter
                                    moveTo(start.x, start.y)
                                    
                                    for (i in 1 until points.size) {
                                        val p = points[i] + jitter
                                        lineTo(p.x, p.y)
                                    }
                                    
                                    if (points.size > 2) {
                                        // Close loop for groups of 3+ to form a nice crayon triangle/polygon!
                                        lineTo(start.x, start.y)
                                    }
                                }
                                
                                drawPath(
                                    path = path,
                                    color = grpColor.copy(alpha = if (jitterIdx == 0) 0.8f else 0.4f),
                                    style = Stroke(
                                        width = if (jitterIdx == 0) strokeWidthVal else strokeWidthVal * 0.7f,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }

                    // Draw rings and dynamic speech labels around every finger
                    winnerPositions.forEach { (pointerId, pos) ->
                        val grpIdx = winnerGroups[pointerId] ?: 1
                        val grpColor = groupColors.getOrElse(grpIdx - 1) { NeonCyan }
                        
                        // Pulsating circular glow
                        drawCircle(
                            color = grpColor.copy(alpha = (0.2f * progress).coerceIn(0f, 1f)),
                            radius = 65.dp.toPx() * progress,
                            center = pos
                        )
                        
                        // Outer retro dashed ring
                        drawCircle(
                            color = BorderColor.copy(alpha = progress.coerceIn(0f, 1f)),
                            radius = 52.dp.toPx() * progress,
                            center = pos,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                        )

                        // 1. Standalone sketchy double-ring highlight
                        drawCircle(
                            color = BorderColor,
                            radius = currentRadius + 8.dp.toPx(),
                            center = pos,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = grpColor,
                            radius = currentRadius,
                            center = pos
                        )
                        drawCircle(
                            color = BorderColor,
                            radius = currentRadius,
                            center = pos,
                            style = Stroke(width = 3.5.dp.toPx())
                        )

                        // 2. Handwritten "GROUP X" bubble box floating above the finger
                        val bubbleW = 68.dp.toPx() * progress
                        val bubbleH = 26.dp.toPx() * progress
                        
                        if (bubbleW > 10f) {
                            val bubbleX = pos.x - bubbleW / 2f
                            val bubbleY = pos.y - currentRadius - 38.dp.toPx()
                            
                            // Draw white card label with neon border outline
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(bubbleX, bubbleY),
                                size = Size(bubbleW, bubbleH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                            )
                            drawRoundRect(
                                color = grpColor,
                                topLeft = Offset(bubbleX, bubbleY),
                                size = Size(bubbleW, bubbleH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                            drawRoundRect(
                                color = BorderColor,
                                topLeft = Offset(bubbleX - 1f, bubbleY - 1f),
                                size = Size(bubbleW + 2f, bubbleH + 2f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx())
                            )
                            
                            // Write group label text inside the box
                            val labelText = "GROUP $grpIdx"
                            val textLayout = textMeasurer.measure(
                                text = AnnotatedString(labelText),
                                style = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 9.sp,
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
                    }
                }
            }

            // Game countdown and guide labels
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (gameState) {
                    GrouperGameState.WAITING -> {
                        Text(
                            text = "FINGER GROUPER",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = CabinSketchFamily,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Place at least ${minParticipants} fingers on the screen to start splitting into groups!",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CabinFamily
                        )
                    }
                    GrouperGameState.COUNTDOWN -> {
                        Text(
                            text = countdownText,
                            fontSize = 96.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = CabinSketchFamily,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "HOLD YOUR FINGERS!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = CabinSketchFamily,
                            color = CrayonRed,
                            letterSpacing = 4.sp
                        )
                    }
                    GrouperGameState.SELECTED -> {
                        Text(
                            text = "GROUPED!",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = CabinSketchFamily,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Successfully split into $targetGroups teams!",
                            fontSize = 15.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CabinFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Header Settings Bar
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
                    if (gameState != GrouperGameState.SELECTED) {
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
                                ) {                                    Text(
                                        text = "Groups:",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = CabinFamily,
                                        modifier = Modifier.width(68.dp)
                                    )
                                    (2..4).forEach { count ->
                                        val isSelected = targetGroups == count
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    if (isSelected) NeonGreen else Color.Transparent, 
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    if (isSelected) 2.dp else 0.dp, 
                                                    if (isSelected) BorderColor else Color.Transparent, 
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    if (gameState == GrouperGameState.WAITING) {
                                                        org.example.audio.BubbleSoundPlayer.playSmallPop()
                                                        targetGroups = count
                                                        triggerVibration(30, 100)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = CabinFamily
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
                                        fontFamily = CabinFamily,
                                        modifier = Modifier.width(68.dp)
                                    )
                                    val minPossible = targetGroups + 1
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
                                                    if (gameState == GrouperGameState.WAITING) {
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
                                                fontWeight = FontWeight.Black,
                                                fontFamily = CabinFamily
                                            )
                                        }
                                    }
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
                                    gameState = GrouperGameState.WAITING
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
                                    style = Stroke(width = strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
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
