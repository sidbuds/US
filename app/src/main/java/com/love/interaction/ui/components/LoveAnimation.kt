package com.love.interaction.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val startX: Float,
    val startY: Float,
    val speedX: Float,
    val speedY: Float,
    val size: Float,
    val rotation: Float,
    val color: Color
)

/**
 * Full-screen floating hearts/kisses animation.
 * @param type "hug" for hearts, "kiss" for lips
 */
@Composable
fun LoveAnimation(
    type: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "love_anim")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val particles = remember {
        List(30) {
            Particle(
                startX = Random.nextFloat(),
                startY = 1f + Random.nextFloat() * 0.3f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                speedY = -(0.3f + Random.nextFloat() * 0.5f),
                size = 20f + Random.nextFloat() * 30f,
                rotation = Random.nextFloat() * 360f,
                color = if (type == "hug") {
                    listOf(
                        Color(0xFFFF6B9D),
                        Color(0xFFFF8FAB),
                        Color(0xFFFFB1C1),
                        Color(0xFFE8537A),
                        Color(0xFFFFD700)
                    ).random()
                } else {
                    listOf(
                        Color(0xFFFF1744),
                        Color(0xFFFF4081),
                        Color(0xFFF50057),
                        Color(0xFFFF6D00),
                        Color(0xFFFFD740)
                    ).random()
                }
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val x = (p.startX + p.speedX * progress) * w
            val y = (p.startY + p.speedY * progress) * h
            val alpha = (1f - progress).coerceIn(0f, 1f)

            if (y > -50f && y < h + 50f) {
                rotate(degrees = p.rotation + progress * 360f, pivot = Offset(x, y)) {
                    if (type == "hug") {
                        drawHeart(Offset(x, y), p.size, p.color.copy(alpha = alpha))
                    } else {
                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = p.size / 2,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeart(
    center: Offset,
    size: Float,
    color: Color
) {
    val s = size / 2
    drawCircle(color = color, radius = s / 2, center = Offset(center.x - s / 3, center.y - s / 4))
    drawCircle(color = color, radius = s / 2, center = Offset(center.x + s / 3, center.y - s / 4))

    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y + s / 2)
        lineTo(center.x - s / 2, center.y - s / 8)
        lineTo(center.x, center.y - s / 4)
        lineTo(center.x + s / 2, center.y - s / 8)
        close()
    }
    drawPath(path, color)
}
