package com.example.rentmanager

data class Property(
    val id: String = "default_prop",
    val name: String = "Main Complex",
    val address: String = "Building 1"
)

data class RoomUnit(
    val id: String = "",
    val propertyId: String = "",
    val roomNumber: String = "",
    val unitType: String = "Standard",
    val baseRent: Double = 0.0,
    val electricityRate: Double = 0.0
)

data class Tenant(
    val id: String = "",
    val propertyId: String = "",
    val roomId: String = "",
    val name: String = "",
    val phone: String = "",
    val aadhaarNumber: String = "",
    val moveInDate: String = "",
    val depositAmount: Double = 0.0,
    val initialMeterReading: Double = 0.0,
    val isActive: Boolean = true
)

data class BillRecord(
    val id: String = "",
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
    val remainingDue: Double = 0.0,
    val paymentMode: String = "Cash",
    val timestamp: Long = System.currentTimeMillis()
)

data class PastTenancyRecord(
    val id: String = "",
    val roomId: String = "",
    val tenantName: String = "",
    val phone: String = "",
    val moveInDate: String = "",
    val vacateDate: String = "",
    val depositReturned: Double = 0.0
)
