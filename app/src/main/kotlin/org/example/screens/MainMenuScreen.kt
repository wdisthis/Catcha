package org.example.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import org.example.components.DoodleCard
import org.example.components.DoodleButton
import org.example.components.NotebookBackground
import org.example.components.drawScribbleStar
import org.example.components.drawZigzagLine
import org.example.theme.*

@Composable
fun MainMenuScreen(
    onNavigateToFingerChooser: () -> Unit,
    onNavigateToFingerGrouper: () -> Unit,
    onNavigateToFingerOrder: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(context) {
        org.example.audio.BubbleSoundPlayer.initialize(context)
    }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    NotebookBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = "CATCHA",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = CabinSketchFamily,
                    color = TextPrimary,
                    letterSpacing = 6.sp,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .drawBehind {
                            // Draw a beautiful crayon-red hand-drawn zigzag under the title
                            drawZigzagLine(
                                color = CrayonRed,
                                start = Offset(-12f, size.height + 6.dp.toPx()),
                                end = Offset(size.width + 12f, size.height + 6.dp.toPx()),
                                strokeWidth = 6f,
                                amplitude = 7f
                            )
                        }
                )
                
                Text(
                    text = "A Fun Decision-Making Assistant",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CabinFamily,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Cards Menu Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MenuCard(
                    title = "Finger Chooser",
                    subtitle = "Touch the screen together to see who gets chosen!",
                    icon = { FingerChooserDoodle() },
                    accentColor = NeonCyan,
                    onClick = onNavigateToFingerChooser
                )

                Spacer(modifier = Modifier.height(12.dp))

                MenuCard(
                    title = "Finger Grouper",
                    subtitle = "Touch together to split players into balanced teams!",
                    icon = { FingerGrouperDoodle() },
                    accentColor = NeonGreen,
                    onClick = onNavigateToFingerGrouper
                )

                Spacer(modifier = Modifier.height(12.dp))

                MenuCard(
                    title = "Finger Order",
                    subtitle = "Touch together to get a random turn order!",
                    icon = { FingerOrderDoodle() },
                    accentColor = CrayonOrange,
                    onClick = onNavigateToFingerOrder
                )
            }

            // Footer Section with Settings & About
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Black,
                    fontFamily = CabinFamily,
                    modifier = Modifier.clickable {
                        org.example.audio.BubbleSoundPlayer.playSmallPop()
                        showSettingsDialog = true
                    }
                )
                Text(
                    text = "About",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Black,
                    fontFamily = CabinFamily,
                    modifier = Modifier.clickable {
                        org.example.audio.BubbleSoundPlayer.playSmallPop()
                        showAboutDialog = true
                    }
                )
            }
        }

        if (showSettingsDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showSettingsDialog = false },
                contentAlignment = Alignment.Center
            ) {
                var soundEnabled by remember { mutableStateOf(org.example.data.AppSettings.isSoundEnabled) }
                var vibrationEnabled by remember { mutableStateOf(org.example.data.AppSettings.isVibrationEnabled) }

                DoodleCard(
                    backgroundColor = Color.White,
                    shadowOffset = 8.dp,
                    modifier = Modifier
                        .width(310.dp)
                        .padding(24.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "SETTINGS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = CabinSketchFamily,
                            color = TextPrimary,
                            letterSpacing = 2.sp
                        )

                        // Sound Effects Toggle Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CrayonYellow.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    soundEnabled = !soundEnabled
                                    org.example.data.AppSettings.isSoundEnabled = soundEnabled
                                    org.example.audio.BubbleSoundPlayer.playSmallPop()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sound Effects",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CabinFamily,
                                color = TextPrimary
                            )
                            // Doodle style checkbox
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(if (soundEnabled) NeonCyan else Color.White, RoundedCornerShape(6.dp))
                                    .border(2.dp, BorderColor, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (soundEnabled) {
                                    Text("✓", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }

                        // Vibration Toggle Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NeonPink.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    vibrationEnabled = !vibrationEnabled
                                    org.example.data.AppSettings.isVibrationEnabled = vibrationEnabled
                                    org.example.audio.BubbleSoundPlayer.playSmallPop()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Haptic Vibration",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CabinFamily,
                                color = TextPrimary
                            )
                            // Doodle style checkbox
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(if (vibrationEnabled) NeonPurple else Color.White, RoundedCornerShape(6.dp))
                                    .border(2.dp, BorderColor, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (vibrationEnabled) {
                                    Text("✓", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        DoodleButton(
                            onClick = {
                                org.example.audio.BubbleSoundPlayer.playSmallPop()
                                showSettingsDialog = false
                            },
                            backgroundColor = NeonGreen,
                            text = "SAVE & CLOSE",
                            textColor = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showAboutDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showAboutDialog = false },
                contentAlignment = Alignment.Center
            ) {
                DoodleCard(
                    backgroundColor = Color.White,
                    shadowOffset = 8.dp,
                    modifier = Modifier
                        .width(310.dp)
                        .padding(24.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "ABOUT CATCHA",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = CabinSketchFamily,
                            color = TextPrimary,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "Catcha is a playful, hand-drawn decision assistant designed to solve everyday dilemmas in a fun and interactive way!\n\nFeatures include:\n• Finger Chooser\n• Finger Grouper\n• Finger Order",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CabinFamily,
                            color = TextSecondary,
                            textAlign = TextAlign.Start,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        DoodleButton(
                            onClick = {
                                org.example.audio.BubbleSoundPlayer.playSmallPop()
                                showAboutDialog = false
                            },
                            backgroundColor = NeonCyan,
                            text = "CLOSE",
                            textColor = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    accentColor: Color,
    onClick: () -> Unit
) {
    DoodleCard(
        backgroundColor = Color.White,
        rotation = 0f, // No rotation as requested (cards are completely straight!)
        shadowOffset = 6.dp,
        onClick = {
            org.example.audio.BubbleSoundPlayer.playSmallPop()
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Playful crayon background bullet with custom doodle icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accentColor, RoundedCornerShape(12.dp))
                    .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = CabinSketchFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = CabinFamily
                )
            }
        }
    }
}

@Composable
fun FingerGrouperDoodle() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        
        // Draw left cluster of two dots
        drawCircle(color = BorderColor, radius = 6.dp.toPx(), center = Offset(cx - 10.dp.toPx(), cy - 6.dp.toPx()))
        drawCircle(color = BorderColor, radius = 6.dp.toPx(), center = Offset(cx - 14.dp.toPx(), cy + 8.dp.toPx()))
        
        // Draw right cluster of two dots
        drawCircle(color = BorderColor, radius = 6.dp.toPx(), center = Offset(cx + 12.dp.toPx(), cy - 8.dp.toPx()))
        drawCircle(color = BorderColor, radius = 6.dp.toPx(), center = Offset(cx + 14.dp.toPx(), cy + 6.dp.toPx()))
        
        // Draw a dashed wavy line separating them
        val path = Path().apply {
            moveTo(cx - 2.dp.toPx(), cy - 16.dp.toPx())
            quadraticBezierTo(cx + 4.dp.toPx(), cy, cx - 4.dp.toPx(), cy + 16.dp.toPx())
        }
        drawPath(
            path = path,
            color = BorderColor.copy(alpha = 0.6f),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        )
    }
}

@Composable
fun FingerOrderDoodle() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Draw three numbered dots arranged in a triangle pattern
        val dot1 = Offset(cx - 12.dp.toPx(), cy - 8.dp.toPx())
        val dot2 = Offset(cx + 12.dp.toPx(), cy - 10.dp.toPx())
        val dot3 = Offset(cx, cy + 10.dp.toPx())

        // Connecting dashed arrows between dots (1 -> 2 -> 3)
        val arrowPath = Path().apply {
            moveTo(dot1.x, dot1.y)
            lineTo(dot2.x, dot2.y)
            lineTo(dot3.x, dot3.y)
        }
        drawPath(
            path = arrowPath,
            color = BorderColor.copy(alpha = 0.4f),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw filled circles at each dot
        listOf(dot1, dot2, dot3).forEach { pos ->
            drawCircle(color = BorderColor, radius = 7.dp.toPx(), center = pos)
            // Small white number text placeholder (tiny circle inset)
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = pos)
        }
    }
}

