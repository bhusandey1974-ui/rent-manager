package com.example.rentmanager

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ==========================================
// DATA MODELS
// ==========================================

data class Property(
    val id: String = "default_prop",
    val name: String = "Rent Manager"
)

data class RoomUnit(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String = "default_prop",
    val roomNumber: String,
    val unitType: String = "Room",
    val baseRent: Double,
    val electricityRate: Double = 10.0,
    val isVacant: Boolean = true
)

data class Tenant(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String = "default_prop",
    val roomId: String,
    val name: String,
    val phone: String,
    val aadhaarNumber: String = "",
    val moveInDate: String,
    val securityDeposit: Double = 0.0,
    val initialMeterReading: Double = 0.0,
    val isActive: Boolean = true
)

data class BillRecord(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String = "default_prop",
    val roomId: String,
    val tenantId: String,
    val monthYear: String,
    val baseRent: Double,
    val prevMeterReading: Double,
    val currentMeterReading: Double,
    val electricityRate: Double,
    val maintenanceCharge: Double = 0.0,
    val previousDueCarryover: Double = 0.0,
    val amountPaid: Double = 0.0,
    val remainingDue: Double = 0.0,
    val paymentMode: String = "UPI",
    val timestamp: Long = System.currentTimeMillis()
)

data class PastTenancyRecord(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String,
    val tenantName: String,
    val tenantPhone: String,
    val moveInDate: String,
    val vacateDate: String,
    val totalDaysStayed: Long,
    val totalPaid: Double
)
// ==========================================
// VIEWMODEL IMPLEMENTATION (PART 1)
// ==========================================

class RentViewModel(application: Application) : AndroidViewModel(application) {

    private val _rooms = MutableStateFlow<List<RoomUnit>>(emptyList())
    val rooms: StateFlow<List<RoomUnit>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    private val _pastTenancies = MutableStateFlow<List<PastTenancyRecord>>(emptyList())
    val pastTenancies: StateFlow<List<PastTenancyRecord>> = _pastTenancies.asStateFlow()

