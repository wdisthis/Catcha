package org.example.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.components.ConfettiState
import org.example.components.SparkleConfetti
import org.example.components.DoodleCard
import org.example.components.DoodleButton
import org.example.components.NotebookBackground
import org.example.theme.*
import kotlin.random.Random

data class RouletteOption(
    val label: String,
    val color: Color
)

@OptIn(ExperimentalTextApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RouletteScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        org.example.audio.BubbleSoundPlayer.initialize(context)
    }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    // Crayon colors for roulette slices
    val colorsPalette = listOf(
        NeonPurple,
        NeonCyan,
        NeonPink,
        CrayonYellow,
        NeonGreen,
        CrayonOrange,
        CrayonRed
    )

    // Initial default options
    var optionsList by remember {
        mutableStateOf(
            listOf(
                RouletteOption("Fried Rice", NeonPurple),
                RouletteOption("Chicken Satay", NeonCyan),
                RouletteOption("Meatballs", NeonPink),
                RouletteOption("Noodles", CrayonYellow),
                RouletteOption("Pizza", NeonGreen),
                RouletteOption("Sushi", CrayonOrange)
            )
        )
    }

    var newOptionText by remember { mutableStateOf("") }
    var isSpinning by remember { mutableStateOf(false) }
    val rotationAngle = remember { Animatable(0f) }
    
    var winnerOption by remember { mutableStateOf<RouletteOption?>(null) }
    var showWinnerDialog by remember { mutableStateOf(false) }
    
    val confettiState = remember { ConfettiState() }

    // Trigger haptics
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

    var lastTickAngle = remember { 0f }
    val sweepPerItem = 360f / optionsList.size

    // Handle ticking vibration when rotation passes division boundaries
    val currentAngle = rotationAngle.value
    val division = 360f / optionsList.size
    if (Math.abs(currentAngle - lastTickAngle) >= division) {
        lastTickAngle = currentAngle
        if (isSpinning) {
            triggerVibration(15, 60)
        }
    }

    // Function to add option
    fun addOption() {
        if (newOptionText.isNotBlank()) {
            val color = colorsPalette[optionsList.size % colorsPalette.size]
            optionsList = optionsList + RouletteOption(newOptionText.trim(), color)
            newOptionText = ""
            triggerVibration(30, 100)
        }
    }

    // Function to delete option
    fun deleteOption(index: Int) {
        if (optionsList.size > 2) {
            optionsList = optionsList.filterIndexed { i, _ -> i != index }
            triggerVibration(25, 80)
        }
    }

    // Presets loader
    fun loadPreset(presetName: String) {
        val newOptions = when (presetName) {
            "What to Eat?" -> listOf(
                RouletteOption("Fried Rice", NeonPurple),
                RouletteOption("Chicken Satay", NeonCyan),
                RouletteOption("Meatballs", NeonPink),
                RouletteOption("Noodles", CrayonYellow),
                RouletteOption("Pizza", NeonGreen),
                RouletteOption("Sushi", CrayonOrange)
            )
            "Wash Dishes?" -> listOf(
                RouletteOption("You", NeonPurple),
                RouletteOption("Me", NeonCyan),
                RouletteOption("Skip", NeonPink),
                RouletteOption("Spin Again", CrayonYellow)
            )
            "Truth or Dare" -> listOf(
                RouletteOption("TRUTH", NeonPurple),
                RouletteOption("DARE", NeonPink),
                RouletteOption("TRUTH", NeonCyan),
                RouletteOption("DARE", CrayonYellow)
            )
            "Yes or No" -> listOf(
                RouletteOption("YES", NeonGreen),
                RouletteOption("NO", NeonPink),
                RouletteOption("YES", NeonCyan),
                RouletteOption("NO", CrayonOrange)
            )
            else -> emptyList()
        }
        if (newOptions.isNotEmpty()) {
            optionsList = newOptions
            triggerVibration(50, 120)
        }
    }

    // Spin function
    fun spinWheel() {
        if (isSpinning) return
        isSpinning = true
        winnerOption = null
        showWinnerDialog = false

        scope.launch {
            val totalSpins = 8 + Random.nextInt(4)
            val randomOffset = Random.nextFloat() * 360f
            val targetRotation = rotationAngle.value + (totalSpins * 360f) + randomOffset
            
            rotationAngle.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = 5000,
                    easing = CubicBezierEasing(0.1f, 1.0f, 0.2f, 1.0f)
                )
            )

            isSpinning = false
            
            // MATH to find which item landed under pointer (pointer is at Top / -90 degrees)
            val finalAngleMod = (rotationAngle.value) % 360f
            val itemArcSweep = 360f / optionsList.size
            
            val pointerReferenceAngle = 270f
            var winningIndex = ((pointerReferenceAngle - finalAngleMod) % 360f) / itemArcSweep
            if (winningIndex < 0) {
                winningIndex += optionsList.size
            }
            
            val finalWinner = optionsList[winningIndex.toInt() % optionsList.size]
            winnerOption = finalWinner
            showWinnerDialog = true
            
            // Confetti
            confettiState.spawn(x = context.resources.displayMetrics.widthPixels / 2f, y = 800f, count = 50)
            
            triggerVibration(300, 200)
            delay(150)
            triggerVibration(100, 200)
        }
    }

    NotebookBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp)
        ) {
            // Custom Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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

                Text(
                    text = "SPIN WHEEL",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Wheel Spin Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer sketchy circle ring
                Box(
                    modifier = Modifier
                        .size(265.dp)
                        .background(Color.White, CircleShape)
                        .border(4.5.dp, BorderColor, CircleShape)
                )

                // Wheel Canvas
                Canvas(
                    modifier = Modifier
                        .size(246.dp)
                        .clickable(enabled = !isSpinning) { spinWheel() }
                ) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val arcSize = Size(size.width, size.height)

                    optionsList.forEachIndexed { index, option ->
                        val startAngle = -90f + (index * sweepPerItem) + (rotationAngle.value % 360f)
                        
                        // Draw segment slice
                        drawArc(
                            color = option.color,
                            startAngle = startAngle,
                            sweepAngle = sweepPerItem,
                            useCenter = true,
                            size = arcSize,
                            topLeft = Offset.Zero
                        )

                        // Draw divider lines (Thick ink sketchy look)
                        val dividerAngleRad = Math.toRadians(startAngle.toDouble())
                        val endX = center.x + radius * Math.cos(dividerAngleRad).toFloat()
                        val endY = center.y + radius * Math.sin(dividerAngleRad).toFloat()
                        drawLine(
                            color = BorderColor,
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 3.5f
                        )

                        // Draw rotated item label text
                        rotate(degrees = startAngle + sweepPerItem / 2f, pivot = center) {
                            val textStr = if (option.label.length > 8) option.label.take(7) + ".." else option.label
                            val textLayout = textMeasurer.measure(
                                text = AnnotatedString(textStr.uppercase()),
                                style = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(center.x + radius * 0.38f, center.y - textLayout.size.height / 2f)
                            )
                        }
                    }

                    // Centered decorative hand-drawn pin node
                    drawCircle(
                        color = Color.White,
                        radius = 24f,
                        center = center
                    )
                    drawCircle(
                        color = BorderColor,
                        radius = 24f,
                        center = center,
                        style = Stroke(width = 4f)
                    )
                    drawCircle(
                        color = BorderColor,
                        radius = 8f,
                        center = center
                    )
                }

                // Arrow Pointer Indicator (sketchy red pointer at the top center pointing down)
                Canvas(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 12.dp)
                ) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, size.height)
                        lineTo(size.width * 0.2f, 0f)
                        lineTo(size.width * 0.8f, 0f)
                        close()
                    }
                    // Crayon red pointer fill
                    drawPath(
                        path = path,
                        color = CrayonRed
                    )
                    // Pencil sketchy outline
                    drawPath(
                        path = path,
                        color = BorderColor,
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            // Spin Button Action (Doodle style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                DoodleButton(
                    onClick = { spinWheel() },
                    backgroundColor = NeonPurple,
                    text = if (isSpinning) "SPINNING..." else "SPIN NOW!",
                    textColor = Color.White,
                    enabled = !isSpinning,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }

            // Quick Preset Row
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preset:", 
                    color = TextPrimary, 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Black
                )
                val presetsList = listOf("What to Eat?", "Wash Dishes?", "Truth or Dare", "Yes or No")
                presetsList.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(2.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isSpinning) { loadPreset(preset) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = preset.replace("?", ""),
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(2.5.dp)
                    .background(BorderColor)
            )

            // Dynamic Options List Manager
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Add item textfield header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newOptionText,
                            onValueChange = { newOptionText = it },
                            placeholder = { Text("Add New Choice...", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            enabled = !isSpinning && optionsList.size < 12,
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
                                .height(52.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // Crayon styled Add button
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    if (!isSpinning && optionsList.size < 12) NeonCyan else Color.LightGray.copy(alpha = 0.5f), 
                                    RoundedCornerShape(12.dp)
                                )
                                .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .clickable(enabled = !isSpinning && optionsList.size < 12) { addOption() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+", 
                                color = TextPrimary, 
                                fontWeight = FontWeight.Black, 
                                fontSize = 24.sp
                            )
                        }
                    }
                }

                // List of items in notebook notebook entries
                itemsIndexed(optionsList) { index, option ->
                    DoodleCard(
                        backgroundColor = Color.White,
                        shadowOffset = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Sector Crayon Bubble
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(option.color, CircleShape)
                                        .border(1.5.dp, BorderColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = option.label,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            if (!isSpinning && optionsList.size > 2) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(CrayonRed.copy(alpha = 0.2f), CircleShape)
                                        .border(1.5.dp, CrayonRed, CircleShape)
                                        .clickable { deleteOption(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "×", 
                                        color = CrayonRed, 
                                        fontSize = 16.sp, 
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Particle confetti for victory
        SparkleConfetti(state = confettiState)

        // Winning Modal dialog
        if (showWinnerDialog && winnerOption != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showWinnerDialog = false },
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
                            text = "SPIN RESULT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Winner highlight Crayon Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(winnerOption!!.color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .border(2.5.dp, BorderColor, RoundedCornerShape(16.dp))
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = winnerOption!!.label.uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        DoodleButton(
                            onClick = { showWinnerDialog = false },
                            backgroundColor = NeonPurple,
                            text = "AWESOME!",
                            textColor = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