@Composable
fun FingerChooserDoodle() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        
        // 🌟 Symmetrical Bounds for clawless compact paw: Width 316f, Height 278f, Center (340f, 296f)
        val scale = minOf(
            (size.width * 0.70f) / 316f,
            (size.height * 0.70f) / 278f
        )
        
        // Helper to draw a scaled and rotated ellipse
        fun drawScaledEllipse(
            ecx: Float,
            ecy: Float,
            erx: Float,
            ery: Float,
            angle: Float,
            fillColor: Color
        ) {
            val px = (ecx - 340f) * scale + cx
            val py = (ecy - 296f) * scale + cy
            val prx = erx * scale
            val pry = ery * scale
            
            if (angle != 0f) {
                withTransform({
                    rotate(degrees = angle, pivot = Offset(px, py))
                }) {
                    drawOval(
                        color = fillColor,
                        topLeft = Offset(px - prx, py - pry),
                        size = Size(prx * 2f, pry * 2f)
                    )
                }
            } else {
                drawOval(
                    color = fillColor,
                    topLeft = Offset(px - prx, py - pry),
                    size = Size(prx * 2f, pry * 2f)
                )
            }
        }
        
        // Solid silhouette color matching our doodle borders
        val pawColor = BorderColor
        
        // --- 🐾 TIGHTLY COMPACT, SYMMETRICAL, CLAW-LESS CAT PAW ---
        // 1. Jari 1 - kiri luar (miring jauh ke kiri)
        drawScaledEllipse(220f, 235f, 38f, 48f, -25f, pawColor)
        
        // 2. Jari 2 - kiri dalam (miring sedikit ke kiri)
        drawScaledEllipse(295f, 205f, 38f, 48f, -8f, pawColor)
        
        // 3. Jari 3 - kanan dalam (miring sedikit ke kanan)
        drawScaledEllipse(385f, 205f, 38f, 48f, 8f, pawColor)
        
        // 4. Jari 4 - kanan luar (miring jauh ke kanan)
        drawScaledEllipse(460f, 235f, 38f, 48f, 25f, pawColor)
        
        // 5. Badan cakar (Main pad)
        drawScaledEllipse(340f, 345f, 110f, 90f, 0f, pawColor)
    }
}