    fun getTodayDateFormatted(): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    fun getPreviousMonthFormatted(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun addRoom(propertyId: String, roomNumber: String, unitType: String, baseRent: Double, electricityRate: Double) {
        val newRoom = RoomUnit(
            propertyId = propertyId,
            roomNumber = roomNumber,
            unitType = unitType,
            baseRent = baseRent,
            electricityRate = electricityRate,
            isVacant = true
        )
        _rooms.value = _rooms.value + newRoom
    }

    fun editRoom(roomId: String, roomNumber: String, baseRent: Double, electricityRate: Double) {
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) it.copy(roomNumber = roomNumber, baseRent = baseRent, electricityRate = electricityRate) else it
        }
    }

    fun deleteRoom(roomId: String) {
        _rooms.value = _rooms.value.filter { it.id != roomId }
        _tenants.value = _tenants.value.filter { it.roomId != roomId }
        _bills.value = _bills.value.filter { it.roomId != roomId }
        _pastTenancies.value = _pastTenancies.value.filter { it.roomId != roomId }
    }

    fun assignTenant(
        propertyId: String,
        roomId: String,
        name: String,
        phone: String,
        aadhaar: String,
        moveInDate: String,
        securityDeposit: Double,
        initialReading: Double
    ) {
        val newTenant = Tenant(
            propertyId = propertyId,
            roomId = roomId,
            name = name,
            phone = phone,
            aadhaarNumber = aadhaar,
            moveInDate = moveInDate,
            securityDeposit = securityDeposit,
            initialMeterReading = initialReading,
            isActive = true
        )
        _tenants.value = _tenants.value.filter { it.roomId != roomId } + newTenant
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) it.copy(isVacant = false) else it
        }
    }

    fun editTenant(tenantId: String, name: String, phone: String, aadhaar: String) {
        _tenants.value = _tenants.value.map {
            if (it.id == tenantId) it.copy(name = name, phone = phone, aadhaarNumber = aadhaar) else it
        }
    }
        fun checkoutTenant(tenantId: String, vacateDate: String, refundAmount: Double) {
        val tenant = _tenants.value.find { it.id == tenantId } ?: return
        val roomBills = _bills.value.filter { it.tenantId == tenantId }
        val totalPaid = roomBills.sumOf { it.amountPaid }

        val totalDays = try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val d1 = sdf.parse(tenant.moveInDate)
            val d2 = sdf.parse(vacateDate)
            if (d1 != null && d2 != null) {
                val diff = d2.time - d1.time
                TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).coerceAtLeast(1)
            } else 1L
        } catch (e: Exception) {
            1L
        }

        val record = PastTenancyRecord(
            roomId = tenant.roomId,
            tenantName = tenant.name,
            tenantPhone = tenant.phone,
            moveInDate = tenant.moveInDate,
            vacateDate = vacateDate,
            totalDaysStayed = totalDays,
            totalPaid = totalPaid
        )

        _pastTenancies.value = _pastTenancies.value + record
        _tenants.value = _tenants.value.filter { it.id != tenantId }
        _rooms.value = _rooms.value.map {
            if (it.id == tenant.roomId) it.copy(isVacant = true) else it
        }
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
        val units = (curUnit - prevUnit).coerceAtLeast(0.0)
        val electricityTotal = units * rate
        val grandTotal = baseRent + electricityTotal + maintenance + previousDue
        val remaining = (grandTotal - amountPaid).coerceAtLeast(0.0)

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
            remainingDue = remaining,
            paymentMode = paymentMode
        )
        _bills.value = _bills.value + bill
    }

    fun getCumulativePendingDue(roomId: String): Double {
        val lastBill = _bills.value.filter { it.roomId == roomId }.maxByOrNull { it.timestamp }
        return lastBill?.remainingDue ?: 0.0
    }

    fun resetAllRevenueData() {
        _bills.value = emptyList()
    }

    fun clearRoomHistory(roomId: String) {
        _bills.value = _bills.value.filter { it.roomId != roomId }
        _pastTenancies.value = _pastTenancies.value.filter { it.roomId != roomId }
    }

    fun getWhatsAppReceiptText(bill: BillRecord, tenant: Tenant, property: Property, room: RoomUnit): String {
        val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
        val totalElec = units * bill.electricityRate
        val totalAmount = bill.baseRent + totalElec + bill.maintenanceCharge + bill.previousDueCarryover
        val paymentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(bill.timestamp))

        return """
🏠 *RENT & ELECTRICITY RECEIPT*
━━━━━━━━━━━━━━━━━━━━━━━━━
👤 *Tenant:* ${tenant.name} (Room ${room.roomNumber})
📅 *Billing Period:* ${bill.monthYear}
🗓️ *Date of Payment:* $paymentDate

⚡ *Electricity Details:*
• Previous Reading: ${bill.prevMeterReading}
• Current Reading: ${bill.currentMeterReading}
• Units Consumed: $units
• Rate / Unit: ₹${"%.2f".format(bill.electricityRate)}
• Total Electricity: ₹${"%.2f".format(totalElec)}

🏢 *Base Rent:* ₹${"%.2f".format(bill.baseRent)}
🧾 *Total Amount:* ₹${"%.2f".format(totalAmount)}
━━━━━━━━━━━━━━━━━━━━━━━━━
✅ *Amount Paid:* ₹${"%.2f".format(bill.amountPaid)} (${bill.paymentMode})
⚠️ *Pending Due:* ₹${"%.2f".format(bill.remainingDue)}
━━━━━━━━━━━━━━━━━━━━━━━━━
Thank you!
        """.trimIndent()
    }
}
// ==========================================
// COLOR PALETTE & STYLES
// ==========================================

private val UIBluePrimary = Color(0xFF0284C7)
private val UIBlueGradientStart = Color(0xFF1EAEFF)
private val UIBlueGradientEnd = Color(0xFF007AEB)
private val UIAppBg = Color(0xFFFBFDFF)
private val UICardBorder = Color(0xFFE2E8F0)
private val UIDarkText = Color(0xFF0F172A)
private val UIMutedText = Color(0xFF64748B)
private val UIGreenSuccess = Color(0xFF10B981)
private val UIRedDanger = Color(0xFFEF4444)
val CleanFont = FontFamily.SansSerif

