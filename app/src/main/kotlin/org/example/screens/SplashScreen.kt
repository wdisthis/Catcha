package org.example.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.R
import org.example.components.drawZigzagLine
import org.example.theme.*

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Entrance animations
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Run entrance scale and fade animations in parallel
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = EaseOutBack
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = EaseInOutQuad
                )
            )
        }
        
        // Let the splash screen linger beautifully for 2.2 seconds before entering the main screen
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
        ) {
            // Display Logo inside a rounded doodle box with thick black border
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .border(4.dp, BorderColor, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.catcha_ico),
                    contentDescription = "Catcha Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Playful Underlined Catcha! Text
            Text(
                text = "CATCHA!",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CabinSketchFamily,
                color = TextPrimary,
                letterSpacing = 4.sp,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .drawBehind {
                        // Playful crayon-red hand-drawn zigzag under the title
                        drawZigzagLine(
                            color = CrayonRed,
                            start = Offset(-16f, size.height + 8.dp.toPx()),
                            end = Offset(size.width + 16f, size.height + 8.dp.toPx()),
                            strokeWidth = 6f,
                            amplitude = 7f
                        )
                    }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Let's Choose!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CabinFamily,
                color = TextSecondary.copy(alpha = 0.8f),
                letterSpacing = 2.sp
            )
        }
    }
}
