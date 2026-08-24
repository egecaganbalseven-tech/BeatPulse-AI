package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BeatPulseDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF003816),
    primaryContainer = Color(0xFF005324),
    onPrimaryContainer = EmeraldLight,
    secondary = MutedTeal,
    onSecondary = Color(0xFF003642),
    secondaryContainer = Color(0xFF004E5F),
    onSecondaryContainer = Color(0xFF70D7FF),
    tertiary = CyanAccent,
    onTertiary = Color(0xFF00363D),
    background = CharcoalBackground,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalElevated,
    onSurfaceVariant = TextSecondary,
    outline = CharcoalCardBorder,
    outlineVariant = Color(0xFF383842),
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BeatPulseDarkColorScheme,
        typography = Typography,
        content = content
    )
}
