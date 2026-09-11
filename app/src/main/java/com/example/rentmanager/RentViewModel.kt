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
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _billingConvention = MutableStateFlow(
        if (prefs.getString("billing_convention", BillingConvention.PREVIOUS_MONTH.name) == BillingConvention.CURRENT_MONTH.name)
            BillingConvention.CURRENT_MONTH
        else
            BillingConvention.PREVIOUS_MONTH
    )
    val billingConvention: StateFlow<BillingConvention> = _billingConvention.asStateFlow()

    init {
        loadFromLocalStorage()
        syncWithCloudIfAvailable()
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
        _properties.value = emptyList()
        _rooms.value = emptyList()
        _tenants.value = emptyList()
        _bills.value = emptyList()
        _selectedPropertyId.value = null
        loadFromLocalStorage()
        onComplete()
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
                    moveInDate = moveInDateMillis ?: t.moveInDate
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
                    settlementNote = settlementNote
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
            val totalRent = tenantBills.sumOf { it.rentPaid.takeIf { p -> p > 0 } ?: it.baseRent.coerceAtMost(it.amountPaid) }
            val totalElec = tenantBills.sumOf { it.electricityPaid.takeIf { p -> p > 0 } ?: (it.amountPaid - it.baseRent).coerceAtLeast(0.0) }
            val totalMoney = tenantBills.sumOf { it.amountPaid }

            val latestBill = tenantBills.maxByOrNull { it.timestamp }
            val pendingDue = latestBill?.remainingDue ?: 0.0

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
        val maintCollected = filteredBills.sumOf { it.maintenanceAmount }

        val activeDues = _rooms.value.filter { it.isOccupied }.sumOf { room ->
            getPendingDueForCurrentTenant(room.id).coerceAtLeast(0.0)
        }

        return RevenueBreakdown(
            totalCollected = totalCollected,
            rentCollected = rentCollected,
            electricityCollected = elecCollected,
            maintenanceCollected = maintCollected,
            activeDues = activeDues
        )
    }

    fun getPendingDueForCurrentTenant(roomId: String): Double {
        val currentRoom = _rooms.value.find { it.id == roomId } ?: return 0.0
        val activeTenantId = currentRoom.currentTenantId
        if (activeTenantId.isBlank()) return 0.0

        return _bills.value
            .filter { it.roomId == roomId && it.tenantId == activeTenantId }
            .sumOf { it.remainingDue }
    }

fun getTotalAdvance(): Double {
    return _rooms.value.filter { it.isOccupied }.sumOf { room ->
        val due = getPendingDueForCurrentTenant(room.id)
        if (due < 0.0) kotlin.math.abs(due) else 0.0
    }
}

