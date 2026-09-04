package com.example.rentmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Property(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Room(
    val id: String = "",
    val propertyId: String = "default_property",
    val roomNumber: String = "",
    val baseRent: Double = 0.0,
    val electricityRate: Double = 10.0,
    val initialMeterReading: Double = 0.0,
    val isOccupied: Boolean = false,
    val currentTenantId: String = ""
)

data class Tenant(
    val id: String = "",
    val roomId: String = "",
    val name: String = "",
    val phone: String = "",
    val entryDate: Long = System.currentTimeMillis(),
    val exitDate: Long? = null,
    val securityDeposit: Double = 0.0,
    val isCurrent: Boolean = true
)

data class Bill(
    val id: String = "",
    val roomId: String = "",
    val tenantId: String = "",
    val billingPeriod: String = "",
    val previousReading: Double = 0.0,
    val currentReading: Double = 0.0,
    val unitsConsumed: Double = 0.0,
    val electricityRate: Double = 10.0,
    val electricityAmount: Double = 0.0,
    val baseRent: Double = 0.0,
    val maintenanceAmount: Double = 0.0,
    val totalPayable: Double = 0.0,
    val amountPaid: Double = 0.0,
    val paymentMode: String = "Cash",
    val remainingDue: Double = 0.0, // Positive = Due, Negative = Advance
    val timestamp: Long = System.currentTimeMillis()
)

class RentViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("rent_manager_local_prefs", Context.MODE_PRIVATE)
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    private val _selectedPropertyId = MutableStateFlow<String?>("default_property")
    val selectedPropertyId: StateFlow<String?> = _selectedPropertyId.asStateFlow()

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    init {
        loadFromLocalStorage()
        syncWithCloudIfAvailable()
    }

    fun setSelectedProperty(propertyId: String?) {
        _selectedPropertyId.value = propertyId
    }
        // ==========================================
    // PROPERTY OPERATIONS
    // ==========================================

    fun addProperty(name: String, address: String) {
        val newProperty = Property(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            address = address.trim(),
            createdAt = System.currentTimeMillis()
        )
        val updated = _properties.value + newProperty
        _properties.value = updated
        _selectedPropertyId.value = newProperty.id
        saveToLocalStorage()
        syncPropertyToCloud(newProperty)
    }

    // ==========================================
    // ROOM & TENANT OPERATIONS
    // ==========================================

    fun addRoom(
        roomNumber: String,
        baseRent: Double,
        electricityRate: Double,
        initialReading: Double,
        propertyId: String = _selectedPropertyId.value ?: "default_property"
    ) {
        val newRoom = Room(
            id = UUID.randomUUID().toString(),
            propertyId = propertyId,
            roomNumber = roomNumber.trim(),
            baseRent = baseRent,
            electricityRate = electricityRate,
            initialMeterReading = initialReading,
            isOccupied = false,
            currentTenantId = ""
        )
        val updated = _rooms.value + newRoom
        _rooms.value = updated
        saveToLocalStorage()
        syncRoomToCloud(newRoom)
    }

    fun assignTenant(
        roomId: String,
        tenantName: String,
        tenantPhone: String,
        deposit: Double
    ) {
        val newTenant = Tenant(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            name = tenantName.trim(),
            phone = tenantPhone.trim(),
            entryDate = System.currentTimeMillis(),
            securityDeposit = deposit,
            isCurrent = true
        )
        _tenants.value = _tenants.value + newTenant

        _rooms.value = _rooms.value.map { r ->
            if (r.id == roomId) {
                r.copy(isOccupied = true, currentTenantId = newTenant.id)
            } else r
        }

        saveToLocalStorage()
        syncTenantToCloud(newTenant)
        _rooms.value.find { it.id == roomId }?.let { syncRoomToCloud(it) }
    }

    fun vacateRoom(roomId: String) {
        val room = _rooms.value.find { it.id == roomId } ?: return
        val tenantId = room.currentTenantId

        // Mark active tenant as vacated
        if (tenantId.isNotBlank()) {
            _tenants.value = _tenants.value.map { t ->
                if (t.id == tenantId) t.copy(isCurrent = false, exitDate = System.currentTimeMillis()) else t
            }
        }

        // Room is reset for the next tenant
        _rooms.value = _rooms.value.map { r ->
            if (r.id == roomId) r.copy(isOccupied = false, currentTenantId = "") else r
        }

        saveToLocalStorage()
    }
        // ==========================================
    // BILLING & FINANCIAL LOGIC (SCOPED TO TENANT)
    // ==========================================

    /**
     * Retrieves the pending balance (Due > 0 or Advance < 0) for the current active tenant ONLY.
     * Prevents previous tenant refunds/advances (e.g. -₹17) from leaking to a new tenant.
     */
    fun getPendingDueForCurrentTenant(roomId: String): Double {
        val currentRoom = _rooms.value.find { it.id == roomId } ?: return 0.0
        val activeTenantId = currentRoom.currentTenantId
        if (activeTenantId.isBlank()) return 0.0

        val tenantBills = _bills.value
            .filter { it.roomId == roomId && it.tenantId == activeTenantId }
            .sortedByDescending { it.timestamp }

        return if (tenantBills.isNotEmpty()) tenantBills.first().remainingDue else 0.0
    }

    fun getLastRecordedMeterReading(roomId: String): Double {
        val roomBills = _bills.value
            .filter { it.roomId == roomId }
            .sortedByDescending { it.timestamp }

        return if (roomBills.isNotEmpty()) {
            roomBills.first().currentReading
        } else {
            _rooms.value.find { it.id == roomId }?.initialMeterReading ?: 0.0
        }
    }

    fun lodgeBill(
        roomId: String,
        billingPeriod: String,
        currentReading: Double,
        maintenanceAmount: Double,
        amountPaid: Double,
        paymentMode: String
    ): Bill {
        val room = _rooms.value.find { it.id == roomId } ?: throw IllegalStateException("Room not found")
        val tenantId = room.currentTenantId
        val prevReading = getLastRecordedMeterReading(roomId)
        val units = (currentReading - prevReading).coerceAtLeast(0.0)
        val electricityTotal = units * room.electricityRate

        // Scoped strictly to active tenant's prior dues/advances
        val priorAdjustment = getPendingDueForCurrentTenant(roomId)
        val grossTotal = room.baseRent + electricityTotal + maintenanceAmount + priorAdjustment
        val remaining = grossTotal - amountPaid

        val newBill = Bill(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            tenantId = tenantId,
            billingPeriod = billingPeriod,
            previousReading = prevReading,
            currentReading = currentReading,
            unitsConsumed = units,
            electricityRate = room.electricityRate,
            electricityAmount = electricityTotal,
            baseRent = room.baseRent,
            maintenanceAmount = maintenanceAmount,
            totalPayable = grossTotal,
            amountPaid = amountPaid,
            paymentMode = paymentMode,
            remainingDue = remaining,
            timestamp = System.currentTimeMillis()
        )

        _bills.value = _bills.value + newBill
        saveToLocalStorage()
        syncBillToCloud(newBill)
        return newBill
    }

    // ==========================================
    // HISTORICAL SETTLED INDICATORS FOR LEDGER
    // ==========================================

    /**
     * Checks if an older bill that had a due (remainingDue > 0) was settled in a later bill.
     */
    fun wasHistoricalDueSettled(bill: Bill): Boolean {
        if (bill.remainingDue <= 0.0) return false
        val laterBills = _bills.value
            .filter { it.tenantId == bill.tenantId && it.timestamp > bill.timestamp }
            .sortedBy { it.timestamp }
        return laterBills.any { it.amountPaid >= bill.remainingDue || it.remainingDue <= 0.0 }
    }

    /**
     * Checks if an older bill that had an advance credit (remainingDue < 0) was absorbed later.
     */
    fun wasHistoricalAdvanceConsumed(bill: Bill): Boolean {
        if (bill.remainingDue >= 0.0) return false
        val laterBills = _bills.value
            .filter { it.tenantId == bill.tenantId && it.timestamp > bill.timestamp }
        return laterBills.isNotEmpty()
    }

    fun getTenantForBill(bill: Bill): Tenant? {
        return _tenants.value.find { it.id == bill.tenantId }
    }

    fun getRoomForBill(bill: Bill): Room? {
        return _rooms.value.find { it.id == bill.roomId }
    }
        // ==========================================
    // LOCAL STORAGE PERSISTENCE
    // ==========================================

    private fun saveToLocalStorage() {
        viewModelScope.launch {
            try {
                // Save Properties
                val propArr = JSONArray()
                _properties.value.forEach {
                    val obj = JSONObject()
                    obj.put("id", it.id)
                    obj.put("name", it.name)
                    obj.put("address", it.address)
                    obj.put("createdAt", it.createdAt)
                    propArr.put(obj)
                }
                prefs.edit().putString("saved_properties", propArr.toString()).apply()

                // Save Rooms
                val roomArr = JSONArray()
                _rooms.value.forEach {
                    val obj = JSONObject()
                    obj.put("id", it.id)
                    obj.put("propertyId", it.propertyId)
                    obj.put("roomNumber", it.roomNumber)
                    obj.put("baseRent", it.baseRent)
                    obj.put("electricityRate", it.electricityRate)
                    obj.put("initialMeterReading", it.initialMeterReading)
                    obj.put("isOccupied", it.isOccupied)
                    obj.put("currentTenantId", it.currentTenantId)
                    roomArr.put(obj)
                }
                prefs.edit().putString("saved_rooms", roomArr.toString()).apply()

                // Save Tenants
                val tenantArr = JSONArray()
                _tenants.value.forEach {
                    val obj = JSONObject()
                    obj.put("id", it.id)
                    obj.put("roomId", it.roomId)
                    obj.put("name", it.name)
                    obj.put("phone", it.phone)
                    obj.put("entryDate", it.entryDate)
                    if (it.exitDate != null) obj.put("exitDate", it.exitDate)
                    obj.put("securityDeposit", it.securityDeposit)
                    obj.put("isCurrent", it.isCurrent)
                    tenantArr.put(obj)
                }
                prefs.edit().putString("saved_tenants", tenantArr.toString()).apply()

                // Save Bills
                val billArr = JSONArray()
                _bills.value.forEach {
                    val obj = JSONObject()
                    obj.put("id", it.id)
                    obj.put("roomId", it.roomId)
                    obj.put("tenantId", it.tenantId)
                    obj.put("billingPeriod", it.billingPeriod)
                    obj.put("previousReading", it.previousReading)
                    obj.put("currentReading", it.currentReading)
                    obj.put("unitsConsumed", it.unitsConsumed)
                    obj.put("electricityRate", it.electricityRate)
                    obj.put("electricityAmount", it.electricityAmount)
                    obj.put("baseRent", it.baseRent)
                    obj.put("maintenanceAmount", it.maintenanceAmount)
                    obj.put("totalPayable", it.totalPayable)
                    obj.put("amountPaid", it.amountPaid)
                    obj.put("paymentMode", it.paymentMode)
                    obj.put("remainingDue", it.remainingDue)
                    obj.put("timestamp", it.timestamp)
                    billArr.put(obj)
                }
                prefs.edit().putString("saved_bills", billArr.toString()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadFromLocalStorage() {
        try {
            // Load Properties
            val propStr = prefs.getString("saved_properties", null)
            if (!propStr.isNullOrEmpty()) {
                val arr = JSONArray(propStr)
                val list = mutableListOf<Property>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Property(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Main Building"),
                        address = obj.optString("address", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    ))
                }
                _properties.value = list
            } else {
                val defaultProp = Property(id = "default_property", name = "Main Property", address = "Primary Location")
                _properties.value = listOf(defaultProp)
            }

            // Load Rooms
            val roomStr = prefs.getString("saved_rooms", null)
            if (!roomStr.isNullOrEmpty()) {
                val arr = JSONArray(roomStr)
                val list = mutableListOf<Room>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Room(
                        id = obj.getString("id"),
                        propertyId = obj.optString("propertyId", "default_property"),
                        roomNumber = obj.getString("roomNumber"),
                        baseRent = obj.getDouble("baseRent"),
                        electricityRate = obj.optDouble("electricityRate", 10.0),
                        initialMeterReading = obj.optDouble("initialMeterReading", 0.0),
                        isOccupied = obj.optBoolean("isOccupied", false),
                        currentTenantId = obj.optString("currentTenantId", "")
                    ))
                }
                _rooms.value = list
            }

            // Load Tenants
            val tenantStr = prefs.getString("saved_tenants", null)
            if (!tenantStr.isNullOrEmpty()) {
                val arr = JSONArray(tenantStr)
                val list = mutableListOf<Tenant>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Tenant(
                        id = obj.getString("id"),
                        roomId = obj.getString("roomId"),
                        name = obj.getString("name"),
                        phone = obj.getString("phone"),
                        entryDate = obj.getLong("entryDate"),
                        exitDate = if (obj.has("exitDate")) obj.getLong("exitDate") else null,
                        securityDeposit = obj.optDouble("securityDeposit", 0.0),
                        isCurrent = obj.optBoolean("isCurrent", true)
                    ))
                }
                _tenants.value = list
            }

            // Load Bills
            val billStr = prefs.getString("saved_bills", null)
            if (!billStr.isNullOrEmpty()) {
                val arr = JSONArray(billStr)
                val list = mutableListOf<Bill>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Bill(
                        id = obj.getString("id"),
                        roomId = obj.getString("roomId"),
                        tenantId = obj.optString("tenantId", ""),
                        billingPeriod = obj.getString("billingPeriod"),
                        previousReading = obj.optDouble("previousReading", 0.0),
                        currentReading = obj.optDouble("currentReading", 0.0),
                        unitsConsumed = obj.optDouble("unitsConsumed", 0.0),
                        electricityRate = obj.optDouble("electricityRate", 10.0),
                        electricityAmount = obj.optDouble("electricityAmount", 0.0),
                        baseRent = obj.optDouble("baseRent", 0.0),
                        maintenanceAmount = obj.optDouble("maintenanceAmount", 0.0),
                        totalPayable = obj.getDouble("totalPayable"),
                        amountPaid = obj.getDouble("amountPaid"),
                        paymentMode = obj.optString("paymentMode", "Cash"),
                        remainingDue = obj.getDouble("remainingDue"),
                        timestamp = obj.getLong("timestamp")
                    ))
                }
                _bills.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun syncWithCloudIfAvailable() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        firestore.collection("users").document(uid).collection("properties")
            .get().addOnSuccessListener { snaps ->
                if (!snaps.isEmpty) {
                    _properties.value = snaps.toObjects(Property::class.java)
                }
            }

        firestore.collection("users").document(uid).collection("rooms")
            .get().addOnSuccessListener { snaps ->
                if (!snaps.isEmpty) {
                    _rooms.value = snaps.toObjects(Room::class.java)
                }
            }

        firestore.collection("users").document(uid).collection("bills")
            .get().addOnSuccessListener { snaps ->
                if (!snaps.isEmpty) {
                    _bills.value = snaps.toObjects(Bill::class.java)
                }
            }
    }

    private fun syncPropertyToCloud(prop: Property) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("properties")
            .document(prop.id).set(prop, SetOptions.merge())
    }

    private fun syncRoomToCloud(room: Room) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("rooms")
            .document(room.id).set(room, SetOptions.merge())
    }

    private fun syncTenantToCloud(tenant: Tenant) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("tenants")
            .document(tenant.id).set(tenant, SetOptions.merge())
    }

    private fun syncBillToCloud(bill: Bill) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("bills")
            .document(bill.id).set(bill, SetOptions.merge())
    }
}
