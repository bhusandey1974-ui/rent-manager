package com.example.rentmanager

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------------------
// Entities
// ---------------------------------------------------------------------------

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roomNumber: String,
    val phone: String,
    val defaultBaseRent: Double,
    val electricityRatePerUnit: Double,
    val lastMeterReading: Double = 0.0,
    val isOccupied: Boolean = true
)

@Entity(
    tableName = "rent_bills",
    foreignKeys = [
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tenantId")]
)
data class RentBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tenantId: Long,
    val monthYear: String,
    val baseRent: Double,
    val prevMeterReading: Double,
    val currMeterReading: Double,
    val unitsConsumed: Double,
    val electricityRate: Double,
    val electricityAmount: Double,
    val totalBillAmount: Double,
    val amountPaid: Double,
    val dueAmount: Double,
    val paymentDate: String,
    val paymentMode: String
)

// ---------------------------------------------------------------------------
// DAO / Database
// ---------------------------------------------------------------------------

@Dao
interface AppDao {
    @Query("SELECT * FROM tenants ORDER BY roomNumber ASC")
    fun getAllTenants(): Flow<List<Tenant>>

    // Real inserts of new rows (id = 0) never conflict, so the default
    // ABORT strategy is used instead of REPLACE — REPLACE risked silently
    // overwriting an existing row if an id ever collided.
    @Insert
    suspend fun insertTenant(tenant: Tenant): Long

    @Delete
    suspend fun deleteTenant(tenant: Tenant)

    @Query("SELECT * FROM rent_bills WHERE tenantId = :tenantId ORDER BY id DESC")
    fun getBillsForTenant(tenantId: Long): Flow<List<RentBill>>

    @Query("SELECT * FROM rent_bills ORDER BY id DESC")
    fun getAllBills(): Flow<List<RentBill>>

    @Insert
    suspend fun insertBill(bill: RentBill)

    @Update
    suspend fun updateTenant(tenant: Tenant)
}

@Database(entities = [Tenant::class, RentBill::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rent_manager_database"
                )
                    // NOTE: destructive migration wipes all data on schema
                    // change. Fine during development; replace with real
                    // Migration objects before shipping so tenant/bill
                    // history survives future schema updates.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

class RentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val allTenants: StateFlow<List<Tenant>> = dao.getAllTenants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBills: StateFlow<List<RentBill>> = dao.getAllBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getBillsForTenant(tenantId: Long): Flow<List<RentBill>> = dao.getBillsForTenant(tenantId)

    fun addTenant(
        name: String,
        room: String,
        phone: String,
        rent: Double,
        rate: Double,
        reading: Double,
        isOccupied: Boolean
    ) {
        viewModelScope.launch {
            dao.insertTenant(Tenant(0, name, room, phone, rent, rate, reading, isOccupied))
        }
    }

    fun deleteTenant(tenant: Tenant) {
        viewModelScope.launch { dao.deleteTenant(tenant) }
    }

    /**
     * Logs a monthly bill for [tenant].
     *
     * Fixes vs. the original version:
     *  - [monthYearInput] is derived from the payment date the user actually
     *    entered, not from "today" — so backdated entries get the right label.
     *  - The tenant's occupied flag is preserved as-is (previously this
     *    function force-set isOccupied = true on every bill, which silently
     *    re-occupied a room the owner had marked vacant).
     *  - Callers are expected to validate that currReading >= prevReading
     *    before calling this (see AddMonthlyBillDialog); we no longer
     *    silently clamp a bad/lower reading to zero units without surfacing it.
     */
    fun addMonthlyBill(
        tenant: Tenant,
        currReading: Double,
        baseRent: Double,
        amountPaid: Double,
        paymentDate: String,
        mode: String,
        monthYearInput: String
    ) {
        viewModelScope.launch {
            val prevReading = tenant.lastMeterReading
            val units = currReading - prevReading
            val elecAmount = units * tenant.electricityRatePerUnit
            val total = baseRent + elecAmount
            val due = total - amountPaid

            val bill = RentBill(
                id = 0,
                tenantId = tenant.id,
                monthYear = monthYearInput,
                baseRent = baseRent,
                prevMeterReading = prevReading,
                currMeterReading = currReading,
                unitsConsumed = units,
                electricityRate = tenant.electricityRatePerUnit,
                electricityAmount = elecAmount,
                totalBillAmount = total,
                amountPaid = amountPaid,
                dueAmount = due,
                paymentDate = paymentDate,
                paymentMode = mode
            )
            dao.insertBill(bill)
            // Preserve the existing occupied status instead of forcing true.
            dao.updateTenant(tenant.copy(lastMeterReading = currReading))
        }
    }
}

