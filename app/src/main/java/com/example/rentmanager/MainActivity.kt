package com.example.rentmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val vm: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                typography = Typography(
                    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp),
                    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp),
                    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                )
            ) {
                MainAppScreen(vm)
            }
        }
    }
}

enum class NavigationTab {
    PROPERTIES, REVENUE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(vm: RentViewModel) {
    val auth = remember { FirebaseAuth.getInstance() }
    var isAuthenticated by remember { mutableStateOf(auth.currentUser != null) }
    var currentTab by remember { mutableStateOf(NavigationTab.PROPERTIES) }

    var showSettingsMenu by remember { mutableStateOf(false) }
    var showResetStep1Dialog by remember { mutableStateOf(false) }
    var showResetStep2Dialog by remember { mutableStateOf(false) }

    // Dialog state holders
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var roomToAssignTenant by remember { mutableStateOf<RoomUnit?>(null) }
    var roomToLodgeBill by remember { mutableStateOf<RoomUnit?>(null) }
    var roomToVacate by remember { mutableStateOf<RoomUnit?>(null) }
    var roomForHistory by remember { mutableStateOf<RoomUnit?>(null) }

    if (!isAuthenticated) {
        AuthView(
            onLoginSuccess = { uid ->
                isAuthenticated = true
                vm.loadCloudData(uid)
            },
            onSkipOffline = {
                isAuthenticated = true
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Rent Manager",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = auth.currentUser?.email
                                    ?: auth.currentUser?.phoneNumber
                                    ?: "Offline Mode",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF64748B)
                            )
                        }
                    },
                    actions = {
                        // Soft & Clean "+ Add Property" Button in Top Right
                        FilledTonalButton(
                            onClick = { showAddRoomDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFEFF6FF),
                                contentColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Property", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                        }

                        // 3-dot settings menu
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Settings",
                                tint = Color(0xFF475569)
                            )
                        }

