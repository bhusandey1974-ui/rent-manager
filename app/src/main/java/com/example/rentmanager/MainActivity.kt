package com.example.rentmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale

val PrimaryBlue = Color(0xFF1E40AF)
val BackgroundLight = Color(0xFFF8FAFC)
val DarkNavy = Color(0xFF0F172A)
val SuccessGreen = Color(0xFF16A34A)
val AlertRed = Color(0xFFDC2626)

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: RentViewModel = viewModel()
                RentManagerMainScreen(vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentManagerMainScreen(viewModel: RentViewModel) {
    val properties by viewModel.properties.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val tenants by viewModel.tenants.collectAsState()
    val tenantHistory by viewModel.tenantHistory.collectAsState()
    val bills by viewModel.bills.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var assignRoomTarget by remember { mutableStateOf<RoomUnit?>(null) }
    var checkoutTenantTarget by remember { mutableStateOf<Tenant?>(null) }
    var viewingHistoryRoom by remember { mutableStateOf<RoomUnit?>(null) }
    var selectedPropertyId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(properties) {
        if (selectedPropertyId == null && properties.isNotEmpty()) {
            selectedPropertyId = properties.first().id
        }
    }

    val currentProperty = properties.find { it.id == selectedPropertyId }
    val currentRooms = rooms.filter { it.propertyId == selectedPropertyId }
    val currentTenants = tenants.filter { it.propertyId == selectedPropertyId }
    val currentBills = bills.filter { it.propertyId == selectedPropertyId }

    val totalUnits = currentRooms.size
    val vacantUnits = currentRooms.count { it.isVacant }
    val occupiedUnits = totalUnits - vacantUnits
    val pendingCollection = currentBills.filter { !it.isPaid }.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentProperty?.name ?: "Rent Manager", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(currentProperty?.address ?: "Manage rooms, billing & stay histories", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddPropertyDialog = true }) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "Add Property", tint = PrimaryBlue)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
        ) {
            if (properties.size > 1) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(properties) { prop ->
                        FilterChip(
                            selected = prop.id == selectedPropertyId,
                            onClick = { selectedPropertyId = prop.id },
                            label = { Text(prop.name) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Occupancy", fontSize = 11.sp, color = Color.Gray)
                        Text("$occupiedUnits/$totalUnits Occupied", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                        Text("$vacantUnits Vacant 🟢", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Pending Dues", fontSize = 11.sp, color = Color.Gray)
                        Text(formatCurrency(pendingCollection), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AlertRed)
                        Text("${currentBills.count { !it.isPaid }} unpaid bill(s)", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(top = 6.dp)) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Rooms & Tenants") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Billing & Ledger") })
            }

            Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                when (selectedTab) {
                    0 -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                            items(currentRooms) { room ->
                                val tenant = currentTenants.find { it.roomId == room.id }
                                RoomCard(
                                    room = room,
                                    tenant = tenant,
                                    onAssign = { assignRoomTarget = room },
                                    onCheckout = { tenant?.let { checkoutTenantTarget = it } },
                                    onHistory = { viewingHistoryRoom = room }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = { showAddRoomDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add New Room")
                                }
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                            items(currentBills) { bill ->
                                val room = currentRooms.find { it.id == bill.roomId }
                                val tenant = currentTenants.find { it.id == bill.tenantId }
                                BillCard(bill = bill, room = room, tenant = tenant, property = currentProperty, viewModel = viewModel)
                            }

                            item {
                                Button(
                                    onClick = { showAddBillDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generate Monthly Rent & Electricity Bill")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddPropertyDialog) {
        AddPropertyModal(
            onDismiss = { showAddPropertyDialog = false },
            onAdd = { n, addr, c, on, op ->
                viewModel.addProperty(n, addr, c, on, op)
                showAddPropertyDialog = false
            }
        )
    }

    if (showAddRoomDialog && currentProperty != null) {
        AddRoomModal(
            propertyName = currentProperty.name,
            onDismiss = { showAddRoomDialog = false },
            onAdd = { no, type, rent, rate ->
                viewModel.addRoom(currentProperty.id, no, type, rent, rate)
                showAddRoomDialog = false
            }
        )
    }

    assignRoomTarget?.let { room ->
        if (currentProperty != null) {
            AssignTenantModal(
                roomNumber = room.roomNumber,
                onDismiss = { assignRoomTarget = null },
                onAssign = { name, phone, aadhaar, date, deposit, reading ->
                    viewModel.assignTenant(currentProperty.id, room.id, name, phone, aadhaar, date, deposit, reading)
                    assignRoomTarget = null
                }
            )
        }
    }

    checkoutTenantTarget?.let { tenant ->
        val tenantPaidSum = bills.filter { it.tenantId == tenant.id && it.isPaid }.sumOf { it.totalAmount }
        CheckoutTenantModal(
            tenantName = tenant.name,
            moveInDate = tenant.moveInDate,
            deposit = tenant.securityDeposit,
            totalPaidSoFar = tenantPaidSum,
            onDismiss = { checkoutTenantTarget = null },
            onConfirm = { date, refund ->
                viewModel.checkoutTenant(tenant.id, date, refund)
                checkoutTenantTarget = null
            }
        )
    }

    viewingHistoryRoom?.let { room ->
        val historyList = tenantHistory.filter { it.roomId == room.id }
        RoomHistoryModal(roomNumber = room.roomNumber, history = historyList, onDismiss = { viewingHistoryRoom = null })
    }

    if (showAddBillDialog && currentProperty != null) {
        AddBillModal(
            tenants = currentTenants,
            rooms = currentRooms,
            onDismiss = { showAddBillDialog = false },
            onGenerate = { rId, tId, month, base, prev, cur, rate ->
                viewModel.generateBill(currentProperty.id, rId, tId, month, base, prev, cur, rate, 200.0)
                showAddBillDialog = false
            }
        )
    }
}
@Composable
fun RoomCard(room: RoomUnit, tenant: Tenant?, onAssign: () -> Unit, onCheckout: () -> Unit, onHistory: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(if (room.isVacant) SuccessGreen else AlertRed, shape = CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Room ${room.roomNumber} (${room.roomType})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text("${formatCurrency(room.baseRent)}/mo", fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tenant != null) {
                Text("👤 ${tenant.name} (${tenant.phone})", fontSize = 12.sp, color = DarkNavy, fontWeight = FontWeight.Medium)
                if (tenant.aadhaarNo.isNotBlank()) {
                    Text("🪪 Aadhaar: ${tenant.aadhaarNo}", fontSize = 11.sp, color = Color.DarkGray)
                }
                Text("📅 Moved In: ${tenant.moveInDate} • Deposit: ${formatCurrency(tenant.securityDeposit)}", fontSize = 11.sp, color = Color.Gray)
            } else {
                Text("🟢 Status: Vacant", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onHistory, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View History", fontSize = 11.sp)
                }

                if (room.isVacant) {
                    Button(
                        onClick = onAssign,
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Add Tenant", fontSize = 11.sp)
                    }
                } else {
                    OutlinedButton(onClick = onCheckout, modifier = Modifier.weight(1.3f)) {
                        Text("Checkout", fontSize = 11.sp, color = AlertRed)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomHistoryModal(roomNumber: String, history: List<TenantHistoryRecord>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Room $roomNumber Stay History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            if (history.isEmpty()) {
                Text("No previous tenant records logged for this room yet.", color = Color.Gray, fontSize = 13.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    items(history) { record ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(record.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = DarkNavy)
                                Text("📞 Phone: ${record.phone}", fontSize = 11.sp, color = Color.DarkGray)
                                Text("🪪 Aadhaar: ${record.aadhaarNo.ifBlank { "N/A" }}", fontSize = 11.sp, color = Color.DarkGray)
                                
                                Divider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp)
                                
                                Text("🗓️ Stay: ${record.moveInDate} → ${record.moveOutDate}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("⏳ Duration: ${record.formattedDuration} (${record.totalDaysStayed} Days total)", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = Color(0xFFE0F2FE),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("💰 Lifetime Rent Paid: ${formatCurrency(record.totalRentPaidLifetime)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                                        Text("💵 Security Deposit Refunded: ${formatCurrency(record.depositRefunded)}", fontSize = 11.sp, color = SuccessGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun BillCard(bill: BillRecord, room: RoomUnit?, tenant: Tenant?, property: Property?, viewModel: RentViewModel) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${tenant?.name ?: "Tenant"} (Room ${room?.roomNumber ?: ""})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(bill.monthYear, fontSize = 11.sp, color = Color.Gray)
                }
                Surface(color = if (bill.isPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(6.dp)) {
                    Text(if (bill.isPaid) "PAID ✅" else "PENDING ⏳", color = if (bill.isPaid) SuccessGreen else AlertRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Base Rent: ${formatCurrency(bill.baseRent)}", fontSize = 12.sp)
                    Text("Electricity (${bill.electricityUnitsUsed.toInt()} units): ${formatCurrency(bill.electricityBill)}", fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Due", fontSize = 11.sp, color = Color.Gray)
                    Text(formatCurrency(bill.totalAmount), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = PrimaryBlue)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (tenant != null && property != null && room != null) {
                            val msg = viewModel.getWhatsAppReceiptText(bill, tenant, property, room)
                            val cleanNum = tenant.phone.replace("+", "").replace(" ", "")
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanNum&text=${Uri.encode(msg)}")))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Bill", fontSize = 11.sp)
                }
                if (!bill.isPaid) {
                    OutlinedButton(onClick = { viewModel.markBillPaid(bill.id) }, modifier = Modifier.weight(1f)) {
                        Text("Mark Paid", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
@Composable
fun AssignTenantModal(roomNumber: String, onDismiss: () -> Unit, onAssign: (String, String, String, String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var moveInDate by remember { mutableStateOf("01 Aug 2026") }
    var deposit by remember { mutableStateOf("5000") }
    var initialMeter by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Tenant (Room $roomNumber)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = moveInDate, onValueChange = { moveInDate = it }, label = { Text("Move-In Date (e.g. 01 Aug 2026)") }, singleLine = true)
                OutlinedTextField(value = deposit, onValueChange = { deposit = it }, label = { Text("Security Deposit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = initialMeter, onValueChange = { initialMeter = it }, label = { Text("Initial Meter Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    onAssign(name, phone, aadhaar, moveInDate, deposit.toDoubleOrNull() ?: 0.0, initialMeter.toDoubleOrNull() ?: 0.0)
                }
            }) { Text("Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CheckoutTenantModal(tenantName: String, moveInDate: String, deposit: Double, totalPaidSoFar: Double, onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var moveOutDate by remember { mutableStateOf("27 Aug 2026") }
    var refund by remember { mutableStateOf(deposit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checkout $tenantName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Move-In Date: $moveInDate", fontSize = 12.sp, color = Color.Gray)
                Text("Total Rent Paid During Stay: ${formatCurrency(totalPaidSoFar)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Text("Original Deposit: ${formatCurrency(deposit)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                
                OutlinedTextField(value = moveOutDate, onValueChange = { moveOutDate = it }, label = { Text("Move-Out Date (e.g. 27 Aug 2026)") }, singleLine = true)
                OutlinedTextField(value = refund, onValueChange = { refund = it }, label = { Text("Refund Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(moveOutDate, refund.toDoubleOrNull() ?: 0.0) }, colors = ButtonDefaults.buttonColors(containerColor = AlertRed)) {
                Text("Confirm Checkout & Save History")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddPropertyModal(onDismiss: () -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Rental Property", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Property Name") }, singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Street Address") }, singleLine = true)
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true)
                OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, singleLine = true)
                OutlinedTextField(value = ownerPhone, onValueChange = { ownerPhone = it }, label = { Text("Owner Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onAdd(name, address, city, ownerName, ownerPhone) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddRoomModal(propertyName: String, onDismiss: () -> Unit, onAdd: (String, String, Double, Double) -> Unit) {
    var roomNo by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("1BHK") }
    var rent by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room to $propertyName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = roomNo, onValueChange = { roomNo = it }, label = { Text("Room / Flat No (e.g. 101)") }, singleLine = true)
                OutlinedTextField(value = roomType, onValueChange = { roomType = it }, label = { Text("Type (1BHK, 2BHK, Single)") }, singleLine = true)
                OutlinedTextField(value = rent, onValueChange = { rent = it }, label = { Text("Base Rent (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Electricity Rate/Unit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                val r = rent.toDoubleOrNull() ?: 0.0
                val rt = rate.toDoubleOrNull() ?: 10.0
                if (roomNo.isNotBlank() && r > 0) onAdd(roomNo, roomType, r, rt)
            }) { Text("Add Room") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddBillModal(tenants: List<Tenant>, rooms: List<RoomUnit>, onDismiss: () -> Unit, onGenerate: (String, String, String, Double, Double, Double, Double) -> Unit) {
    if (tenants.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Tenants") },
            text = { Text("Please assign a tenant to a room before generating a bill.") },
            confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    var selectedTenant by remember { mutableStateOf(tenants.first()) }
    var prevUnits by remember { mutableStateOf(selectedTenant.initialMeterReading.toString()) }
    var curUnits by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("August 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Bill & Meter Calculation", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tenant: ${selectedTenant.name}", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Billing Month") }, singleLine = true)
                OutlinedTextField(value = prevUnits, onValueChange = { prevUnits = it }, label = { Text("Previous Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = curUnits, onValueChange = { curUnits = it }, label = { Text("Current Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                val prev = prevUnits.toDoubleOrNull() ?: 0.0
                val cur = curUnits.toDoubleOrNull() ?: prev
                val room = rooms.find { it.id == selectedTenant.roomId }
                if (room != null) onGenerate(room.id, selectedTenant.id, month, room.baseRent, prev, cur, room.electricityRate)
            }) { Text("Generate") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
