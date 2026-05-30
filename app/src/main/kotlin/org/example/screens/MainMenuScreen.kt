package org.example.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.components.DoodleCard
import org.example.components.NotebookBackground
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
                    emoji = "👆",
                    accentColor = NeonCyan,
                    rotation = -1.5f,
                    onClick = onNavigateToFingerChooser
                )

                Spacer(modifier = Modifier.height(24.dp))

                MenuCard(
                    title = "Custom Roulette",
                    subtitle = "Create your custom choices and spin the wheel of fortune!",
                    emoji = "🎡",
                    accentColor = NeonPurple,
                    rotation = 1.2f,
                    onClick = onNavigateToRoulette
                )

                Spacer(modifier = Modifier.height(24.dp))

                MenuCard(
                    title = "Coin Flip",
                    subtitle = "Quick 50:50 decisions with an interactive 3D coin!",
                    emoji = "🪙",
                    accentColor = NeonPink,
                    rotation = -1.0f,
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
    emoji: String,
    accentColor: Color,
    rotation: Float,
    onClick: () -> Unit
) {
    DoodleCard(
        backgroundColor = Color.White,
        rotation = rotation,
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
            // Playful crayon background bullet with emoji
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accentColor, RoundedCornerShape(12.dp))
                    .border(2.5.dp, BorderColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 28.sp
                )
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