fun shareToWhatsApp(context: Context, phone: String, message: String) {
    try {
        val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
        val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(Intent.createChooser(fallbackIntent, "Share Receipt"))
        } catch (ex: Exception) {
            Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }
}

// ==========================================
// MAIN ACTIVITY
// ==========================================

class MainActivity : ComponentActivity() {

    private val rentViewModel: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RentManagerMainApp(vm = rentViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentManagerMainApp(vm: RentViewModel) {
    val context = LocalContext.current
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()
    val bills by vm.bills.collectAsState()
    val pastTenancies by vm.pastTenancies.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) }

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showAssignTenantDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showBillDialog by remember { mutableStateOf<Pair<RoomUnit, Tenant>?>(null) }
    var showCheckoutDialog by remember { mutableStateOf<Tenant?>(null) }
    var showEditRoomDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showRoomHistoryDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showTenantDetailsDialog by remember { mutableStateOf<Pair<Tenant, RoomUnit>?>(null) }
    var showEditTenantDialog by remember { mutableStateOf<Tenant?>(null) }
    var showClearRevenueDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = UIAppBg,
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(UIBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (currentTab == 0) "Rent Manager" else "Revenue & Analytics",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CleanFont,
                                color = UIDarkText
                            )
                            Text(
                                text = if (currentTab == 0) "${rooms.size} Units Registered" else "Lifetime Ledger",
                                fontSize = 13.sp,
                                fontFamily = CleanFont,
                                color = UIMutedText
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavTab(
                        selected = currentTab == 0,
                        label = "Properties",
                        icon = Icons.Default.Apartment,
                        onClick = { currentTab = 0 }
                    )
                    BottomNavTab(
                        selected = currentTab == 1,
                        label = "Revenue",
                        icon = Icons.Default.QueryStats,
                        onClick = { currentTab = 1 }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                FloatingActionButton(
                    onClick = { showAddRoomDialog = true },
                    containerColor = UIBluePrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(UIAppBg)
        ) {
            when (currentTab) {
                0 -> {
                    if (rooms.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, UICardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 44.dp, horizontal = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0F2FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MeetingRoom,
                                            contentDescription = null,
                                            tint = UIBluePrimary,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Text(
                                        text = "No rooms added yet",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CleanFont,
                                        color = UIDarkText
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tap + to add your first room.",
                                        fontSize = 14.sp,
                                        fontFamily = CleanFont,
                                        color = UIMutedText
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(rooms) { room ->
                                val tenant = tenants.find { it.roomId == room.id }
                                CompactRoomCard(
                                    room = room,
                                    tenant = tenant,
                                    onEdit = { showEditRoomDialog = room },
                                    onDelete = { vm.deleteRoom(room.id) },
                                    onHistory = { showRoomHistoryDialog = room },
                                    onAddTenant = { showAssignTenantDialog = room },
                                    onLodgeBill = { tenant?.let { showBillDialog = Pair(room, it) } },
                                    onTenantClick = { tenant?.let { showTenantDetailsDialog = Pair(it, room) } }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    RevenueView(
                        bills = bills,
                        rooms = rooms,
                        tenants = tenants,
                        onClearAll = { showClearRevenueDialog = true },
                        onShareWhatsApp = { bill: BillRecord, tenant: Tenant, room: RoomUnit ->
                            val msg = vm.getWhatsAppReceiptText(bill, tenant, Property(name = "Rent Manager"), room)
                            shareToWhatsApp(context, tenant.phone, msg)
                        }
                    )
                }
            }
        }
    }
        if (showAddRoomDialog) {
        AddRoomDialog(
            onDismiss = { showAddRoomDialog = false },
            onConfirm = { num: String, rent: Double, rate: Double ->
                vm.addRoom("default_prop", num, "Room", rent, rate)
                showAddRoomDialog = false
            }
        )
    }

    showEditRoomDialog?.let { room ->
        EditRoomDialog(
            room = room,
            onDismiss = { showEditRoomDialog = null },
            onConfirm = { num: String, rent: Double, rate: Double ->
                vm.editRoom(room.id, num, rent, rate)
                showEditRoomDialog = null
            }
        )
    }

    showAssignTenantDialog?.let { room ->
        AssignTenantDialog(
            room = room,
            todayDate = vm.getTodayDateFormatted(),
            onDismiss = { showAssignTenantDialog = null },
            onConfirm = { name: String, phone: String, aadhaar: String, date: String, deposit: Double, reading: Double ->
                vm.assignTenant(room.propertyId, room.id, name, phone, aadhaar, date, deposit, reading)
                showAssignTenantDialog = null
            }
        )
    }

    showTenantDetailsDialog?.let { (tenant, room) ->
        TenantDetailsDialog(
            tenant = tenant,
            room = room,
            onDismiss = { showTenantDetailsDialog = null },
            onEdit = {
                showTenantDetailsDialog = null
                showEditTenantDialog = tenant
            },
            onVacate = {
                showTenantDetailsDialog = null
                showCheckoutDialog = tenant
            }
        )
    }

    showEditTenantDialog?.let { tenant ->
        EditTenantDialog(
            tenant = tenant,
            onDismiss = { showEditTenantDialog = null },
            onConfirm = { name: String, phone: String, aadhaar: String ->
                vm.editTenant(tenant.id, name, phone, aadhaar)
                showEditTenantDialog = null
            }
        )
    }

    showBillDialog?.let { (room, tenant) ->
        val lastBill = bills.filter { it.roomId == room.id }.maxByOrNull { it.timestamp }
        val prevReading = lastBill?.currentMeterReading ?: tenant.initialMeterReading
        val pendingDue = vm.getCumulativePendingDue(room.id)

        GenerateBillDialog(
            room = room,
            tenant = tenant,
            prevReading = prevReading,
            previousDue = pendingDue,
            defaultMonth = vm.getPreviousMonthFormatted(),
            onDismiss = { showBillDialog = null },
            onConfirm = { month: String, curReading: Double, maint: Double, paid: Double, mode: String ->
                vm.lodgeBillAndPayment(
                    propertyId = room.propertyId,
                    roomId = room.id,
                    tenantId = tenant.id,
                    month = month,
                    baseRent = room.baseRent,
                    prevUnit = prevReading,
                    curUnit = curReading,
                    rate = room.electricityRate,
                    maintenance = maint,
                    previousDue = pendingDue,
                    amountPaid = paid,
                    paymentMode = mode
                )
                showBillDialog = null
            }
        )
    }

    showCheckoutDialog?.let { tenant ->
        CheckoutTenantDialog(
            tenant = tenant,
            todayDate = vm.getTodayDateFormatted(),
            onDismiss = { showCheckoutDialog = null },
            onConfirm = { date: String, refund: Double ->
                vm.checkoutTenant(tenant.id, date, refund)
                showCheckoutDialog = null
            }
        )
    }

    showRoomHistoryDialog?.let { room ->
        val roomBills = bills.filter { it.roomId == room.id }
        val roomPastTenants = pastTenancies.filter { it.roomId == room.id }
        RoomHistoryDialog(
            room = room,
            bills = roomBills,
            pastTenants = roomPastTenants,
            onClearHistory = {
                vm.clearRoomHistory(room.id)
                showRoomHistoryDialog = null
            },
            onDismiss = { showRoomHistoryDialog = null }
        )
    }

    if (showClearRevenueDialog) {
        AlertDialog(
            onDismissRequest = { showClearRevenueDialog = false },
            title = { Text("Reset Revenue & Counts?", fontWeight = FontWeight.Bold, fontFamily = CleanFont) },
            text = { Text("This will clear all logged bills and reset your revenue statistics to ₹0.00.", fontFamily = CleanFont) },
            confirmButton = {
                Button(
                    onClick = {
                        vm.resetAllRevenueData()
                        showClearRevenueDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UIRedDanger)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearRevenueDialog = false }) {
                    Text("Cancel", fontFamily = CleanFont)
                }
            }
        )
    }
}

@Composable
fun CompactRoomCard(
    room: RoomUnit,
    tenant: Tenant?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onHistory: () -> Unit,
    onAddTenant: () -> Unit,
    onLodgeBill: () -> Unit,
    onTenantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, UICardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (room.isVacant) UIGreenSuccess else UIRedDanger)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Room ${room.roomNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CleanFont,
                        color = UIDarkText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = UIMutedText,
                        modifier = Modifier.size(16.dp).clickable { onEdit() }
                    )
                    if (room.isVacant) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = UIRedDanger,
                            modifier = Modifier.size(18.dp).clickable { onDelete() }
                        )
                    }
                }

