package com.example.rentmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class Property(
    val id: String = "default_prop",
    val name: String = "Rent Manager"
)

data class RoomUnit(
    val id: Long = 0,
    val propertyId: String = "default_prop",
    val roomNumber: String,
    val unitType: String = "Room",
    val baseRent: Double,
    val electricityRate: Double = 10.0,
    val isVacant: Boolean = true
)

data class Tenant(
    val id: Long = 0,
    val propertyId: String = "default_prop",
    val roomId: Long,
    val name: String,
    val phone: String,
    val aadhaarNumber: String = "",
    val moveInDate: String,
    val securityDeposit: Double = 0.0,
    val initialMeterReading: Double = 0.0,
    val isActive: Boolean = true
)

data class BillRecord(
    val id: Long = 0,
    val propertyId: String = "default_prop",
    val roomId: Long,
    val tenantId: Long,
    val monthYear: String,
    val baseRent: Double,
    val prevMeterReading: Double,
    val currentMeterReading: Double,
    val electricityRate: Double,
    val maintenanceCharge: Double = 0.0,
    val previousDueCarryover: Double = 0.0,
    val amountPaid: Double = 0.0,
    val remainingDue: Double = 0.0,
    val paymentMode: String = "Cash",
    val timestamp: Long = System.currentTimeMillis()
)

data class PastTenancyRecord(
    val id: Long = 0,
    val roomId: Long,
    val tenantName: String,
    val tenantPhone: String,
    val moveInDate: String,
    val vacateDate: String,
    val totalDaysStayed: Long,
    val totalPaid: Double
)

class RentViewModel(application: Application) : AndroidViewModel(application) {

    private val _rooms = MutableStateFlow<List<RoomUnit>>(emptyList())
    val rooms: StateFlow<List<RoomUnit>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    private val _pastTenancies = MutableStateFlow<List<PastTenancyRecord>>(emptyList())
    val pastTenancies: StateFlow<List<PastTenancyRecord>> = _pastTenancies.asStateFlow()

    private var roomCounter = 1L
    private var tenantCounter = 1L
    private var billCounter = 1L
    private var pastTenancyCounter = 1L

