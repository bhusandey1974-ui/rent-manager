package com.example.rentmanager

data class Property(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class RateHistoryRecord(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val previousRent: Double = 0.0,
    val newRent: Double = 0.0,
    val previousElectricityRate: Double = 0.0,
    val newElectricityRate: Double = 0.0
)

data class Room(
    val id: String = "",
    val propertyId: String = "default_property",
    val roomNumber: String = "",
    val baseRent: Double = 0.0,
    val electricityRate: Double = 10.0,
    val initialMeterReading: Double = 0.0,
    val isOccupied: Boolean = false,
    val currentTenantId: String = "",
    val rateHistory: List<RateHistoryRecord> = emptyList()
)

data class Tenant(
    val id: String = "",
    val roomId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val aadhaarNumber: String = "",
    val permanentAddress: String = "",
    val moveInDate: Long = System.currentTimeMillis(),
    val moveOutDate: Long? = null,
    val isCurrent: Boolean = true
)

data class Bill(
    val id: String = "",
    val roomId: String = "",
    val tenantId: String = "",
    val billingPeriod: String = "",
    val previousReading: Double = 0.0,
    val currentReading: Double = 0.0,
    val unitsConsumed: Double = 0.0,
    val electricityRate: Double = 10.0,
    val electricityAmount: Double = 0.0,
    val baseRent: Double = 0.0,
    val maintenanceAmount: Double = 0.0,
    val totalPayable: Double = 0.0,
    val rentPaid: Double = 0.0,
    val electricityPaid: Double = 0.0,
    val amountPaid: Double = 0.0,
    val paymentMode: String = "Cash",
    val remainingDue: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
