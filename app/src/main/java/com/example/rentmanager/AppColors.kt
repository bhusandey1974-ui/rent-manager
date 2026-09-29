package com.example.rentmanager

import androidx.compose.ui.graphics.Color

object AppColors {
    // Primary Brand & Forest Green Accents
    val AzurePrimary = Color(0xFF1F6D4C)
    val AzureDark = Color(0xFF0F3D2B)
    val AzureContainer = Color(0xFFE8F5EC)
    val AzureBorder = Color(0xFFBEE3CC)

    // Warm Cream Surfaces & Backgrounds
    val SurfaceWhite = Color(0xFFFFFFFF)
    val ScaffoldBackground = Color(0xFFF7F5EF)
    val SlateBackground = Color(0xFFEFEDE3)
    val BorderSubtle = Color(0xFFE4E1D6)
    val BorderStrong = Color(0xFFCFCBBC)
    val BorderFocus = Color(0xFF1F6D4C)

    // Typography
    val TextPrimary = Color(0xFF1B2E22)
    val TextSecondary = Color(0xFF5B6B5F)
    val TextMuted = Color(0xFF8B978B)
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
    val HistorySettledDot = Color(0xFFF59E0B) // Small amber/yellow dot for cleared past due
    val HistoryAdvanceDot = Color(0xFF1F6D4C) // Small forest-green dot for absorbed advance
    val HistoryContainer = Color(0xFFEFEDE3)
    val HistoryText = Color(0xFF5B6B5F)

    // Room Card Badges
    val RoomVacantContainer = Color(0xFFEFEDE3)
    val RoomVacantIcon = Color(0xFF5B6B5F)
    val RoomOccupiedContainer = Color(0xFFE8F5EC)
    val RoomOccupiedIcon = Color(0xFF1F6D4C)
}
