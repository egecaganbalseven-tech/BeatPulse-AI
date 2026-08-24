package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun VisualPulseBar(
    currentBeat: Int,
    totalBeats: Int,
    isAccent: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.3f,
        animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
        label = "pulseAlpha"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CharcoalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E36)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Visual Pulse",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                Text(
                    text = if (isPlaying) "Vuruş: $currentBeat / $totalBeats" else "Hazır",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAccent && isPlaying) EmeraldPrimary else TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glowing pulse orb / horizontal light bar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF16161A))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2

                    if (isPlaying) {
                        val activeColor = if (isAccent) EmeraldPrimary else CyanAccent
                        val centerColor = if (isAccent) Color.White else EmeraldLight

                        // Horizontal glowing gradient beam
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    activeColor.copy(alpha = 0.35f * pulseAlpha),
                                    activeColor.copy(alpha = 0.85f * pulseAlpha),
                                    activeColor.copy(alpha = 0.35f * pulseAlpha),
                                    Color.Transparent
                                )
                            )
                        )

                        // Center glowing orb
                        val orbRadius = if (isAccent) 14.dp.toPx() else 10.dp.toPx()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    centerColor,
                                    activeColor,
                                    Color.Transparent
                                ),
                                center = Offset(width / 2, centerY),
                                radius = orbRadius * 2
                            ),
                            radius = orbRadius * 2,
                            center = Offset(width / 2, centerY)
                        )
                    } else {
                        // Inactive soft line
                        drawLine(
                            color = Color(0xFF26262B),
                            start = Offset(width * 0.2f, centerY),
                            end = Offset(width * 0.8f, centerY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Beat indicator dots
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (beat in 1..totalBeats) {
                    val isCurrent = (beat == currentBeat && isPlaying)
                    val isFirstBeat = (beat == 1)

                    val dotColor = when {
                        isCurrent && isFirstBeat -> EmeraldPrimary
                        isCurrent -> CyanAccent
                        isFirstBeat -> Color(0xFF383842)
                        else -> Color(0xFF26262E)
                    }

                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 14.dp else 10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(
                                width = if (isCurrent) 2.dp else 0.dp,
                                color = if (isCurrent) Color.White.copy(alpha = 0.7f) else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}
