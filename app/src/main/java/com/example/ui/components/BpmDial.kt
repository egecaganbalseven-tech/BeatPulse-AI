package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BpmDial(
    bpm: Int,
    onBpmChange: (Int) -> Unit,
    minBpm: Int = 40,
    maxBpm: Int = 260,
    modifier: Modifier = Modifier
) {
    val progress = ((bpm - minBpm).toFloat() / (maxBpm - minBpm)).coerceIn(0f, 1f)
    val startAngle = 135f
    val sweepTotal = 270f
    val currentAngle = startAngle + (progress * sweepTotal)

    val animatedAngle by animateFloatAsState(
        targetValue = currentAngle,
        animationSpec = tween(durationMillis = 100),
        label = "dialAngle"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(240.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touchPoint = change.position
                            val angleRad = atan2(touchPoint.y - center.y, touchPoint.x - center.x)
                            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            if (angleDeg < 0) angleDeg += 360f

                            // Map angle relative to startAngle (135 deg)
                            var relativeAngle = angleDeg - 135f
                            if (relativeAngle < 0) relativeAngle += 360f

                            if (relativeAngle <= 270f) {
                                val newProgress = (relativeAngle / 270f).coerceIn(0f, 1f)
                                val calculatedBpm = (minBpm + newProgress * (maxBpm - minBpm)).toInt()
                                onBpmChange(calculatedBpm)
                            }
                        }
                    }
                    .testTag("bpm_dial_canvas")
            ) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth - 32.dp.toPx()) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Background arc track
                drawArc(
                    color = Color(0xFF26262D),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active progress arc
                val activeSweep = (progress * 270f).coerceAtLeast(1f)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            EmeraldPrimary,
                            CyanAccent,
                            EmeraldLight
                        ),
                        center = center
                    ),
                    startAngle = 135f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Indicator thumb knob
                val angleRad = Math.toRadians(animatedAngle.toDouble())
                val thumbX = center.x + radius * cos(angleRad).toFloat()
                val thumbY = center.y + radius * sin(angleRad).toFloat()

                // Glow halo
                drawCircle(
                    color = Color(0x6600E676),
                    radius = strokeWidth * 1.3f,
                    center = Offset(thumbX, thumbY)
                )
                // Knob core
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.7f,
                    center = Offset(thumbX, thumbY)
                )
                drawCircle(
                    color = EmeraldPrimary,
                    radius = strokeWidth * 0.45f,
                    center = Offset(thumbX, thumbY)
                )

                // Tick center pointer
                val innerRadius = radius - 35.dp.toPx()
                val pointerX = center.x + innerRadius * cos(angleRad).toFloat()
                val pointerY = center.y + innerRadius * sin(angleRad).toFloat()
                drawLine(
                    color = EmeraldLight.copy(alpha = 0.5f),
                    start = Offset(center.x, center.y),
                    end = Offset(pointerX, pointerY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Central Dial Hub Info
            Surface(
                shape = CircleShape,
                color = CharcoalSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.size(140.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "$bpm",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "BPM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // Min / Max indicators
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .width(220.dp)
                .height(20.dp)
        ) {
            Text(
                text = "$minBpm",
                fontSize = 12.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "TEMPO",
                fontSize = 10.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "$maxBpm",
                fontSize = 12.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Plus / Minus fine stepper buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = { onBpmChange((bpm - 1).coerceAtLeast(minBpm)) },
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CharcoalElevated,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("bpm_minus_button")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "BPM Azalt")
            }

            // Quick 5 BPM steppers
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CharcoalElevated
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    FilledIconButton(
                        onClick = { onBpmChange((bpm - 5).coerceAtLeast(minBpm)) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextSecondary
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    FilledIconButton(
                        onClick = { onBpmChange((bpm + 5).coerceAtMost(maxBpm)) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextSecondary
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            FilledIconButton(
                onClick = { onBpmChange((bpm + 1).coerceAtMost(maxBpm)) },
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CharcoalElevated,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("bpm_plus_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "BPM Artır")
            }
        }
    }
}