fun getRoomWiseBreakdown(category: String, forCurrentYearOnly: Boolean): List<RoomWiseAmount> {
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

    return when (category) {
        "rent" -> {
            _rooms.value.mapNotNull { room ->
                val total = filteredBills
                    .filter { it.roomId == room.id }
                    .sumOf { it.rentPaid.takeIf { p -> p > 0 } ?: 0.0 }
                if (total > 0.0) RoomWiseAmount(room.roomNumber, total) else null
            }.sortedBy { it.roomNumber }
        }
        "electricity" -> {
            _rooms.value.mapNotNull { room ->
                val total = filteredBills
                    .filter { it.roomId == room.id }
                    .sumOf { it.electricityPaid.takeIf { p -> p > 0 } ?: 0.0 }
                if (total > 0.0) RoomWiseAmount(room.roomNumber, total) else null
            }.sortedBy { it.roomNumber }
        }
        "dues" -> {
            _rooms.value.filter { it.isOccupied }.mapNotNull { room ->
                val due = getPendingDueForCurrentTenant(room.id)
                if (due > 0.0) RoomWiseAmount(room.roomNumber, due) else null
            }.sortedBy { it.roomNumber }
        }
        "advance" -> {
            _rooms.value.filter { it.isOccupied }.mapNotNull { room ->
                val due = getPendingDueForCurrentTenant(room.id)
                if (due < 0.0) RoomWiseAmount(room.roomNumber, kotlin.math.abs(due)) else null
            }.sortedBy { it.roomNumber }
        }
        else -> emptyList()
    }
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

    /**
     * What billing period a new bill for this room should default to.
     *
     * If the current tenant already has a bill on record, this is simply
     * (that bill's month + 1) — chained forward regardless of today's actual
     * calendar date, so gaps or early/late payments don't throw it off.
     *
     * If there's no bill history yet (e.g. very first bill for this tenant),
     * it falls back to the app-wide billing convention: the current calendar
     * month if rent is collected during the month, or last month if collected
     * in arrears.
     */
    fun getSuggestedBillingPeriod(roomId: String): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        val room = _rooms.value.find { it.id == roomId }
        val tenantId = room?.currentTenantId.orEmpty()

        val roomTenantBills = _bills.value.filter { it.roomId == roomId && it.tenantId == tenantId }

        // Arrears take priority: settle the oldest unpaid month first.
        val oldestUnpaid = roomTenantBills
            .filter { it.remainingDue > 0.0 }
            .minByOrNull { it.timestamp }
        if (oldestUnpaid != null) {
            return oldestUnpaid.billingPeriod
        }

        val lastBill = roomTenantBills.maxByOrNull { it.timestamp }

        if (lastBill != null) {
            try {
                val parsed = sdf.parse(lastBill.billingPeriod)
                if (parsed != null) {
                    val cal = Calendar.getInstance()
                    cal.time = parsed
                    cal.add(Calendar.MONTH, 1)
                    return sdf.format(cal.time)
                }
            } catch (e: Exception) {
                // Fall through to convention-based default below
            }
        }

        val cal = Calendar.getInstance()
        if (_billingConvention.value == BillingConvention.PREVIOUS_MONTH) {
            cal.add(Calendar.MONTH, -1)
        }
        return sdf.format(cal.time)
    }

    /**
     * Billing periods for this tenant that still have money outstanding,
     * oldest first. Useful for flagging "you still owe for these months" on a
     * fresh receipt after a backdated tenant has been backfilled.
     */
    fun getOutstandingUnpaidMonths(roomId: String, tenantId: String, excludeBillId: String? = null): List<String> {
        return _bills.value
            .filter { it.roomId == roomId && it.tenantId == tenantId && it.remainingDue > 0.0 && it.id != excludeBillId }
            .sortedBy { it.timestamp }
            .map { it.billingPeriod }
    }

    /** Every billing period (paid or not) that already has a bill for this tenant, lowercased for easy matching. */
    fun getExistingBillingPeriods(roomId: String, tenantId: String): Set<String> {
        return _bills.value
            .filter { it.roomId == roomId && it.tenantId == tenantId }
            .map { it.billingPeriod.trim().lowercase(Locale.ENGLISH) }
            .toSet()
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

        // If a bill already exists for this exact period (e.g. an old backfilled
        // unpaid month being paid off), we settle that same bill directly —
        // we never stack a second, duplicate charge on top of an existing period.
        val existingBillForPeriod = _bills.value.find {
            it.roomId == roomId && it.tenantId == tenantId &&
                it.billingPeriod.equals(billingPeriod, ignoreCase = true)
        }

        // Every OTHER bill this tenant still owes on, oldest first. The period
        // being lodged right now (if it already exists) is handled separately below.
        val otherOutstanding = _bills.value
            .filter {
                it.roomId == roomId && it.tenantId == tenantId && it.remainingDue > 0.0 &&
                    it.id != existingBillForPeriod?.id
            }
            .sortedBy { it.timestamp }

        var paymentLeft = amountPaid

        // 1) Set aside how much of this payment goes toward this exact period's own
        //    bill (if any) — but don't finalize it yet, since a surplus after every
        //    other due is cleared should still fold back into this bill as an advance.
        val existingDue = existingBillForPeriod?.remainingDue?.coerceAtLeast(0.0) ?: 0.0
        val existingApplied = minOf(existingDue, paymentLeft)
        paymentLeft -= existingApplied

        // 2) Whatever's left after that settles other outstanding arrears, oldest first.
        val settledOthers = otherOutstanding.map { old ->
            if (paymentLeft <= 0.0) return@map old
            val applied = minOf(old.remainingDue, paymentLeft)
            paymentLeft -= applied

            // Within that old bill, apply its own share toward rent first, then electricity.
            val rentGap = (old.baseRent - old.rentPaid).coerceAtLeast(0.0)
            val rentApplied = minOf(applied, rentGap)
            val elecApplied = applied - rentApplied

            old.copy(
                rentPaid = old.rentPaid + rentApplied,
                electricityPaid = old.electricityPaid + elecApplied,
                amountPaid = old.amountPaid + applied,
                remainingDue = old.remainingDue - applied
            )
        }

        val newBill: Bill

        if (existingBillForPeriod != null) {
            // No new row — this period's existing bill was already updated above.
            newBill = updatedExisting!!
            _bills.value = _bills.value.map { b ->
                if (b.id == newBill.id) newBill else (settledOthers.find { it.id == b.id } ?: b)
            }
        } else {
            // A genuinely new period. Whatever's left after arrears are cleared goes
            // toward this month's rent, then electricity. Any surplus beyond this
            // month's own charge becomes an advance credit rather than being dropped.
            val prevReading = getLastRecordedMeterReading(roomId)
            val units = (currentReading - prevReading).coerceAtLeast(0.0)
            val electricityTotal = units * room.electricityRate
            val currentMonthCharge = room.baseRent + electricityTotal + maintenanceAmount
            val priorDue = otherOutstanding.sumOf { it.remainingDue }

            val rentContribution = minOf(room.baseRent, paymentLeft)
            val afterRent = (paymentLeft - rentContribution).coerceAtLeast(0.0)
            val elecContribution = minOf(electricityTotal, afterRent)

            newBill = Bill(
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
                totalPayable = currentMonthCharge + priorDue,
                rentPaid = rentContribution,
                electricityPaid = elecContribution,
                amountPaid = amountPaid,
                paymentMode = paymentMode,
                remainingDue = currentMonthCharge - paymentLeft,
                timestamp = System.currentTimeMillis()
            )

            _bills.value = _bills.value.map { b -> settledOthers.find { it.id == b.id } ?: b } + newBill
        }

        saveToLocalStorage()
        syncBillToCloud(newBill)
        settledOthers.forEach { syncBillToCloud(it) }
        return newBill
    }

    fun backfillUnpaidRent(roomId: String, tenantId: String, paidThroughMonthMillis: Long) {
    val room = _rooms.value.find { it.id == roomId } ?: return

    val cal = Calendar.getInstance()
    cal.timeInMillis = paidThroughMonthMillis
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.add(Calendar.MONTH, 1) // start from the month AFTER the last paid month

    val now = Calendar.getInstance()
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    val newBills = mutableListOf<Bill>()

    while (
        cal.get(Calendar.YEAR) < now.get(Calendar.YEAR) ||
        (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) && cal.get(Calendar.MONTH) < now.get(Calendar.MONTH))
    ) {
        val billTimestamp = cal.timeInMillis
        newBills.add(
            Bill(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                tenantId = tenantId,
                billingPeriod = sdf.format(Date(billTimestamp)),
                previousReading = 0.0,
                currentReading = 0.0,
                unitsConsumed = 0.0,
                electricityRate = room.electricityRate,
                electricityAmount = 0.0,
                baseRent = room.baseRent,
                maintenanceAmount = 0.0,
                totalPayable = room.baseRent,
                rentPaid = 0.0,
                electricityPaid = 0.0,
                amountPaid = 0.0,
                paymentMode = "Backfilled",
                remainingDue = room.baseRent,
                timestamp = billTimestamp
            )
        )
        cal.add(Calendar.MONTH, 1)
    }

    if (newBills.isNotEmpty()) {
        _bills.value = _bills.value + newBills
        saveToLocalStorage()
        newBills.forEach { syncBillToCloud(it) }
    }
    }

    fun settleLumpSumArrears(
        tenantId: String,
        baseRentPayment: Double,
        electricityPayment: Double
    ) {
        val tenantBills = _bills.value
            .filter { it.tenantId == tenantId }
            .sortedBy { it.timestamp }

        var availableRent = baseRentPayment
        var availableElec = electricityPayment

        val updatedTenantBills = tenantBills.map { bill ->
            var currentRentPaid = bill.rentPaid
            var currentElecPaid = bill.electricityPaid

            val unpaidRent = (bill.baseRent - currentRentPaid).coerceAtLeast(0.0)
            if (unpaidRent > 0.0 && availableRent > 0.0) {
                val payment = minOf(unpaidRent, availableRent)
                currentRentPaid += payment
                availableRent -= payment
            }

            val unpaidElec = (bill.electricityAmount - currentElecPaid).coerceAtLeast(0.0)
            if (unpaidElec > 0.0 && availableElec > 0.0) {
                val payment = minOf(unpaidElec, availableElec)
                currentElecPaid += payment
                availableElec -= payment
            }

            val newAmountPaid = currentRentPaid + currentElecPaid
            val newRemainingDue = bill.totalPayable - newAmountPaid

            bill.copy(
                rentPaid = currentRentPaid,
                electricityPaid = currentElecPaid,
                amountPaid = newAmountPaid,
                remainingDue = newRemainingDue
            )
        }

        val updatedIds = updatedTenantBills.map { it.id }.toSet()
        val otherBills = _bills.value.filter { it.id !in updatedIds }
        _bills.value = otherBills + updatedTenantBills

        saveToLocalStorage()
        updatedTenantBills.forEach { syncBillToCloud(it) }
    }

    fun wasHistoricalDueSettled(bill: Bill): Boolean {
        if (bill.remainingDue <= 0.0) return false
        val laterBills = _bills.value
            .filter { it.tenantId == bill.tenantId && it.timestamp > bill.timestamp }
            .sortedBy { it.timestamp }
        return laterBills.any { it.amountPaid >= bill.remainingDue || it.remainingDue <= 0.0 }
    }

    fun wasHistoricalAdvanceConsumed(bill: Bill): Boolean {
        if (bill.remainingDue >= 0.0) return false
        val laterBills = _bills.value
            .filter { it.tenantId == bill.tenantId && it.timestamp > bill.timestamp }
        return laterBills.isNotEmpty()
    }

    fun getTenantForBill(bill: Bill): Tenant? = _tenants.value.find { it.id == bill.tenantId }

    fun getRoomForBill(bill: Bill): Room? = _rooms.value.find { it.id == bill.roomId }

    private fun saveToLocalStorage() {
        viewModelScope.launch {
            try {
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

                val roomArr = JSONArray()
                _rooms.value.forEach { r ->
                    val obj = JSONObject()
                    obj.put("id", r.id)
                    obj.put("propertyId", r.propertyId)
                    obj.put("roomNumber", r.roomNumber)
                    obj.put("baseRent", r.baseRent)
                    obj.put("electricityRate", r.electricityRate)
                    obj.put("initialMeterReading", r.initialMeterReading)
                    obj.put("isOccupied", r.isOccupied)
                    obj.put("currentTenantId", r.currentTenantId)

                    val rateArr = JSONArray()
                    r.rateHistory.forEach { rh ->
                        val rhObj = JSONObject()
                        rhObj.put("id", rh.id)
                        rhObj.put("timestamp", rh.timestamp)
                        rhObj.put("previousRent", rh.previousRent)
                        rhObj.put("newRent", rh.newRent)
                        rhObj.put("previousElectricityRate", rh.previousElectricityRate)
                        rhObj.put("newElectricityRate", rh.newElectricityRate)
                        rateArr.put(rhObj)
                    }
                    obj.put("rateHistory", rateArr)
                    roomArr.put(obj)
                }
                prefs.edit().putString("saved_rooms", roomArr.toString()).apply()

                val tenantArr = JSONArray()
                _tenants.value.forEach {
                    val obj = JSONObject()
                    obj.put("id", it.id)
                    obj.put("roomId", it.roomId)
                    obj.put("name", it.name)
                    obj.put("phoneNumber", it.phoneNumber)
                    obj.put("aadhaarNumber", it.aadhaarNumber)
                    obj.put("permanentAddress", it.permanentAddress)
                    obj.put("moveInDate", it.moveInDate)
                    if (it.moveOutDate != null) obj.put("moveOutDate", it.moveOutDate)
                    obj.put("isCurrent", it.isCurrent)
                    tenantArr.put(obj)
                }
                prefs.edit().putString("saved_tenants", tenantArr.toString()).apply()

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
                    obj.put("rentPaid", it.rentPaid)
                    obj.put("electricityPaid", it.electricityPaid)
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

            val roomStr = prefs.getString("saved_rooms", null)
            if (!roomStr.isNullOrEmpty()) {
                val arr = JSONArray(roomStr)
                val list = mutableListOf<Room>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val rHist = mutableListOf<RateHistoryRecord>()
                    val rateArr = obj.optJSONArray("rateHistory")
                    if (rateArr != null) {
                        for (j in 0 until rateArr.length()) {
                            val rObj = rateArr.getJSONObject(j)
                            rHist.add(RateHistoryRecord(
                                id = rObj.optString("id", UUID.randomUUID().toString()),
                                timestamp = rObj.optLong("timestamp", System.currentTimeMillis()),
                                previousRent = rObj.optDouble("previousRent", 0.0),
                                newRent = rObj.optDouble("newRent", 0.0),
                                previousElectricityRate = rObj.optDouble("previousElectricityRate", 10.0),
                                newElectricityRate = rObj.optDouble("newElectricityRate", 10.0)
                            ))
                        }
                    }
                    list.add(Room(
                        id = obj.getString("id"),
                        propertyId = obj.optString("propertyId", "default_property"),
                        roomNumber = obj.getString("roomNumber"),
                        baseRent = obj.getDouble("baseRent"),
                        electricityRate = obj.optDouble("electricityRate", 10.0),
                        initialMeterReading = obj.optDouble("initialMeterReading", 0.0),
                        isOccupied = obj.optBoolean("isOccupied", false),
                        currentTenantId = obj.optString("currentTenantId", ""),
                        rateHistory = rHist
                    ))
                }
                _rooms.value = list
            }

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
                        phoneNumber = obj.optString("phoneNumber", ""),
                        aadhaarNumber = obj.optString("aadhaarNumber", ""),
                        permanentAddress = obj.optString("permanentAddress", ""),
                        moveInDate = obj.optLong("moveInDate", System.currentTimeMillis()),
                        moveOutDate = if (obj.has("moveOutDate")) obj.getLong("moveOutDate") else null,
                        isCurrent = obj.optBoolean("isCurrent", true)
                    ))
                }
                _tenants.value = list
            }

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
                        rentPaid = obj.optDouble("rentPaid", 0.0),
                        electricityPaid = obj.optDouble("electricityPaid", 0.0),
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

        firestore.collection("users").document(uid).collection("tenants")
            .get().addOnSuccessListener { snaps ->
                if (!snaps.isEmpty) {
                    _tenants.value = snaps.toObjects(Tenant::class.java)
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

    private fun deleteRoomFromCloud(roomId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("rooms")
            .document(roomId).delete()
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
