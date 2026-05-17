package com.example.nammahasiru.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Accent,
    onSecondary = Surface,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Danger,
    onError = OnPrimary,
    outline = OnSurface.copy(alpha = 0.12f)
)

@Composable
fun NammaHasiruTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
