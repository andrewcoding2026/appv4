package com.nfc.security.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AegisColorScheme = darkColorScheme(
    primary           = AegisAccent,
    onPrimary         = AegisBg,
    primaryContainer  = AegisAccentSoft,
    onPrimaryContainer = AegisAccent,
    secondary         = AegisTextDim,
    onSecondary       = AegisBg,
    background        = AegisBg,
    onBackground      = AegisText,
    surface           = AegisSurface,
    onSurface         = AegisText,
    surfaceVariant    = AegisSurfaceAlt,
    onSurfaceVariant  = AegisTextDim,
    outline           = AegisBorder,
    outlineVariant    = AegisBorderHi,
    error             = AegisCrit,
    onError           = AegisBg,
)

@Composable
fun AegisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AegisColorScheme,
        typography = AegisType,
        content = content
    )
}
