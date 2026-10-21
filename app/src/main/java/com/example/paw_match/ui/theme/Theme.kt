package com.example.paw_match.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PawMatchColorScheme = lightColorScheme(
    primary = PawTealPrimary,
    onPrimary = PawWhite,
    background = PawBackgroundCream,
    onBackground = PawTextDark,
    surface = PawWhite,
    onSurface = PawTextDark,
    outline = PawBorderLight
)

@Composable
fun Paw_matchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PawMatchColorScheme,
        typography = Typography,
        content = content
    )
}
