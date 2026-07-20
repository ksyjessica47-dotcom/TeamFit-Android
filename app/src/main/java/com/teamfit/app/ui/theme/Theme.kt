package com.teamfit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TeamFitColors = lightColorScheme(
    primary = Color(0xFF5B4BDB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFEDFF),
    onPrimaryContainer = Color(0xFF35289A),
    secondary = Color(0xFF147A5A),
    background = Color(0xFFF5F6FA),
    surface = Color.White,
    onSurface = Color(0xFF172034),
    outline = Color(0xFFE2E5ED),
)

@Composable
fun TeamFitTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TeamFitColors, content = content)
}
