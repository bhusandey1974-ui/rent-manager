package com.example.rentmanager

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RentViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("RentManagerData", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _rooms = MutableStateFlow<List<RoomUnit>>(emptyList())
    val rooms: StateFlow<List<RoomUnit>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    private val _pastTenancies = MutableStateFlow<List<PastTenancyRecord>>(emptyList())
    val pastTenancies: StateFlow<List<PastTenancyRecord>> = _pastTenancies.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _rooms.value = loadList("ROOMS_KEY")
        _tenants.value = loadList("TENANTS_KEY")
        _bills.value = loadList("BILLS_KEY")
        _pastTenancies.value = loadList("PAST_KEY")
    }

    private fun saveData() {
        saveList("ROOMS_KEY", _rooms.value)
        saveList("TENANTS_KEY", _tenants.value)
        saveList("BILLS_KEY", _bills.value)
        saveList("PAST_KEY", _pastTenancies.value)
    }

    private inline fun <reified T> loadList(key: String): List<T> {
        val json = prefs.getString(key, "[]") ?: "[]"
        val type = object : TypeToken<List<T>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun <T> saveList(key: String, list: List<T>) {
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }

    fun getTodayDateFormatted(): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    fun getPreviousMonthFormatted(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }
        fun addRoom(propertyId: String, roomNumber: String, unitType: String, baseRent: Double, electricityRate: Double) {
        val newRoom = RoomUnit(
            propertyId = propertyId,
            roomNumber = roomNumber,
            unitType = unitType,
            baseRent = baseRent,
            electricityRate = electricityRate,
            isVacant = true
        )
        _rooms.value = _rooms.value + newRoom
        saveData()
    }

    fun editRoom(roomId: String, roomNumber: String, baseRent: Double, electricityRate: Double) {
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) it.copy(roomNumber = roomNumber, baseRent = baseRent, electricityRate = electricityRate) else it
        }
        saveData()
    }

    fun deleteRoom(roomId: String) {
        _rooms.value = _rooms.value.filter { it.id != roomId }
        _tenants.value = _tenants.value.filter { it.roomId != roomId }
        _bills.value = _bills.value.filter { it.roomId != roomId }
        _pastTenancies.value = _pastTenancies.value.filter { it.roomId != roomId }
        saveData()
    }

    fun assignTenant(
        propertyId: String,
        roomId: String,
        name: String,
        phone: String,
        aadhaar: String,
        moveInDate: String,
        securityDeposit: Double,
        initialReading: Double
    ) {
        val newTenant = Tenant(
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
        saveData()
    }

    fun editTenant(tenantId: String, name: String, phone: String, aadhaar: String) {
        _tenants.value = _tenants.value.map {
            if (it.id == tenantId) it.copy(name = name, phone = phone, aadhaarNumber = aadhaar) else it
        }
        saveData()
    }

    fun checkoutTenant(tenantId: String, vacateDate: String, refundAmount: Double) {
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
        saveData()
    }
        fun lodgeBillAndPayment(
        propertyId: String,
        roomId: String,
        tenantId: String,
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
        saveData()
    }

    fun getCumulativePendingDue(roomId: String): Double {
        val lastBill = _bills.value.filter { it.roomId == roomId }.maxByOrNull { it.timestamp }
        return lastBill?.remainingDue ?: 0.0
    }

    fun resetAllRevenueData() {
        _bills.value = emptyList()
        saveData()
    }

    fun clearRoomHistory(roomId: String) {
        _bills.value = _bills.value.filter { it.roomId != roomId }
        _pastTenancies.value = _pastTenancies.value.filter { it.roomId != roomId }
        saveData()
    }

    fun getWhatsAppReceiptText(bill: BillRecord, tenant: Tenant, property: Property, room: RoomUnit): String {
        val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
        val totalElec = units * bill.electricityRate
        val totalAmount = bill.baseRent + totalElec + bill.maintenanceCharge + bill.previousDueCarryover
        val paymentDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(bill.timestamp))

        return """
🏠 *RENT & ELECTRICITY RECEIPT*
━━━━━━━━━━━━━━━━━━━━━━━━━
👤 *Tenant:* ${tenant.name} (Room ${room.roomNumber})
📅 *Billing Period:* ${bill.monthYear}
🗓️ *Payment Date:* $paymentDate

⚡ *Electricity Details:*
• Previous Reading: ${bill.prevMeterReading}
• Current Reading: ${bill.currentMeterReading}
• Units Consumed: $units
• Rate / Unit: ₹${"%.2f".format(bill.electricityRate)}
• Total Electricity: ₹${"%.2f".format(totalElec)}

🏢 *Base Rent:* ₹${"%.2f".format(bill.baseRent)}
🧾 *Total Amount:* ₹${"%.2f".format(totalAmount)}
━━━━━━━━━━━━━━━━━━━━━━━━━
✅ *Amount Paid:* ₹${"%.2f".format(bill.amountPaid)} (${bill.paymentMode})
⚠️ *Pending Due:* ₹${"%.2f".format(bill.remainingDue)}
━━━━━━━━━━━━━━━━━━━━━━━━━
Thank you!
        """.trimIndent()
    }
}