                Text(
                    text = "₹${"%,.2f".format(room.baseRent)}/mo",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CleanFont,
                    color = UIBluePrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tenant != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTenantClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tenant.name.take(1).uppercase(),
                                color = UIBluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CleanFont,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tenant.name,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CleanFont,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = UIDarkText
                            )
                            Text(
                                text = "📞 ${tenant.phone}  •  In: ${tenant.moveInDate}",
                                fontSize = 12.sp,
                                fontFamily = CleanFont,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = UIMutedText
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = UIMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onHistory,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, UICardBorder)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("History", fontSize = 13.sp, fontFamily = CleanFont, fontWeight = FontWeight.SemiBold, color = Color(0xFF7C3AED))
                    }

                    Button(
                        onClick = onLodgeBill,
                        modifier = Modifier.weight(1.3f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lodge Bill", fontSize = 13.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = UIGreenSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Status: Vacant", color = UIGreenSuccess, fontSize = 13.sp, fontFamily = CleanFont, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onHistory,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, UICardBorder)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("History", fontSize = 13.sp, fontFamily = CleanFont, fontWeight = FontWeight.SemiBold, color = Color(0xFF7C3AED))
                    }

                    Button(
                        onClick = onAddTenant,
                        modifier = Modifier.weight(1.3f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Tenant", fontSize = 13.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
@Composable
fun BottomNavTab(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) Color(0xFFE0F2FE) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) UIBluePrimary else UIMutedText,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = CleanFont,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) UIBluePrimary else UIMutedText
        )
    }
}