                        DropdownMenu(
                            expanded = showSettingsMenu,
                            onDismissRequest = { showSettingsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sign Out", fontFamily = FontFamily.SansSerif) },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) },
                                onClick = {
                                    showSettingsMenu = false
                                    vm.signOut { isAuthenticated = false }
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Clear All Data (Reset)",
                                        color = Color(0xFFEF4444),
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444)
                                    )
                                },
                                onClick = {
                                    showSettingsMenu = false
                                    showResetStep1Dialog = true
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.PROPERTIES,
                        onClick = { currentTab = NavigationTab.PROPERTIES },
                        icon = { Icon(Icons.Default.Domain, contentDescription = "Properties") },
                        label = { Text("Properties", fontFamily = FontFamily.SansSerif) }
                    )
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.REVENUE,
                        onClick = { currentTab = NavigationTab.REVENUE },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Revenue") },
                        label = { Text("Revenue", fontFamily = FontFamily.SansSerif) }
                    )
                }
            },
            floatingActionButton = {
                if (currentTab == NavigationTab.PROPERTIES) {
                    FloatingActionButton(
                        onClick = { showAddRoomDialog = true },
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Property")
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8FAFC))
            ) {
                if (currentTab == NavigationTab.PROPERTIES) {
                    PropertiesTabContent(
                        vm = vm,
                        onShowAddRoom = { showAddRoomDialog = true },
                        onAssignTenant = { roomToAssignTenant = it },
                        onLodgeBill = { roomToLodgeBill = it },
                        onVacate = { roomToVacate = it },
                        onViewHistory = { roomForHistory = it }
                    )
                } else {
                    RevenueView(vm = vm)
                }
            }
        }
                // --- DIALOGS ---

        // 1. Account Reset: Step 1 Confirmation
        if (showResetStep1Dialog) {
            AlertDialog(
                onDismissRequest = { showResetStep1Dialog = false },
                title = { Text("Clear All Account Data?", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Text(
                        "This action will permanently delete all rooms, active tenants, past records, and billing ledgers stored on this device and your cloud account.",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetStep1Dialog = false
                            showResetStep2Dialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Proceed", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetStep1Dialog = false }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }

        // 2. Account Reset: Step 2 Final Confirmation
        if (showResetStep2Dialog) {
            AlertDialog(
                onDismissRequest = { showResetStep2Dialog = false },
                title = { Text("Final Warning: Irreversible", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontFamily = FontFamily.SansSerif) },
                text = {
                    Text(
                        "Are you absolutely certain? Once erased, your tenant history and billing records cannot be recovered.",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetStep2Dialog = false
                            vm.clearAllUserData {
                                isAuthenticated = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Erase Everything", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetStep2Dialog = false }) {
                        Text("Keep Data", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }

        // 3. Add Property / Room Dialog
        if (showAddRoomDialog) {
            var roomNum by remember { mutableStateOf("") }
            var baseRentStr by remember { mutableStateOf("") }
            var elecRateStr by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddRoomDialog = false },
                title = { Text("Add New Property Unit", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = roomNum,
                            onValueChange = { roomNum = it },
                            label = { Text("Property / Room Number", fontFamily = FontFamily.SansSerif) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = baseRentStr,
                            onValueChange = { baseRentStr = it },
                            label = { Text("Monthly Rent (₹)", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = elecRateStr,
                            onValueChange = { elecRateStr = it },
                            label = { Text("Electricity Rate per Unit (₹)", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val r = baseRentStr.toDoubleOrNull() ?: 0.0
                            val e = elecRateStr.toDoubleOrNull() ?: 0.0
                            if (roomNum.isNotBlank()) {
                                vm.addRoom(roomNum.trim(), r, e)
                                showAddRoomDialog = false
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Save Property", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddRoomDialog = false }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }

        // 4. Assign Tenant Dialog (with Optional Aadhaar & Address)
        roomToAssignTenant?.let { room ->
            var name by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var aadhaar by remember { mutableStateOf("") }
            var address by remember { mutableStateOf("") }
            var depositStr by remember { mutableStateOf("") }
            var initMeterStr by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { roomToAssignTenant = null },
                title = { Text("Assign Tenant • Unit ${room.roomNumber}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Tenant Full Name *", fontFamily = FontFamily.SansSerif) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Mobile Number *", fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = aadhaar,
                                onValueChange = { aadhaar = it },
                                label = { Text("Aadhaar / ID (Optional)", fontFamily = FontFamily.SansSerif) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Permanent Address (Optional)", fontFamily = FontFamily.SansSerif) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = depositStr,
                                onValueChange = { depositStr = it },
                                label = { Text("Security Deposit Paid (₹)", fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = initMeterStr,
                                onValueChange = { initMeterStr = it },
                                label = { Text("Initial Meter Reading", fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                val d = depositStr.toDoubleOrNull() ?: 0.0
                                val m = initMeterStr.toDoubleOrNull() ?: 0.0
                                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                                vm.assignTenant(
                                    roomId = room.id,
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    aadhaarNumber = aadhaar.trim(),
                                    address = address.trim(),
                                    depositAmount = d,
                                    initialReading = m,
                                    moveInDate = dateStr
                                )
                                roomToAssignTenant = null
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Save & Assign", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToAssignTenant = null }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }
                // 5. Lodge Bill Dialog (FIFO Dues, Advance Credit, and WhatsApp Sharing)
        roomToLodgeBill?.let { room ->
            val context = LocalContext.current
            val currentTenant = vm.tenants.collectAsState().value.find { it.id == room.currentTenantId }

            val calendar = Calendar.getInstance()
            val defaultMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

            var monthYear by remember { mutableStateOf(defaultMonthYear) }
            var baseRentStr by remember { mutableStateOf(room.baseRent.toInt().toString()) }
            var currMeterStr by remember { mutableStateOf("") }
            var maintenanceStr by remember { mutableStateOf("0") }
            var paymentMode by remember { mutableStateOf("Cash") }

            // Automatic Carryover: Positive = Due carryover, Negative = Advance credit
            val previousCarryover = remember(room.id) { vm.getPendingDueForRoom(room.id) }

            val currReading = currMeterStr.toDoubleOrNull() ?: room.lastMeterReading
            val elecUnits = (currReading - room.lastMeterReading).coerceAtLeast(0.0)
            val elecAmount = elecUnits * room.electricityRate
            val currentBaseRent = baseRentStr.toDoubleOrNull() ?: 0.0
            val maintenance = maintenanceStr.toDoubleOrNull() ?: 0.0

            val currentPeriodCharge = currentBaseRent + elecAmount + maintenance
            val totalPayable = currentPeriodCharge + previousCarryover

            var amountPaidStr by remember { mutableStateOf(totalPayable.coerceAtLeast(0.0).toInt().toString()) }
            val amountPaid = amountPaidStr.toDoubleOrNull() ?: 0.0
            val netRemainingBalance = totalPayable - amountPaid

            AlertDialog(
                onDismissRequest = { roomToLodgeBill = null },
                title = { Text("Lodge Bill • Unit ${room.roomNumber}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Text(
                                text = "Tenant: ${currentTenant?.name ?: "Occupant"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2563EB),
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        // Carryover Status Badge
                        if (previousCarryover > 0.0) {
                            item {
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⚠️ Unpaid Dues Carried Over: ₹${previousCarryover.toInt()}",
                                        color = Color(0xFFB45309),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        } else if (previousCarryover < 0.0) {
                            item {
                                Surface(
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🟢 Advance Adjusted: -₹${(-previousCarryover).toInt()}",
                                        color = Color(0xFF047857),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = monthYear,
                                onValueChange = { monthYear = it },
                                label = { Text("Billing Month", fontFamily = FontFamily.SansSerif) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = baseRentStr,
                                onValueChange = { baseRentStr = it },
                                label = { Text("Base Rent (₹)", fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = currMeterStr,
                                onValueChange = { currMeterStr = it },
                                label = { Text("Current Meter (Prev: ${room.lastMeterReading.toInt()})", fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Text(
                                text = "Units: ${elecUnits.toInt()}u × ₹${room.electricityRate.toInt()} = ₹${elecAmount.toInt()}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = amountPaidStr,
                                onValueChange = { amountPaidStr = it },
                                label = { Text("Amount Paid Now (₹)", fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Cash", "UPI", "Bank").forEach { mode ->
                                    FilterChip(
                                        selected = paymentMode == mode,
                                        onClick = { paymentMode = mode },
                                        label = { Text(mode, fontFamily = FontFamily.SansSerif) }
                                    )
                                }
                            }
                        }

                        item {
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Net Due:", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                Text(
                                    text = if (netRemainingBalance < 0.0) "Advance: ₹${(-netRemainingBalance).toInt()}" else "₹${netRemainingBalance.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = if (netRemainingBalance > 0.0) Color(0xFFEF4444) else Color(0xFF10B981)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val bill = BillRecord(
                                id = UUID.randomUUID().toString(),
                                roomId = room.id,
                                tenantId = room.currentTenantId,
                                monthYear = monthYear.trim(),
                                baseRent = currentBaseRent,
                                prevMeterReading = room.lastMeterReading,
                                currentMeterReading = currReading,
                                electricityRate = room.electricityRate,
                                maintenanceCharge = maintenance,
                                previousDueCarryover = previousCarryover,
                                amountPaid = amountPaid,
                                remainingDue = netRemainingBalance,
                                paymentMode = paymentMode,
                                timestamp = System.currentTimeMillis()
                            )

                            vm.lodgeBill(bill)
                            roomToLodgeBill = null

                            // Generate WhatsApp Message
                            val message = buildString {
                                appendLine("🏠 *RENT & ELECTRICITY RECEIPT*")
                                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                                appendLine("👤 *Tenant:* ${currentTenant?.name ?: "Occupant"} (Unit ${room.roomNumber})")
                                appendLine("📅 *Billing Period:* $monthYear")
                                appendLine("🏢 *Base Rent:* ₹${currentBaseRent.toInt()}")
                                appendLine("⚡ *Electricity (${elecUnits.toInt()} units):* ₹${elecAmount.toInt()}")
                                if (previousCarryover > 0.0) {
                                    appendLine("⚠️ *Previous Carryover Due:* ₹${previousCarryover.toInt()}")
                                } else if (previousCarryover < 0.0) {
                                    appendLine("🟢 *Advance Credit Adjusted:* -₹${(-previousCarryover).toInt()}")
                                }
                                appendLine("🧾 *Total Net Billed:* ₹${totalPayable.toInt()}")
                                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                                appendLine("✅ *Amount Paid Today:* ₹${amountPaid.toInt()} ($paymentMode)")
                                if (netRemainingBalance > 0.0) {
                                    appendLine("⚠️ *Pending Balance Due:* ₹${netRemainingBalance.toInt()}")
                                } else if (netRemainingBalance < 0.0) {
                                    appendLine("🟢 *Advance Balance Carried Forward:* ₹${(-netRemainingBalance).toInt()}")
                                } else {
                                    appendLine("🎉 *Balance:* Fully Cleared")
                                }
                                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                                appendLine("Thank you!")
                            }

                            val cleanPhone = currentTenant?.phoneNumber?.replace("+", "")?.replace(" ", "") ?: ""
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "WhatsApp not installed. Bill saved locally.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Lodge & Send WhatsApp", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToLodgeBill = null }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }

        // 6. Vacate Room Dialog
        roomToVacate?.let { room ->
            val activeTenant = vm.tenants.collectAsState().value.find { it.id == room.currentTenantId }
            var finalMeterStr by remember { mutableStateOf(room.lastMeterReading.toInt().toString()) }

            AlertDialog(
                onDismissRequest = { roomToVacate = null },
                title = { Text("Vacate Unit ${room.roomNumber}?", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Vacating will archive ${activeTenant?.name ?: "tenant"} and release security deposit tracking.",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            fontFamily = FontFamily.SansSerif
                        )
                        OutlinedTextField(
                            value = finalMeterStr,
                            onValueChange = { finalMeterStr = it },
                            label = { Text("Final Meter Reading", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val fReading = finalMeterStr.toDoubleOrNull() ?: room.lastMeterReading
                            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                            vm.vacateRoom(room.id, fReading, dateStr)
                            roomToVacate = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm Vacate", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToVacate = null }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }

        // 7. Tenant History Dialog
        roomForHistory?.let { room ->
            val tenants by vm.tenants.collectAsState()
            val history = tenants.filter { it.roomId == room.id }

            AlertDialog(
                onDismissRequest = { roomForHistory = null },
                title = { Text("Unit ${room.roomNumber} History", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    if (history.isEmpty()) {
                        Text("No tenant history recorded.", fontFamily = FontFamily.SansSerif, color = Color(0xFF64748B))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(history) { t ->
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(t.name, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                        Text("Phone: ${t.phoneNumber}", fontSize = 12.sp, fontFamily = FontFamily.SansSerif)
                                        Text("Stayed: ${t.moveInDate} - ${t.moveOutDate ?: "Present"}", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { roomForHistory = null }) {
                        Text("Close", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }
    }
}
@Composable
fun PropertiesTabContent(
    vm: RentViewModel,
    onShowAddRoom: () -> Unit,
    onAssignTenant: (RoomUnit) -> Unit,
    onLodgeBill: (RoomUnit) -> Unit,
    onVacate: (RoomUnit) -> Unit,
    onViewHistory: (RoomUnit) -> Unit
) {
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()

    if (rooms.isEmpty()) {
        // Welcoming & Presentable First-Page Hero Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFDBEAFE), Color(0xFFBFDBFE))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Welcome to Rent Manager",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Keep your rental records, meter readings, dues, and WhatsApp receipts organized in one place.",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("1. Add Property", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontFamily = FontFamily.SansSerif)
                                Text("Rent & rate", fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                            }
                            Text("→", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("2. Assign Tenant", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontFamily = FontFamily.SansSerif)
                                Text("Name & phone", fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                            }
                            Text("→", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("3. Send Bill", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontFamily = FontFamily.SansSerif)
                                Text("WhatsApp receipt", fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onShowAddRoom() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Your First Property",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            items(rooms, key = { it.id }) { room ->
                val tenant = tenants.find { it.id == room.currentTenantId }
                val pendingBalance = vm.getPendingDueForRoom(room.id)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (room.isOccupied) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MeetingRoom,
                                        contentDescription = null,
                                        tint = if (room.isOccupied) Color(0xFF2563EB) else Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Unit ${room.roomNumber}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "₹${room.baseRent.toInt()}/mo • ₹${room.electricityRate.toInt()}/u",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }

                            // Active Balance Badge
                            if (room.isOccupied) {
                                if (pendingBalance > 0.0) {
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Due: ₹${pendingBalance.toInt()}",
                                            color = Color(0xFFB45309),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else if (pendingBalance < 0.0) {
                                    Surface(
                                        color = Color(0xFFECFDF5),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Advance: ₹${(-pendingBalance).toInt()}",
                                            color = Color(0xFF047857),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (room.isOccupied && tenant != null) {
                            Text(
                                text = "Tenant: ${tenant.name} (${tenant.phoneNumber})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Last Meter Reading: ${room.lastMeterReading.toInt()} units",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.SansSerif
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onLodgeBill(room) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Text("Lodge Bill", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                }

                                OutlinedButton(
                                    onClick = { onVacate(room) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                                ) {
                                    Text("Vacate", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                }
                            }
                        } else {
                            Text(
                                text = "Currently Vacant",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.SansSerif
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onAssignTenant(room) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Text("Assign Tenant", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                }

                                OutlinedButton(
                                    onClick = { onViewHistory(room) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("History", fontSize = 12.sp, fontFamily = FontFamily.SansSerif)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
