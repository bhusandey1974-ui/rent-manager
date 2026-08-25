package com.example.rentmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roomNumber: String,
    val phone: String,
    val defaultBaseRent: Double,
    val electricityRatePerUnit: Double,
    val lastMeterReading: Double = 0.0
)
