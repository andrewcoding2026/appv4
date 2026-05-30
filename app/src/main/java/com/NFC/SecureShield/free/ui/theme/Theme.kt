package com.NFC.SecureShield.free.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NFCSecurityColorScheme = darkColorScheme(
    primary           = NFCSecurityAccent,
    onPrimary         = NFCSecurityBg,
    primaryContainer  = NFCSecurityAccentSoft,
    onPrimaryContainer = NFCSecurityAccent,
    secondary         = NFCSecurityTextDim,
    onSecondary       = NFCSecurityBg,
    background        = NFCSecurityBg,
    onBackground      = NFCSecurityText,
    surface           = NFCSecuritySurface,
    onSurface         = NFCSecurityText,
    surfaceVariant    = NFCSecuritySurfaceAlt,
    onSurfaceVariant  = NFCSecurityTextDim,
    outline           = NFCSecurityBorder,
    outlineVariant    = NFCSecurityBorderHi,
    error             = NFCSecurityCrit,
    onError           = NFCSecurityBg,
)

@Composable
fun NFCSecurityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NFCSecurityColorScheme,
        typography = NFCSecurityType,
        content = content
    )
}
