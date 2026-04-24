package com.flowerwine.taskmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F6DFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE7FF),
    onPrimaryContainer = Color(0xFF0E214C),
    secondary = Color(0xFF4C5D77),
    onSecondary = Color.White,
    background = Color(0xFFF5F7FB),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFEAF0F8),
    onSurfaceVariant = Color(0xFF5A6472),
    outline = Color(0xFFD9E1EC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9AB8FF),
    onPrimary = Color(0xFF06235A),
    primaryContainer = Color(0xFF183C85),
    onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = Color(0xFFB7C3D6),
    onSecondary = Color(0xFF223246),
    background = Color(0xFF11151C),
    onBackground = Color(0xFFF2F5F9),
    surface = Color(0xFF1A2029),
    onSurface = Color(0xFFF2F5F9),
    surfaceVariant = Color(0xFF24303D),
    onSurfaceVariant = Color(0xFFC5D1DF),
    outline = Color(0xFF3A4655),
)

val ColorScheme.memoryBlue: Color
    get() = Color(0xFF2F7AF8)

val ColorScheme.networkGreen: Color
    get() = Color(0xFF23B25F)

val ColorScheme.thermalOrange: Color
    get() = Color(0xFFFF7A00)

val ColorScheme.storagePurple: Color
    get() = Color(0xFF8257E6)

val ColorScheme.warningRed: Color
    get() = Color(0xFFE6495B)

@Composable
fun TaskManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
