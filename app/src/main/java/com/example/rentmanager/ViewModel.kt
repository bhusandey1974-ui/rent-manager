package com.example.rentmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class RentViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("rent_manager_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val firestore = FirebaseFirestore.getInstance()
    private var currentUserId: String? = null

    private val _rooms = MutableStateFlow<List<RoomUnit>>(emptyList())
    val rooms: StateFlow<List<RoomUnit>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    private val _pastTenancies = MutableStateFlow<List<PastTenancyRecord>>(emptyList())
    val pastTenancies: StateFlow<List<PastTenancyRecord>> = _pastTenancies.asStateFlow()

    init {
        loadLocalState()
    }

    private fun loadLocalState() {
        val roomsJson = prefs.getString("rooms_data", null)
        val tenantsJson = prefs.getString("tenants_data", null)
        val billsJson = prefs.getString("bills_data", null)
        val pastJson = prefs.getString("past_tenancies_data", null)

        roomsJson?.let {
            val type = object : TypeToken<List<RoomUnit>>() {}.type
            _rooms.value = gson.fromJson(it, type) ?: emptyList()
        }
        tenantsJson?.let {
            val type = object : TypeToken<List<Tenant>>() {}.type
            _tenants.value = gson.fromJson(it, type) ?: emptyList()
        }
        billsJson?.let {
            val type = object : TypeToken<List<BillRecord>>() {}.type
            _bills.value = gson.fromJson(it, type) ?: emptyList()
        }
        pastJson?.let {
            val type = object : TypeToken<List<PastTenancyRecord>>() {}.type
            _pastTenancies.value = gson.fromJson(it, type) ?: emptyList()
        }
    }

    private fun saveLocalState() {
        prefs.edit()
            .putString("rooms_data", gson.toJson(_rooms.value))
            .putString("tenants_data", gson.toJson(_tenants.value))
            .putString("bills_data", gson.toJson(_bills.value))
            .putString("past_tenancies_data", gson.toJson(_pastTenancies.value))
            .apply()
    }

    // Cloud Persistence Handlers
    fun loadCloudData(userId: String) {
        currentUserId = userId
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val roomsRaw = doc.getString("rooms_json")
                    val tenantsRaw = doc.getString("tenants_json")
                    val billsRaw = doc.getString("bills_json")
                    val pastRaw = doc.getString("past_json")

                    roomsRaw?.let {
                        val type = object : TypeToken<List<RoomUnit>>() {}.type
                        _rooms.value = gson.fromJson(it, type) ?: emptyList()
                    }
                    tenantsRaw?.let {
                        val type = object : TypeToken<List<Tenant>>() {}.type
                        _tenants.value = gson.fromJson(it, type) ?: emptyList()
                    }
                    billsRaw?.let {
                        val type = object : TypeToken<List<BillRecord>>() {}.type
                        _bills.value = gson.fromJson(it, type) ?: emptyList()
                    }
                    pastRaw?.let {
                        val type = object : TypeToken<List<PastTenancyRecord>>() {}.type
                        _pastTenancies.value = gson.fromJson(it, type) ?: emptyList()
                    }
                    saveLocalState()
                } else {
                    pushDataToCloud()
                }
            }
    }

    fun pushDataToCloud() {
        val uid = currentUserId ?: return
        val data = hashMapOf(
            "rooms_json" to gson.toJson(_rooms.value),
            "tenants_json" to gson.toJson(_tenants.value),
            "bills_json" to gson.toJson(_bills.value),
            "past_json" to gson.toJson(_pastTenancies.value),
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid)
            .set(data, SetOptions.merge())
    }

    // Room Actions
    fun addRoom(propertyId: String, roomNumber: String, unitType: String, baseRent: Double, electricityRate: Double) {
        val newRoom = RoomUnit(
            id = UUID.randomUUID().toString(),
            propertyId = propertyId,
            roomNumber = roomNumber,
            unitType = unitType,
            baseRent = baseRent,
            electricityRate = electricityRate
        )
        _rooms.value = _rooms.value + newRoom
        saveLocalState()
        pushDataToCloud()
    }

    fun editRoom(roomId: String, roomNumber: String, baseRent: Double, electricityRate: Double) {
        _rooms.value = _rooms.value.map { room ->
            if (room.id == roomId) {
                room.copy(roomNumber = roomNumber, baseRent = baseRent, electricityRate = electricityRate)
            } else room
        }
        saveLocalState()
        pushDataToCloud()
    }

    fun deleteRoom(roomId: String) {
        _rooms.value = _rooms.value.filter { it.id != roomId }
        _tenants.value = _tenants.value.filter { it.roomId != roomId }
        _bills.value = _bills.value.filter { it.roomId != roomId }
        _pastTenancies.value = _pastTenancies.value.filter { it.roomId != roomId }
        saveLocalState()
        pushDataToCloud()
    }

    // Tenant Actions
    fun assignTenant(
        propertyId: String,
        roomId: String,
        name: String,
        phone: String,
        aadhaar: String,
        moveInDate: String,
        depositAmount: Double,
        initialMeterReading: Double
    ) {
        val newTenant = Tenant(
            id = UUID.randomUUID().toString(),
            propertyId = propertyId,
            roomId = roomId,
            name = name,
            phone = phone,
            aadhaarNumber = aadhaar,
            moveInDate = moveInDate,
            depositAmount = depositAmount,
            initialMeterReading = initialMeterReading,
            isActive = true
        )
        _tenants.value = _tenants.value + newTenant
        saveLocalState()
        pushDataToCloud()
    }

    fun editTenant(tenantId: String, name: String, phone: String, aadhaar: String) {
        _tenants.value = _tenants.value.map { tenant ->
            if (tenant.id == tenantId) {
                tenant.copy(name = name, phone = phone, aadhaarNumber = aadhaar)
            } else tenant
        }
        saveLocalState()
        pushDataToCloud()
    }

    fun checkoutTenant(tenantId: String, vacateDate: String, refundAmount: Double) {
        val currentTenant = _tenants.value.find { it.id == tenantId } ?: return
        val pastRecord = PastTenancyRecord(
            id = UUID.randomUUID().toString(),
            roomId = currentTenant.roomId,
            tenantName = currentTenant.name,
            phone = currentTenant.phone,
            moveInDate = currentTenant.moveInDate,
            vacateDate = vacateDate,
            depositReturned = refundAmount
        )
        _pastTenancies.value = _pastTenancies.value + pastRecord
        _tenants.value = _tenants.value.filter { it.id != tenantId }
        saveLocalState()
        pushDataToCloud()
    }

    // Billing Actions
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
        val electricityCharge = (curUnit - prevUnit).coerceAtLeast(0.0) * rate
        val totalCalculated = baseRent + electricityCharge + maintenance + previousDue
        val remainingDue = totalCalculated - amountPaid

        val bill = BillRecord(
            id = UUID.randomUUID().toString(),
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
            remainingDue = remainingDue,
            paymentMode = paymentMode,
            timestamp = System.currentTimeMillis()
        )
        _bills.value = _bills.value + bill
        saveLocalState()
        pushDataToCloud()
    }

    fun getCumulativePendingDue(roomId: String): Double {
        val lastBill = _bills.value.filter { it.roomId == roomId }.maxByOrNull { it.timestamp }
        return lastBill?.remainingDue ?: 0.0
    }

    fun clearRoomHistory(roomId: String) {
        _bills.value = _bills.value.filter { it.roomId != roomId }
        _pastTenancies.value = _pastTenancies.value.filter { it.roomId != roomId }
        saveLocalState()
        pushDataToCloud()
    }

    fun resetAllRevenueData() {
        _bills.value = emptyList()
        saveLocalState()
        pushDataToCloud()
    }

    // Utility Helpers
    fun getTodayDateFormatted(): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    fun getPreviousMonthFormatted(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun getWhatsAppReceiptText(
        bill: BillRecord,
        tenant: Tenant,
        property: Property,
        room: RoomUnit
    ): String {
        val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
        val electricityCost = units * bill.electricityRate
        val totalAmount = bill.baseRent + electricityCost + bill.maintenanceCharge + bill.previousDueCarryover

        return """
        *RENT RECEIPT - ${property.name}*
        ----------------------------------
        Tenant: ${tenant.name}
        Unit: Room ${room.roomNumber}
        Billing Period: ${bill.monthYear}

        Base Rent: ₹${"%,.2f".format(bill.baseRent)}
        Electricity: ₹${"%,.2f".format(electricityCost)} ($units units @ ₹${bill.electricityRate}/u)
        Maintenance: ₹${"%,.2f".format(bill.maintenanceCharge)}
        Previous Dues: ₹${"%,.2f".format(bill.previousDueCarryover)}
        ----------------------------------
        Total Payable: ₹${"%,.2f".format(totalAmount)}
        Amount Paid: ₹${"%,.2f".format(bill.amountPaid)} (${bill.paymentMode})
        Remaining Balance: ₹${"%,.2f".format(bill.remainingDue)}
        ----------------------------------
        Thank you for your prompt payment!
        """.trimIndent()
    }
}
