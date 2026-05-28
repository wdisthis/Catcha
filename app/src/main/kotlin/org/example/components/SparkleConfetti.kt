package org.example.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1f,
    val decay: Float = Random.nextFloat() * 0.02f + 0.01f
)

class ConfettiState {
    var particles = mutableStateListOf<ConfettiParticle>()

    fun spawn(x: Float, y: Float, count: Int = 40) {
        val colors = listOf(
            Color(0xFFFF007F), // Pink
            Color(0xFF00F0FF), // Cyan
            Color(0xFF9D4EDD), // Purple
            Color(0xFF39FF14), // Green
            Color(0xFFFFD700), // Gold
            Color(0xFFFF5722)  // Orange
        )
        
        repeat(count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 25f + 10f
            particles.add(
                ConfettiParticle(
                    x = x,
                    y = y,
                    vx = Math.cos(angle.toDouble()).toFloat() * speed,
                    vy = Math.sin(angle.toDouble()).toFloat() * speed - 5f, // shoot slightly upwards
                    color = colors.random(),
                    size = Random.nextFloat() * 12f + 6f
                )
            )
        }
    }

    fun update() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.8f // Gravity
            p.vx *= 0.98f // Air friction
            p.alpha -= p.decay
            if (p.alpha <= 0f) {
                iterator.remove()
            }
        }
    }
}

@Composable
fun SparkleConfetti(state: ConfettiState, modifier: Modifier = Modifier) {
    if (state.particles.isNotEmpty()) {
        LaunchedEffect(state.particles.size) {
            while (state.particles.isNotEmpty()) {
                state.update()
                delay(16) // ~60fps refresh
            }
        }

        Canvas(modifier = modifier.fillMaxSize()) {
            state.particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }
        }
    }
}