@Composable
fun RouletteDoodle(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = 17.dp.toPx()
        
        // Fill circle with soft background tint
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius,
            center = Offset(cx, cy)
        )
        
        // Outer circle border
        drawCircle(
            color = BorderColor,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5.dp.toPx())
        )
        
        // Inner hub
        drawCircle(
            color = BorderColor,
            radius = 3.dp.toPx(),
            center = Offset(cx, cy)
        )
        
        // Segments (spokes)
        val angles = listOf(0f, 60f, 120f, 180f, 240f, 300f)
        for (angle in angles) {
            val rad = Math.toRadians(angle.toDouble())
            val targetX = (cx + radius * Math.cos(rad)).toFloat()
            val targetY = (cy + radius * Math.sin(rad)).toFloat()
            drawLine(
                color = BorderColor,
                start = Offset(cx, cy),
                end = Offset(targetX, targetY),
                strokeWidth = 2.dp.toPx()
            )
        }
        
        // 🌟 PERFECT TOP POINTER ARROW POINTING DOWN
        val pointerPath = Path().apply {
            // Tip pointing down at the wheel edge
            moveTo(cx, cy - radius + 3.dp.toPx())
            // Top-left of the arrow
            lineTo(cx - 7.dp.toPx(), cy - radius - 8.dp.toPx())
            // Top-right of the arrow
            lineTo(cx + 7.dp.toPx(), cy - radius - 8.dp.toPx())
            close()
        }
        
        // Fill pointer with CrayonRed
        drawPath(
            pointerPath,
            color = CrayonRed
        )
        // Stroke pointer with BorderColor
        drawPath(
            pointerPath,
            color = BorderColor,
            style = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round)
        )
    }
}

@Composable
fun CoinFlipDoodle(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = 17.dp.toPx()
        
        // Draw a motion trail loop behind/around the coin (a flip curl!)
        val motionPath = Path().apply {
            moveTo(cx - radius - 3.dp.toPx(), cy + 6.dp.toPx())
            quadraticBezierTo(
                cx - radius - 9.dp.toPx(), cy - 12.dp.toPx(),
                cx, cy - radius - 3.dp.toPx()
            )
            quadraticBezierTo(
                cx + radius + 5.dp.toPx(), cy - radius - 7.dp.toPx(),
                cx + radius + 3.dp.toPx(), cy - 2.dp.toPx()
            )
        }
        drawPath(
            motionPath,
            color = TextSecondary.copy(alpha = 0.4f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
        )
        
        // Fill coin with pink tint
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius,
            center = Offset(cx, cy)
        )
        
        // Coin outer border
        drawCircle(
            color = BorderColor,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5.dp.toPx())
        )
        
        // Coin inner decorative circle
        drawCircle(
            color = BorderColor.copy(alpha = 0.5f),
            radius = radius - 4.dp.toPx(),
            center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
        )
        
        // Draw a cute star in the center of the coin
        drawScribbleStar(
            color = BorderColor,
            center = Offset(cx - 0.5.dp.toPx(), cy - 0.5.dp.toPx()),
            radius = 6.dp.toPx(),
            strokeWidth = 2.dp.toPx()
        )
    }
}
