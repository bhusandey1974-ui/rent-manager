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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val vm: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
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
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Settings",
                                tint = Color(0xFF0F172A)
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
                                    vm.signOut {
                                        isAuthenticated = false
                                    }
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
                        Icon(Icons.Default.Add, contentDescription = "Add Room")
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

        // 3. Add Room Dialog
        if (showAddRoomDialog) {
            var roomNum by remember { mutableStateOf("") }
            var baseRentStr by remember { mutableStateOf("") }
            var elecRateStr by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddRoomDialog = false },
                title = { Text("Add New Room", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = roomNum,
                            onValueChange = { roomNum = it },
                            label = { Text("Room / Flat Number", fontFamily = FontFamily.SansSerif) },
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
                        Text("Create Room", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddRoomDialog = false }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }

        // 4. Assign Tenant Dialog (with Optional Aadhaar and Address)
        roomToAssignTenant?.let { room ->
            var name by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var aadhaar by remember { mutableStateOf("") }
            var address by remember { mutableStateOf("") }
            var depositStr by remember { mutableStateOf("") }
            var initMeterStr by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { roomToAssignTenant = null },
                title = { Text("Assign Tenant • Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
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
                // 5. Lodge Bill Dialog
        roomToLodgeBill?.let { room ->
            val context = LocalContext.current
            val tenant = vm.tenants.collectAsState().value.find { it.roomId == room.id && it.isActive }
            val prevReading = remember(room.id) { vm.getLatestMeterReading(room.id) }
            val carryoverDue = remember(room.id) { vm.getPendingDueForRoom(room.id) }

            var curReadingStr by remember { mutableStateOf("") }
            var paymentMode by remember { mutableStateOf("Cash") }
            var amountPaidStr by remember { mutableStateOf("") }
            val billingMonth = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()) }

            val curReading = curReadingStr.toDoubleOrNull() ?: prevReading
            val unitsConsumed = (curReading - prevReading).coerceAtLeast(0.0)
            val elecTotal = unitsConsumed * room.electricityRate
            val totalBill = room.baseRent + elecTotal + carryoverDue

            AlertDialog(
                onDismissRequest = { roomToLodgeBill = null },
                title = { Text("Generate Bill • Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Billing Period: $billingMonth", fontSize = 12.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                        Text("Base Rent: ₹${room.baseRent.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
                        Text("Previous Reading: $prevReading", fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)

                        OutlinedTextField(
                            value = curReadingStr,
                            onValueChange = { curReadingStr = it },
                            label = { Text("Current Reading", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Units: $unitsConsumed  •  Elec Cost: ₹${elecTotal.toInt()} (₹${room.electricityRate}/u)", fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)

                        if (carryoverDue > 0.0) {
                            Text("Pending Carryover Due: +₹${carryoverDue.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), fontFamily = FontFamily.SansSerif)
                        }

                        Text("Total Amount: ₹${totalBill.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontFamily = FontFamily.SansSerif)

                        OutlinedTextField(
                            value = amountPaidStr,
                            onValueChange = { amountPaidStr = it },
                            label = { Text("Amount Paid Now (₹)", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Mode Selector: Cash / UPI / Bank
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Cash", "UPI", "Bank").forEach { mode ->
                                FilterChip(
                                    selected = paymentMode == mode,
                                    onClick = { paymentMode = mode },
                                    label = { Text(mode, fontSize = 11.sp, fontFamily = FontFamily.SansSerif) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val paid = amountPaidStr.toDoubleOrNull() ?: totalBill
                            val remaining = (totalBill - paid).coerceAtLeast(0.0)
                            val tId = tenant?.id ?: ""

                            vm.lodgeBill(
                                roomId = room.id,
                                tenantId = tId,
                                monthYear = billingMonth,
                                baseRent = room.baseRent,
                                prevReading = prevReading,
                                curReading = curReading,
                                rate = room.electricityRate,
                                maintenance = 0.0,
                                carryoverDue = carryoverDue,
                                amountPaid = paid,
                                paymentMode = paymentMode
                            )

                            // Launch WhatsApp with exact receipt layout
                            val tName = tenant?.name ?: "Tenant"
                            val paymentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                            val receiptText = """
🏠 *RENT & ELECTRICITY RECEIPT*
━━━━━━━━━━━━━━━━━━━━━━━━━
👤 *Tenant:* $tName (Room ${room.roomNumber})
📅 *Billing Period:* $billingMonth
🗓️ *Date of Payment:* $paymentDate

⚡ *Electricity Details:*
• Previous Reading: $prevReading
• Current Reading: $curReading
• Units Consumed: $unitsConsumed
• Rate / Unit: ₹${room.electricityRate}0
• Total Electricity: ₹${elecTotal}0

🏢 *Base Rent:* ₹${room.baseRent}0
🧾 *Total Amount:* ₹${totalBill}0
━━━━━━━━━━━━━━━━━━━━━━━━━
✅ *Amount Paid:* ₹${paid}0 ($paymentMode)
⚠️ *Pending Due:* ₹${remaining}0
━━━━━━━━━━━━━━━━━━━━━━━━━
Thank you!
                            """.trimIndent()

                            try {
                                val cleanPhone = tenant?.phone?.replace("+", "")?.replace(" ", "") ?: ""
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(receiptText)}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp not installed. Bill saved successfully.", Toast.LENGTH_SHORT).show()
                            }

                            roomToLodgeBill = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Lodge & Send", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToLodgeBill = null }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }

        // 6. Vacate Tenant Dialog
        roomToVacate?.let { room ->
            val tenant = vm.tenants.collectAsState().value.find { it.roomId == room.id && it.isActive }
            var refundStr by remember { mutableStateOf(tenant?.depositAmount?.toString() ?: "0.0") }
            val vacateDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }

            AlertDialog(
                onDismissRequest = { roomToVacate = null },
                title = { Text("Vacate Tenant", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tenant: ${tenant?.name.orEmpty()}", fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
                        Text("Initial Deposit: ₹${tenant?.depositAmount ?: 0.0}", fontSize = 12.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                        OutlinedTextField(
                            value = refundStr,
                            onValueChange = { refundStr = it },
                            label = { Text("Refunded Deposit Amount (₹)", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val ref = refundStr.toDoubleOrNull() ?: 0.0
                            vm.vacateTenant(room.id, vacateDate, ref)
                            roomToVacate = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm Vacate", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToVacate = null }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }

        // 7. History Dialog
        roomForHistory?.let { room ->
            val bills = vm.bills.collectAsState().value.filter { it.roomId == room.id }
            val pasts = vm.pastTenancies.collectAsState().value.filter { it.roomId == room.id }
            var tabIndex by remember { mutableStateOf(0) }

            AlertDialog(
                onDismissRequest = { roomForHistory = null },
                title = { Text("Room ${room.roomNumber} History", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Column {
                        TabRow(selectedTabIndex = tabIndex) {
                            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Bills (${bills.size})", fontFamily = FontFamily.SansSerif) })
                            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Past Tenants (${pasts.size})", fontFamily = FontFamily.SansSerif) })
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (tabIndex == 0) {
                                items(bills) { b ->
                                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("${b.monthYear} • Paid: ₹${b.amountPaid.toInt()} (${b.paymentMode})", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.SansSerif)
                                            if (b.remainingDue > 0.0) {
                                                Text("Remaining Due: ₹${b.remainingDue.toInt()}", color = Color(0xFFD97706), fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(pasts) { p ->
                                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(p.tenantName, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.SansSerif)
                                            Text("📞 ${p.phone} • Stay: ${p.moveInDate} -> ${p.vacateDate}", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                            if (p.address.isNotBlank()) {
                                                Text("📍 ${p.address}", fontSize = 10.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                                            }
                                        }
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
    onAssignTenant: (RoomUnit) -> Unit,
    onLodgeBill: (RoomUnit) -> Unit,
    onVacate: (RoomUnit) -> Unit,
    onViewHistory: (RoomUnit) -> Unit
) {
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()

    if (rooms.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No rooms added yet. Tap '+' to create your first room.",
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF94A3B8)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            items(rooms, key = { it.id }) { room ->
                val tenant = tenants.find { it.roomId == room.id && it.isActive }
                val isVacant = tenant == null

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title bar
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
                                        .background(if (isVacant) Color(0xFF10B981) else Color(0xFF3B82F6))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Room ${room.roomNumber}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Text(
                                text = "₹${room.baseRent.toInt()}/mo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status or Tenant Card
                        if (isVacant) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFECFDF5))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Unit is Vacant", color = Color(0xFF059669), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
                                    Text("Rate: ₹${room.electricityRate}/u", color = Color(0xFF059669), fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tenant!!.name.take(1).uppercase(),
                                            color = Color(0xFF2563EB),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tenant!!.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A),
                                            fontFamily = FontFamily.SansSerif
                                        )
                                        Text(
                                            text = "📞 ${tenant.phone}  •  In: ${tenant.moveInDate}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Uniform 42dp Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onViewHistory(room) },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF334155))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("History", fontSize = 12.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF334155), maxLines = 1)
                            }

                            if (isVacant) {
                                Button(
                                    onClick = { onAssignTenant(room) },
                                    modifier = Modifier.weight(2f).height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Assign Tenant", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                }
                            } else {
                                Button(
                                    onClick = { onLodgeBill(room) },
                                    modifier = Modifier.weight(1.3f).height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lodge Bill", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = { onVacate(room) },
                                    modifier = Modifier.weight(0.9f).height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                ) {
                                    Text("Vacate", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFFEF4444), maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
