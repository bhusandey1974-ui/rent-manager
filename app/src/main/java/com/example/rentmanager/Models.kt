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
    val previousDueCarryover: Double = 0.0,
    val amountPaid: Double = 0.0,
    val remainingDue: Double = 0.0,
    val paymentMode: String = "Cash",
    val timestamp: Long = System.currentTimeMillis()
)

data class MoveOutSettlement(
    val roomId: String = "",
    val tenantId: String = "",
    val tenantName: String = "",
    val moveInDate: String = "",
    val moveOutDate: String = "",
    val depositHeld: Double = 0.0,
    val unpaidDues: Double = 0.0,
    val finalMeterReading: Double = 0.0,
    val finalElectricityUnits: Double = 0.0,
    val finalElectricityCharge: Double = 0.0,
    val damageDeductions: Double = 0.0,
    val deductionReason: String = "",
    val netRefundAmount: Double = 0.0,
    val isTenantOwing: Boolean = false
)
