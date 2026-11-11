package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FFColorScheme = darkColorScheme(
    primary = CyberOrange,
    onPrimary = Color.White,
    primaryContainer = CyberOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = CyberCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = CyberCyan,
    tertiary = CyberYellow,
    onTertiary = Color.Black,
    background = GamingBackground,
    onBackground = TextPrimary,
    surface = GamingSurface,
    onSurface = TextPrimary,
    surfaceVariant = GamingSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = GamingBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun FFGeniusTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FFColorScheme,
        typography = Typography,
        content = content
    )
}