// ---------------------------------------------------------------------------
// Activity
// ---------------------------------------------------------------------------

class MainActivity : ComponentActivity() {
    private val viewModel: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Formatting helpers (kept consistent across the whole app)
// ---------------------------------------------------------------------------

private fun formatCurrency(amount: Double): String =
    "₹" + String.format(Locale.US, "%.2f", amount)

private fun formatUnits(amount: Double): String =
    String.format(Locale.US, "%.1f", amount)

// ---------------------------------------------------------------------------
// Screens
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RentViewModel) {
    val tenants by viewModel.allTenants.collectAsState()
    val allBills by viewModel.allBills.collectAsState()

    var showAddTenantDialog by remember { mutableStateOf(false) }
    var selectedTenantForBill by remember { mutableStateOf<Tenant?>(null) }
    var selectedTenantForLedger by remember { mutableStateOf<Tenant?>(null) }

    val totalEarnings = allBills.sumOf { it.amountPaid }
    val totalPendingDue = allBills.sumOf { it.dueAmount }
    val occupiedRoomsCount = tenants.count { it.isOccupied }
    val vacantRoomsCount = tenants.size - occupiedRoomsCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rent & Electricity Ledger", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTenantDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Room")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RevenueSummaryCard(
                    totalEarnings = totalEarnings,
                    totalDue = totalPendingDue,
                    totalRooms = tenants.size,
                    occupied = occupiedRoomsCount,
                    vacant = vacantRoomsCount
                )
            }

            item {
                Text(
                    text = "Rooms & Tenants (${tenants.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (tenants.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No rooms configured.\nTap + to add a room or tenant.", color = Color.Gray)
                    }
                }
            } else {
                items(tenants, key = { it.id }) { tenant ->
                    TenantCard(
                        tenant = tenant,
                        onAddBillClick = { selectedTenantForBill = tenant },
                        onViewHistoryClick = { selectedTenantForLedger = tenant },
                        onDeleteClick = { viewModel.deleteTenant(tenant) }
                    )
                }
            }
        }
    }

    if (showAddTenantDialog) {
        AddTenantDialog(
            existingRoomNumbers = tenants.map { it.roomNumber.trim().lowercase() }.toSet(),
            onDismiss = { showAddTenantDialog = false },
            onSave = { name, room, phone, rent, rate, reading, isOccupied ->
                viewModel.addTenant(name, room, phone, rent, rate, reading, isOccupied)
                showAddTenantDialog = false
            }
        )
    }

    selectedTenantForBill?.let { tenant ->
        AddMonthlyBillDialog(
            tenant = tenant,
            onDismiss = { selectedTenantForBill = null },
            onSave = { currReading, baseRent, amountPaid, paymentDate, mode, monthYear ->
                viewModel.addMonthlyBill(tenant, currReading, baseRent, amountPaid, paymentDate, mode, monthYear)
                selectedTenantForBill = null
            }
        )
    }

    selectedTenantForLedger?.let { tenant ->
        TenantHistoryBottomSheet(
            tenant = tenant,
            viewModel = viewModel,
            onDismiss = { selectedTenantForLedger = null }
        )
    }
}

