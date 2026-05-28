package org.example.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.theme.*

@Composable
fun MainMenuScreen(
    onNavigateToFingerChooser: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    onNavigateToCoinFlip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(24.dp)
    ) {
        // Subtle cyber-grid or glowing light orbs behind menu
        GlowingCircleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(
                    text = "CATCHA",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 6.sp,
                    modifier = Modifier.drawBehind {
                        // Drawing a tiny neon glowing line under title
                    }
                )
                Text(
                    text = "Asisten Pengambil Keputusan Seru",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
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
                    title = "Pemilih Jari (Finger Chooser)",
                    subtitle = "Sentuh layar bersama dan tentukan siapa yang terpilih!",
                    accentColor = NeonCyan,
                    glowColor = NeonCyanGlow,
                    onClick = onNavigateToFingerChooser
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuCard(
                    title = "Roda Putar Custom (Roulette)",
                    subtitle = "Buat pilihan kustommu dan putar roda keberuntungan!",
                    accentColor = NeonPurple,
                    glowColor = NeonPurpleGlow,
                    onClick = onNavigateToRoulette
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuCard(
                    title = "Lempar Koin (Coin Flipper)",
                    subtitle = "Keputusan cepat 50:50 dengan 3D koin interaktif!",
                    accentColor = NeonPink,
                    glowColor = Color(0x66FF007F),
                    onClick = onNavigateToCoinFlip
                )
            }

            // Footer Section
            Text(
                text = "v1.0.0 • Let's Choose!",
                fontSize = 12.sp,
                color = TextSecondary.copy(alpha = 0.5f),
                fontWeight = FontWeight.Light,
                modifier = Modifier.navigationBarsPadding().padding(bottom = 10.dp)
            )
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    glowColor: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ObsidianSurface)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor.copy(alpha = borderAlpha), BorderColor)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // A decorative neon glowing dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(accentColor)
                        .drawBehind {
                            drawCircle(
                                color = glowColor,
                                radius = size.minDimension * 2f
                            )
                        }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun GlowingCircleBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bgOrb")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x2200F0FF), Color.Transparent),
                radius = 400f
            ),
            radius = 400f,
            center = Offset(size.width * 0.2f + offset1, size.height * 0.2f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x189D4EDD), Color.Transparent),
                radius = 500f
            ),
            radius = 500f,
            center = Offset(size.width * 0.8f - offset1, size.height * 0.7f)
        )
    }
}

// Inline helper for Offset
typealias Offset = androidx.compose.ui.geometry.Offset
