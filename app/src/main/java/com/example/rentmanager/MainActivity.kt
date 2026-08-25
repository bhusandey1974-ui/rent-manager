package com.example.rentmanager

import android.app.Application
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------- DATABASE ENTITIES ----------------
@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roomNumber: String,
    val phone: String,
    val defaultBaseRent: Double,
    val electricityRatePerUnit: Double,
    val lastMeterReading: Double = 0.0
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

// ---------------- DAO & DATABASE ----------------
@Dao
interface AppDao {
    @Query("SELECT * FROM tenants ORDER BY roomNumber ASC")
    fun getAllTenants(): Flow<List<Tenant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: Tenant): Long

    @Delete
    suspend fun deleteTenant(tenant: Tenant)

    @Query("SELECT * FROM rent_bills WHERE tenantId = :tenantId ORDER BY id DESC")
    fun getBillsForTenant(tenantId: Long): Flow<List<RentBill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ---------------- VIEWMODEL ----------------
class RentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val allTenants: StateFlow<List<Tenant>> = dao.getAllTenants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getBillsForTenant(tenantId: Long): Flow<List<RentBill>> = dao.getBillsForTenant(tenantId)

    fun addTenant(name: String, room: String, phone: String, rent: Double, rate: Double, reading: Double) {
        viewModelScope.launch {
            dao.insertTenant(Tenant(0, name, room, phone, rent, rate, reading))
        }
    }

    fun deleteTenant(tenant: Tenant) {
        viewModelScope.launch { dao.deleteTenant(tenant) }
    }

    fun addMonthlyBill(tenant: Tenant, currReading: Double, baseRent: Double, amountPaid: Double, mode: String) {
        viewModelScope.launch {
            val prevReading = tenant.lastMeterReading
            val units = (currReading - prevReading).coerceAtLeast(0.0)
            val elecAmount = units * tenant.electricityRatePerUnit
            val total = baseRent + elecAmount
            val due = total - amountPaid
            val curDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

            val bill = RentBill(
                0, tenant.id, monthYear, baseRent, prevReading, currReading,
                units, tenant.electricityRatePerUnit, elecAmount, total, amountPaid, due, curDate, mode
            )
            dao.insertBill(bill)
            dao.updateTenant(tenant.copy(lastMeterReading = currReading))
        }
    }
}

// ---------------- MAIN ACTIVITY & UI ----------------
class MainActivity : ComponentActivity() {
    private val viewModel: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RentViewModel) {
    val tenants by viewModel.allTenants.collectAsState()
    var showAddTenantDialog by remember { mutableStateOf(false) }
    var selectedTenantForBill by remember { mutableStateOf<Tenant?>(null) }
    var selectedTenantForLedger by remember { mutableStateOf<Tenant?>(null) }

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
        if (tenants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tenants found.\nTap + to add a room.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tenants) { tenant ->
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
            onDismiss = { showAddTenantDialog = false },
            onSave = { name, room, phone, rent, rate, reading ->
                viewModel.addTenant(name, room, phone, rent, rate, reading)
                showAddTenantDialog = false
            }
        )
    }

    selectedTenantForBill?.let { tenant ->
        AddMonthlyBillDialog(
            tenant = tenant,
            onDismiss = { selectedTenantForBill = null },
            onSave = { currReading, baseRent, amountPaid, mode ->
                viewModel.addMonthlyBill(tenant, currReading, baseRent, amountPaid, mode)
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
fun TenantCard(tenant: Tenant, onAddBillClick: () -> Unit, onViewHistoryClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Room ${tenant.roomNumber} - ${tenant.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Phone: ${tenant.phone}", fontSize = 13.sp, color = Color.DarkGray)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rent: ₹${tenant.defaultBaseRent}", fontSize = 14.sp)
                Text("Rate: ₹${tenant.electricityRatePerUnit}/unit", fontSize = 14.sp)
                Text("Meter: ${tenant.lastMeterReading}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onViewHistoryClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("History")
                }
                Button(onClick = onAddBillClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Bill")
                }
            }
        }
    }
}

