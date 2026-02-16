package com.animegen.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LofterLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF6C8A93),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF8FA9B0),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFF3B8B0),
    onTertiary = Color(0xFF3B2521),
    background = Color(0xFFFFFCFA),
    onBackground = Color(0xFF1F2A2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F2A2E),
    surfaceVariant = Color(0xFFF3F6F7),
    onSurfaceVariant = Color(0xFF6C7B80),
    outline = Color(0xFFD8E2E5)
)

@Composable
fun AnimeGenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LofterLightColors,
        content = content
    )
}
