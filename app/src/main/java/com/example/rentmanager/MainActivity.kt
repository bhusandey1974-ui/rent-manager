package com.example.rentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale

val BrandBlue = Color(0xFF0066DB)
val BrandBlueDark = Color(0xFF004CB7)
val CardBackground = Color(0xFFFFFFFF)
val PageBackground = Color(0xFFFAF9FF)
val TextDark = Color(0xFF1E293B)
val TextMuted = Color(0xFF64748B)
val SuccessGreen = Color(0xFF10B981)
val AlertRed = Color(0xFFEF4444)

fun formatRupee(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 2
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

@Composable
fun RentManagerMainScreen(viewModel: RentViewModel) {
    val properties by viewModel.properties.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val tenants by viewModel.tenants.collectAsState()
    val tenantHistory by viewModel.tenantHistory.collectAsState()
    val bills by viewModel.bills.collectAsState()

    var currentNavTab by remember { mutableIntStateOf(0) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var assignRoomTarget by remember { mutableStateOf<RoomUnit?>(null) }
    var checkoutTenantTarget by remember { mutableStateOf<Tenant?>(null) }
    var viewingHistoryRoom by remember { mutableStateOf<RoomUnit?>(null) }

    Scaffold(
        topBar = {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(BrandBlue, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (currentNavTab == 0) "Rent Manager" else "Revenue & Analytics",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentNavTab == 0,
                    onClick = { currentNavTab = 0 },
                    icon = { Icon(Icons.Default.Apartment, contentDescription = "Properties") },
                    label = { Text("Properties") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandBlue,
                        selectedTextColor = BrandBlue,
                        indicatorColor = BrandBlue.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = currentNavTab == 1,
                    onClick = { currentNavTab = 1 },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Revenue") },
                    label = { Text("Revenue") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandBlue,
                        selectedTextColor = BrandBlue,
                        indicatorColor = BrandBlue.copy(alpha = 0.12f)
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentNavTab == 0) {
                FloatingActionButton(
                    onClick = { showAddRoomDialog = true },
                    containerColor = BrandBlueDark,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Unit", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(paddingValues)
        ) {
            when (currentNavTab) {
                0 -> PropertiesRoomsTab(
                    rooms = rooms,
                    tenants = tenants,
                    onAddRoomClick = { showAddRoomDialog = true },
                    onAssign = { room -> assignRoomTarget = room },
                    onCheckout = { tenant -> checkoutTenantTarget = tenant },
                    onHistory = { room -> viewingHistoryRoom = room }
                )
                1 -> RevenueAnalyticsTab(bills = bills)
            }
        }
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            properties = properties,
            onDismiss = { showAddRoomDialog = false },
            onSave = { roomNo, rent, rate ->
                val propId = if (properties.isEmpty()) {
                    viewModel.addProperty("My Property", "Main Building")
                } else {
                    properties.first().id
                }
                viewModel.addRoom(propId, roomNo, "Room", rent, rate)
                showAddRoomDialog = false
            }
        )
    }

    assignRoomTarget?.let { room ->
        AssignTenantDialog(
            roomNumber = room.roomNumber,
            onDismiss = { assignRoomTarget = null },
            onAssign = { name, phone, aadhaar, date, deposit, reading ->
                viewModel.assignTenant(room.propertyId, room.id, name, phone, aadhaar, date, deposit, reading)
                assignRoomTarget = null
            }
        )
    }

    checkoutTenantTarget?.let { tenant ->
        val totalPaidSoFar = bills.filter { it.tenantId == tenant.id && it.isPaid }.sumOf { it.totalAmount }
        CheckoutTenantDialog(
            tenantName = tenant.name,
            moveInDate = tenant.moveInDate,
            deposit = tenant.securityDeposit,
            totalPaidSoFar = totalPaidSoFar,
            onDismiss = { checkoutTenantTarget = null },
            onConfirm = { date, refund ->
                viewModel.checkoutTenant(tenant.id, date, refund)
                checkoutTenantTarget = null
            }
        )
    }

    viewingHistoryRoom?.let { room ->
        val historyList = tenantHistory.filter { it.roomId == room.id }
        RoomHistoryDialog(
            roomNumber = room.roomNumber,
            history = historyList,
            onDismiss = { viewingHistoryRoom = null }
        )
    }
}
@Composable
fun PropertiesRoomsTab(
    rooms: List<RoomUnit>,
    tenants: List<Tenant>,
    onAddRoomClick: () -> Unit,
    onAssign: (RoomUnit) -> Unit,
    onCheckout: (Tenant) -> Unit,
    onHistory: (RoomUnit) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Properties & Rooms (${rooms.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (rooms.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No rooms added yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap + to add your first room/flat.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(rooms) { room ->
                    val tenant = tenants.find { it.roomId == room.id }
                    RoomCardItem(
                        room = room,
                        tenant = tenant,
                        onAssign = { onAssign(room) },
                        onCheckout = { tenant?.let { onCheckout(it) } },
                        onHistory = { onHistory(room) }
                    )
                }
            }
        }
    }
}

@Composable
fun RoomCardItem(
    room: RoomUnit,
    tenant: Tenant?,
    onAssign: () -> Unit,
    onCheckout: () -> Unit,
    onHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .background(if (room.isVacant) SuccessGreen else AlertRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Room ${room.roomNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark
                    )
                }
                Text(
                    text = "${formatRupee(room.baseRent)}/mo",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = BrandBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tenant != null) {
                Text("👤 ${tenant.name} (${tenant.phone})", fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
                if (tenant.aadhaarNo.isNotBlank()) {
                    Text("🪪 Aadhaar: ${tenant.aadhaarNo}", fontSize = 12.sp, color = TextMuted)
                }
                Text("📅 Moved In: ${tenant.moveInDate}", fontSize = 12.sp, color = TextMuted)
            } else {
                Text("🟢 Status: Vacant", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.6.dp, color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onHistory,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stay History", fontSize = 12.sp)
                }

                if (room.isVacant) {
                    Button(
                        onClick = onAssign,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add Tenant", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onCheckout,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Checkout", fontSize = 12.sp, color = AlertRed)
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueAnalyticsTab(bills: List<BillRecord>) {
    val totalPaidLifetime = bills.filter { it.isPaid }.sumOf { it.totalAmount }
    val rentEarnings = bills.filter { it.isPaid }.sumOf { it.baseRent }
    val elecEarnings = bills.filter { it.isPaid }.sumOf { it.electricityBill }
    val totalDue = bills.filter { !it.isPaid }.sumOf { it.totalAmount }
    val totalInvoiced = bills.sumOf { it.totalAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF0284C7))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text("LIFETIME COLLECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(formatRupee(totalPaidLifetime), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Rent Earnings", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(formatRupee(rentEarnings), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Elec. Earnings", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(formatRupee(elecEarnings), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Total Due", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(formatRupee(totalDue), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Yearly Breakdown (2026)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Surface(color = Color(0xFFEDE9FE), shape = RoundedCornerShape(8.dp)) {
                            Text("2026", color = Color(0xFF6D28D9), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF6D28D9))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Past Year Records", fontSize = 12.sp, color = Color(0xFF6D28D9))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricSmallCard(modifier = Modifier.weight(1f), label = "Rent Billed", value = formatRupee(rentEarnings), valueColor = TextDark)
                        MetricSmallCard(modifier = Modifier.weight(1f), label = "Electricity", value = formatRupee(elecEarnings), valueColor = BrandBlue)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricSmallCard(modifier = Modifier.weight(1f), label = "Total Invoiced", value = formatRupee(totalInvoiced), valueColor = BrandBlue)
                        MetricSmallCard(modifier = Modifier.weight(1f), label = "Collected", value = formatRupee(totalPaidLifetime), valueColor = SuccessGreen)
                    }
                }
            }
        }

        item {
            Text("Monthly Ledger Entries (2026)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
        }

        if (bills.isEmpty()) {
            item {
                Text("No bills generated for 2026.", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
        } else {
            items(bills) { bill ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bill.monthYear, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                            Text("Rent: ${formatRupee(bill.baseRent)} • Elec: ${formatRupee(bill.electricityBill)}", fontSize = 11.sp, color = TextMuted)
                        }
                        Text(formatRupee(bill.totalAmount), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = if (bill.isPaid) SuccessGreen else AlertRed)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricSmallCard(modifier: Modifier = Modifier, label: String, value: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
@Composable
fun RoomHistoryDialog(roomNumber: String, history: List<TenantHistoryRecord>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = BrandBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Room $roomNumber Stay History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            if (history.isEmpty()) {
                Text("No previous tenants on record for this room yet.", color = TextMuted, fontSize = 13.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    items(history) { record ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(record.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TextDark)
                                Text("📞 Phone: ${record.phone}", fontSize = 11.sp, color = TextMuted)
                                if (record.aadhaarNo.isNotBlank()) {
                                    Text("🪪 Aadhaar: ${record.aadhaarNo}", fontSize = 11.sp, color = TextMuted)
                                }
                                Divider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp)
                                Text("🗓️ Stay: ${record.moveInDate} → ${record.moveOutDate}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("⏳ Duration: ${record.formattedDuration} (${record.totalDaysStayed} Days)", fontWeight = FontWeight.Bold, color = BrandBlue, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = Color(0xFFE0F2FE),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("💰 Total Rent Paid in Lifetime: ${formatRupee(record.totalRentPaidLifetime)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandBlue)
                                        Text("💵 Deposit Refunded: ${formatRupee(record.depositRefunded)}", fontSize = 11.sp, color = SuccessGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) { Text("Close") } }
    )
}

@Composable
fun AddRoomDialog(properties: List<Property>, onDismiss: () -> Unit, onSave: (String, Double, Double) -> Unit) {
    var roomNo by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room / Flat", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = roomNo, onValueChange = { roomNo = it }, label = { Text("Room / Flat No (e.g. 101)") }, singleLine = true)
                OutlinedTextField(value = rent, onValueChange = { rent = it }, label = { Text("Monthly Rent (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Electricity Rate/Unit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rent.toDoubleOrNull() ?: 0.0
                    val rt = rate.toDoubleOrNull() ?: 10.0
                    if (roomNo.isNotBlank() && r > 0) onSave(roomNo, r, rt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Save Room") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AssignTenantDialog(roomNumber: String, onDismiss: () -> Unit, onAssign: (String, String, String, String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var moveInDate by remember { mutableStateOf("01 Aug 2026") }
    var deposit by remember { mutableStateOf("5000") }
    var meterReading by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Tenant to Room $roomNumber", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tenant Name") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = moveInDate, onValueChange = { moveInDate = it }, label = { Text("Move-In Date (e.g. 01 Aug 2026)") }, singleLine = true)
                OutlinedTextField(value = deposit, onValueChange = { deposit = it }, label = { Text("Security Deposit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = meterReading, onValueChange = { meterReading = it }, label = { Text("Initial Meter Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAssign(name, phone, aadhaar, moveInDate, deposit.toDoubleOrNull() ?: 0.0, meterReading.toDoubleOrNull() ?: 0.0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CheckoutTenantDialog(tenantName: String, moveInDate: String, deposit: Double, totalPaidSoFar: Double, onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var moveOutDate by remember { mutableStateOf("28 Aug 2026") }
    var refund by remember { mutableStateOf(deposit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checkout $tenantName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Move-In Date: $moveInDate", fontSize = 12.sp, color = TextMuted)
                Text("Total Rent Paid: ${formatRupee(totalPaidSoFar)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                Text("Deposit Paid: ${formatRupee(deposit)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = moveOutDate, onValueChange = { moveOutDate = it }, label = { Text("Move-Out Date (e.g. 28 Aug 2026)") }, singleLine = true)
                OutlinedTextField(value = refund, onValueChange = { refund = it }, label = { Text("Refund Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(moveOutDate, refund.toDoubleOrNull() ?: 0.0) },
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
            ) { Text("Confirm Checkout & Save History") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
