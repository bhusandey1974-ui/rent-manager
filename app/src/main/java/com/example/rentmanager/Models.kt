package com.example.rentmanager

import java.util.UUID

data class Property(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String = "",
    val city: String = "",
    val ownerName: String = "",
    val ownerPhone: String = ""
)

data class RentChangeLog(
    val id: String = UUID.randomUUID().toString(),
    val dateChanged: String,
    val oldRent: Double,
    val newRent: Double,
    val oldRate: Double,
    val newRate: Double
)

data class RoomUnit(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val roomNumber: String,
    val roomType: String = "Room",
    val baseRent: Double,
    val electricityRate: Double = 10.0,
    val isVacant: Boolean = true,
    val rentChangeLogs: List<RentChangeLog> = emptyList()
)

data class Tenant(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val roomId: String,
    val name: String,
    val phone: String,
    val aadhaarNo: String = "",
    val moveInDate: String,
    val securityDeposit: Double = 0.0,
    val initialMeterReading: Double = 0.0
)

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
    val previousDueCarryover: Double = 0.0,
    val amountPaid: Double = 0.0,
    val isPaid: Boolean = false
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
    val roomId: String,
    val propertyId: String,
    val tenantId: String,
    val name: String,
    val phone: String,
    val aadhaarNo: String,
    val moveInDate: String,
    val moveOutDate: String,
    val formattedDuration: String,
    val totalDaysStayed: Long,
    val totalRentPaidLifetime: Double,
    val depositRefunded: Double
)