@Composable
fun RevenueView(
    bills: List<BillRecord>,
    rooms: List<RoomUnit>,
    tenants: List<Tenant>,
    onClearAll: () -> Unit,
    onShareWhatsApp: (BillRecord, Tenant, RoomUnit) -> Unit
) {
    var ledgerFilter by remember { mutableStateOf("All") }

    val totalCollected = bills.sumOf { it.amountPaid }
    val totalRent = bills.sumOf { it.baseRent }
    val totalElec = bills.sumOf { (it.currentMeterReading - it.prevMeterReading).coerceAtLeast(0.0) * it.electricityRate }
    val totalDue = bills.sumOf { it.remainingDue }

    val filteredBills = remember(bills, ledgerFilter) {
        when (ledgerFilter) {
            "Paid" -> bills.filter { it.remainingDue <= 0 }
            "Pending" -> bills.filter { it.remainingDue > 0 }
            else -> bills
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(UIBlueGradientStart, UIBlueGradientEnd)))
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIFETIME REVENUE COLLECTION",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontFamily = CleanFont,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            IconButton(onClick = onClearAll, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Clear All Revenue", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "₹${"%,.2f".format(totalCollected)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = CleanFont,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rent Earnings", fontSize = 12.sp, fontFamily = CleanFont, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${"%,.2f".format(totalRent)}", fontSize = 14.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Electricity", fontSize = 12.sp, fontFamily = CleanFont, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${"%,.2f".format(totalElec)}", fontSize = 14.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Total Due", fontSize = 12.sp, fontFamily = CleanFont, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${"%,.2f".format(totalDue)}", fontSize = 14.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, UICardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Year Breakdown (2026)",
                            fontWeight = FontWeight.Bold,
                            fontFamily = CleanFont,
                            fontSize = 16.sp,
                            color = UIDarkText
                        )
                        IconButton(onClick = onClearAll, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Stats", tint = Color(0xFF7C3AED))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        YearStatBox(modifier = Modifier.weight(1f), label = "Base Rent Billed", value = "₹${"%,.2f".format(totalRent)}", valueColor = UIDarkText)
                        YearStatBox(modifier = Modifier.weight(1f), label = "Electricity Charges", value = "₹${"%,.2f".format(totalElec)}", valueColor = UIBluePrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        YearStatBox(modifier = Modifier.weight(1f), label = "Total Invoiced", value = "₹${"%,.2f".format(totalRent + totalElec)}", valueColor = UIBluePrimary)
                        YearStatBox(modifier = Modifier.weight(1f), label = "Collected", value = "₹${"%,.2f".format(totalCollected)}", valueColor = UIGreenSuccess)
                    }
                }
            }
        }
                item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Billing Ledger (${filteredBills.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CleanFont,
                    color = UIDarkText
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Paid", "Pending").forEach { filter ->
                        val isSelected = ledgerFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) UIBluePrimary else Color(0xFFF1F5F9))
                                .clickable { ledgerFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontFamily = CleanFont,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else UIDarkText
                            )
                        }
                    }
                }
            }
        }

        if (filteredBills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, UICardBorder)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("No billing entries found.", color = UIMutedText, fontFamily = CleanFont, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredBills.reversed()) { bill ->
                val room = rooms.find { it.id == bill.roomId }
                val tenant = tenants.find { it.id == bill.tenantId }
                val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
                val totalBillAmount = bill.baseRent + (units * bill.electricityRate) + bill.maintenanceCharge + bill.previousDueCarryover

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, UICardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(bill.monthYear, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 15.sp, color = UIDarkText)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Room ${room?.roomNumber ?: ""} • ${tenant?.name ?: "Tenant"}", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${"%,.2f".format(totalBillAmount)}", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 15.sp, color = UIBluePrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (bill.remainingDue <= 0) "Paid: ₹${"%,.2f".format(bill.amountPaid)}" else "Due: ₹${"%,.2f".format(bill.remainingDue)}",
                                    fontSize = 12.sp,
                                    fontFamily = CleanFont,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (bill.remainingDue <= 0) UIGreenSuccess else UIRedDanger
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = UICardBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Units: $units (${bill.prevMeterReading} → ${bill.currentMeterReading})", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                            if (tenant != null && room != null) {
                                TextButton(
                                    onClick = { onShareWhatsApp(bill, tenant, room) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = UIGreenSuccess, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 12.sp, fontFamily = CleanFont, color = UIGreenSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YearStatBox(modifier: Modifier = Modifier, label: String, value: String, valueColor: Color) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color(0xFFF8FAFC)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, fontSize = 11.sp, fontFamily = CleanFont, color = UIMutedText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

// ==========================================
// DIALOGS & OVERLAYS (PART 1)
// ==========================================

@Composable
fun TenantDetailsDialog(
    tenant: Tenant,
    room: RoomUnit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onVacate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tenant Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = CleanFont,
                    color = UIDarkText
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Tenant", tint = UIBluePrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tenant.name.take(1).uppercase(),
                                color = UIBluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                fontFamily = CleanFont
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(tenant.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFamily = CleanFont, color = UIDarkText)
                            Text("Assigned to Room ${room.roomNumber}", fontSize = 13.sp, fontFamily = CleanFont, color = UIMutedText)
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Tenant Details", tint = UIBluePrimary)
                    }
                }

                Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)

                TenantInfoRow(icon = Icons.Default.Phone, label = "Phone", value = tenant.phone)
                TenantInfoRow(icon = Icons.Default.Badge, label = "Aadhaar / ID", value = if (tenant.aadhaarNumber.isNotBlank()) tenant.aadhaarNumber else "Not Provided")
                TenantInfoRow(icon = Icons.Default.CalendarToday, label = "Move-In Date", value = tenant.moveInDate)
                TenantInfoRow(icon = Icons.Default.Savings, label = "Security Deposit", value = "₹${"%,.2f".format(tenant.securityDeposit)}")
                TenantInfoRow(icon = Icons.Default.ElectricMeter, label = "Initial Meter", value = "${tenant.initialMeterReading} kWh")

                Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)

                Text("Tenancy Records", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = CleanFont, color = UIDarkText)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFEDF2F7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• Assigned on: ${tenant.moveInDate}", fontSize = 12.sp, fontFamily = CleanFont, color = UIDarkText)
                        Text("• Base Room Rent: ₹${room.baseRent}/mo", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                        Text("• Electricity Unit Rate: ₹${room.electricityRate}/kWh", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Close", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onVacate,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, UIRedDanger)
            ) {
                Text("Vacate Tenant", color = UIRedDanger, fontFamily = CleanFont, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun EditTenantDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, aadhaar: String) -> Unit
) {
    var name by remember { mutableStateOf(tenant.name) }
    var phone by remember { mutableStateOf(tenant.phone) }
    var aadhaar by remember { mutableStateOf(tenant.aadhaarNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tenant Info", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = CleanFont, color = UIDarkText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar / ID Card", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name, phone, aadhaar)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}
@Composable
fun TenantInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = UIMutedText, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = UIMutedText, fontSize = 13.sp, fontFamily = CleanFont)
        }
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, fontFamily = CleanFont, color = UIDarkText)
    }
}

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNum: String, baseRent: Double, elecRate: Double) -> Unit
) {
    var roomNum by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = CleanFont, color = UIDarkText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = roomNum,
                    onValueChange = { roomNum = it },
                    label = { Text("Room No (e.g. 101, 01)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate/Unit (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rent.toDoubleOrNull() ?: 0.0
                    val e = rate.toDoubleOrNull() ?: 10.0
                    if (roomNum.isNotBlank() && r > 0) {
                        onConfirm(roomNum, r, e)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Save Room", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}

@Composable
fun AssignTenantDialog(
    room: RoomUnit,
    todayDate: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, aadhaar: String, date: String, deposit: Double, initialMeter: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayDate) }
    var deposit by remember { mutableStateOf("0") }
    var reading by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Tenant to Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = CleanFont, color = UIDarkText) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Name", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar Number", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Move-In Date", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deposit,
                    onValueChange = { deposit = it },
                    label = { Text("Security Deposit (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reading,
                    onValueChange = { reading = it },
                    label = { Text("Initial Meter Reading (kWh)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dep = deposit.toDoubleOrNull() ?: 0.0
                    val meter = reading.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name, phone, aadhaar, date, dep, meter)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Assign Tenant", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}

@Composable
fun GenerateBillDialog(
    room: RoomUnit,
    tenant: Tenant,
    prevReading: Double,
    previousDue: Double,
    defaultMonth: String,
    onDismiss: () -> Unit,
    onConfirm: (month: String, curReading: Double, maint: Double, paid: Double, mode: String) -> Unit
) {
    var month by remember { mutableStateOf(defaultMonth) }
    var curReadingStr by remember { mutableStateOf("") }
    var maintStr by remember { mutableStateOf("0") }
    var paidStr by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("UPI") }

    val curReading = curReadingStr.toDoubleOrNull() ?: prevReading
    val units = (curReading - prevReading).coerceAtLeast(0.0)
    val elecCharge = units * room.electricityRate
    val maint = maintStr.toDoubleOrNull() ?: 0.0
    val totalBill = room.baseRent + elecCharge + maint + previousDue

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lodge Bill - Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = CleanFont, color = UIDarkText) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Billing Month", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Base Rent: ₹${room.baseRent}", fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIDarkText, fontSize = 14.sp)
                Text("Previous Reading: $prevReading kWh", color = UIMutedText, fontFamily = CleanFont, fontSize = 12.sp)

                OutlinedTextField(
                    value = curReadingStr,
                    onValueChange = { curReadingStr = it },
                    label = { Text("Current Meter Reading (kWh)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Units: $units = ₹${"%.2f".format(elecCharge)} (@ ₹${room.electricityRate}/unit)", fontSize = 12.sp, fontFamily = CleanFont, color = UIBluePrimary)

                OutlinedTextField(
                    value = maintStr,
                    onValueChange = { maintStr = it },
                    label = { Text("Maintenance / Other (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (previousDue > 0) {
                    Text("Previous Overdue: ₹$previousDue", color = UIRedDanger, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 12.sp)
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Payable:", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 14.sp, color = UIDarkText)
                        Text("₹${"%,.2f".format(totalBill)}", fontWeight = FontWeight.ExtraBold, fontFamily = CleanFont, fontSize = 16.sp, color = UIBluePrimary)
                    }
                }

                OutlinedTextField(
                    value = paidStr,
                    onValueChange = { paidStr = it },
                    label = { Text("Amount Paid Now (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    placeholder = { Text("Enter ₹${"%,.2f".format(totalBill)}") },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = paidStr.toDoubleOrNull() ?: 0.0
                    if (curReading >= prevReading) {
                        onConfirm(month, curReading, maint, p, mode)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Save Bill", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}

@Composable
fun EditRoomDialog(
    room: RoomUnit,
    onDismiss: () -> Unit,
    onConfirm: (roomNum: String, baseRent: Double, elecRate: Double) -> Unit
) {
    var roomNum by remember { mutableStateOf(room.roomNumber) }
    var rent by remember { mutableStateOf(room.baseRent.toString()) }
    var rate by remember { mutableStateOf(room.electricityRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = CleanFont, color = UIDarkText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = roomNum,
                    onValueChange = { roomNum = it },
                    label = { Text("Room No", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate/Unit (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rent.toDoubleOrNull() ?: room.baseRent
                    val e = rate.toDoubleOrNull() ?: room.electricityRate
                    onConfirm(roomNum, r, e)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}

@Composable
fun CheckoutTenantDialog(
    tenant: Tenant,
    todayDate: String,
    onDismiss: () -> Unit,
    onConfirm: (date: String, refundAmount: Double) -> Unit
) {
    var checkoutDate by remember { mutableStateOf(todayDate) }
    var refundStr by remember { mutableStateOf(tenant.securityDeposit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vacate Tenant: ${tenant.name}", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = CleanFont, color = UIDarkText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Security Deposit: ₹${tenant.securityDeposit}", color = UIDarkText, fontFamily = CleanFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                OutlinedTextField(
                    value = checkoutDate,
                    onValueChange = { checkoutDate = it },
                    label = { Text("Vacate Date", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = refundStr,
                    onValueChange = { refundStr = it },
                    label = { Text("Refund Amount (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val refund = refundStr.toDoubleOrNull() ?: 0.0
                    onConfirm(checkoutDate, refund)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIRedDanger)
            ) {
                Text("Confirm Vacate", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}

@Composable
fun RoomHistoryDialog(
    room: RoomUnit,
    bills: List<BillRecord>,
    pastTenants: List<PastTenancyRecord>,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Room ${room.roomNumber} History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = CleanFont,
                    color = UIDarkText
                )
                if (bills.isNotEmpty() || pastTenants.isNotEmpty()) {
                    IconButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = UIRedDanger
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (pastTenants.isNotEmpty()) {
                    Text(
                        text = "Past Occupants",
                        fontWeight = FontWeight.Bold,
                        fontFamily = CleanFont,
                        fontSize = 15.sp,
                        color = UIDarkText
                    )
                    pastTenants.reversed().forEach { past ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(past.tenantName, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 14.sp, color = UIDarkText)
                                Text("📞 ${past.tenantPhone}", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                                Text("🗓 ${past.moveInDate} → ${past.vacateDate} (${past.totalDaysStayed} days)", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                                Text("💰 Total Paid: ₹${"%,.2f".format(past.totalPaid)}", fontSize = 12.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = UIGreenSuccess)
                            }
                        }
                    }
                    Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                }

                Text(
                    text = "Billing Records",
                    fontWeight = FontWeight.Bold,
                    fontFamily = CleanFont,
                    fontSize = 15.sp,
                    color = UIDarkText
                )
                if (bills.isEmpty()) {
                    Text("No billing history found for this room.", color = UIMutedText, fontFamily = CleanFont, fontSize = 13.sp)
                } else {
                    bills.reversed().forEach { bill ->
                        val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
                        val totalBill = bill.baseRent + (units * bill.electricityRate) + bill.maintenanceCharge + bill.previousDueCarryover
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(bill.monthYear, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 14.sp)
                                    Text("Paid: ₹${bill.amountPaid}", color = UIGreenSuccess, fontFamily = CleanFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("Units: $units (Total: ₹$totalBill)", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Close", fontFamily = CleanFont, fontSize = 14.sp)
            }
        }
    )
}