@Composable
fun AddTenantDialog(onDismiss: () -> Unit, onSave: (String, String, String, Double, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var baseRent by remember { mutableStateOf("") }
    var ratePerUnit by remember { mutableStateOf("") }
    var initialReading by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tenant Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = roomNumber, onValueChange = { roomNumber = it }, label = { Text("Room / Flat No.") })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tenant Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = baseRent, onValueChange = { baseRent = it }, label = { Text("Monthly Rent (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = ratePerUnit, onValueChange = { ratePerUnit = it }, label = { Text("Rate per Unit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = initialReading, onValueChange = { initialReading = it }, label = { Text("Current Meter Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, roomNumber, phone, baseRent.toDoubleOrNull() ?: 0.0, ratePerUnit.toDoubleOrNull() ?: 0.0, initialReading.toDoubleOrNull() ?: 0.0)
                },
                enabled = name.isNotBlank() && roomNumber.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddMonthlyBillDialog(tenant: Tenant, onDismiss: () -> Unit, onSave: (Double, Double, Double, String) -> Unit) {
    var currReadingStr by remember { mutableStateOf("") }
    var baseRentStr by remember { mutableStateOf(tenant.defaultBaseRent.toString()) }
    var amountPaidStr by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }

    val currReading = currReadingStr.toDoubleOrNull() ?: tenant.lastMeterReading
    val baseRent = baseRentStr.toDoubleOrNull() ?: 0.0
    val unitsConsumed = (currReading - tenant.lastMeterReading).coerceAtLeast(0.0)
    val elecAmount = unitsConsumed * tenant.electricityRatePerUnit
    val totalBill = baseRent + elecAmount
    val amountPaid = amountPaidStr.toDoubleOrNull() ?: 0.0
    val due = totalBill - amountPaid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Rent: Room ${tenant.roomNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Previous Meter: ${tenant.lastMeterReading}", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = currReadingStr, onValueChange = { currReadingStr = it }, label = { Text("Current Meter Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = baseRentStr, onValueChange = { baseRentStr = it }, label = { Text("Base Rent (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                HorizontalDivider()
                Text("Units: $unitsConsumed ($unitsConsumed × ₹${tenant.electricityRatePerUnit} = ₹$elecAmount)")
                Text("Total Payable: ₹$totalBill", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider()
                OutlinedTextField(value = amountPaidStr, onValueChange = { amountPaidStr = it }, label = { Text("Amount Paid Now (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Text("Remaining Due: ₹$due", color = if (due > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cash", "UPI", "Bank").forEach { mode ->
                        FilterChip(selected = paymentMode == mode, onClick = { paymentMode = mode }, label = { Text(mode) })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(currReading, baseRent, amountPaid, paymentMode) }, enabled = currReadingStr.isNotBlank()) { Text("Save Bill") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantHistoryBottomSheet(tenant: Tenant, viewModel: RentViewModel, onDismiss: () -> Unit) {
    val bills by viewModel.getBillsForTenant(tenant.id).collectAsState(initial = emptyList())
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Ledger: Room ${tenant.roomNumber} (${tenant.name})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (bills.isEmpty()) {
                Text("No past billing records found.", color = Color.Gray, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bills) { bill ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(bill.monthYear, fontWeight = FontWeight.Bold)
                                    Text(bill.paymentDate, fontSize = 12.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Rent: ₹${bill.baseRent} | Elec: ₹${bill.electricityAmount} (${bill.unitsConsumed} units)")
                                Text("Total: ₹${bill.totalBillAmount} | Paid: ₹${bill.amountPaid} (${bill.paymentMode})")
                                if (bill.dueAmount > 0) {
                                    Text("Due: ₹${bill.dueAmount}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { shareReceiptOnWhatsApp(context, tenant, bill) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share WhatsApp Receipt")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun shareReceiptOnWhatsApp(context: Context, tenant: Tenant, bill: RentBill) {
    val message = """
        *RENT & ELECTRICITY RECEIPT*
        ----------------------------------
        *Tenant:* ${tenant.name} (Room: ${tenant.roomNumber})
        *Period:* ${bill.monthYear}
        *Date:* ${bill.paymentDate}
        
        *Electricity Breakdown:*
        - Prev Reading: ${bill.prevMeterReading}
        - Curr Reading: ${bill.currMeterReading}
        - Units Consumed: ${bill.unitsConsumed}
        - Rate/Unit: ₹${bill.electricityRate}
        - Total Elec Bill: ₹${bill.electricityAmount}
        
        *Base Rent:* ₹${bill.baseRent}
        *Total Bill:* ₹${bill.totalBillAmount}
        ----------------------------------
        *Amount Paid:* ₹${bill.amountPaid} (${bill.paymentMode})
        *Pending Due:* ₹${bill.dueAmount}
        ----------------------------------
        Thank you!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Share Receipt via"))
}
