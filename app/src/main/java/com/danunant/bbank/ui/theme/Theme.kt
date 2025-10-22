package com.danunant.bbank.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme // Import this
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light mode palette
private val LightColors = lightColorScheme(
    primary = Color(0xFF205B8F),
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    secondary = Color(0xFF2C74B3),
    onSecondary = Color.White,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF555555),
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFDE8E8), // Good for error backgrounds
    onErrorContainer = Color(0xFFB00020)
)

// Dark mode palette
private val DarkColors = darkColorScheme(
    primary = Color(0xFF5A9BDB), // Lighter blue for dark mode
    onPrimary = Color.Black,
    secondary = Color(0xFF3E6F9F),
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFAAAAAA),
    error = Color(0xFFFF5252),
    onError = Color.Black,
    errorContainer = Color(0xFF5C1F1F), // Good for error backgrounds
    onErrorContainer = Color(0xFFFFB2B2)
)


@Composable
fun BbankTheme(
    // Add this parameter
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Use the parameter here
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography, // Use local Typography
        content = content
    )
}