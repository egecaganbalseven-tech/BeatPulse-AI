package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import kotlin.math.sin

@Composable
fun LiveWaveformView(
    samples: List<Float> = emptyList(),
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveformTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseAnimation"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CharcoalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E36)),
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2

            // Draw soft background grid lines
            for (i in 1..3) {
                val y = height * (i / 4f)
                drawLine(
                    color = Color(0xFF1E1E24),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val path = Path()
            val fillPath = Path()
            fillPath.moveTo(0f, height)

            val pointCount = 64
            val dx = width / pointCount

            for (i in 0..pointCount) {
                val x = i * dx
                val normX = i.toFloat() / pointCount

                val wave1 = sin(normX * 4 * Math.PI + phase).toFloat()
                val wave2 = sin(normX * 8 * Math.PI - phase * 1.5f).toFloat() * 0.4f

                val sampleAmplitude = if (samples.isNotEmpty()) {
                    val sampleIdx = (normX * (samples.size - 1)).toInt().coerceIn(0, samples.size - 1)
                    samples[sampleIdx] * 35.dp.toPx()
                } else {
                    if (isActive) 18.dp.toPx() else 6.dp.toPx()
                }

                val yOffset = (wave1 + wave2) * sampleAmplitude
                val y = centerY + yOffset

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Fill gradient under waveform
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        EmeraldPrimary.copy(alpha = if (isActive) 0.25f else 0.08f),
                        Color.Transparent
                    ),
                    startY = centerY - 30.dp.toPx(),
                    endY = height
                )
            )

            // Waveform line
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        EmeraldLight,
                        EmeraldPrimary,
                        CyanAccent,
                        EmeraldPrimary
                    )
                ),
                style = Stroke(
                    width = if (isActive) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
