package com.example.nfc_parking.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.nfc_parking.data.ThemeManager

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onBackground = DarkText,
    onSurface = DarkText
)

private val LightColorScheme = lightColorScheme(
    primary = NeonGreen,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.Black,
    onBackground = LightText,
    onSurface = LightText
)

@Composable
fun Nfc_parkingTheme(
    content: @Composable () -> Unit
) {
    // Get theme from ThemeManager
    val isDarkTheme by ThemeManager.isDarkTheme

    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}