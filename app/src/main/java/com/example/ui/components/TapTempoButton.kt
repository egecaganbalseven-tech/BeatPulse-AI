package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun TapTempoButton(
    onTap: () -> Unit,
    tapCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val buttonScale = remember { Animatable(1f) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CharcoalElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32323D)),
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = EmeraldLight),
                onClick = {
                    coroutineScope.launch {
                        buttonScale.animateTo(0.96f, tween(50))
                        buttonScale.animateTo(1f, tween(100))
                    }
                    onTap()
                }
            )
            .testTag("tap_tempo_button")
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = "Tap Tempo",
                tint = EmeraldPrimary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Tap Tempo",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            if (tapCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(EmeraldPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = "$tapCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLight,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
