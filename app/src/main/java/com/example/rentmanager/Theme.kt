package com.example.rentmanager

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density

// Kept for any screens still referencing these names.
val UIAppBg = AppColors.ScaffoldBackground
val UIBluePrimary = AppColors.AzurePrimary
val UIBlueGradientStart = AppColors.AzurePrimary
val UIBlueGradientEnd = AppColors.AzureDark
val UIDarkText = AppColors.TextPrimary
val UIMutedText = AppColors.TextSecondary
val UICardBorder = AppColors.BorderSubtle
val UIGreenSuccess = AppColors.EmeraldSuccess
val UIRedDanger = AppColors.CrimsonAlert

val CleanFont = FontFamily.SansSerif

private val AppColorScheme = lightColorScheme(
    primary = AppColors.AzurePrimary,
    onPrimary = Color.White,
    background = AppColors.ScaffoldBackground,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.SurfaceWhite,
    onSurface = AppColors.TextPrimary,
    error = AppColors.CrimsonAlert
)

/** Highest system font scale the app will honour. 1.0f = always design size,
 *  so the UI matches the reference on every phone regardless of the
 *  "Font size" setting. Raise to 1.1f if you want a little accessibility room. */
private const val MAX_FONT_SCALE = 1.0f

@Composable
fun RentManagerTheme(content: @Composable () -> Unit) {
    val base = LocalDensity.current
    val capped = Density(
        density = base.density,
        fontScale = base.fontScale.coerceAtMost(MAX_FONT_SCALE)
    )
    CompositionLocalProvider(LocalDensity provides capped) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            content = content
        )
    }
}
