package org.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.theme.*

/**
 * Renders a warm notebook paper background with faint grid/ruled lines,
 * left pink margin, and cute childish doodles drawn around the corners.
 */
@Composable
fun NotebookBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .drawBehind {
                // 1. Draw Grid Lines
                val gridSize = 32.dp.toPx()
                val gridColor = GridLineColor
                
                // Horizontal lines
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += gridSize
                }
                
                // Vertical lines
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += gridSize
                }
                
                // 2. Left Pink Margin Line
                val marginX = 40.dp.toPx()
                drawLine(
                    color = MarginLineColor,
                    start = Offset(marginX, 0f),
                    end = Offset(marginX, size.height),
                    strokeWidth = 2.dp.toPx()
                )
                
                // 3. Cute Childish Doodles in the background (faint ink/crayon)
                drawNotebookDoodles(size)
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            content()
        }
    }
}

/**
 * Draws playful faint school notebook doodles in the corners
 */
private fun DrawScope.drawNotebookDoodles(canvasSize: Size) {
    val doodleColor = TextSecondary.copy(alpha = 0.15f)
    val strokeWidth = 2.5f

    // Doodle 1: A sketchy cloud and smiley in the top-right corner
    val trCenterX = canvasSize.width * 0.85f
    val trCenterY = canvasSize.height * 0.15f
    
    // Draw cloud outline
    val cloudPath = Path().apply {
        moveTo(trCenterX - 30f, trCenterY)
        cubicTo(trCenterX - 30f, trCenterY - 20f, trCenterX - 10f, trCenterY - 25f, trCenterX, trCenterY - 15f)
        cubicTo(trCenterX + 10f, trCenterY - 25f, trCenterX + 35f, trCenterY - 20f, trCenterX + 30f, trCenterY)
        cubicTo(trCenterX + 45f, trCenterY + 5f, trCenterX + 35f, trCenterY + 25f, trCenterX + 15f, trCenterY + 20f)
        cubicTo(trCenterX, trCenterY + 25f, trCenterX - 20f, trCenterY + 20f, trCenterX - 30f, trCenterY)
        close()
    }
    drawPath(cloudPath, doodleColor, style = Stroke(width = strokeWidth))
    
    // Doodle 2: A chaotic hand-drawn 5-point star in the bottom-left corner
    val blCenterX = canvasSize.width * 0.12f
    val blCenterY = canvasSize.height * 0.85f
    drawScribbleStar(doodleColor, Offset(blCenterX, blCenterY), radius = 25f, strokeWidth = strokeWidth)

    // Doodle 3: A spiral scribble / paper binding ring or loop on the margin
    val marginX = 40.dp.toPx()
    val ringSpacing = 60f
    for (i in 2..8) {
        val ringY = ringSpacing * i
        if (ringY < canvasSize.height * 0.7f) {
            // Draw a quick hand-drawn binding loop across the margin
            val loopPath = Path().apply {
                moveTo(marginX - 20f, ringY)
                cubicTo(marginX - 20f, ringY - 15f, marginX + 15f, ringY - 10f, marginX + 15f, ringY)
                cubicTo(marginX + 15f, ringY + 10f, marginX - 20f, ringY + 15f, marginX - 20f, ringY)
            }
            drawPath(loopPath, doodleColor.copy(alpha = 0.1f), style = Stroke(width = strokeWidth))
        }
    }

    // Doodle 4: A cute happy face in the bottom-right corner
    val brCenterX = canvasSize.width * 0.88f
    val brCenterY = canvasSize.height * 0.88f
    
    // Draw face circle
    drawCircle(
        color = doodleColor,
        radius = 22f,
        center = Offset(brCenterX, brCenterY),
        style = Stroke(width = strokeWidth)
    )
    // Draw eyes
    drawCircle(
        color = doodleColor,
        radius = 2f,
        center = Offset(brCenterX - 8f, brCenterY - 4f)
    )
    drawCircle(
        color = doodleColor,
        radius = 2f,
        center = Offset(brCenterX + 8f, brCenterY - 4f)
    )
    // Draw mouth smile path
    val smilePath = Path().apply {
        moveTo(brCenterX - 10f, brCenterY + 4f)
        quadraticBezierTo(
            brCenterX, brCenterY + 14f,
            brCenterX + 10f, brCenterY + 4f
        )
    }
    drawPath(smilePath, doodleColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

/**
 * Draws a chaotic, imperfect hand-drawn star
 */
fun DrawScope.drawScribbleStar(color: Color, center: Offset, radius: Float, strokeWidth: Float = 4f) {
    val path = Path()
    val points = 5
    val outerRadius = radius
    val innerRadius = radius * 0.4f
    
    for (i in 0 until (points * 2 + 1)) {
        val angle = Math.toRadians((i * 180f / points - 90f).toDouble())
        val r = if (i % 2 == 0) outerRadius else innerRadius
        // Introduce slight randomness for that "childish sketch" feel
        val noiseX = (Math.random() * 4f - 2f).toFloat()
        val noiseY = (Math.random() * 4f - 2f).toFloat()
        val px = (center.x + r * Math.cos(angle) + noiseX).toFloat()
        val py = (center.y + r * Math.sin(angle) + noiseY).toFloat()
        
        if (i == 0) {
            path.moveTo(px, py)
        } else {
            path.lineTo(px, py)
        }
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/**
 * A beautiful hand-drawn styled Card.
 * Uses a thick ink-black border, custom background color, slight rotation,
 * and a static solid black 3D shadow offset to the bottom-right.
 */
@Composable
fun DoodleCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = BorderColor,
    borderWidth: Dp = 3.dp,
    shadowColor: Color = Color.Black,
    shadowOffset: Dp = 6.dp,
    rotation: Float = 0f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {
        // 1. Static Solid Black Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, RoundedCornerShape(16.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
        )
        // 2. Main Card Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(16.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
                .then(clickableModifier)
        ) {
            content()
        }
    }
}

/**
 * An interactive, highly-satisfying Doodle Button.
 * When clicked/pressed, it animates down physically towards the shadow.
 */
@Composable
fun DoodleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    text: String,
    textColor: Color = Color.Black,
    fontSize: Dp = 16.dp, // Note: using Dp-equivalent or converting later
    fontWeight: FontWeight = FontWeight.Black,
    fontFamily: androidx.compose.ui.text.font.FontFamily = CabinSketchFamily,
    borderWidth: Dp = 3.dp,
    shadowOffset: Dp = 6.dp,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate the button pressing down into the shadow
    val animatedOffset = if (isPressed && enabled) shadowOffset else 0.dp
    
    val buttonClickModifier = if (enabled) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .height(54.dp)
            .then(buttonClickModifier)
    ) {
        // 1. Static Shadow (does not move)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(if (enabled) Color.Black else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .border(borderWidth, Color.Black, RoundedCornerShape(16.dp))
        )
        // 2. Front Button Face (slides on press)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = animatedOffset, y = animatedOffset)
                .background(
                    if (enabled) backgroundColor else Color.LightGray.copy(alpha = 0.4f), 
                    RoundedCornerShape(16.dp)
                )
                .border(borderWidth, Color.Black, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) textColor else Color.Gray,
                fontSize = fontSize.value.sp,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = 1.5.sp
            )
        }
    }
}

/**
 * Draws a hand-drawn zigzag under-scribble line (often used for underlining titles)
 */
fun DrawScope.drawZigzagLine(
    color: Color,
    start: Offset,
    end: Offset,
    strokeWidth: Float = 6f,
    amplitude: Float = 8f,
    segments: Int = 14
) {
    val path = Path()
    val dx = end.x - start.x
    val dy = end.y - start.y
    val length = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
    
    if (length <= 0f) return
    
    val ux = dx / length
    val uy = dy / length
    
    // Perpendicular vector for zigzag displacement
    val px = -uy
    val py = ux
    
    path.moveTo(start.x, start.y)
    
    for (i in 1..segments) {
        val fraction = i.toFloat() / segments
        val cx = start.x + dx * fraction
        val cy = start.y + dy * fraction
        
        // Alternate displacement positive/negative
        val offset = if (i % 2 == 0) -amplitude else amplitude
        
        // Slight randomness to make it look imperfect and child-drawn
        val noise = (Math.random() * 4f - 2f).toFloat()
        
        path.lineTo(cx + px * (offset + noise), cy + py * (offset + noise))
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
