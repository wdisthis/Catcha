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
import org.example.theme.*
import kotlin.random.Random

data class RouletteOption(
    val label: String,
    val color: Color
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun RouletteScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    val colorsPalette = listOf(
        Color(0xFF9D4EDD), // Purple
        Color(0xFF00F0FF), // Cyan
        Color(0xFFFF007F), // Pink
        Color(0xFFFFD700), // Gold
        Color(0xFF39FF14), // Green
        Color(0xFFFF5722), // Orange
        Color(0xFFE040FB), // Magenta
        Color(0xFF00E676)  // Light Green
    )

    // Initial default options
    var optionsList by remember {
        mutableStateOf(
            listOf(
                RouletteOption("Nasi Goreng", Color(0xFF9D4EDD)),
                RouletteOption("Sate Ayam", Color(0xFF00F0FF)),
                RouletteOption("Bakso", Color(0xFFFF007F)),
                RouletteOption("Mie Ayam", Color(0xFFFFD700)),
                RouletteOption("Pizza", Color(0xFF39FF14)),
                RouletteOption("Sushi", Color(0xFFFF5722))
            )
        )
    }

    var newOptionText by remember { mutableStateOf("") }
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle = remember { Animatable(0f) }
    
    var winnerOption by remember { mutableStateOf<RouletteOption?>(null) }
    var showWinnerDialog by remember { mutableStateOf(false) }
    
    val confettiState = remember { ConfettiState() }

    // Trigger haptics
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

    // Ticking audio-haptic feedback logic during spin
    var lastTickAngle = remember { 0f }
    val sweepPerItem = 360f / optionsList.size

    // Handle ticking vibration when rotation passes division boundaries
    val currentAngle = rotationAngle.value
    val division = 360f / optionsList.size
    val currentModulo = currentAngle % division
    val lastModulo = lastTickAngle % division
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
            "Makan Apa?" -> listOf(
                RouletteOption("Nasi Goreng", Color(0xFF9D4EDD)),
                RouletteOption("Sate Ayam", Color(0xFF00F0FF)),
                RouletteOption("Bakso", Color(0xFFFF007F)),
                RouletteOption("Mie Ayam", Color(0xFFFFD700)),
                RouletteOption("Pizza", Color(0xFF39FF14)),
                RouletteOption("Sushi", Color(0xFFFF5722))
            )
            "Cuci Piring?" -> listOf(
                RouletteOption("Kamu", Color(0xFF9D4EDD)),
                RouletteOption("Aku", Color(0xFF00F0FF)),
                RouletteOption("Bebas", Color(0xFFFF007F)),
                RouletteOption("Suit Lagi", Color(0xFFFFD700))
            )
            "Truth or Dare" -> listOf(
                RouletteOption("TRUTH", Color(0xFF9D4EDD)),
                RouletteOption("DARE", Color(0xFFFF007F)),
                RouletteOption("TRUTH", Color(0xFF00F0FF)),
                RouletteOption("DARE", Color(0xFFFFD700))
            )
            "Ya atau Tidak" -> listOf(
                RouletteOption("YA", Color(0xFF39FF14)),
                RouletteOption("TIDAK", Color(0xFFFF007F)),
                RouletteOption("YA", Color(0xFF00F0FF)),
                RouletteOption("TIDAK", Color(0xFFFF5722))
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
            // Setup target rotation: spin around 8-12 full times + random offset
            val totalSpins = 8 + Random.nextInt(4)
            val randomOffset = Random.nextFloat() * 360f
            val targetRotation = rotationAngle.value + (totalSpins * 360f) + randomOffset
            
            // Decelerating tween curve
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
            // Pointer pointing at top means -90 degrees (or 270 degrees in normalized coordinates).
            // A higher rotation angle spins clockwise, so visual moves clockwise.
            // Option 0 starts at 0 and sweeps positive.
            val itemArcSweep = 360f / optionsList.size
            
            // Calculating selected item
            val pointerReferenceAngle = 270f
            var winningIndex = ((pointerReferenceAngle - finalAngleMod) % 360f) / itemArcSweep
            if (winningIndex < 0) {
                winningIndex += optionsList.size
            }
            
            val finalWinner = optionsList[winningIndex.toInt() % optionsList.size]
            winnerOption = finalWinner
            showWinnerDialog = true
            
            // Spawn Confetti
            confettiState.spawn(x = context.resources.displayMetrics.widthPixels / 2f, y = 800f, count = 50)
            
            // Pulsing Vibration
            triggerVibration(300, 200)
            delay(150)
            triggerVibration(100, 200)
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
            // Custom Header Bar
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
                    text = "RODA PUTAR",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Wheel Spin Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glowing circle
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .clip(CircleShape)
                        .background(ObsidianSurface)
                        .border(4.dp, Brush.radialGradient(listOf(NeonCyan, NeonPurple)), CircleShape)
                )

                // Wheel Canvas
                Canvas(
                    modifier = Modifier
                        .size(250.dp)
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

                        // Draw divider lines
                        val dividerAngleRad = Math.toRadians(startAngle.toDouble())
                        val endX = center.x + radius * Math.cos(dividerAngleRad).toFloat()
                        val endY = center.y + radius * Math.sin(dividerAngleRad).toFloat()
                        drawLine(
                            color = ObsidianBg,
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 4f
                        )

                        // Draw rotated item label text
                        rotate(degrees = startAngle + sweepPerItem / 2f, pivot = center) {
                            val textStr = if (option.label.length > 8) option.label.take(7) + ".." else option.label
                            val textLayout = textMeasurer.measure(
                                text = AnnotatedString(textStr),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(center.x + radius * 0.4f, center.y - textLayout.size.height / 2f)
                            )
                        }
                    }

                    // Centered decorative metal node
                    drawCircle(
                        color = Color.White,
                        radius = 20f,
                        center = center
                    )
                    drawCircle(
                        color = ObsidianBg,
                        radius = 12f,
                        center = center
                    )
                }

                // Arrow Pointer Indicator (at the top pointing straight down)
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
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(listOf(Color.White, Color.LightGray))
                    )
                }
            }

            // Spin Button Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { spinWheel() },
                    enabled = !isSpinning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple,
                        disabledContainerColor = NeonPurple.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(50.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = NeonPurple)
                ) {
                    Text(
                        text = if (isSpinning) "MEMUTAR..." else "PUTAR SEKARANG",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Quick Preset Row
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Preset:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val presetsList = listOf("Makan Apa?", "Cuci Piring?", "Truth or Dare", "Ya atau Tidak")
                presetsList.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isSpinning) { loadPreset(preset) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = preset.replace("?", ""),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderColor, thickness = 1.dp)

            // Dynamic Options List Manager
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add item textfield header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newOptionText,
                            onValueChange = { newOptionText = it },
                            placeholder = { Text("Tambah Pilihan Baru...", color = TextSecondary, fontSize = 13.sp) },
                            singleLine = true,
                            enabled = !isSpinning && optionsList.size < 12,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderColor
                            ),
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .background(ObsidianSurface, RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isSpinning && optionsList.size < 12) NeonCyan else Color.Gray.copy(alpha = 0.3f))
                                .clickable(enabled = !isSpinning && optionsList.size < 12) { addOption() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = ObsidianBg, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }
                }

                // List of items
                itemsIndexed(optionsList) { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Sector Color bubble
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(option.color)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.label,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (!isSpinning && optionsList.size > 2) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FF007F))
                                    .clickable { deleteOption(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("×", color = NeonPink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { showWinnerDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ObsidianSurface)
                        .border(2.dp, NeonPurple, RoundedCornerShape(24.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HASIL PILIHAN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Winner Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(winnerOption!!.color.copy(alpha = 0.15f))
                            .border(1.5.dp, winnerOption!!.color, RoundedCornerShape(16.dp))
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = winnerOption!!.label,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showWinnerDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("MANTAP", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
