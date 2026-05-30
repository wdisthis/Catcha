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
import org.example.components.DoodleCard
import org.example.components.NotebookBackground
import org.example.components.drawScribbleStar
import org.example.components.drawZigzagLine
import org.example.theme.*

@Composable
fun MainMenuScreen(
    onNavigateToFingerChooser: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    onNavigateToCoinFlip: () -> Unit
) {
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
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Cards Menu Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp),
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

                Spacer(modifier = Modifier.height(24.dp))

                MenuCard(
                    title = "Custom Roulette",
                    subtitle = "Create your custom choices and spin the wheel of fortune!",
                    icon = { RouletteDoodle(NeonPurple) },
                    accentColor = NeonPurple,
                    onClick = onNavigateToRoulette
                )

                Spacer(modifier = Modifier.height(24.dp))

                MenuCard(
                    title = "Coin Flip",
                    subtitle = "Quick 50:50 decisions with an interactive 3D coin!",
                    icon = { CoinFlipDoodle(NeonPink) },
                    accentColor = NeonPink,
                    onClick = onNavigateToCoinFlip
                )
            }

            // Footer Section
            Text(
                text = "v1.0.0 • Let's Choose!",
                fontSize = 12.sp,
                color = TextSecondary.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
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
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
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
