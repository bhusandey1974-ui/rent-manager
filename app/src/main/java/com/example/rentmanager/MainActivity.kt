package com.example.rentmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

enum class PropertyFilter {
    ALL, OCCUPIED, VACANT, HAS_DUES
}

fun calculateDaysLived(moveIn: String, moveOut: String?): Long {
    val formats = listOf(
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    )
    for (fmt in formats) {
        try {
            val inDate = fmt.parse(moveIn) ?: continue
            val outDate = if (!moveOut.isNullOrBlank()) fmt.parse(moveOut) ?: Date() else Date()
            val diff = outDate.time - inDate.time
            return (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
        } catch (_: Exception) {}
    }
    return 1
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
    var roomToEdit by remember { mutableStateOf<RoomUnit?>(null) }
    var roomToAssignTenant by remember { mutableStateOf<RoomUnit?>(null) }
    var roomToLodgeBill by remember { mutableStateOf<RoomUnit?>(null) }
    var roomToVacate by remember { mutableStateOf<RoomUnit?>(null) }
    var roomForHistory by remember { mutableStateOf<RoomUnit?>(null) }
    var tenantToViewDetails by remember { mutableStateOf<Tenant?>(null) }

    if (!isAuthenticated) {
        AuthView(
            onLoginSuccess = { uid ->
                isAuthenticated = true
                vm.loadCloudData(uid)
            },
            onSkipOffline = { isAuthenticated = true }
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
                                    ?: "Offline Storage Mode",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF64748B)
                            )
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = { showAddRoomDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFEFF6FF),
                                contentColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Property", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                        }

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
                        onEditRoom = { roomToEdit = it },
                        onAssignTenant = { roomToAssignTenant = it },
                        onLodgeBill = { roomToLodgeBill = it },
                        onVacate = { roomToVacate = it },
                        onViewHistory = { roomForHistory = it },
                        onViewTenantProfile = { tenantToViewDetails = it }
                    )
                } else {
                    RevenueView(vm = vm)
                }
            }
        }
                // --- DIALOGS & MODALS ---

        // 1. Account Reset: Step 1 Confirmation
        if (showResetStep1Dialog) {
            AlertDialog(
                onDismissRequest = { showResetStep1Dialog = false },
                title = { Text("Clear All Account Data?", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Text(
                        "This will permanently delete all properties, active and past tenants, and billing records stored on this device and your cloud account.",
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
                    Text("Are you completely sure? Once cleared, no rent records or past tenant histories can be recovered.", fontFamily = FontFamily.SansSerif, fontSize = 13.sp)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetStep2Dialog = false
                            vm.clearAllUserData { isAuthenticated = false }
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

        // 3. Add Property Dialog
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
                            label = { Text("Property / Unit Number *", fontFamily = FontFamily.SansSerif) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = baseRentStr,
                            onValueChange = { baseRentStr = it },
                            label = { Text("Monthly Rent (₹) *", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = elecRateStr,
                            onValueChange = { elecRateStr = it },
                            label = { Text("Electricity Rate per Unit (₹)", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
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

        // 4. Edit Property Dialog
        roomToEdit?.let { room ->
            var roomNum by remember { mutableStateOf(room.roomNumber) }
            var baseRentStr by remember { mutableStateOf(room.baseRent.toInt().toString()) }
            var elecRateStr by remember { mutableStateOf(room.electricityRate.toInt().toString()) }
            var meterReadingStr by remember { mutableStateOf(room.lastMeterReading.toInt().toString()) }

            AlertDialog(
                onDismissRequest = { roomToEdit = null },
                title = { Text("Edit Property • Unit ${room.roomNumber}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = roomNum,
                            onValueChange = { roomNum = it },
                            label = { Text("Unit Number", fontFamily = FontFamily.SansSerif) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = baseRentStr,
                            onValueChange = { baseRentStr = it },
                            label = { Text("Monthly Rent (₹)", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = elecRateStr,
                            onValueChange = { elecRateStr = it },
                            label = { Text("Electricity Rate (₹/unit)", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = meterReadingStr,
                            onValueChange = { meterReadingStr = it },
                            label = { Text("Meter Reading Base", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            roomToEdit = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Update", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToEdit = null }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }
                // 5. Assign Tenant Dialog
        roomToAssignTenant?.let { room ->
            var name by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var aadhaar by remember { mutableStateOf("") }
            var address by remember { mutableStateOf("") }
            var depositStr by remember { mutableStateOf("") }
            var initMeterStr by remember { mutableStateOf(room.lastMeterReading.toInt().toString()) }

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
                                label = { Text("Mobile Phone Number *", fontFamily = FontFamily.SansSerif) },
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
                                label = { Text("Aadhaar / Gov ID (Optional)", fontFamily = FontFamily.SansSerif) },
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
                                label = { Text("Initial Meter Reading (Units)", fontFamily = FontFamily.SansSerif) },
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

        // 6. Tenant Detailed Profile Modal
        tenantToViewDetails?.let { tenant ->
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { tenantToViewDetails = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tenant.name, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, fontSize = 18.sp)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (tenant.isActive) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (tenant.isActive) "Active Tenant" else "Vacated",
                                color = if (tenant.isActive) Color(0xFF2563EB) else Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("📞 Phone: ${tenant.phoneNumber}", fontSize = 13.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF334155))
                                if (tenant.aadhaarNumber.isNotBlank()) {
                                    Text("🪪 Aadhaar: ${tenant.aadhaarNumber}", fontSize = 13.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF334155))
                                }
                                if (tenant.address.isNotBlank()) {
                                    Text("📍 Address: ${tenant.address}", fontSize = 13.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF334155))
                                }
                                Text("💰 Security Deposit: ₹${tenant.depositAmount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
                                Text("⚡ Entry Meter: ${tenant.initialReading.toInt()} units", fontSize = 13.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF334155))
                                Text("🗓️ Move-In Date: ${tenant.moveInDate}", fontSize = 13.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF334155))
                                if (!tenant.moveOutDate.isNullOrBlank()) {
                                    Text("🚪 Move-Out Date: ${tenant.moveOutDate}", fontSize = 13.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF334155))
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tenant.phoneNumber}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontFamily = FontFamily.SansSerif)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val cleanPhone = tenant.phoneNumber.replace("+", "").replace(" ", "")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontFamily = FontFamily.SansSerif)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { tenantToViewDetails = null }) {
                        Text("Close", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
                // 7. Full Lodge Bill Dialog (FIFO Dues, Advance Deductions, WhatsApp Generation)
        roomToLodgeBill?.let { room ->
            val context = LocalContext.current
            val currentTenant = vm.tenants.collectAsState().value.find { it.id == room.currentTenantId }
            val defaultMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)

            var monthYear by remember { mutableStateOf(defaultMonthYear) }
            var baseRentStr by remember { mutableStateOf(room.baseRent.toInt().toString()) }
            var currMeterStr by remember { mutableStateOf("") }
            var maintenanceStr by remember { mutableStateOf("0") }
            var paymentMode by remember { mutableStateOf("Cash") }

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
                                        text = "🟢 Advance Credit Adjusted: -₹${(-previousCarryover).toInt()}",
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
                                    appendLine("🎉 *Balance Status:* Fully Cleared")
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
                // 8. Move-Out Settlement & Vacate Dialog
        roomToVacate?.let { room ->
            val context = LocalContext.current
            val activeTenant = vm.tenants.collectAsState().value.find { it.id == room.currentTenantId }
            var finalMeterStr by remember { mutableStateOf(room.lastMeterReading.toInt().toString()) }

            val fReading = finalMeterStr.toDoubleOrNull() ?: room.lastMeterReading
            val finalUnits = (fReading - room.lastMeterReading).coerceAtLeast(0.0)
            val finalElecCost = finalUnits * room.electricityRate
            val pendingDues = vm.getPendingDueForRoom(room.id)
            val deposit = activeTenant?.depositAmount ?: 0.0
            val finalRefund = deposit - (pendingDues + finalElecCost)

            AlertDialog(
                onDismissRequest = { roomToVacate = null },
                title = { Text("Vacate Unit ${room.roomNumber} & Settle", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Vacating will archive ${activeTenant?.name ?: "tenant"} and produce a final settlement statement.",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            fontFamily = FontFamily.SansSerif
                        )

                        OutlinedTextField(
                            value = finalMeterStr,
                            onValueChange = { finalMeterStr = it },
                            label = { Text("Final Meter Reading (Prev: ${room.lastMeterReading.toInt()})", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("⚡ Unbilled Electricity: ₹${finalElecCost.toInt()} (${finalUnits.toInt()}u)", fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                                Text("⚠️ Outstanding Dues: ₹${pendingDues.toInt()}", fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                                Text("💰 Security Deposit Held: ₹${deposit.toInt()}", fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Net Security Refund:", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.SansSerif)
                                    Text(
                                        text = if (finalRefund >= 0) "₹${finalRefund.toInt()} (Refund)" else "₹${(-finalRefund).toInt()} (Tenant Owes)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = if (finalRefund >= 0) Color(0xFF059669) else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                            vm.vacateRoom(room.id, fReading, dateStr)
                            roomToVacate = null

                            val statement = buildString {
                                appendLine("🏁 *FINAL MOVE-OUT & SETTLEMENT STATEMENT*")
                                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                                appendLine("👤 *Tenant:* ${activeTenant?.name} (Unit ${room.roomNumber})")
                                appendLine("🗓️ *Move-In:* ${activeTenant?.moveInDate}")
                                appendLine("🗓️ *Move-Out:* $dateStr")
                                appendLine("⚡ *Final Meter Reading:* ${fReading.toInt()} (${finalUnits.toInt()}u)")
                                appendLine("⚡ *Final Electricity Charge:* ₹${finalElecCost.toInt()}")
                                appendLine("⚠️ *Unpaid Dues:* ₹${pendingDues.toInt()}")
                                appendLine("💰 *Security Deposit:* ₹${deposit.toInt()}")
                                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                                if (finalRefund >= 0) {
                                    appendLine("🟢 *Security Deposit Refund to Tenant:* ₹${finalRefund.toInt()}")
                                } else {
                                    appendLine("🔴 *Pending Recovery from Tenant:* ₹${(-finalRefund).toInt()}")
                                }
                                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                                appendLine("Thank you for your tenancy!")
                            }

                            val cleanPhone = activeTenant?.phoneNumber?.replace("+", "")?.replace(" ", "") ?: ""
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(statement)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm & Settle", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToVacate = null }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }
                }
            )
        }

        // 9. Comprehensive Past Tenants & Bills History
        roomForHistory?.let { room ->
            val tenants by vm.tenants.collectAsState()
            val bills by vm.bills.collectAsState()

            val roomTenants = tenants.filter { it.roomId == room.id }
            val roomBills = bills.filter { it.roomId == room.id }
            var selectedHistoryTab by remember { mutableStateOf(1) } // Default to Past Tenants

            AlertDialog(
                onDismissRequest = { roomForHistory = null },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                title = {
                    Text("Unit ${room.roomNumber} History", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedHistoryTab == 1) Color.White else Color.Transparent)
                                    .clickable { selectedHistoryTab = 1 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Past Tenants (${roomTenants.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedHistoryTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif,
                                    color = if (selectedHistoryTab == 1) Color(0xFF2563EB) else Color(0xFF64748B)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedHistoryTab == 0) Color.White else Color.Transparent)
                                    .clickable { selectedHistoryTab = 0 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Bills (${roomBills.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedHistoryTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif,
                                    color = if (selectedHistoryTab == 0) Color(0xFF2563EB) else Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (selectedHistoryTab == 1) {
                            if (roomTenants.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                    Text("No past tenants recorded.", color = Color(0xFF94A3B8), fontFamily = FontFamily.SansSerif, fontSize = 13.sp)
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(roomTenants) { tenant ->
                                        val tenantBills = roomBills.filter { it.tenantId == tenant.id }
                                        val totalRentPaid = tenantBills.sumOf { it.baseRent }
                                        val totalElecPaid = tenantBills.sumOf {
                                            val u = (it.currentMeterReading - it.prevMeterReading).coerceAtLeast(0.0)
                                            u * it.electricityRate
                                        }
                                        val totalPaidSum = tenantBills.sumOf { it.amountPaid }
                                        val daysLived = calculateDaysLived(tenant.moveInDate, tenant.moveOutDate)

                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            shadowElevation = 1.dp
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text(tenant.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(CircleShape)
                                                            .background(if (tenant.isActive) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = if (tenant.isActive) "Active" else "Past Tenant",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = FontFamily.SansSerif,
                                                            color = if (tenant.isActive) Color(0xFF2563EB) else Color(0xFF64748B)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(tenant.phoneNumber, fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)

                                                    if (tenant.aadhaarNumber.isNotBlank()) {
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text("•", color = Color(0xFFCBD5E1))
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("ID: ${tenant.aadhaarNumber}", fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    color = Color(0xFFF8FAFC),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                            Text("Entry: ${tenant.moveInDate}", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                                            Text("Exit: ${tenant.moveOutDate ?: "Present"}", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                                        }
                                                        Spacer(modifier = Modifier.height(3.dp))
                                                        Text("Duration: $daysLived days lived", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontFamily = FontFamily.SansSerif)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Column {
                                                        Text("Total Paid", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                                        Text("₹${totalPaidSum.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontFamily = FontFamily.SansSerif)
                                                    }

                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("Rent: ₹${totalRentPaid.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155), fontFamily = FontFamily.SansSerif)
                                                        Text("Electricity: ₹${totalElecPaid.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155), fontFamily = FontFamily.SansSerif)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (selectedHistoryTab == 0) {
                            if (roomBills.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                    Text("No bills found.", color = Color(0xFF94A3B8), fontFamily = FontFamily.SansSerif, fontSize = 13.sp)
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(roomBills) { bill ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(bill.monthYear, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
                                                    Text("₹${bill.amountPaid.toInt()} (${bill.paymentMode})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF059669), fontFamily = FontFamily.SansSerif)
                                                }
                                                val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
                                                val elecCost = units * bill.electricityRate
                                                Text(
                                                    "Rent: ₹${bill.baseRent.toInt()}  •  Elec: ₹${elecCost.toInt()} (${units.toInt()}u)",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B),
                                                    fontFamily = FontFamily.SansSerif
                                                )
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
                        Text("Close", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFF2563EB))
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
    onEditRoom: (RoomUnit) -> Unit,
    onAssignTenant: (RoomUnit) -> Unit,
    onLodgeBill: (RoomUnit) -> Unit,
    onVacate: (RoomUnit) -> Unit,
    onViewHistory: (RoomUnit) -> Unit,
    onViewTenantProfile: (Tenant) -> Unit
) {
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(PropertyFilter.ALL) }

    val filteredRooms = remember(rooms, activeFilter, searchQuery) {
        rooms.filter { room ->
            val matchesQuery = room.roomNumber.contains(searchQuery, ignoreCase = true) ||
                tenants.find { it.id == room.currentTenantId }?.name?.contains(searchQuery, ignoreCase = true) == true

            val matchesFilter = when (activeFilter) {
                PropertyFilter.ALL -> true
                PropertyFilter.OCCUPIED -> room.isOccupied
                PropertyFilter.VACANT -> !room.isOccupied
                PropertyFilter.HAS_DUES -> vm.getPendingDueForRoom(room.id) > 0.0
            }

            matchesQuery && matchesFilter
        }
    }

    if (rooms.isEmpty()) {
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar & Filter Strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by unit or tenant name...", fontSize = 13.sp, fontFamily = FontFamily.SansSerif) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = activeFilter == PropertyFilter.ALL,
                            onClick = { activeFilter = PropertyFilter.ALL },
                            label = { Text("All (${rooms.size})", fontSize = 11.sp, fontFamily = FontFamily.SansSerif) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = activeFilter == PropertyFilter.OCCUPIED,
                            onClick = { activeFilter = PropertyFilter.OCCUPIED },
                            label = { Text("Occupied (${rooms.count { it.isOccupied }})", fontSize = 11.sp, fontFamily = FontFamily.SansSerif) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = activeFilter == PropertyFilter.VACANT,
                            onClick = { activeFilter = PropertyFilter.VACANT },
                            label = { Text("Vacant (${rooms.count { !it.isOccupied }})", fontSize = 11.sp, fontFamily = FontFamily.SansSerif) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = activeFilter == PropertyFilter.HAS_DUES,
                            onClick = { activeFilter = PropertyFilter.HAS_DUES },
                            label = { Text("Dues Pending", fontSize = 11.sp, fontFamily = FontFamily.SansSerif) }
                        )
                    }
                }
            }

            // Units List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                items(filteredRooms, key = { it.id }) { room ->
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

                                Row(verticalAlignment = Alignment.CenterVertically) {
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

                                    IconButton(onClick = { onEditRoom(room) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Unit", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (room.isOccupied && tenant != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onViewTenantProfile(tenant) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Tenant: ${tenant.name}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            color = Color(0xFF2563EB)
                                        )
                                        Text(
                                            text = "📞 ${tenant.phoneNumber} • Joined: ${tenant.moveInDate}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            fontFamily = FontFamily.SansSerif
                                        )
                                        Text(
                                            text = "Last Meter: ${room.lastMeterReading.toInt()} units",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                                }

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

                                    FilledTonalIconButton(
                                        onClick = { onViewHistory(room) },
                                        modifier = Modifier.size(38.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = "History", tint = Color(0xFF475569))
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
}
