package com.vinav.helmet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ViNavDarkColorScheme = darkColorScheme(
    primary = ViNavPrimary,
    onPrimary = ViNavBackground,
    primaryContainer = ViNavPrimaryVariant,
    secondary = ViNavSecondary,
    background = ViNavBackground,
    surface = ViNavSurface,
    surfaceVariant = ViNavSurfaceVariant,
    onBackground = ViNavTextPrimary,
    onSurface = ViNavTextPrimary,
    onSurfaceVariant = ViNavTextSecondary,
    error = ViNavError,
    onError = ViNavTextPrimary
)

@Composable
fun ViNavTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ViNavDarkColorScheme,
        typography = ViNavTypography,
        content = content
    )
}
