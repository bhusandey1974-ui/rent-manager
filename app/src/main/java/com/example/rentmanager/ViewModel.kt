package com.example.rentmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    val authRepo = AuthRepository()

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

    init {
        authRepo.getUserId()?.let { uid ->
            listenToUserData(uid)
        }
    }

    fun listenToUserData(userId: String) {
        val userDoc = db.collection("users").document(userId)

        userDoc.collection("properties").addSnapshotListener { snap, _ ->
            snap?.toObjects(Property::class.java)?.let { _properties.value = it }
        }
        userDoc.collection("rooms").addSnapshotListener { snap, _ ->
            snap?.toObjects(RoomUnit::class.java)?.let { _rooms.value = it }
        }
        userDoc.collection("tenants").addSnapshotListener { snap, _ ->
            snap?.toObjects(Tenant::class.java)?.let { _tenants.value = it }
        }
        userDoc.collection("bills").addSnapshotListener { snap, _ ->
            snap?.toObjects(BillRecord::class.java)?.let { _bills.value = it }
        }
        userDoc.collection("history").addSnapshotListener { snap, _ ->
            snap?.toObjects(TenantHistoryRecord::class.java)?.let { _tenantHistory.value = it }
        }
    }

    private fun persist(collectionName: String, docId: String, data: Any) {
        val userId = authRepo.getUserId() ?: return
        db.collection("users").document(userId)
            .collection(collectionName).document(docId)
            .set(data, SetOptions.merge())
    }

    private fun deleteDoc(collectionName: String, docId: String) {
        val userId = authRepo.getUserId() ?: return
        db.collection("users").document(userId)
            .collection(collectionName).document(docId)
            .delete()
    }

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
        val prop = Property(name = name, address = address, city = city, ownerName = ownerName, ownerPhone = ownerPhone)
        _properties.update { it + prop }
        persist("properties", prop.id, prop)
        return prop.id
    }

    fun addRoom(propertyId: String, roomNumber: String, roomType: String = "Room", baseRent: Double, rate: Double) {
        val room = RoomUnit(propertyId = propertyId, roomNumber = roomNumber, roomType = roomType, baseRent = baseRent, electricityRate = rate, isVacant = true)
        _rooms.update { it + room }
        persist("rooms", room.id, room)
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
                    val updated = room.copy(
                        roomNumber = newRoomNo,
                        baseRent = newRent,
                        electricityRate = newRate,
                        rentChangeLogs = room.rentChangeLogs + log
                    )
                    persist("rooms", updated.id, updated)
                    updated
                } else room
            }
        }
    }

    fun deleteRoom(roomId: String) {
        _rooms.update { list -> list.filter { it.id != roomId } }
        deleteDoc("rooms", roomId)
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
        persist("tenants", tenant.id, tenant)

        _rooms.update { list ->
            list.map {
                if (it.id == roomId) {
                    val updated = it.copy(isVacant = false)
                    persist("rooms", updated.id, updated)
                    updated
                } else it
            }
        }
    }

    fun getCumulativePendingDue(roomId: String): Double {
        return _bills.value
            .filter { it.roomId == roomId && !it.isPaid }
            .sumOf { it.remainingDue }
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
        val transactions = if (amountPaid > 0) {
            listOf(PaymentTransaction(date = getTodayDateFormatted(), amount = amountPaid, paymentMode = paymentMode))
        } else emptyList()

        val totalAmount = baseRent + ((curUnit - prevUnit).coerceAtLeast(0.0) * rate) + maintenance + previousDue
        val isPaid = amountPaid >= totalAmount

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
            paymentMode = paymentMode,
            isPaid = isPaid,
            paymentTransactions = transactions
        )
        _bills.update { it + bill }
        persist("bills", bill.id, bill)
    }

    fun recordPayment(billId: String, payingAmount: Double, paymentMode: String) {
        val today = getTodayDateFormatted()
        _bills.update { list ->
            list.map { bill ->
                if (bill.id == billId) {
                    val updatedPaid = (bill.amountPaid + payingAmount).coerceAtMost(bill.totalAmount)
                    val paidInFull = updatedPaid >= bill.totalAmount
                    val newTx = PaymentTransaction(date = today, amount = payingAmount, paymentMode = paymentMode)
                    val updated = bill.copy(
                        amountPaid = updatedPaid,
                        paymentMode = paymentMode,
                        isPaid = paidInFull,
                        paymentTransactions = bill.paymentTransactions + newTx
                    )
                    persist("bills", updated.id, updated)
                    updated
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
        persist("history", historyEntry.id, historyEntry)

        _tenants.update { list -> list.filter { it.id != tenantId } }
        deleteDoc("tenants", tenant.id)

        _rooms.update { list ->
            list.map {
                if (it.id == tenant.roomId) {
                    val updated = it.copy(isVacant = true)
                    persist("rooms", updated.id, updated)
                    updated
                } else it
            }
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
        val dateOfPayment = bill.paymentTransactions.lastOrNull()?.date ?: getTodayDateFormatted()
        val formattedElecCost = String.format(Locale.ENGLISH, "%.2f", bill.electricityBill)
        val formattedRate = String.format(Locale.ENGLISH, "%.2f", bill.electricityRate)
        val formattedBaseRent = String.format(Locale.ENGLISH, "%.2f", bill.baseRent)
        val formattedTotal = String.format(Locale.ENGLISH, "%.2f", bill.totalAmount)
        val formattedPaid = String.format(Locale.ENGLISH, "%.2f", bill.amountPaid)
        val formattedDue = String.format(Locale.ENGLISH, "%.2f", bill.remainingDue)

        val pendingDueLine = if (bill.remainingDue > 0) {
            "⚠️ *Pending Due:* ₹$formattedDue\n"
        } else {
            "🎉 *Status:* Fully Paid\n"
        }

        return """
🏠 *RENT & ELECTRICITY RECEIPT*
━━━━━━━━━━━━━━━━━━━━━━━━━
🏢 *Property:* ${property.name}
👤 *Tenant:* ${tenant.name} (${room.roomType} ${room.roomNumber})
📅 *Billing Month:* ${bill.monthYear}
🗓️ *Payment Date:* $dateOfPayment

⚡ *Electricity:*
• Prev Reading: ${bill.prevMeterReading}
• Current Reading: ${bill.currentMeterReading}
• Units: ${bill.electricityUnitsUsed} @ ₹$formattedRate
• Total Electricity: ₹$formattedElecCost

🏢 *Base Rent:* ₹$formattedBaseRent
🛠️ *Maintenance:* ₹${bill.maintenanceCharge}
🔄 *Previous Due:* ₹${bill.previousDueCarryover}
🧾 *Grand Total:* ₹$formattedTotal
━━━━━━━━━━━━━━━━━━━━━━━━━
✅ *Paid:* ₹$formattedPaid (${bill.paymentMode})
$pendingDueLine━━━━━━━━━━━━━━━━━━━━━━━━━
Thank you!
        """.trimIndent()
    }
}
