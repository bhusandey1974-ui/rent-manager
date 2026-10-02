package com.example.rentmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TenantHistorySummary(
    val tenant: Tenant,
    val daysStayed: Long,
    val totalRentCollected: Double,
    val totalElectricityCollected: Double,
    val totalMoneyCollected: Double,
    val currentPendingDue: Double
)

data class RevenueBreakdown(
    val totalCollected: Double,
    val rentCollected: Double,
    val electricityCollected: Double,
    val maintenanceCollected: Double,
    val activeDues: Double
)

data class RoomWiseAmount(
    val roomNumber: String,
    val amount: Double
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
    val rooms: StateFlow<List<Room>> = _rooms
        .map { list -> list.sortedBy { it.roomNumber.toIntOrNull() ?: Int.MAX_VALUE } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<Withdrawal>>(emptyList())
    val withdrawals: StateFlow<List<Withdrawal>> = _withdrawals.asStateFlow()

    private val _billingConvention = MutableStateFlow(
        if (prefs.getString("billing_convention", BillingConvention.PREVIOUS_MONTH.name) == BillingConvention.CURRENT_MONTH.name)
            BillingConvention.CURRENT_MONTH
        else
            BillingConvention.PREVIOUS_MONTH
    )
    val billingConvention: StateFlow<BillingConvention> = _billingConvention.asStateFlow()

    init {
        loadFromLocalStorage()
        loadWithdrawalsFromLocal()
        syncWithCloudIfAvailable()
        syncWithdrawalsFromCloud()
    }

    fun setSelectedProperty(propertyId: String?) {
        _selectedPropertyId.value = propertyId
    }

    fun setBillingConvention(convention: BillingConvention) {
        _billingConvention.value = convention
        prefs.edit().putString("billing_convention", convention.name).apply()
    }

    /** More than ~1 month in the past, used to decide whether to prompt for historical unpaid rent. */
    fun isBackdatedMoveIn(moveInMillis: Long): Boolean {
        val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000
        return moveInMillis < (System.currentTimeMillis() - thirtyDaysMillis)
    }

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun isCloudConnected(): Boolean = auth.currentUser != null

    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        prefs.edit().clear().apply()
        _properties.value = emptyList()
        _rooms.value = emptyList()
        _tenants.value = emptyList()
        _bills.value = emptyList()
        _withdrawals.value = emptyList()
        _selectedPropertyId.value = null
        loadFromLocalStorage()
        onComplete()
    }

    /** Call this right after a successful sign-in so cloud data loads without needing an app restart. */
    fun refreshFromCloud() {
        syncWithCloudIfAvailable()
        syncWithdrawalsFromCloud()
    }

    // ---------- Withdrawals ----------

    fun addWithdrawal(amount: Double, recipient: String, purpose: String) {
        val w = Withdrawal(
            id = UUID.randomUUID().toString(),
            amount = amount,
            recipient = recipient.trim(),
            purpose = purpose.trim(),
            timestamp = System.currentTimeMillis()
        )
        _withdrawals.value = _withdrawals.value + w
        saveWithdrawalsToLocal()
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("withdrawals")
            .document(w.id).set(w, SetOptions.merge())
    }

    fun deleteWithdrawal(id: String) {
        _withdrawals.value = _withdrawals.value.filter { it.id != id }
        saveWithdrawalsToLocal()
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("withdrawals")
            .document(id).delete()
    }

    fun getWithdrawalsList(forCurrentYearOnly: Boolean): List<Withdrawal> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val cal = Calendar.getInstance()
        return _withdrawals.value
            .filter { w ->
                if (!forCurrentYearOnly) true else {
                    cal.timeInMillis = w.timestamp
                    cal.get(Calendar.YEAR) == currentYear
                }
            }
            .sortedByDescending { it.timestamp }
    }

    private fun saveWithdrawalsToLocal() {
        try {
            val arr = JSONArray()
            _withdrawals.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("amount", it.amount)
                obj.put("recipient", it.recipient)
                obj.put("purpose", it.purpose)
                obj.put("timestamp", it.timestamp)
                arr.put(obj)
            }
            prefs.edit().putString("saved_withdrawals", arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadWithdrawalsFromLocal() {
        try {
            val str = prefs.getString("saved_withdrawals", null)
            if (!str.isNullOrEmpty()) {
                val arr = JSONArray(str)
                val list = mutableListOf<Withdrawal>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Withdrawal(
                        id = obj.getString("id"),
                        amount = obj.optDouble("amount", 0.0),
                        recipient = obj.optString("recipient", ""),
                        purpose = obj.optString("purpose", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    ))
                }
                _withdrawals.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun syncWithdrawalsFromCloud() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("withdrawals")
            .get().addOnSuccessListener { snaps ->
                if (!snaps.isEmpty) {
                    val cloud = snaps.toObjects(Withdrawal::class.java)
                    val localIds = _withdrawals.value.map { it.id }.toSet()
                    val missing = cloud.filter { it.id !in localIds }
                    if (missing.isNotEmpty()) {
                        _withdrawals.value = _withdrawals.value + missing
                        saveWithdrawalsToLocal()
                    }
                }
            }
    }
    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                try {
                    val userDoc = firestore.collection("users").document(uid)
                    userDoc.collection("properties").get().addOnSuccessListener { s -> s.forEach { it.reference.delete() } }
                    userDoc.collection("rooms").get().addOnSuccessListener { s -> s.forEach { it.reference.delete() } }
                    userDoc.collection("tenants").get().addOnSuccessListener { s -> s.forEach { it.reference.delete() } }
                    userDoc.collection("bills").get().addOnSuccessListener { s -> s.forEach { it.reference.delete() } }
                    userDoc.collection("withdrawals").get().addOnSuccessListener { s -> s.forEach { it.reference.delete() } }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            prefs.edit().clear().apply()

            val defaultProp = Property(id = "default_property", name = "Main Property", address = "Primary Location")
            _properties.value = listOf(defaultProp)
            _selectedPropertyId.value = defaultProp.id
            _rooms.value = emptyList()
            _tenants.value = emptyList()
            _bills.value = emptyList()
            _withdrawals.value = emptyList()

            onComplete()
        }
    }

    fun addProperty(name: String, address: String) {
        val newProperty = Property(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            address = address.trim(),
            createdAt = System.currentTimeMillis()
        )
        _properties.value = _properties.value + newProperty
        _selectedPropertyId.value = newProperty.id
        saveToLocalStorage()
        syncPropertyToCloud(newProperty)
    }

    /**
     * Removes a property (floor) together with its rooms, those rooms' tenants and bills.
     * Returns false (and changes nothing) if it is the only property left or doesn't exist.
     * Call this only after the user has passed both confirmations.
     */
    fun deleteProperty(propertyId: String): Boolean {
        if (_properties.value.none { it.id == propertyId }) return false
        if (_properties.value.size <= 1) return false

        val roomIds = _rooms.value.filter { it.propertyId == propertyId }.map { it.id }.toSet()
        val tenantIds = _tenants.value.filter { it.roomId in roomIds }.map { it.id }.toSet()
        val billIds = _bills.value
            .filter { it.roomId in roomIds || it.tenantId in tenantIds }
            .map { it.id }.toSet()

        _bills.value = _bills.value.filter { it.id !in billIds }
        _tenants.value = _tenants.value.filter { it.id !in tenantIds }
        _rooms.value = _rooms.value.filter { it.id !in roomIds }
        _properties.value = _properties.value.filter { it.id != propertyId }

        if (_selectedPropertyId.value == propertyId) {
            _selectedPropertyId.value = _properties.value.firstOrNull()?.id
        }
        saveToLocalStorage()

        // Cloud: must be removed too, otherwise the cloud merge would bring everything back.
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val userDoc = firestore.collection("users").document(uid)
            userDoc.collection("properties").document(propertyId).delete()
            roomIds.forEach { userDoc.collection("rooms").document(it).delete() }
            tenantIds.forEach { userDoc.collection("tenants").document(it).delete() }
            billIds.forEach { userDoc.collection("bills").document(it).delete() }
        }
        return true
    }
        // ==========================================
    // PART 2: Rooms, Tenants & History Aggregation
    // ==========================================

    fun addRoom(
        roomNumber: String,
        baseRent: Double,
        electricityRate: Double,
        initialReading: Double,
        propertyId: String = _selectedPropertyId.value ?: "default_property"
    ) {
        val initialRecord = RateHistoryRecord(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            previousRent = baseRent,
            newRent = baseRent,
            previousElectricityRate = electricityRate,
            newElectricityRate = electricityRate
        )
        val newRoom = Room(
            id = UUID.randomUUID().toString(),
            propertyId = propertyId,
            roomNumber = roomNumber.trim(),
            baseRent = baseRent,
            electricityRate = electricityRate,
            initialMeterReading = initialReading,
            isOccupied = false,
            currentTenantId = "",
            rateHistory = listOf(initialRecord)
        )
        _rooms.value = _rooms.value + newRoom
        saveToLocalStorage()
        syncRoomToCloud(newRoom)
    }

    fun updateRoom(
        roomId: String,
        roomNumber: String,
        baseRent: Double,
        electricityRate: Double,
        initialReading: Double
    ) {
        _rooms.value = _rooms.value.map { r ->
            if (r.id == roomId) {
                val rateChanged = r.baseRent != baseRent || r.electricityRate != electricityRate
                val updatedHistory = if (rateChanged) {
                    val audit = RateHistoryRecord(
                        id = UUID.randomUUID().toString(),
                        timestamp = System.currentTimeMillis(),
                        previousRent = r.baseRent,
                        newRent = baseRent,
                        previousElectricityRate = r.electricityRate,
                        newElectricityRate = electricityRate
                    )
                    r.rateHistory + audit
                } else {
                    r.rateHistory
                }

                val updated = r.copy(
                    roomNumber = roomNumber.trim(),
                    baseRent = baseRent,
                    electricityRate = electricityRate,
                    initialMeterReading = initialReading,
                    rateHistory = updatedHistory
                )
                syncRoomToCloud(updated)
                updated
            } else r
        }
        saveToLocalStorage()
    }

    fun deleteRoom(roomId: String) {
        _rooms.value = _rooms.value.filter { it.id != roomId }
        saveToLocalStorage()
        deleteRoomFromCloud(roomId)
    }

    fun assignTenant(
        roomId: String,
        tenantName: String,
        tenantPhone: String,
        deposit: Double,
        aadhaarNumber: String = "",
        permanentAddress: String = "",
        moveInDateMillis: Long = System.currentTimeMillis()
    ): Tenant {
        val newTenant = Tenant(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            name = tenantName.trim(),
            phoneNumber = tenantPhone.trim(),
            aadhaarNumber = aadhaarNumber.trim(),
            permanentAddress = permanentAddress.trim(),
            moveInDate = moveInDateMillis,
            moveOutDate = null,
            isCurrent = true,
            securityDeposit = deposit
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
        return newTenant
    }

    fun updateTenant(
        tenantId: String,
        name: String,
        phone: String,
        deposit: Double,
        aadhaarNumber: String = "",
        permanentAddress: String = "",
        moveInDateMillis: Long? = null
    ) {
        _tenants.value = _tenants.value.map { t ->
            if (t.id == tenantId) {
                val updated = t.copy(
                    name = name.trim(),
                    phoneNumber = phone.trim(),
                    aadhaarNumber = aadhaarNumber.trim(),
                    permanentAddress = permanentAddress.trim(),
                    moveInDate = moveInDateMillis ?: t.moveInDate,
                    securityDeposit = deposit
                )
                syncTenantToCloud(updated)
                updated
            } else t
        }
        saveToLocalStorage()
    }

    fun checkVacateSettlement(roomId: String): Double {
    return getPendingDueForCurrentTenant(roomId)
}

fun confirmVacateRoom(
    roomId: String,
    settlementAmount: Double,
    settlementNote: String,
    depositRefunded: Boolean,
    moveOutDateMillis: Long = System.currentTimeMillis()
) {
    val room = _rooms.value.find { it.id == roomId } ?: return
    val currentTenantId = room.currentTenantId

    if (currentTenantId.isNotBlank()) {
        _tenants.value = _tenants.value.map { t ->
            if (t.id == currentTenantId) {
                val vacated = t.copy(
                    isCurrent = false,
                    moveOutDate = moveOutDateMillis,
                    finalSettlementAmount = settlementAmount,
                    settlementNote = settlementNote,
                    depositRefunded = depositRefunded
                )
                syncTenantToCloud(vacated)
                vacated
            } else t
        }
    }

    _rooms.value = _rooms.value.map { r ->
        if (r.id == roomId) r.copy(isOccupied = false, currentTenantId = "") else r
    }

    saveToLocalStorage()
    _rooms.value.find { it.id == roomId }?.let { syncRoomToCloud(it) }
}
    fun getRoomTenancyHistory(roomId: String): List<TenantHistorySummary> {
        val roomTenants = _tenants.value
            .filter { it.roomId == roomId }
            .sortedByDescending { it.moveInDate }

        return roomTenants.map { tenant ->
            val now = System.currentTimeMillis()
            val exit = tenant.moveOutDate ?: now
            val durationMillis = (exit - tenant.moveInDate).coerceAtLeast(0L)
            val daysStayed = (durationMillis / (1000L * 60 * 60 * 24)).coerceAtLeast(1L)

            val tenantBills = _bills.value.filter { it.tenantId == tenant.id }
            val totalRent = tenantBills.sumOf { it.rentPaid }
            val totalElec = tenantBills.sumOf { it.electricityPaid }
            val totalMoney = tenantBills.sumOf { it.amountPaid }

            val pendingDue = tenantBills.sumOf { it.remainingDue }

            TenantHistorySummary(
                tenant = tenant,
                daysStayed = daysStayed,
                totalRentCollected = totalRent,
                totalElectricityCollected = totalElec,
                totalMoneyCollected = totalMoney,
                currentPendingDue = pendingDue
            )
        }
    }
        // ==========================================
    // PART 3: Revenue, FIFO Billing & Persistence
    // ==========================================

    fun getRevenueSummary(forCurrentYearOnly: Boolean): RevenueBreakdown {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val cal = Calendar.getInstance()

        val filteredBills = if (forCurrentYearOnly) {
            _bills.value.filter { bill ->
                cal.timeInMillis = bill.timestamp
                cal.get(Calendar.YEAR) == currentYear
            }
        } else {
            _bills.value
        }

        val totalCollected = filteredBills.sumOf { it.amountPaid }
        val rentCollected = filteredBills.sumOf { it.rentPaid }
        val elecCollected = filteredBills.sumOf { it.electricityPaid }
        
