package com.example.rentmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RentViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("RentManagerData", Context.MODE_PRIVATE)

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
        loadDataFromStorage()
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
        val prop = Property(
            name = name,
            address = address,
            city = city,
            ownerName = ownerName,
            ownerPhone = ownerPhone
        )
        _properties.update { it + prop }
        saveDataToStorage()
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
        saveDataToStorage()
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
                    room.copy(
                        roomNumber = newRoomNo,
                        baseRent = newRent,
                        electricityRate = newRate,
                        rentChangeLogs = room.rentChangeLogs + log
                    )
                } else room
            }
        }
        saveDataToStorage()
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
        saveDataToStorage()
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
        saveDataToStorage()
    }
        fun recordPayment(billId: String, payingAmount: Double, paymentMode: String) {
        val today = getTodayDateFormatted()
        _bills.update { list ->
            list.map { bill ->
                if (bill.id == billId) {
                    val updatedPaid = (bill.amountPaid + payingAmount).coerceAtMost(bill.totalAmount)
                    val paidInFull = updatedPaid >= bill.totalAmount
                    val newTx = PaymentTransaction(date = today, amount = payingAmount, paymentMode = paymentMode)
                    bill.copy(
                        amountPaid = updatedPaid,
                        paymentMode = paymentMode,
                        isPaid = paidInFull,
                        paymentTransactions = bill.paymentTransactions + newTx
                    )
                } else bill
            }
        }
        saveDataToStorage()
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
        saveDataToStorage()
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
            📅 *Billing Cycle:* ${bill.monthYear}
            --------------------------------
            💵 Base Rent: ₹${bill.baseRent.toInt()}
            ⚡ Units: ${bill.electricityUnitsUsed.toInt()} (${bill.prevMeterReading.toInt()} -> ${bill.currentMeterReading.toInt()})
            ⚡ Electricity Due: ₹${bill.electricityBill.toInt()} (@ ₹${bill.electricityRate}/unit)
            🛠️ Maintenance: ₹${bill.maintenanceCharge.toInt()}
            ${if (bill.previousDueCarryover > 0) "⚠️ Previous Unpaid Dues: ₹${bill.previousDueCarryover.toInt()}\n" else ""}--------------------------------
            💰 *TOTAL BILL: ₹${bill.totalAmount.toInt()}*
            💳 *PAID: ₹${bill.amountPaid.toInt()} via ${bill.paymentMode}*
            ${if (bill.remainingDue > 0) "⏳ *REMAINING DUE: ₹${bill.remainingDue.toInt()}*\n" else "✅ *FULLY PAID*\n"}
            _Generated via Rent Manager_
        """.trimIndent()
    }

    private fun saveDataToStorage() {
        try {
            val editor = prefs.edit()

            val propArr = JSONArray()
            _properties.value.forEach { p ->
                propArr.put(JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("address", p.address)
                    put("city", p.city)
                    put("ownerName", p.ownerName)
                    put("ownerPhone", p.ownerPhone)
                })
            }
            editor.putString("properties_json", propArr.toString())

            val roomArr = JSONArray()
            _rooms.value.forEach { r ->
                val logsArr = JSONArray()
                r.rentChangeLogs.forEach { log ->
                    logsArr.put(JSONObject().apply {
                        put("id", log.id)
                        put("dateChanged", log.dateChanged)
                        put("oldRent", log.oldRent)
                        put("newRent", log.newRent)
                        put("oldRate", log.oldRate)
                        put("newRate", log.newRate)
                    })
                }
                roomArr.put(JSONObject().apply {
                    put("id", r.id)
                    put("propertyId", r.propertyId)
                    put("roomNumber", r.roomNumber)
                    put("roomType", r.roomType)
                    put("baseRent", r.baseRent)
                    put("electricityRate", r.electricityRate)
                    put("isVacant", r.isVacant)
                    put("rentChangeLogs", logsArr)
                })
            }
            editor.putString("rooms_json", roomArr.toString())

            val tenantArr = JSONArray()
            _tenants.value.forEach { t ->
                tenantArr.put(JSONObject().apply {
                    put("id", t.id)
                    put("propertyId", t.propertyId)
                    put("roomId", t.roomId)
                    put("name", t.name)
                    put("phone", t.phone)
                    put("aadhaarNo", t.aadhaarNo)
                    put("moveInDate", t.moveInDate)
                    put("securityDeposit", t.securityDeposit)
                    put("initialMeterReading", t.initialMeterReading)
                })
            }
            editor.putString("tenants_json", tenantArr.toString())

            val historyArr = JSONArray()
            _tenantHistory.value.forEach { h ->
                historyArr.put(JSONObject().apply {
                    put("id", h.id)
                    put("roomId", h.roomId)
                    put("propertyId", h.propertyId)
                    put("tenantId", h.tenantId)
                    put("name", h.name)
                    put("phone", h.phone)
                    put("aadhaarNo", h.aadhaarNo)
                    put("moveInDate", h.moveInDate)
                    put("moveOutDate", h.moveOutDate)
                    put("formattedDuration", h.formattedDuration)
                    put("totalDaysStayed", h.totalDaysStayed)
                    put("totalRentPaidLifetime", h.totalRentPaidLifetime)
                    put("depositRefunded", h.depositRefunded)
                })
            }
            editor.putString("history_json", historyArr.toString())

            val billsArr = JSONArray()
            _bills.value.forEach { b ->
                val txArr = JSONArray()
                b.paymentTransactions.forEach { tx ->
                    txArr.put(JSONObject().apply {
                        put("id", tx.id)
                        put("date", tx.date)
                        put("amount", tx.amount)
                        put("paymentMode", tx.paymentMode)
                    })
                }
                billsArr.put(JSONObject().apply {
                    put("id", b.id)
                    put("propertyId", b.propertyId)
                    put("roomId", b.roomId)
                    put("tenantId", b.tenantId)
                    put("monthYear", b.monthYear)
                    put("baseRent", b.baseRent)
                    put("prevMeterReading", b.prevMeterReading)
                    put("currentMeterReading", b.currentMeterReading)
                    put("electricityRate", b.electricityRate)
                    put("maintenanceCharge", b.maintenanceCharge)
                    put("previousDueCarryover", b.previousDueCarryover)
                    put("amountPaid", b.amountPaid)
                    put("paymentMode", b.paymentMode)
                    put("isPaid", b.isPaid)
                    put("paymentTransactions", txArr)
                })
            }
            editor.putString("bills_json", billsArr.toString())
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDataFromStorage() {
        try {
            val propStr = prefs.getString("properties_json", null)
            if (!propStr.isNullOrEmpty()) {
                val arr = JSONArray(propStr)
                val list = mutableListOf<Property>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Property(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            address = obj.optString("address", ""),
                            city = obj.optString("city", ""),
                            ownerName = obj.optString("ownerName", ""),
                            ownerPhone = obj.optString("ownerPhone", "")
                        )
                    )
                }
                _properties.value = list
            }

            val roomsStr = prefs.getString("rooms_json", null)
            if (!roomsStr.isNullOrEmpty()) {
                val arr = JSONArray(roomsStr)
                val list = mutableListOf<RoomUnit>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val logs = mutableListOf<RentChangeLog>()
                    val logsArr = obj.optJSONArray("rentChangeLogs")
                    if (logsArr != null) {
                        for (j in 0 until logsArr.length()) {
                            val lObj = logsArr.getJSONObject(j)
                            logs.add(
                                RentChangeLog(
                                    id = lObj.getString("id"),
                                    dateChanged = lObj.getString("dateChanged"),
                                    oldRent = lObj.getDouble("oldRent"),
                                    newRent = lObj.getDouble("newRent"),
                                    oldRate = lObj.getDouble("oldRate"),
                                    newRate = lObj.getDouble("newRate")
                                )
                            )
                        }
                    }
                    list.add(
                        RoomUnit(
                            id = obj.getString("id"),
                            propertyId = obj.getString("propertyId"),
                            roomNumber = obj.getString("roomNumber"),
                            roomType = obj.optString("roomType", "Room"),
                            baseRent = obj.getDouble("baseRent"),
                            electricityRate = obj.optDouble("electricityRate", 10.0),
                            isVacant = obj.getBoolean("isVacant"),
                            rentChangeLogs = logs
                        )
                    )
                }
                _rooms.value = list
            }

            val tenantsStr = prefs.getString("tenants_json", null)
            if (!tenantsStr.isNullOrEmpty()) {
                val arr = JSONArray(tenantsStr)
                val list = mutableListOf<Tenant>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Tenant(
                            id = obj.getString("id"),
                            propertyId = obj.getString("propertyId"),
                            roomId = obj.getString("roomId"),
                            name = obj.getString("name"),
                            phone = obj.getString("phone"),
                            aadhaarNo = obj.optString("aadhaarNo", ""),
                            moveInDate = obj.getString("moveInDate"),
                            securityDeposit = obj.optDouble("securityDeposit", 0.0),
                            initialMeterReading = obj.optDouble("initialMeterReading", 0.0)
                        )
                    )
                }
                _tenants.value = list
            }

            val histStr = prefs.getString("history_json", null)
            if (!histStr.isNullOrEmpty()) {
                val arr = JSONArray(histStr)
                val list = mutableListOf<TenantHistoryRecord>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        TenantHistoryRecord(
                            id = obj.getString("id"),
                            roomId = obj.getString("roomId"),
                            propertyId = obj.getString("propertyId"),
                            tenantId = obj.getString("tenantId"),
                            name = obj.getString("name"),
                            phone = obj.getString("phone"),
                            aadhaarNo = obj.optString("aadhaarNo", ""),
                            moveInDate = obj.getString("moveInDate"),
                            moveOutDate = obj.getString("moveOutDate"),
                            formattedDuration = obj.getString("formattedDuration"),
                            totalDaysStayed = obj.getLong("totalDaysStayed"),
                            totalRentPaidLifetime = obj.getDouble("totalRentPaidLifetime"),
                            depositRefunded = obj.optDouble("depositRefunded", 0.0)
                        )
                    )
                }
                _tenantHistory.value = list
            }

            val billsStr = prefs.getString("bills_json", null)
            if (!billsStr.isNullOrEmpty()) {
                val arr = JSONArray(billsStr)
                val list = mutableListOf<BillRecord>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val txList = mutableListOf<PaymentTransaction>()
                    val txArr = obj.optJSONArray("paymentTransactions")
                    if (txArr != null) {
                        for (k in 0 until txArr.length()) {
                            val txObj = txArr.getJSONObject(k)
                            txList.add(
                                PaymentTransaction(
                                    id = txObj.getString("id"),
                                    date = txObj.getString("date"),
                                    amount = txObj.getDouble("amount"),
                                    paymentMode = txObj.getString("paymentMode")
                                )
                            )
                        }
                    }
                    list.add(
                        BillRecord(
                            id = obj.getString("id"),
                            propertyId = obj.getString("propertyId"),
                            roomId = obj.getString("roomId"),
                            tenantId = obj.getString("tenantId"),
                            monthYear = obj.getString("monthYear"),
                            baseRent = obj.getDouble("baseRent"),
                            prevMeterReading = obj.getDouble("prevMeterReading"),
                            currentMeterReading = obj.getDouble("currentMeterReading"),
                            electricityRate = obj.getDouble("electricityRate"),
                            maintenanceCharge = obj.optDouble("maintenanceCharge", 0.0),
                            previousDueCarryover = obj.optDouble("previousDueCarryover", 0.0),
                            amountPaid = obj.optDouble("amountPaid", 0.0),
                            paymentMode = obj.optString("paymentMode", "UPI"),
                            isPaid = obj.optBoolean("isPaid", false),
                            paymentTransactions = txList
                        )
                    )
                }
                _bills.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
