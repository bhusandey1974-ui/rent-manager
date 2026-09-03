package com.example.rentmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.UUID

class RentViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val prefs = context.getSharedPreferences("rent_manager_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var activeUserId: String = auth.currentUser?.uid.orEmpty()

    // State flows
    private val _properties = MutableStateFlow<List<Property>>(listOf(Property()))
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    private val _rooms = MutableStateFlow<List<RoomUnit>>(emptyList())
    val rooms: StateFlow<List<RoomUnit>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    private val _pastTenancies = MutableStateFlow<List<PastTenancyRecord>>(emptyList())
    val pastTenancies: StateFlow<List<PastTenancyRecord>> = _pastTenancies.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        loadLocalData()
        if (activeUserId.isNotBlank()) {
            loadCloudData(activeUserId)
        }
    }

    fun setUserId(uid: String) {
        activeUserId = uid
        loadCloudData(uid)
    }

    // ==========================================
    // LOCAL STORAGE (PREFERENCES)
    // ==========================================
    private fun loadLocalData() {
        val roomsJson = prefs.getString("rooms_cache", null)
        val tenantsJson = prefs.getString("tenants_cache", null)
        val billsJson = prefs.getString("bills_cache", null)
        val pastJson = prefs.getString("past_cache", null)

        if (!roomsJson.isNullOrBlank()) {
            val type = object : TypeToken<List<RoomUnit>>() {}.type
            _rooms.value = gson.fromJson(roomsJson, type) ?: emptyList()
        }
        if (!tenantsJson.isNullOrBlank()) {
            val type = object : TypeToken<List<Tenant>>() {}.type
            _tenants.value = gson.fromJson(tenantsJson, type) ?: emptyList()
        }
        if (!billsJson.isNullOrBlank()) {
            val type = object : TypeToken<List<BillRecord>>() {}.type
            _bills.value = gson.fromJson(billsJson, type) ?: emptyList()
        }
        if (!pastJson.isNullOrBlank()) {
            val type = object : TypeToken<List<PastTenancyRecord>>() {}.type
            _pastTenancies.value = gson.fromJson(pastJson, type) ?: emptyList()
        }
    }

    private fun persistLocally() {
        prefs.edit()
            .putString("rooms_cache", gson.toJson(_rooms.value))
            .putString("tenants_cache", gson.toJson(_tenants.value))
            .putString("bills_cache", gson.toJson(_bills.value))
            .putString("past_cache", gson.toJson(_pastTenancies.value))
            .apply()
    }

    // ==========================================
    // CLOUD SYNC (FIRESTORE)
    // ==========================================
    fun loadCloudData(uid: String) {
        if (uid.isBlank()) return
        activeUserId = uid
        _isSyncing.value = true

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _isSyncing.value = false
                if (doc != null && doc.exists()) {
                    val rJson = doc.getString("rooms_json")
                    val tJson = doc.getString("tenants_json")
                    val bJson = doc.getString("bills_json")
                    val pJson = doc.getString("past_json")

                    if (!rJson.isNullOrBlank()) {
                        val type = object : TypeToken<List<RoomUnit>>() {}.type
                        _rooms.value = gson.fromJson(rJson, type) ?: emptyList()
                    }
                    if (!tJson.isNullOrBlank()) {
                        val type = object : TypeToken<List<Tenant>>() {}.type
                        _tenants.value = gson.fromJson(tJson, type) ?: emptyList()
                    }
                    if (!bJson.isNullOrBlank()) {
                        val type = object : TypeToken<List<BillRecord>>() {}.type
                        _bills.value = gson.fromJson(bJson, type) ?: emptyList()
                    }
                    if (!pJson.isNullOrBlank()) {
                        val type = object : TypeToken<List<PastTenancyRecord>>() {}.type
                        _pastTenancies.value = gson.fromJson(pJson, type) ?: emptyList()
                    }
                    persistLocally()
                } else {
                    syncToCloud()
                }
            }
            .addOnFailureListener {
                _isSyncing.value = false
            }
    }

    private fun syncToCloud() {
        persistLocally()
        if (activeUserId.isBlank()) return

        val payload = hashMapOf(
            "rooms_json" to gson.toJson(_rooms.value),
            "tenants_json" to gson.toJson(_tenants.value),
            "bills_json" to gson.toJson(_bills.value),
            "past_json" to gson.toJson(_pastTenancies.value),
            "last_updated" to System.currentTimeMillis()
        )

        firestore.collection("users").document(activeUserId)
            .set(payload, SetOptions.merge())
    }
        // ==========================================
    // ROOM & TENANT OPERATIONS
    // ==========================================
    fun addRoom(roomNumber: String, baseRent: Double, electricityRate: Double) {
        val newRoom = RoomUnit(
            id = UUID.randomUUID().toString(),
            propertyId = "default_prop",
            roomNumber = roomNumber,
            baseRent = baseRent,
            electricityRate = electricityRate
        )
        _rooms.value = _rooms.value + newRoom
        syncToCloud()
    }

    fun updateRoom(roomId: String, roomNumber: String, baseRent: Double, electricityRate: Double) {
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) it.copy(roomNumber = roomNumber, baseRent = baseRent, electricityRate = electricityRate)
            else it
        }
        syncToCloud()
    }

    fun deleteRoom(roomId: String) {
        _rooms.value = _rooms.value.filterNot { it.id == roomId }
        _tenants.value = _tenants.value.filterNot { it.roomId == roomId }
        _bills.value = _bills.value.filterNot { it.roomId == roomId }
        _pastTenancies.value = _pastTenancies.value.filterNot { it.roomId == roomId }
        syncToCloud()
    }

    fun assignTenant(
        roomId: String,
        name: String,
        phone: String,
        aadhaarNumber: String,
        address: String,
        depositAmount: Double,
        initialReading: Double,
        moveInDate: String
    ) {
        val newTenant = Tenant(
            id = UUID.randomUUID().toString(),
            propertyId = "default_prop",
            roomId = roomId,
            name = name,
            phone = phone,
            aadhaarNumber = aadhaarNumber,
            address = address,
            moveInDate = moveInDate,
            depositAmount = depositAmount,
            initialMeterReading = initialReading,
            isActive = true
        )
        _tenants.value = _tenants.value.filterNot { it.roomId == roomId && it.isActive } + newTenant
        syncToCloud()
    }

    fun vacateTenant(roomId: String, vacateDate: String, refundAmount: Double) {
        val activeTenant = _tenants.value.find { it.roomId == roomId && it.isActive } ?: return

        val pastRecord = PastTenancyRecord(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            tenantName = activeTenant.name,
            phone = activeTenant.phone,
            aadhaarNumber = activeTenant.aadhaarNumber,
            address = activeTenant.address,
            moveInDate = activeTenant.moveInDate,
            vacateDate = vacateDate,
            depositReturned = refundAmount
        )

        _pastTenancies.value = _pastTenancies.value + pastRecord
        _tenants.value = _tenants.value.filterNot { it.id == activeTenant.id }
        syncToCloud()
    }

    // ==========================================
    // BILLING & DUES LOGIC
    // ==========================================
    fun getLatestMeterReading(roomId: String): Double {
        val roomBills = _bills.value.filter { it.roomId == roomId }.sortedByDescending { it.timestamp }
        if (roomBills.isNotEmpty()) {
            return roomBills.first().currentMeterReading
        }
        val tenant = _tenants.value.find { it.roomId == roomId && it.isActive }
        return tenant?.initialMeterReading ?: 0.0
    }

    fun getPendingDueForRoom(roomId: String): Double {
        val roomBills = _bills.value.filter { it.roomId == roomId }.sortedByDescending { it.timestamp }
        return if (roomBills.isNotEmpty()) {
            roomBills.first().remainingDue
        } else {
            0.0
        }
    }

    fun lodgeBill(
        roomId: String,
        tenantId: String,
        monthYear: String,
        baseRent: Double,
        prevReading: Double,
        curReading: Double,
        rate: Double,
        maintenance: Double,
        carryoverDue: Double,
        amountPaid: Double,
        paymentMode: String
    ) {
        val consumed = (curReading - prevReading).coerceAtLeast(0.0)
        val electricityCost = consumed * rate
        val totalCalculated = baseRent + electricityCost + maintenance + carryoverDue
        val remainingDue = (totalCalculated - amountPaid).coerceAtLeast(0.0)

        val newBill = BillRecord(
            id = UUID.randomUUID().toString(),
            propertyId = "default_prop",
            roomId = roomId,
            tenantId = tenantId,
            monthYear = monthYear,
            baseRent = baseRent,
            prevMeterReading = prevReading,
            currentMeterReading = curReading,
            electricityRate = rate,
            maintenanceCharge = maintenance,
            previousDueCarryover = carryoverDue,
            amountPaid = amountPaid,
            remainingDue = remainingDue,
            paymentMode = paymentMode,
            timestamp = System.currentTimeMillis()
        )

        _bills.value = listOf(newBill) + _bills.value
        syncToCloud()
    }

    // ==========================================
    // METRICS CALCULATIONS
    // ==========================================
    fun calculateTotalOutstandingDues(): Double {
        return _rooms.value.sumOf { getPendingDueForRoom(it.id) }
    }

    fun calculateLifetimeRevenue(): Double {
        return _bills.value.sumOf { it.amountPaid }
    }

    fun calculateLifetimeRentEarnings(): Double {
        return _bills.value.sumOf { it.baseRent }
    }

    fun calculateLifetimeElectricityRevenue(): Double {
        return _bills.value.sumOf {
            val units = (it.currentMeterReading - it.prevMeterReading).coerceAtLeast(0.0)
            units * it.electricityRate
        }
    }

    private fun isCurrentYear(timestamp: Long): Boolean {
        val calCurrent = Calendar.getInstance()
        val calItem = Calendar.getInstance().apply { timeInMillis = timestamp }
        return calCurrent.get(Calendar.YEAR) == calItem.get(Calendar.YEAR)
    }

    fun calculateYearlyRevenue(): Double {
        return _bills.value.filter { isCurrentYear(it.timestamp) }.sumOf { it.amountPaid }
    }

    fun calculateYearlyRentEarnings(): Double {
        return _bills.value.filter { isCurrentYear(it.timestamp) }.sumOf { it.baseRent }
    }

    fun calculateYearlyElectricityRevenue(): Double {
        return _bills.value.filter { isCurrentYear(it.timestamp) }.sumOf {
            val units = (it.currentMeterReading - it.prevMeterReading).coerceAtLeast(0.0)
            units * it.electricityRate
        }
    }

    fun resolveTenantName(tenantId: String, roomId: String): String {
        val active = _tenants.value.find { it.id == tenantId || (it.roomId == roomId && it.isActive) }
        if (active != null) return active.name

        val past = _pastTenancies.value.find { it.roomId == roomId }
        if (past != null) return past.tenantName

        return "Tenant"
    }

    // ==========================================
    // ACCOUNT & RESET ACTIONS
    // ==========================================
    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        activeUserId = ""
        onComplete()
    }

    fun clearAllUserData(onComplete: (Boolean) -> Unit) {
        val uid = activeUserId
        if (uid.isNotBlank()) {
            firestore.collection("users").document(uid).delete()
                .addOnSuccessListener {
                    clearLocalSession(onComplete)
                }
                .addOnFailureListener {
                    clearLocalSession(onComplete)
                }
        } else {
            clearLocalSession(onComplete)
        }
    }

    private fun clearLocalSession(onComplete: (Boolean) -> Unit) {
        prefs.edit().clear().apply()
        _rooms.value = emptyList()
        _tenants.value = emptyList()
        _bills.value = emptyList()
        _pastTenancies.value = emptyList()
        auth.signOut()
        activeUserId = ""
        onComplete(true)
    }
}
