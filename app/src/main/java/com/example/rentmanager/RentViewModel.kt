package com.example.rentmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

class RentViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences("rent_manager_prefs", Context.MODE_PRIVATE)

    private val _rooms = MutableStateFlow<List<RoomUnit>>(emptyList())
    val rooms: StateFlow<List<RoomUnit>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    init {
        loadLocalData()
        auth.currentUser?.uid?.let { loadCloudData(it) }
    }

    fun calculateElectricityUnits(currentReading: Double, prevReading: Double, rolloverMax: Double = 10000.0): Double {
        return if (currentReading >= prevReading) {
            currentReading - prevReading
        } else {
            (rolloverMax - prevReading) + currentReading
        }
    }

    fun getPendingDueForRoom(roomId: String): Double {
        val roomBills = _bills.value
            .filter { it.roomId == roomId }
            .sortedByDescending { it.timestamp }
        return if (roomBills.isNotEmpty()) roomBills.first().remainingDue else 0.0
    }

    fun lodgeBill(bill: BillRecord) {
        _bills.value = listOf(bill) + _bills.value
        _rooms.value = _rooms.value.map { room ->
            if (room.id == bill.roomId) {
                room.copy(lastMeterReading = bill.currentMeterReading)
            } else room
        }
        saveLocalData()

        auth.currentUser?.uid?.let { uid ->
            val userRef = firestore.collection("users").document(uid)
            userRef.collection("bills").document(bill.id).set(bill)
            userRef.collection("rooms").document(bill.roomId).update(
                "lastMeterReading", bill.currentMeterReading
            )
        }
    }

    fun addRoom(roomNumber: String, baseRent: Double, electricityRate: Double) {
        val newRoom = RoomUnit(
            id = UUID.randomUUID().toString(),
            roomNumber = roomNumber,
            baseRent = baseRent,
            electricityRate = electricityRate,
            lastMeterReading = 0.0,
            isOccupied = false,
            currentTenantId = ""
        )
        _rooms.value = _rooms.value + newRoom
        saveLocalData()

        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .collection("rooms").document(newRoom.id).set(newRoom)
        }
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
        val tenantId = UUID.randomUUID().toString()
        val newTenant = Tenant(
            id = tenantId,
            roomId = roomId,
            name = name,
            phoneNumber = phone,
            aadhaarNumber = aadhaarNumber,
            address = address,
            depositAmount = depositAmount,
            initialReading = initialReading,
            moveInDate = moveInDate,
            isActive = true
        )

        _tenants.value = _tenants.value + newTenant
        _rooms.value = _rooms.value.map { room ->
            if (room.id == roomId) {
                room.copy(
                    isOccupied = true,
                    currentTenantId = tenantId,
                    lastMeterReading = initialReading
                )
            } else room
        }
        saveLocalData()

        auth.currentUser?.uid?.let { uid ->
            val userRef = firestore.collection("users").document(uid)
            userRef.collection("tenants").document(tenantId).set(newTenant)
            _rooms.value.find { it.id == roomId }?.let { updatedRoom ->
                userRef.collection("rooms").document(roomId).set(updatedRoom)
            }
        }
    }

    fun vacateRoom(roomId: String, finalReading: Double, moveOutDate: String) {
        val currentRoom = _rooms.value.find { it.id == roomId } ?: return
        val currentTenantId = currentRoom.currentTenantId

        _tenants.value = _tenants.value.map { tenant ->
            if (tenant.id == currentTenantId) {
                tenant.copy(isActive = false, moveOutDate = moveOutDate)
            } else tenant
        }

        _rooms.value = _rooms.value.map { room ->
            if (room.id == roomId) {
                room.copy(
                    isOccupied = false,
                    currentTenantId = "",
                    lastMeterReading = finalReading
                )
            } else room
        }
        saveLocalData()

        auth.currentUser?.uid?.let { uid ->
            val userRef = firestore.collection("users").document(uid)
            userRef.collection("rooms").document(roomId).update(
                "isOccupied", false,
                "currentTenantId", "",
                "lastMeterReading", finalReading
            )
            if (currentTenantId.isNotBlank()) {
                userRef.collection("tenants").document(currentTenantId).update(
                    "isActive", false,
                    "moveOutDate", moveOutDate
                )
            }
        }
    }

    fun resolveTenantName(tenantId: String, roomId: String): String {
        val byId = _tenants.value.find { it.id == tenantId }
        if (byId != null) return byId.name
        val byRoom = _tenants.value.find { it.roomId == roomId && it.isActive }
        return byRoom?.name ?: "Occupant"
    }
        // --- FINANCIAL METRICS (Lifetime & Yearly) ---

    fun calculateLifetimeRevenue(): Double {
        return _bills.value.sumOf { it.amountPaid }
    }

    fun calculateLifetimeRentEarnings(): Double {
        return _bills.value.sumOf { it.baseRent }
    }

    fun calculateLifetimeElectricityRevenue(): Double {
        return _bills.value.sumOf {
            val units = calculateElectricityUnits(it.currentMeterReading, it.prevMeterReading)
            units * it.electricityRate
        }
    }

    fun calculateYearlyRevenue(): Double {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        return _bills.value
            .filter { it.monthYear.contains(currentYear) }
            .sumOf { it.amountPaid }
    }

    fun calculateYearlyRentEarnings(): Double {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        return _bills.value
            .filter { it.monthYear.contains(currentYear) }
            .sumOf { it.baseRent }
    }

    fun calculateYearlyElectricityRevenue(): Double {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        return _bills.value
            .filter { it.monthYear.contains(currentYear) }
            .sumOf {
                val units = calculateElectricityUnits(it.currentMeterReading, it.prevMeterReading)
                units * it.electricityRate
            }
    }

    fun calculateTotalOutstandingDues(): Double {
        val activeRooms = _rooms.value.filter { it.isOccupied }
        return activeRooms.sumOf { room ->
            val due = getPendingDueForRoom(room.id)
            if (due > 0.0) due else 0.0
        }
    }

    // --- CACHE & PERSISTENCE ---

    private fun saveLocalData() {
        val roomsJson = JSONArray().apply {
            _rooms.value.forEach { r ->
                put(JSONObject().apply {
                    put("id", r.id)
                    put("roomNumber", r.roomNumber)
                    put("baseRent", r.baseRent)
                    put("electricityRate", r.electricityRate)
                    put("lastMeterReading", r.lastMeterReading)
                    put("isOccupied", r.isOccupied)
                    put("currentTenantId", r.currentTenantId)
                })
            }
        }

        val tenantsJson = JSONArray().apply {
            _tenants.value.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("roomId", t.roomId)
                    put("name", t.name)
                    put("phoneNumber", t.phoneNumber)
                    put("aadhaarNumber", t.aadhaarNumber)
                    put("address", t.address)
                    put("depositAmount", t.depositAmount)
                    put("initialReading", t.initialReading)
                    put("moveInDate", t.moveInDate)
                    put("moveOutDate", t.moveOutDate ?: "")
                    put("isActive", t.isActive)
                })
            }
        }

        val billsJson = JSONArray().apply {
            _bills.value.forEach { b ->
                put(JSONObject().apply {
                    put("id", b.id)
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
                    put("remainingDue", b.remainingDue)
                    put("paymentMode", b.paymentMode)
                    put("timestamp", b.timestamp)
                })
            }
        }

        prefs.edit()
            .putString("rooms_cache", roomsJson.toString())
            .putString("tenants_cache", tenantsJson.toString())
            .putString("bills_cache", billsJson.toString())
            .apply()
    }

    private fun loadLocalData() {
        try {
            val roomsStr = prefs.getString("rooms_cache", null)
            if (!roomsStr.isNullOrBlank()) {
                val array = JSONArray(roomsStr)
                val list = mutableListOf<RoomUnit>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        RoomUnit(
                            id = obj.optString("id"),
                            roomNumber = obj.optString("roomNumber"),
                            baseRent = obj.optDouble("baseRent", 0.0),
                            electricityRate = obj.optDouble("electricityRate", 0.0),
                            lastMeterReading = obj.optDouble("lastMeterReading", 0.0),
                            isOccupied = obj.optBoolean("isOccupied", false),
                            currentTenantId = obj.optString("currentTenantId")
                        )
                    )
                }
                _rooms.value = list
            }

            val tenantsStr = prefs.getString("tenants_cache", null)
            if (!tenantsStr.isNullOrBlank()) {
                val array = JSONArray(tenantsStr)
                val list = mutableListOf<Tenant>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Tenant(
                            id = obj.optString("id"),
                            roomId = obj.optString("roomId"),
                            name = obj.optString("name"),
                            phoneNumber = obj.optString("phoneNumber"),
                            aadhaarNumber = obj.optString("aadhaarNumber"),
                            address = obj.optString("address"),
                            depositAmount = obj.optDouble("depositAmount", 0.0),
                            initialReading = obj.optDouble("initialReading", 0.0),
                            moveInDate = obj.optString("moveInDate"),
                            moveOutDate = obj.optString("moveOutDate").takeIf { it.isNotBlank() },
                            isActive = obj.optBoolean("isActive", true)
                        )
                    )
                }
                _tenants.value = list
            }

            val billsStr = prefs.getString("bills_cache", null)
            if (!billsStr.isNullOrBlank()) {
                val array = JSONArray(billsStr)
                val list = mutableListOf<BillRecord>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        BillRecord(
                            id = obj.optString("id"),
                            roomId = obj.optString("roomId"),
                            tenantId = obj.optString("tenantId"),
                            monthYear = obj.optString("monthYear"),
                            baseRent = obj.optDouble("baseRent", 0.0),
                            prevMeterReading = obj.optDouble("prevMeterReading", 0.0),
                            currentMeterReading = obj.optDouble("currentMeterReading", 0.0),
                            electricityRate = obj.optDouble("electricityRate", 0.0),
                            maintenanceCharge = obj.optDouble("maintenanceCharge", 0.0),
                            previousDueCarryover = obj.optDouble("previousDueCarryover", 0.0),
                            amountPaid = obj.optDouble("amountPaid", 0.0),
                            remainingDue = obj.optDouble("remainingDue", 0.0),
                            paymentMode = obj.optString("paymentMode", "Cash"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _bills.value = list
            }
        } catch (_: Exception) {}
    }

    fun loadCloudData(uid: String) {
        val userDoc = firestore.collection("users").document(uid)

        userDoc.collection("rooms").get().addOnSuccessListener { snap ->
            val list = snap.toObjects(RoomUnit::class.java)
            if (list.isNotEmpty()) {
                _rooms.value = list
                saveLocalData()
            }
        }

        userDoc.collection("tenants").get().addOnSuccessListener { snap ->
            val list = snap.toObjects(Tenant::class.java)
            if (list.isNotEmpty()) {
                _tenants.value = list
                saveLocalData()
            }
        }

        userDoc.collection("bills").get().addOnSuccessListener { snap ->
            val list = snap.toObjects(BillRecord::class.java)
            if (list.isNotEmpty()) {
                _bills.value = list
                saveLocalData()
            }
        }
    }

    fun clearAllUserData(onComplete: () -> Unit) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val userRef = firestore.collection("users").document(uid)
                userRef.collection("rooms").get().addOnSuccessListener { snap ->
                    snap.documents.forEach { it.reference.delete() }
                }
                userRef.collection("tenants").get().addOnSuccessListener { snap ->
                    snap.documents.forEach { it.reference.delete() }
                }
                userRef.collection("bills").get().addOnSuccessListener { snap ->
                    snap.documents.forEach { it.reference.delete() }
                }
            }

            prefs.edit().clear().apply()
            _rooms.value = emptyList()
            _tenants.value = emptyList()
            _bills.value = emptyList()

            auth.signOut()
            onComplete()
        }
    }

    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        onComplete()
    }
}
