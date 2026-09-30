package com.example.rentmanager

import androidx.compose.ui.graphics.Color

object AppColors {
    // Primary Brand & Blue Accents
    val AzurePrimary = Color(0xFF0B5FCB)
    val AzureDark = Color(0xFF0A3E96)
    val AzureContainer = Color(0xFFE6F0FD)
    val AzureBorder = Color(0xFFBFD6F5)

    // Cool White Surfaces & Backgrounds
    val SurfaceWhite = Color(0xFFFFFFFF)
    val ScaffoldBackground = Color(0xFFF5F8FD)
    val SlateBackground = Color(0xFFEBF1F9)
    val BorderSubtle = Color(0xFFE1E8F2)
    val BorderStrong = Color(0xFFC8D3E3)
    val BorderFocus = Color(0xFF0B5FCB)

    // Typography
    val TextPrimary = Color(0xFF0F1F3D)
    val TextSecondary = Color(0xFF55627A)
    val TextMuted = Color(0xFF8792A8)
    val TextWhite = Color(0xFFFFFFFF)

    // Financial Indicators: Success, Paid, WhatsApp
    val EmeraldSuccess = Color(0xFF059669)
    val EmeraldContainer = Color(0xFFE7F6ED)
    val EmeraldBorder = Color(0xFFA9E6C0)
    val WhatsAppGreen = Color(0xFF25D366)
    val WhatsAppContainer = Color(0xFFE8F8EE)

    // Financial Indicators: Pending Dues & Warnings
    val AmberWarning = Color(0xFFD97706)
    val AmberContainer = Color(0xFFFEF3C7)
    val AmberBorder = Color(0xFFFDE68A)

    // Critical Alerts & Vacate Deductions
    val CrimsonAlert = Color(0xFFDC2626)
    val CrimsonContainer = Color(0xFFFEF2F2)
    val CrimsonBorder = Color(0xFFFECACA)

    // Historical Indicators (Settled Dues & Consumed Advances)
    val HistorySettledDot = Color(0xFFF59E0B)
    val HistoryAdvanceDot = Color(0xFF0B5FCB)
    val HistoryContainer = Color(0xFFEBF1F9)
    val HistoryText = Color(0xFF55627A)

    // Room Card Badges
    val RoomVacantContainer = Color(0xFFEBF1F9)
    val RoomVacantIcon = Color(0xFF55627A)
    val RoomOccupiedContainer = Color(0xFFE7F6ED)
    val RoomOccupiedIcon = Color(0xFF059669)

    // Header gradient (top -> bottom). Status bar uses HeaderTop so they blend.
    val HeaderTop = Color(0xFF0E4FB3)
    val HeaderBottom = Color(0xFF0A2F73)
}
