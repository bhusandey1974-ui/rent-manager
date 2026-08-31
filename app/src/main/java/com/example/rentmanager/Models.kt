package com.example.rentmanager

import java.util.UUID

// ==========================================
// DATA MODELS
// ==========================================
data class Property(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "My Rental Property",
    val address: String = "Building 1"
)

data class RoomUnit(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String = "",
    val roomNumber: String = "",
    val unitType: String = "Standard Room",
    val baseRent: Double = 0.0,
    val electricityRate: Double = 10.0,
    val isVacant: Boolean = true
)

data class Tenant(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String = "",
    val roomId: String = "",
    val name: String = "",
    val phone: String = "",
    val aadhaarNumber: String = "",
    val moveInDate: String = "",
    val securityDeposit: Double = 0.0,
    val initialMeterReading: Double = 0.0,
    val isActive: Boolean = true
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
    val electricityRate: Double = 10.0,
    val maintenanceCharge: Double = 0.0,
    val previousDueCarryover: Double = 0.0,
    val amountPaid: Double = 0.0,
    val remainingDue: Double = 0.0,
    val paymentMode: String = "Cash",
    val timestamp: Long = System.currentTimeMillis()
)

data class PastTenancyRecord(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String = "",
    val tenantName: String = "",
    val tenantPhone: String = "",
    val moveInDate: String = "",
    val vacateDate: String = "",
    val totalDaysStayed: Long = 0,
    val totalPaid: Double = 0.0
)

