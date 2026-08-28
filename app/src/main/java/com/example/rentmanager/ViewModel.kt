package com.example.rentmanager

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RentViewModel : ViewModel() {

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    private val _rooms = MutableStateFlow<List<RoomUnit>>(emptyList())
    val rooms: StateFlow<List<RoomUnit>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _tenantHistory = MutableStateFlow<List<TenantHistoryRecord>>(emptyList())
    val tenantHistory: StateFlow<List<TenantHistoryRecord>> = _tenantHistory.asStateFlow()

    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    fun getTodayDateFormatted(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun getPreviousMonthFormatted(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        return sdf.format(cal.time)
    }

    fun addProperty(
        name: String,
        address: String = "",
        city: String = "",
        ownerName: String = "",
        ownerPhone: String = ""
    ): String {
        val prop = Property(
            name = name,
            address = address,
            city = city,
            ownerName = ownerName,
            ownerPhone = ownerPhone
        )
        _properties.update { it + prop }
        return prop.id
    }

    fun addRoom(
        propertyId: String,
        roomNumber: String,
        roomType: String = "Room",
        baseRent: Double,
        rate: Double
    ) {
        val room = RoomUnit(
            propertyId = propertyId,
            roomNumber = roomNumber,
            roomType = roomType,
            baseRent = baseRent,
            electricityRate = rate,
            isVacant = true
        )
        _rooms.update { it + room }
    }

    fun editRoom(roomId: String, newRoomNo: String, newRent: Double, newRate: Double) {
        val currentDate = getTodayDateFormatted()
        _rooms.update { list ->
            list.map { room ->
                if (room.id == roomId) {
                    val log = RentChangeLog(
                        dateChanged = currentDate,
                        oldRent = room.baseRent,
                        newRent = newRent,
                        oldRate = room.electricityRate,
                        newRate = newRate
                    )
                    val updatedLogs = room.rentChangeLogs + log
                    room.copy(
                        roomNumber = newRoomNo,
                        baseRent = newRent,
                        electricityRate = newRate,
                        rentChangeLogs = updatedLogs
                    )
                } else room
            }
        }
    }

    fun assignTenant(
        propertyId: String,
        roomId: String,
        name: String,
        phone: String,
        aadhaar: String,
        moveInDate: String,
        deposit: Double,
        meterReading: Double
    ) {
        val tenant = Tenant(
            propertyId = propertyId,
            roomId = roomId,
            name = name,
            phone = phone,
            aadhaarNo = aadhaar,
            moveInDate = moveInDate,
            securityDeposit = deposit,
            initialMeterReading = meterReading
        )
        _tenants.update { it + tenant }
        _rooms.update { list ->
            list.map { if (it.id == roomId) it.copy(isVacant = false) else it }
        }
    }

    fun getCumulativePendingDue(roomId: String): Double {
        return _bills.value
            .filter { it.roomId == roomId && !it.isPaid }
            .sumOf { it.remainingDue }
    }

    fun generateBill(
        propertyId: String,
        roomId: String,
        tenantId: String,
        month: String,
        baseRent: Double,
        prevUnit: Double,
        curUnit: Double,
        rate: Double,
        maintenance: Double,
        previousDue: Double
    ) {
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
            amountPaid = 0.0,
            isPaid = false
        )
        _bills.update { it + bill }
    }

    fun recordPayment(billId: String, payingAmount: Double) {
        _bills.update { list ->
            list.map { bill ->
                if (bill.id == billId) {
                    val updatedPaid = (bill.amountPaid + payingAmount).coerceAtMost(bill.totalAmount)
                    val paidInFull = updatedPaid >= bill.totalAmount
                    bill.copy(amountPaid = updatedPaid, isPaid = paidInFull)
                } else bill
            }
        }
    }

    fun checkoutTenant(tenantId: String, moveOutDate: String, depositRefunded: Double) {
        val tenant = _tenants.value.find { it.id == tenantId } ?: return

        val lifetimePaid = _bills.value
            .filter { it.tenantId == tenantId }
            .sumOf { it.amountPaid }

        val (formattedDuration, totalDays) = calculateDetailedDuration(tenant.moveInDate, moveOutDate)

        val historyEntry = TenantHistoryRecord(
            roomId = tenant.roomId,
            propertyId = tenant.propertyId,
            tenantId = tenant.id,
            name = tenant.name,
            phone = tenant.phone,
            aadhaarNo = tenant.aadhaarNo,
            moveInDate = tenant.moveInDate,
            moveOutDate = moveOutDate,
            formattedDuration = formattedDuration,
            totalDaysStayed = totalDays,
            totalRentPaidLifetime = lifetimePaid,
            depositRefunded = depositRefunded
        )

        _tenantHistory.update { it + historyEntry }
        _tenants.update { list -> list.filter { it.id != tenantId } }
        _rooms.update { list ->
            list.map { if (it.id == tenant.roomId) it.copy(isVacant = true) else it }
        }
    }

    private fun calculateDetailedDuration(startDateStr: String, endDateStr: String): Pair<String, Long> {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val start = sdf.parse(startDateStr) ?: Date()
            val end = sdf.parse(endDateStr) ?: Date()

            val diffMillis = (end.time - start.time).coerceAtLeast(0)
            val totalDays = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS)

            val startCal = Calendar.getInstance().apply { time = start }
            val endCal = Calendar.getInstance().apply { time = end }

            var years = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
            var months = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)
            var days = endCal.get(Calendar.DAY_OF_MONTH) - startCal.get(Calendar.DAY_OF_MONTH)

            if (days < 0) {
                months -= 1
                val prevMonthCal = (startCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                days += prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            if (months < 0) {
                years -= 1
                months += 12
            }
            if (years < 0) {
                years = 0
                months = 0
                days = totalDays.toInt()
            }

            val parts = mutableListOf<String>()
            if (years > 0) parts.add("$years ${if (years == 1) "Year" else "Years"}")
            if (months > 0) parts.add("$months ${if (months == 1) "Month" else "Months"}")
            if (days > 0 || parts.isEmpty()) parts.add("$days ${if (days == 1) "Day" else "Days"}")

            Pair(parts.joinToString(", "), totalDays)
        } catch (e: Exception) {
            Pair("Duration unavailable", 0L)
        }
    }

    fun getWhatsAppReceiptText(bill: BillRecord, tenant: Tenant, property: Property, room: RoomUnit): String {
        return """
            🧾 *RENT INVOICE - RENT MANAGER*
            --------------------------------
            🏠 *Property:* ${property.name}
            🚪 *Room:* ${room.roomNumber}
            👤 *Tenant:* ${tenant.name}
            📅 *Billing Cycle (Completed Month):* ${bill.monthYear}
            --------------------------------
            💵 Base Rent: ₹${bill.baseRent.toInt()}
            ⚡ Units Used: ${bill.electricityUnitsUsed.toInt()} (${bill.prevMeterReading.toInt()} -> ${bill.currentMeterReading.toInt()})
            ⚡ Electricity Due: ₹${bill.electricityBill.toInt()} (@ ₹${bill.electricityRate}/unit)
            🛠️ Maintenance / Other: ₹${bill.maintenanceCharge.toInt()}
            ${if (bill.previousDueCarryover > 0) "⚠️ Previous Unpaid Dues: ₹${bill.previousDueCarryover.toInt()}\n" else ""}--------------------------------
            💰 *TOTAL BILL: ₹${bill.totalAmount.toInt()}*
            💳 *AMOUNT PAID: ₹${bill.amountPaid.toInt()}*
            ${if (bill.remainingDue > 0) "⏳ *REMAINING DUE: ₹${bill.remainingDue.toInt()}*\n" else "✅ *FULLY PAID*\n"}
            _Generated via Rent Manager_
        """.trimIndent()
    }
}
