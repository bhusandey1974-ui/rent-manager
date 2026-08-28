package com.example.rentmanager

import androidx.compose.ui.graphics.Color
import java.util.UUID

// --- COLOR PALETTE ---
val PageBackground = Color(0xFFF8FAFC)
val BrandBlue = Color(0xFF2563EB)
val BrandBlueDark = Color(0xFF1D4ED8)
val SkyBlueGradientStart = Color(0xFF38BDF8)
val SkyBlueGradientEnd = Color(0xFF0284C7)
val TextDark = Color(0xFF0F172A)
val TextMuted = Color(0xFF64748B)
val SuccessGreen = Color(0xFF16A34A)
val DangerRed = Color(0xFFDC2626)
val CardBorder = Color(0xFFE2E8F0)

// --- DATA CLASSES (Firestore Compatible) ---
data class Property(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val ownerName: String = "",
    val ownerPhone: String = ""
)

data class RentChangeLog(
    val dateChanged: String = "",
    val oldRent: Double = 0.0,
    val newRent: Double = 0.0,
    val oldRate: Double = 0.0,
    val newRate: Double = 0.0
)

data class RoomUnit(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String = "",
    val roomNumber: String = "",
    val roomType: String = "Room",
    val baseRent: Double = 0.0,
    val electricityRate: Double = 0.0,
    val isVacant: Boolean = true,
    val rentChangeLogs: List<RentChangeLog> = emptyList()
)

data class Tenant(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String = "",
    val roomId: String = "",
    val name: String = "",
    val phone: String = "",
    val aadhaarNo: String = "",
    val moveInDate: String = "",
    val securityDeposit: Double = 0.0,
    val initialMeterReading: Double = 0.0
)

data class PaymentTransaction(
    val id: String = UUID.randomUUID().toString(),
    val date: String = "",
    val amount: Double = 0.0,
    val paymentMode: String = "Cash"
)

data class BillRecord(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String = "",
    val roomId: String = "",
    val tenantId: String = "",
    val monthYear: String = "",
    val baseRent: Double = 0.0,
    val prevMeterReading: Double = 0.0,
    val currentMeterReading: Double = 0.0,
    val electricityRate: Double = 0.0,
    val maintenanceCharge: Double = 0.0,
    val previousDueCarryover: Double = 0.0,
    val amountPaid: Double = 0.0,
    val paymentMode: String = "Cash",
    val isPaid: Boolean = false,
    val paymentTransactions: List<PaymentTransaction> = emptyList()
) {
    val electricityUnitsUsed: Double
        get() = (currentMeterReading - prevMeterReading).coerceAtLeast(0.0)

    val electricityBill: Double
        get() = electricityUnitsUsed * electricityRate

    val totalAmount: Double
        get() = baseRent + electricityBill + maintenanceCharge + previousDueCarryover

    val remainingDue: Double
        get() = (totalAmount - amountPaid).coerceAtLeast(0.0)
}

data class TenantHistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String = "",
    val propertyId: String = "",
    val tenantId: String = "",
    val name: String = "",
    val phone: String = "",
    val aadhaarNo: String = "",
    val moveInDate: String = "",
    val moveOutDate: String = "",
    val formattedDuration: String = "",
    val totalDaysStayed: Long = 0L,
    val totalRentPaidLifetime: Double = 0.0,
    val depositRefunded: Double = 0.0
)

