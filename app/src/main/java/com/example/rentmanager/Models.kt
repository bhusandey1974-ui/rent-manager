package com.example.rentmanager

data class RoomUnit(
    val id: String = "",
    val roomNumber: String = "",
    val baseRent: Double = 0.0,
    val electricityRate: Double = 0.0,
    val lastMeterReading: Double = 0.0,
    val isOccupied: Boolean = false,
    val currentTenantId: String = ""
)

data class Tenant(
    val id: String = "",
    val roomId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val aadhaarNumber: String = "",
    val address: String = "",
    val depositAmount: Double = 0.0,
    val initialReading: Double = 0.0,
    val moveInDate: String = "",
    val moveOutDate: String? = null,
    val isActive: Boolean = true
)

data class BillRecord(
    val id: String = "",
    val roomId: String = "",
    val tenantId: String = "",
    val monthYear: String = "",
    val baseRent: Double = 0.0,
    val prevMeterReading: Double = 0.0,
    val currentMeterReading: Double = 0.0,
    val electricityRate: Double = 0.0,
    val maintenanceCharge: Double = 0.0,
    val previousDueCarryover: Double = 0.0, // Positive = Due, Negative = Advance credit
    val amountPaid: Double = 0.0,
    val remainingDue: Double = 0.0,         // Positive = Remaining due, Negative = Carried forward advance
    val paymentMode: String = "Cash",
    val timestamp: Long = System.currentTimeMillis()
)
