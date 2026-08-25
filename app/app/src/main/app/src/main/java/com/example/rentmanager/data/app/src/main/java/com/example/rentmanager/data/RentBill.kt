package com.example.rentmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rent_bills",
    foreignKeys = [
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tenantId")]
)
data class RentBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tenantId: Long,
    val monthYear: String,
    val baseRent: Double,
    val prevMeterReading: Double,
    val currMeterReading: Double,
    val unitsConsumed: Double,
    val electricityRate: Double,
    val electricityAmount: Double,
    val totalBillAmount: Double,
    val amountPaid: Double,
    val dueAmount: Double,
    val paymentDate: String,
    val paymentMode: String
)
