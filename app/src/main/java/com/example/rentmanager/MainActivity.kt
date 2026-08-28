package com.example.rentmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale

val BrandBlue = Color(0xFF0284C7)
val SkyBlueLight = Color(0xFFE0F2FE)
val SkyBlueGradientStart = Color(0xFF38BDF8)
val SkyBlueGradientEnd = Color(0xFF0284C7)
val CardBackground = Color(0xFFFFFFFF)
val PageBackground = Color(0xFFF8FAFC)
val TextDark = Color(0xFF0F172A)
val TextMuted = Color(0xFF64748B)
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenLight = Color(0xFFD1FAE5)
val AlertRed = Color(0xFFEF4444)
val AlertRedLight = Color(0xFFFEE2E2)
val WarningYellowLight = Color(0xFFFEF3C7)
val WarningYellowDark = Color(0xFFB45309)
val PurpleAccent = Color(0xFF8B5CF6)
val PurpleAccentLight = Color(0xFFEDE9FE)

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
                RentManagerMainApp(vm)
            }
        }
    }
}
@Composable
fun RentManagerMainApp(viewModel: RentViewModel) {
    val properties by viewModel.properties.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val tenants by viewModel.tenants.collectAsState()
    val tenantHistory by viewModel.tenantHistory.collectAsState()
    val bills by viewModel.bills.collectAsState()

    var currentNavTab by remember { mutableIntStateOf(0) }
    var selectedPropertyId by remember { mutableStateOf<String?>(null) }

    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var assignRoomTarget by remember { mutableStateOf<RoomUnit?>(null) }
    var checkoutTenantTarget by remember { mutableStateOf<Tenant?>(null) }
    var viewingHistoryRoom by remember { mutableStateOf<RoomUnit?>(null) }
    var editingRoomTarget by remember { mutableStateOf<RoomUnit?>(null) }
    var deletingRoomTarget by remember { mutableStateOf<RoomUnit?>(null) }
    var unifiedBillTarget by remember { mutableStateOf<Pair<RoomUnit, Tenant>?>(null) }
    var standalonePaymentTarget by remember { mutableStateOf<BillRecord?>(null) }
    var viewingTenantDetails by remember { mutableStateOf<Tenant?>(null) }

    LaunchedEffect(properties) {
        if (selectedPropertyId == null && properties.isNotEmpty()) {
            selectedPropertyId = properties.first().id
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        Brush.linearGradient(listOf(SkyBlueGradientStart, SkyBlueGradientEnd)),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HomeWork,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (currentNavTab == 0) "Rent Manager" else "Revenue & Analytics",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = if (currentNavTab == 0) "${rooms.size} Units Registered" else "Lifetime Ledger",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        if (currentNavTab == 0) {
                            IconButton(
                                onClick = { showAddPropertyDialog = true },
                                modifier = Modifier
                                    .background(SkyBlueLight, CircleShape)
                                    .size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DomainAdd,
                                    contentDescription = "Add Property",
                                    tint = BrandBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (currentNavTab == 0 && properties.isNotEmpty()) {
                        PropertySelectorRow(
                            properties = properties,
                            selectedId = selectedPropertyId,
                            onSelect = { selectedPropertyId = it },
                            onAddNew = { showAddPropertyDialog = true }
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentNavTab == 0,
                    onClick = { currentNavTab = 0 },
                    icon = { Icon(Icons.Default.Apartment, contentDescription = "Properties") },
                    label = { Text("Properties", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandBlue,
                        selectedTextColor = BrandBlue,
                        indicatorColor = SkyBlueLight
                    )
                )
                NavigationBarItem(
                    selected = currentNavTab == 1,
                    onClick = { currentNavTab = 1 },
                    icon = { Icon(Icons.Default.QueryStats, contentDescription = "Revenue") },
                    label = { Text("Revenue", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandBlue,
                        selectedTextColor = BrandBlue,
                        indicatorColor = SkyBlueLight
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentNavTab == 0) {
                FloatingActionButton(
                    onClick = { showAddRoomDialog = true },
                    containerColor = BrandBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Room",
                        modifier = Modifier.size(28.dp)
                    )
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
                0 -> {
                    val filteredRooms = if (selectedPropertyId != null) {
                        rooms.filter { it.propertyId == selectedPropertyId }
                    } else rooms

                    PropertiesRoomsTab(
                        rooms = filteredRooms,
                        tenants = tenants,
                        bills = bills,
                        properties = properties,
                        viewModel = viewModel,
                        onAddRoomClick = { showAddRoomDialog = true },
                        onAssign = { room -> assignRoomTarget = room },
                        onCheckout = { tenant -> checkoutTenantTarget = tenant },
                        onHistory = { room -> viewingHistoryRoom = room },
                        onEditRoom = { room -> editingRoomTarget = room },
                        onDeleteRoom = { room -> deletingRoomTarget = room },
                        onUnifiedLodgeBill = { room, tenant -> unifiedBillTarget = Pair(room, tenant) },
                        onViewTenant = { tenant -> viewingTenantDetails = tenant },
                        onOpenPayment = { bill -> standalonePaymentTarget = bill }
                    )
                }
                1 -> {
                    RevenueAnalyticsTab(
                        bills = bills,
                        tenants = tenants,
                        rooms = rooms,
                        properties = properties,
                        viewModel = viewModel,
                        onOpenPayment = { bill -> standalonePaymentTarget = bill }
                    )
                }
            }
        }
    }

    if (showAddPropertyDialog) {
        AddPropertyDialog(
            onDismiss = { showAddPropertyDialog = false },
            onSave = { name, address, city, owner, phone ->
                val newId = viewModel.addProperty(name, address, city, owner, phone)
                selectedPropertyId = newId
                showAddPropertyDialog = false
            }
        )
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            properties = properties,
            selectedPropertyId = selectedPropertyId,
            onDismiss = { showAddRoomDialog = false },
            onSave = { propId, roomNo, rent, rate ->
                viewModel.addRoom(propId, roomNo, "Room", rent, rate)
                showAddRoomDialog = false
            }
        )
    }

    editingRoomTarget?.let { room ->
        EditRoomDialog(
            room = room,
            onDismiss = { editingRoomTarget = null },
            onSave = { newNo, newRent, newRate ->
                viewModel.editRoom(room.id, newNo, newRent, newRate)
                editingRoomTarget = null
            }
        )
    }

    deletingRoomTarget?.let { room ->
        AlertDialog(
            onDismissRequest = { deletingRoomTarget = null },
            shape = RoundedCornerShape(18.dp),
            title = { Text("Delete Room ${room.roomNumber}?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete Room ${room.roomNumber}, along with all its bills, payment history, and tenant records.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRoom(room.id)
                        deletingRoomTarget = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) { Text("Delete Permanently") }
            },
            dismissButton = {
                TextButton(onClick = { deletingRoomTarget = null }) { Text("Cancel", color = TextMuted) }
            }
        )
    }

    assignRoomTarget?.let { room ->
        AssignTenantDialog(
            roomNumber = room.roomNumber,
            todayDate = viewModel.getTodayDateFormatted(),
            onDismiss = { assignRoomTarget = null },
            onAssign = { name, phone, aadhaar, date, deposit, reading ->
                viewModel.assignTenant(room.propertyId, room.id, name, phone, aadhaar, date, deposit, reading)
                assignRoomTarget = null
            }
        )
    }

    checkoutTenantTarget?.let { tenant ->
        val totalPaidSoFar = bills.filter { it.tenantId == tenant.id }.sumOf { it.amountPaid }
        CheckoutTenantDialog(
            tenantName = tenant.name,
            moveInDate = tenant.moveInDate,
            todayDate = viewModel.getTodayDateFormatted(),
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
        val roomBills = bills.filter { it.roomId == room.id }
        RoomHistoryDialog(
            roomNumber = room.roomNumber,
            history = historyList,
            rentLogs = room.rentChangeLogs,
            roomBills = roomBills,
            onDismiss = { viewingHistoryRoom = null }
        )
    }

    unifiedBillTarget?.let { (room, tenant) ->
        val lastBill = bills.filter { it.roomId == room.id }.lastOrNull()
        val prevReading = lastBill?.currentMeterReading ?: tenant.initialMeterReading
        val pendingDueCarryover = viewModel.getCumulativePendingDue(room.id)
        val defaultCycle = viewModel.getPreviousMonthFormatted()

        UnifiedLodgeBillDialog(
            roomNumber = room.roomNumber,
            tenantName = tenant.name,
            defaultMonth = defaultCycle,
            baseRent = room.baseRent,
            electricityRate = room.electricityRate,
            prevReading = prevReading,
            pendingDueCarryover = pendingDueCarryover,
            onDismiss = { unifiedBillTarget = null },
            onLodgeAndPay = { month, curReading, maint, payingAmount, payMode ->
                viewModel.lodgeBillAndPayment(
                    propertyId = room.propertyId,
                    roomId = room.id,
                    tenantId = tenant.id,
                    month = month,
                    baseRent = room.baseRent,
                    prevUnit = prevReading,
                    curUnit = curReading,
                    rate = room.electricityRate,
                    maintenance = maint,
                    previousDue = pendingDueCarryover,
                    amountPaid = payingAmount,
                    paymentMode = payMode
                )
                unifiedBillTarget = null
            }
        )
    }

    standalonePaymentTarget?.let { bill ->
        val room = rooms.find { it.id == bill.roomId }
        val tenant = tenants.find { it.id == bill.tenantId }
        RecordPaymentDialog(
            bill = bill,
            roomNumber = room?.roomNumber ?: "-",
            tenantName = tenant?.name ?: "Tenant",
            onDismiss = { standalonePaymentTarget = null },
            onConfirmPayment = { amount, payMode ->
                viewModel.recordPayment(bill.id, amount, payMode)
                standalonePaymentTarget = null
            }
        )
    }

    viewingTenantDetails?.let { tenant ->
        TenantDetailsModal(
            tenant = tenant,
            onDismiss = { viewingTenantDetails = null }
        )
    }
}
@Composable
fun PropertySelectorRow(
    properties: List<Property>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAddNew: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(properties) { property ->
            val isSelected = property.id == selectedId
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(property.id) },
                color = if (isSelected) BrandBlue else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TextDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = property.name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextDark
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAddNew() },
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Property", fontSize = 12.sp, color = BrandBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PropertiesRoomsTab(
    rooms: List<RoomUnit>,
    tenants: List<Tenant>,
    bills: List<BillRecord>,
    properties: List<Property>,
    viewModel: RentViewModel,
    onAddRoomClick: () -> Unit,
    onAssign: (RoomUnit) -> Unit,
    onCheckout: (Tenant) -> Unit,
    onHistory: (RoomUnit) -> Unit,
    onEditRoom: (RoomUnit) -> Unit,
    onDeleteRoom: (RoomUnit) -> Unit,
    onUnifiedLodgeBill: (RoomUnit, Tenant) -> Unit,
    onViewTenant: (Tenant) -> Unit,
    onOpenPayment: (BillRecord) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (rooms.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(SkyBlueLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "No rooms added yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap + to add your first room.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(rooms) { room ->
                    val tenant = tenants.find { it.roomId == room.id }
                    val latestUnpaidBill = bills.findLast { it.roomId == room.id && !it.isPaid }
                    val cumulativeDue = viewModel.getCumulativePendingDue(room.id)
                    val prop = properties.find { it.id == room.propertyId } ?: Property(name = "Property")

                    RoomUnitCard(
                        room = room,
                        tenant = tenant,
                        latestUnpaidBill = latestUnpaidBill,
                        cumulativeDue = cumulativeDue,
                        property = prop,
                        onAssign = { onAssign(room) },
                        onCheckout = { tenant?.let { onCheckout(it) } },
                        onHistory = { onHistory(room) },
                        onEditRoom = { onEditRoom(room) },
                        onDeleteRoom = { onDeleteRoom(room) },
                        onUnifiedLodgeBill = { tenant?.let { onUnifiedLodgeBill(room, it) } },
                        onViewTenant = { tenant?.let { onViewTenant(it) } },
                        onOpenPayment = { latestUnpaidBill?.let { onOpenPayment(it) } }
                    )
                }
            }
        }
    }
}

@Composable
fun RoomUnitCard(
    room: RoomUnit,
    tenant: Tenant?,
    latestUnpaidBill: BillRecord?,
    cumulativeDue: Double,
    property: Property,
    onAssign: () -> Unit,
    onCheckout: () -> Unit,
    onHistory: () -> Unit,
    onEditRoom: () -> Unit,
    onDeleteRoom: () -> Unit,
    onUnifiedLodgeBill: () -> Unit,
    onViewTenant: () -> Unit,
    onOpenPayment: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (room.isVacant) SuccessGreen else AlertRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Room ${room.roomNumber}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = TextDark
                    )
                    IconButton(
                        onClick = onEditRoom,
                        modifier = Modifier.size(28.dp).padding(start = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Room",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteRoom,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete Room",
                            tint = AlertRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    text = "${formatRupee(room.baseRent)}/mo",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = BrandBlue
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (tenant != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onViewTenant() },
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SkyBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tenant.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tenant.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "📞 ${tenant.phone} • In: ${tenant.moveInDate}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                if (cumulativeDue > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = WarningYellowLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = WarningYellowDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pending Due: ${formatRupee(cumulativeDue)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningYellowDark
                                )
                            }
                            if (latestUnpaidBill != null) {
                                Button(
                                    onClick = onOpenPayment,
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Status: Vacant", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.6.dp, color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onHistory,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("History", fontSize = 11.sp)
                }

                if (room.isVacant) {
                    Button(
                        onClick = onAssign,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Tenant", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onUnifiedLodgeBill,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lodge Bill", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onCheckout,
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("Vacate", fontSize = 11.sp, color = AlertRed)
                    }
                }
            }
        }
    }
}
@Composable
fun RevenueAnalyticsTab(
    bills: List<BillRecord>,
    tenants: List<Tenant>,
    rooms: List<RoomUnit>,
    properties: List<Property>,
    viewModel: RentViewModel,
    onOpenPayment: (BillRecord) -> Unit
) {
    val context = LocalContext.current
    var ledgerFilter by remember { mutableStateOf("ALL") }

    val totalPaidLifetime = bills.sumOf { it.amountPaid }
    val rentEarnings = bills.filter { it.isPaid }.sumOf { it.baseRent }
    val elecEarnings = bills.filter { it.isPaid }.sumOf { it.electricityBill }
    val totalDue = bills.sumOf { it.remainingDue }
    val totalInvoiced = bills.sumOf { it.totalAmount }

    val chronologicalBills = bills.reversed()
    val filteredBills = when (ledgerFilter) {
        "PAID" -> chronologicalBills.filter { it.isPaid }
        "UNPAID" -> chronologicalBills.filter { !it.isPaid }
        else -> chronologicalBills
    }

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
                            Brush.linearGradient(
                                colors = listOf(SkyBlueGradientStart, SkyBlueGradientEnd)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "LIFETIME REVENUE COLLECTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f),
                                letterSpacing = 1.sp
                            )
                            Icon(Icons.Default.AutoGraph, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = formatRupee(totalPaidLifetime),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Divider(color = Color.White.copy(alpha = 0.25f), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rent Earnings", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                                Text(formatRupee(rentEarnings), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Electricity", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                                Text(formatRupee(elecEarnings), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Total Due", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                                Text(formatRupee(totalDue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarningYellowLight)
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
                        Text("Current Year Breakdown (2026)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Surface(
                            color = PurpleAccentLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Live",
                                color = PurpleAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricSmallCard(modifier = Modifier.weight(1f), label = "Base Rent Billed", value = formatRupee(rentEarnings), valueColor = TextDark)
                        MetricSmallCard(modifier = Modifier.weight(1f), label = "Electricity Charges", value = formatRupee(elecEarnings), valueColor = BrandBlue)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Billing Ledger (${filteredBills.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterLedgerChip(label = "All", isSelected = ledgerFilter == "ALL", onSelect = { ledgerFilter = "ALL" })
                    FilterLedgerChip(label = "Paid", isSelected = ledgerFilter == "PAID", onSelect = { ledgerFilter = "PAID" })
                    FilterLedgerChip(label = "Pending", isSelected = ledgerFilter == "UNPAID", onSelect = { ledgerFilter = "UNPAID" })
                }
            }
        }

        if (filteredBills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No billing entries found.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredBills) { bill ->
                val tenant = tenants.find { it.id == bill.tenantId }
                val room = rooms.find { it.id == bill.roomId }
                val prop = properties.find { it.id == bill.propertyId } ?: Property(name = "Property")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${tenant?.name ?: "Past Tenant"} (Room ${room?.roomNumber ?: "-"})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "Billing Month: ${bill.monthYear}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Surface(
                                color = if (bill.isPaid) SuccessGreenLight else WarningYellowLight,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (bill.isPaid) "PAID ✅ (${bill.paymentMode})" else "DUE: ${formatRupee(bill.remainingDue)}",
                                    color = if (bill.isPaid) SuccessGreen else WarningYellowDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Rent: ${formatRupee(bill.baseRent)} • Elec: ${formatRupee(bill.electricityBill)}", fontSize = 11.sp, color = TextDark)
                                Text("Units: ${bill.electricityUnitsUsed.toInt()} (${bill.prevMeterReading.toInt()} -> ${bill.currentMeterReading.toInt()})", fontSize = 10.sp, color = TextMuted)
                                if (bill.previousDueCarryover > 0) {
                                    Text("Carried Due: ${formatRupee(bill.previousDueCarryover)}", fontSize = 10.sp, color = WarningYellowDark)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatRupee(bill.totalAmount),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = BrandBlue
                                )
                                Text(
                                    text = "Paid: ${formatRupee(bill.amountPaid)} (${bill.paymentMode})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tenant != null && room != null) {
                                IconButton(
                                    onClick = {
                                        val msg = viewModel.getWhatsAppReceiptText(bill, tenant, prop, room)
                                        val cleanNum = tenant.phone.replace("+", "").replace(" ", "")
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanNum&text=${Uri.encode(msg)}"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                }
                            }

                            if (!bill.isPaid) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { onOpenPayment(bill) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Pay Remaining", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
fun FilterLedgerChip(label: String, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() },
        color = if (isSelected) BrandBlue else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TextDark,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
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
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
@Composable
fun UnifiedLodgeBillDialog(
    roomNumber: String,
    tenantName: String,
    defaultMonth: String,
    baseRent: Double,
    electricityRate: Double,
    prevReading: Double,
    pendingDueCarryover: Double,
    onDismiss: () -> Unit,
    onLodgeAndPay: (String, Double, Double, Double, String) -> Unit
) {
    var month by remember { mutableStateOf(defaultMonth) }
    var currentReading by remember { mutableStateOf("") }
    var maintenance by remember { mutableStateOf("0") }
    var amountPaidText by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf("UPI") }

    val cur = currentReading.toDoubleOrNull() ?: prevReading
    val units = (cur - prevReading).coerceAtLeast(0.0)
    val elecCost = units * electricityRate
    val calculatedTotal = baseRent + elecCost + (maintenance.toDoubleOrNull() ?: 0.0) + pendingDueCarryover

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SkyBlueLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Lodge Bill - Room $roomNumber", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                }
                Text(
                    text = "Tenant: $tenantName",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Billing Month (Completed Month)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = prevReading.toString(),
                    onValueChange = {},
                    label = { Text("Previous Meter Reading") },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = currentReading,
                    onValueChange = { currentReading = it },
                    label = { Text("Current Reading (Enter manually)") },
                    placeholder = { Text("e.g. 150.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = maintenance,
                    onValueChange = { maintenance = it },
                    label = { Text("Maintenance / Other Charges (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Surface(
                    color = SkyBlueLight.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Units Consumed: ${units.toInt()} units (@ ₹${electricityRate}/unit)",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                        if (pendingDueCarryover > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "⚠️ Carried Due: ${formatRupee(pendingDueCarryover)}",
                                fontSize = 12.sp,
                                color = WarningYellowDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount Due:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            Text(
                                text = formatRupee(calculatedTotal),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandBlue
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountPaidText,
                    onValueChange = { amountPaidText = it },
                    label = { Text("Amount Paid Now (₹)") },
                    placeholder = { Text("Leave blank or 0 if unpaid") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Cash", "UPI", "Bank", "Cheque").forEach { mode ->
                            val isSel = selectedPaymentMode == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPaymentMode = mode },
                                color = if (isSel) BrandBlue else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else TextDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentReading.isNotBlank()) {
                        val paying = amountPaidText.toDoubleOrNull() ?: 0.0
                        onLodgeAndPay(
                            month,
                            cur,
                            maintenance.toDoubleOrNull() ?: 0.0,
                            paying,
                            selectedPaymentMode
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Create & Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    bill: BillRecord,
    roomNumber: String,
    tenantName: String,
    onDismiss: () -> Unit,
    onConfirmPayment: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf(bill.remainingDue.toInt().toString()) }
    var selectedPaymentMode by remember { mutableStateOf("UPI") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Record Payment - Room $roomNumber", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tenant: $tenantName", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Total Bill: ${formatRupee(bill.totalAmount)}", fontSize = 12.sp, color = TextMuted)
                Text("Already Paid: ${formatRupee(bill.amountPaid)}", fontSize = 12.sp, color = SuccessGreen)
                Text("Remaining Due: ${formatRupee(bill.remainingDue)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarningYellowDark)

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Enter Amount Paying Now (₹)") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Cash", "UPI", "Bank", "Cheque").forEach { mode ->
                        val isSel = selectedPaymentMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPaymentMode = mode },
                            color = if (isSel) SuccessGreen else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = mode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else TextDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paying = amountText.toDoubleOrNull() ?: 0.0
                    if (paying > 0) onConfirmPayment(paying, selectedPaymentMode)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } }
    )
}
@Composable
fun RoomHistoryDialog(
    roomNumber: String,
    history: List<TenantHistoryRecord>,
    rentLogs: List<RentChangeLog>,
    roomBills: List<BillRecord>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = BrandBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Room $roomNumber History & Payment Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                if (roomBills.any { it.paymentTransactions.isNotEmpty() }) {
                    item {
                        Text("Payment Transactions Log", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                    }
                    items(roomBills.flatMap { it.paymentTransactions }) { tx ->
                        Surface(
                            color = SuccessGreenLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Paid: ${formatRupee(tx.amount)} via ${tx.paymentMode}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(tx.date, fontSize = 10.sp, color = TextMuted)
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    item { Divider(modifier = Modifier.padding(vertical = 4.dp)) }
                }

                if (rentLogs.isNotEmpty()) {
                    item {
                        Text("Rent Revision Log", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                    }
                    items(rentLogs) { log ->
                        Surface(
                            color = PurpleAccentLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("📅 ${log.dateChanged}", fontSize = 11.sp, color = PurpleAccent, fontWeight = FontWeight.Bold)
                                Text("Rent: ₹${log.oldRent.toInt()} ➔ ₹${log.newRent.toInt()} | Elec: ₹${log.oldRate} ➔ ₹${log.newRate}", fontSize = 11.sp, color = TextDark)
                            }
                        }
                    }
                    item { Divider(modifier = Modifier.padding(vertical = 4.dp)) }
                }

                item {
                    Text("Past Tenants Stay History", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                }

                if (history.isEmpty()) {
                    item {
                        Text("No past tenant records saved for this room yet.", color = TextMuted, fontSize = 12.sp)
                    }
                } else {
                    items(history) { record ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = SkyBlueLight,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("💰 Total Paid: ${formatRupee(record.totalRentPaidLifetime)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandBlue)
                                        Text("💵 Deposit Refunded: ${formatRupee(record.depositRefunded)}", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) {
                Text("Close")
            }
        }
    )
}

@Composable
fun EditRoomDialog(
    room: RoomUnit,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double) -> Unit
) {
    var roomNo by remember { mutableStateOf(room.roomNumber) }
    var rent by remember { mutableStateOf(room.baseRent.toInt().toString()) }
    var rate by remember { mutableStateOf(room.electricityRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Edit Room ${room.roomNumber}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomNo,
                    onValueChange = { roomNo = it },
                    label = { Text("Room No") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate/Unit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rent.toDoubleOrNull() ?: room.baseRent
                    val rt = rate.toDoubleOrNull() ?: room.electricityRate
                    if (roomNo.isNotBlank()) onSave(roomNo, r, rt)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Save & Log Changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } }
    )
}

@Composable
fun AddRoomDialog(
    properties: List<Property>,
    selectedPropertyId: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double) -> Unit
) {
    var propId by remember { mutableStateOf(selectedPropertyId ?: properties.firstOrNull()?.id ?: "") }
    var roomNo by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Add Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomNo,
                    onValueChange = { roomNo = it },
                    label = { Text("Room No (e.g. 101, 01)") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate/Unit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rent.toDoubleOrNull() ?: 0.0
                    val rt = rate.toDoubleOrNull() ?: 10.0
                    if (roomNo.isNotBlank() && r > 0) onSave(propId, roomNo, r, rt)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Save Room") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } }
    )
}

@Composable
fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Add New Property", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Property Name") }, shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address / Area") }, shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = ownerPhone, onValueChange = { ownerPhone = it }, label = { Text("Owner Phone") }, shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, address, city, ownerName, ownerPhone) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Save Property") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } }
    )
}

@Composable
fun AssignTenantDialog(
    roomNumber: String,
    todayDate: String,
    onDismiss: () -> Unit,
    onAssign: (String, String, String, String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var moveInDate by remember { mutableStateOf(todayDate) }
    var deposit by remember { mutableStateOf("0") }
    var meterReading by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Assign Tenant to Room $roomNumber", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tenant Name") }, shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar Number") }, shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = moveInDate, onValueChange = { moveInDate = it }, label = { Text("Move-In Date") }, shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = deposit, onValueChange = { deposit = it }, label = { Text("Security Deposit (₹)") }, shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = meterReading, onValueChange = { meterReading = it }, label = { Text("Initial Meter Reading (kWh)") }, shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAssign(name, phone, aadhaar, moveInDate, deposit.toDoubleOrNull() ?: 0.0, meterReading.toDoubleOrNull() ?: 0.0)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Assign Tenant") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } }
    )
}

@Composable
fun CheckoutTenantDialog(
    tenantName: String,
    moveInDate: String,
    todayDate: String,
    deposit: Double,
    totalPaidSoFar: Double,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var moveOutDate by remember { mutableStateOf(todayDate) }
    var refund by remember { mutableStateOf(deposit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Vacate $tenantName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Move-In Date: $moveInDate", fontSize = 12.sp, color = TextMuted)
                Text("Lifetime Rent Collected: ${formatRupee(totalPaidSoFar)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                Text("Security Deposit Paid: ${formatRupee(deposit)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = moveOutDate, onValueChange = { moveOutDate = it }, label = { Text("Move-Out Date") }, shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = refund, onValueChange = { refund = it }, label = { Text("Deposit Refund Amount (₹)") }, shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(moveOutDate, refund.toDoubleOrNull() ?: 0.0) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
            ) { Text("Confirm Checkout & Archive") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } }
    )
}

@Composable
fun TenantDetailsModal(tenant: Tenant, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = BrandBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tenant Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Name: ${tenant.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Phone: ${tenant.phone}", fontSize = 13.sp)
                if (tenant.aadhaarNo.isNotBlank()) {
                    Text("Aadhaar: ${tenant.aadhaarNo}", fontSize = 13.sp)
                }
                Text("Move-in: ${tenant.moveInDate}", fontSize = 13.sp)
                Text("Security Deposit: ${formatRupee(tenant.securityDeposit)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                Text("Initial Meter: ${tenant.initialMeterReading} units", fontSize = 13.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) {
                Text("Done")
            }
        }
    )
}
