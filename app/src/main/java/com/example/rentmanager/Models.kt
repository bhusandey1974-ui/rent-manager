package com.example.rentmanager

import java.util.UUID

// Core Property Definition
data class Property(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val city: String,
    val ownerName: String,
    val ownerPhone: String
)

// Unit / Room Definition
data class RoomUnit(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val roomNumber: String,
    val roomType: String,
    val baseRent: Double,
    val electricityRate: Double = 10.0,
    val isVacant: Boolean = true
)

// Active Tenant Profile
data class Tenant(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val roomId: String,
    val name: String,
    val phone: String,
    val aadhaarNo: String,
    val moveInDate: String, // e.g. "01 Jan 2025"
    val securityDeposit: Double = 0.0,
    val initialMeterReading: Double = 0.0
)

// Complete Lifetime & Stay History for Past Tenants
data class TenantHistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String,
    val propertyId: String,
    val tenantId: String,
    val name: String,
    val phone: String,
    val aadhaarNo: String,
    val moveInDate: String,
    val moveOutDate: String,
    val formattedDuration: String, // e.g., "1 Year, 2 Months, 10 Days"
    val totalDaysStayed: Long,
    val totalRentPaidLifetime: Double, // Sum of all rent + electricity paid
    val depositRefunded: Double
)

// Monthly Ledger & Bill Statement
data class BillRecord(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val roomId: String,
    val tenantId: String,
    val monthYear: String,
    val baseRent: Double,
    val prevMeterReading: Double,
    val currentMeterReading: Double,
    val electricityRate: Double,
    val maintenanceCharge: Double = 0.0,
    val isPaid: Boolean = false
) {
    val electricityUnitsUsed: Double
        get() = (currentMeterReading - prevMeterReading).coerceAtLeast(0.0)

    val electricityBill: Double
        get() = electricityUnitsUsed * electricityRate

    val totalAmount: Double
        get() = baseRent + electricityBill + maintenanceCharge
}
