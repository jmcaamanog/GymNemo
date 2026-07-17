package com.jmcaamanog.gymnemo.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val GymNemoAccent = Color.White
val GymNemoBackground = Color.Black

private val GymNemoColorScheme = ColorScheme(
    primary = GymNemoAccent,
    onPrimary = Color.Black,
    secondary = Color(0xFF1C1C1C),
    onSecondary = Color.White,
    background = GymNemoBackground,
    onBackground = Color.White,
    surfaceContainer = Color(0xFF1C1C1C),
    onSurface = Color.White,
)

@Composable
fun GymNemoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GymNemoColorScheme,
        content = content
    )
}