    fun getTodayDateFormatted(): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    fun getPreviousMonthFormatted(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun addRoom(propertyId: String, roomNumber: String, unitType: String, baseRent: Double, electricityRate: Double) {
        val newRoom = RoomUnit(
            id = roomCounter++,
            propertyId = propertyId,
            roomNumber = roomNumber,
            unitType = unitType,
            baseRent = baseRent,
            electricityRate = electricityRate,
            isVacant = true
        )
        _rooms.value = _rooms.value + newRoom
    }

    fun editRoom(roomId: Long, roomNumber: String, baseRent: Double, electricityRate: Double) {
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) it.copy(roomNumber = roomNumber, baseRent = baseRent, electricityRate = electricityRate) else it
        }
    }

    fun deleteRoom(roomId: Long) {
        _rooms.value = _rooms.value.filter { it.id != roomId }
        _tenants.value = _tenants.value.filter { it.roomId != roomId }
        _bills.value = _bills.value.filter { it.roomId != roomId }
        _pastTenancies.value = _pastTenancies.value.filter { it.roomId != roomId }
    }

    fun assignTenant(
        propertyId: String,
        roomId: Long,
        name: String,
        phone: String,
        aadhaar: String,
        moveInDate: String,
        securityDeposit: Double,
        initialReading: Double
    ) {
        val newTenant = Tenant(
            id = tenantCounter++,
            propertyId = propertyId,
            roomId = roomId,
            name = name,
            phone = phone,
            aadhaarNumber = aadhaar,
            moveInDate = moveInDate,
            securityDeposit = securityDeposit,
            initialMeterReading = initialReading,
            isActive = true
        )
        _tenants.value = _tenants.value.filter { it.roomId != roomId } + newTenant
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) it.copy(isVacant = false) else it
        }
    }

    fun editTenant(tenantId: Long, name: String, phone: String, aadhaar: String) {
        _tenants.value = _tenants.value.map {
            if (it.id == tenantId) it.copy(name = name, phone = phone, aadhaarNumber = aadhaar) else it
        }
    }
        fun checkoutTenant(tenantId: Long, vacateDate: String, refundAmount: Double) {
        val tenant = _tenants.value.find { it.id == tenantId } ?: return
        val roomBills = _bills.value.filter { it.tenantId == tenantId }
        val totalPaid = roomBills.sumOf { it.amountPaid }

        val totalDays = try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val d1 = sdf.parse(tenant.moveInDate)
            val d2 = sdf.parse(vacateDate)
            if (d1 != null && d2 != null) {
                val diff = d2.time - d1.time
                TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).coerceAtLeast(1)
            } else 1L
        } catch (e: Exception) {
            1L
        }

        val record = PastTenancyRecord(
            id = pastTenancyCounter++,
            roomId = tenant.roomId,
            tenantName = tenant.name,
            tenantPhone = tenant.phone,
            moveInDate = tenant.moveInDate,
            vacateDate = vacateDate,
            totalDaysStayed = totalDays,
            totalPaid = totalPaid
        )

        _pastTenancies.value = _pastTenancies.value + record
        _tenants.value = _tenants.value.filter { it.id != tenantId }
        _rooms.value = _rooms.value.map {
            if (it.id == tenant.roomId) it.copy(isVacant = true) else it
        }
    }

    fun lodgeBillAndPayment(
        propertyId: String,
        roomId: Long,
        tenantId: Long,
        month: String,
        baseRent: Double,
        prevUnit: Double,
        curUnit: Double,
        rate: Double,
        maintenance: Double,
        previousDue: Double,
        amountPaid: Double,
        paymentMode: String
    ) {
        val units = (curUnit - prevUnit).coerceAtLeast(0.0)
        val electricityTotal = units * rate
        val grandTotal = baseRent + electricityTotal + maintenance + previousDue
        val remaining = (grandTotal - amountPaid).coerceAtLeast(0.0)

        val bill = BillRecord(
            id = billCounter++,
            propertyId = propertyId,
            roomId = roomId,
            tenantId = tenantId,
            monthYear = month,
            baseRent = baseRent,
            prevMeterReading = prevUnit,
            currentMeterReading = curUnit,
            electricityRate = rate,
            maintenanceCharge = maintenance,
            previousDueCarryover = previousDue,
            amountPaid = amountPaid,
            remainingDue = remaining,
            paymentMode = paymentMode
        )
        _bills.value = _bills.value + bill
    }

    fun getCumulativePendingDue(roomId: Long): Double {
        val lastBill = _bills.value.filter { it.roomId == roomId }.maxByOrNull { it.id }
        return lastBill?.remainingDue ?: 0.0
    }

    fun resetAllRevenueData() {
        _bills.value = emptyList()
    }

    fun clearRoomHistory(roomId: Long) {
        _bills.value = _bills.value.filter { it.roomId != roomId }
        _pastTenancies.value = _pastTenancies.value.filter { it.roomId != roomId }
    }

    fun getWhatsAppReceiptText(bill: BillRecord, tenant: Tenant, property: Property, room: RoomUnit): String {
        val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
        val elecCharge = units * bill.electricityRate
        val total = bill.baseRent + elecCharge + bill.maintenanceCharge + bill.previousDueCarryover
        return """
            🧾 *RENT & UTILITY RECEIPT*
            --------------------------------
            🏠 *Room:* ${room.roomNumber}
            👤 *Tenant:* ${tenant.name}
            📅 *Month:* ${bill.monthYear}
            
            💵 *Base Rent:* ₹${"%,.2f".format(bill.baseRent)}
            ⚡ *Electricity:* $units kWh (@ ₹${bill.electricityRate}) = ₹${"%,.2f".format(elecCharge)}
            🛠 *Maintenance:* ₹${"%,.2f".format(bill.maintenanceCharge)}
            ⏳ *Previous Due:* ₹${"%,.2f".format(bill.previousDueCarryover)}
            --------------------------------
            💰 *Total Payable:* ₹${"%,.2f".format(total)}
            ✅ *Amount Paid:* ₹${"%,.2f".format(bill.amountPaid)}
            📌 *Remaining Balance:* ₹${"%,.2f".format(bill.remainingDue)}
            --------------------------------
            _Generated by Rent Manager_
        """.trimIndent()
    }
}

