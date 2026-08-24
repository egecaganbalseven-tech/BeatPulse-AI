package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary

@Composable
fun EdgeGlowPulse(
    isPlaying: Boolean,
    isAccent: Boolean,
    beatCounter: Int,
    modifier: Modifier = Modifier
) {
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(beatCounter, isPlaying) {
        if (isPlaying) {
            val peakAlpha = if (isAccent) 0.35f else 0.18f
            glowAlpha.snapTo(peakAlpha)
            glowAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        } else {
            glowAlpha.snapTo(0f)
        }
    }

    if (glowAlpha.value > 0.01f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val glowWidth = 32.dp.toPx()
            val glowColor = if (isAccent) EmeraldPrimary else CyanAccent

            // Top edge glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        glowColor.copy(alpha = glowAlpha.value),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = glowWidth
                ),
                topLeft = Offset(0f, 0f),
                size = Size(width, glowWidth)
            )

            // Bottom edge glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        glowColor.copy(alpha = glowAlpha.value)
                    ),
                    startY = height - glowWidth,
                    endY = height
                ),
                topLeft = Offset(0f, height - glowWidth),
                size = Size(width, glowWidth)
            )

            // Left edge glow
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        glowColor.copy(alpha = glowAlpha.value),
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = glowWidth
                ),
                topLeft = Offset(0f, 0f),
                size = Size(glowWidth, height)
            )

            // Right edge glow
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        glowColor.copy(alpha = glowAlpha.value)
                    ),
                    startX = width - glowWidth,
                    endX = width
                ),
                topLeft = Offset(width - glowWidth, 0f),
                size = Size(glowWidth, height)
            )
        }
    }
}