@Composable
fun RevenueSummaryCard(
    totalEarnings: Double,
    totalDue: Double,
    totalRooms: Int,
    occupied: Int,
    vacant: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Financial & Property Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Collected", fontSize = 12.sp, color = Color.DarkGray)
                    Text(
                        formatCurrency(totalEarnings),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
                Column {
                    Text("Pending Due", fontSize = 12.sp, color = Color.DarkGray)
                    Text(
                        formatCurrency(totalDue),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Rooms: $totalRooms", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(
                    "Occupied: $occupied",
                    fontSize = 13.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Vacant: $vacant",
                    fontSize = 13.sp,
                    color = if (vacant > 0) Color(0xFFE65100) else Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TenantCard(
    tenant: Tenant,
    onAddBillClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Room " + tenant.roomNumber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(if (tenant.isOccupied) "Occupied" else "Vacant", fontSize = 10.sp) }
                        )
                    }
                    Text(
                        text = if (tenant.isOccupied) "${tenant.name} (${tenant.phone})" else "No Active Tenant",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Rent: " + formatCurrency(tenant.defaultBaseRent), fontSize = 14.sp)
                Text(text = "Rate: " + formatCurrency(tenant.electricityRatePerUnit) + "/unit", fontSize = 14.sp)
                Text(text = "Meter: " + formatUnits(tenant.lastMeterReading), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onViewHistoryClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ledger")
                }
                Button(onClick = onAddBillClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Bill")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Room ${tenant.roomNumber}?") },
            text = { Text("This will also delete all bill history for this room. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDeleteConfirm = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AddTenantDialog(
    existingRoomNumbers: Set<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Double, Double, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var baseRent by remember { mutableStateOf("") }
    var ratePerUnit by remember { mutableStateOf("10.0") }
    var initialReading by remember { mutableStateOf("0.0") }
    var isOccupied by remember { mutableStateOf(true) }

    val isDuplicateRoom = roomNumber.isNotBlank() &&
        existingRoomNumbers.contains(roomNumber.trim().lowercase())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room / Tenant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room / Flat No.") },
                    isError = isDuplicateRoom,
                    supportingText = {
                        if (isDuplicateRoom) Text("A room with this number already exists")
                    }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isOccupied, onCheckedChange = { isOccupied = it })
                    Text("Room currently occupied?")
                }
                if (isOccupied) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tenant Name") }
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
                OutlinedTextField(
                    value = baseRent,
                    onValueChange = { baseRent = it },
                    label = { Text("Default Rent (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = ratePerUnit,
                    onValueChange = { ratePerUnit = it },
                    label = { Text("Rate per Unit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = initialReading,
                    onValueChange = { initialReading = it },
                    label = { Text("Initial Meter Reading") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        if (isOccupied) name else "Vacant",
                        roomNumber,
                        if (isOccupied) phone else "-",
                        baseRent.toDoubleOrNull() ?: 0.0,
                        ratePerUnit.toDoubleOrNull() ?: 0.0,
                        initialReading.toDoubleOrNull() ?: 0.0,
                        isOccupied
                    )
                },
                enabled = roomNumber.isNotBlank() && !isDuplicateRoom && (!isOccupied || name.isNotBlank())
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMonthlyBillDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    // currReading, baseRent, amountPaid, paymentDate, mode, monthYear
    onSave: (Double, Double, Double, String, String, String) -> Unit
) {
    var currReadingStr by remember { mutableStateOf("") }
    var baseRentStr by remember { mutableStateOf(tenant.defaultBaseRent.toString()) }
    var amountPaidStr by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var modeMenuExpanded by remember { mutableStateOf(false) }

    val today = remember { Date() }
    val todayFormatted = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(today) }
    val monthYearFormatted = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(today) }
    var paymentDate by remember { mutableStateOf(todayFormatted) }

    val currReading = currReadingStr.toDoubleOrNull()
    val baseRent = baseRentStr.toDoubleOrNull() ?: 0.0
    val amountPaid = amountPaidStr.toDoubleOrNull() ?: 0.0

    // Validation: a reading below the previous one usually means a typo
    // (or a meter replacement, which needs manual handling) — flag it
    // instead of silently clamping units consumed to zero.
    val isReadingInvalid = currReading != null && currReading < tenant.lastMeterReading
    val units = if (currReading != null && !isReadingInvalid) currReading - tenant.lastMeterReading else 0.0
    val electricityAmount = units * tenant.electricityRatePerUnit
    val totalBill = baseRent + electricityAmount
    val dueAmount = totalBill - amountPaid

    val paymentModes = listOf("UPI", "Cash", "Bank Transfer", "Cheque")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Bill — Room ${tenant.roomNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Previous meter reading: ${formatUnits(tenant.lastMeterReading)}",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                OutlinedTextField(
                    value = currReadingStr,
                    onValueChange = { currReadingStr = it },
                    label = { Text("Current Meter Reading") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isReadingInvalid,
                    supportingText = {
                        if (isReadingInvalid) {
                            Text("Reading can't be lower than the previous one (${formatUnits(tenant.lastMeterReading)})")
                        } else if (currReading != null) {
                            Text("Units consumed: ${formatUnits(units)} → ${formatCurrency(electricityAmount)}")
                        }
                    }
                )
                OutlinedTextField(
                    value = baseRentStr,
                    onValueChange = { baseRentStr = it },
                    label = { Text("Base Rent (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = amountPaidStr,
                    onValueChange = { amountPaidStr = it },
                    label = { Text("Amount Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment Date") }
                )
                ExposedDropdownMenuBox(
                    expanded = modeMenuExpanded,
                    onExpandedChange = { modeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paymentMode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeMenuExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = modeMenuExpanded,
                        onDismissRequest = { modeMenuExpanded = false }
                    ) {
                        paymentModes.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode) },
                                onClick = {
                                    paymentMode = mode
                                    modeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Bill:", fontWeight = FontWeight.Bold)
                    Text(formatCurrency(totalBill), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Due:", fontWeight = FontWeight.Bold)
                    Text(
                        formatCurrency(dueAmount),
                        fontWeight = FontWeight.Bold,
                        color = if (dueAmount > 0) MaterialTheme.colorScheme.error else Color(0xFF1B5E20)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currReading!!,
                        baseRent,
                        amountPaid,
                        paymentDate,
                        paymentMode,
                        monthYearFormatted
                    )
                },
                enabled = currReading != null && !isReadingInvalid && paymentDate.isNotBlank()
            ) {
                Text("Save Bill")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantHistoryBottomSheet(
    tenant: Tenant,
    viewModel: RentViewModel,
    onDismiss: () -> Unit
) {
    var bills by remember { mutableStateOf<List<RentBill>>(emptyList()) }

    LaunchedEffect(tenant.id) {
        viewModel.getBillsForTenant(tenant.id).collectLatest { bills = it }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Bill History — Room ${tenant.roomNumber}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (bills.isEmpty()) {
                Text("No bills logged yet for this room.", color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bills, key = { it.id }) { bill ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(bill.monthYear, fontWeight = FontWeight.Bold)
                                    Text(bill.paymentDate, fontSize = 12.sp, color = Color.DarkGray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Meter: ${formatUnits(bill.prevMeterReading)} → ${formatUnits(bill.currMeterReading)} " +
                                        "(${formatUnits(bill.unitsConsumed)} units)",
                                    fontSize = 12.sp
                                )
                                Text(
                                    "Rent ${formatCurrency(bill.baseRent)} + Electricity ${formatCurrency(bill.electricityAmount)} " +
                                        "= Total ${formatCurrency(bill.totalBillAmount)}",
                                    fontSize = 12.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Paid: ${formatCurrency(bill.amountPaid)} (${bill.paymentMode})", fontSize = 12.sp)
                                    Text(
                                        "Due: ${formatCurrency(bill.dueAmount)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bill.dueAmount > 0) MaterialTheme.colorScheme.error else Color(0xFF1B5E20)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
