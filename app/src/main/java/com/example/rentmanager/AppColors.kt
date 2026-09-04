package com.example.rentmanager

import androidx.compose.ui.graphics.Color

object AppColors {
    // Primary Brand & Electric Azure Accents
    val AzurePrimary = Color(0xFF0284FE)
    val AzureDark = Color(0xFF0066D6)
    val AzureContainer = Color(0xFFEDF5FF)
    val AzureBorder = Color(0xFFBFDBFE)

    // Crisp Surfaces & Backgrounds (Zero Lavender Tones)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val ScaffoldBackground = Color(0xFFF8FAFC)
    val SlateBackground = Color(0xFFF1F5F9)
    val BorderSubtle = Color(0xFFE2E8F0)
    val BorderStrong = Color(0xFFCBD5E1)
    val BorderFocus = Color(0xFF0284FE)

    // Typography
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF64748B)
    val TextMuted = Color(0xFF94A3B8)
    val TextWhite = Color(0xFFFFFFFF)

    // Financial Indicators: Success, Paid, WhatsApp
    val EmeraldSuccess = Color(0xFF059669)
    val EmeraldContainer = Color(0xFFECFDF5)
    val EmeraldBorder = Color(0xFFA7F3D0)
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
    val HistoryAdvanceDot = Color(0xFF0284FE) // Small azure dot for absorbed advance
    val HistoryContainer = Color(0xFFF1F5F9)
    val HistoryText = Color(0xFF64748B)

    // Room Card Badges
    val RoomVacantContainer = Color(0xFFF1F5F9)
    val RoomVacantIcon = Color(0xFF64748B)
    val RoomOccupiedContainer = Color(0xFFEDF5FF)
    val RoomOccupiedIcon = Color(0xFF0284FE)
}

