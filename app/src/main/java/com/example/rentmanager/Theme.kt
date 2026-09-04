package com.example.rentmanager

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ==========================================
// COLOR PALETTE & STYLING
// ==========================================
val UIAppBg = Color(0xFFF8FAFC)
val UIBluePrimary = Color(0xFF0284C7)
val UIBlueGradientStart = Color(0xFF0284C7)
val UIBlueGradientEnd = Color(0xFF0369A1)
val UIDarkText = Color(0xFF1E293B)
val UIMutedText = Color(0xFF64748B)
val UICardBorder = Color(0xFFE2E8F0)
val UIGreenSuccess = Color(0xFF16A34A)
val UIRedDanger = Color(0xFFDC2626)

val CleanFont = FontFamily.SansSerif

private val AppColorScheme = lightColorScheme(
    primary = UIBluePrimary,
    background = UIAppBg,
    onBackground = UIDarkText,
    error = UIRedDanger
)

@Composable
fun RentManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}
