package com.example.rentmanager

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentManagerMainApp(vm: RentViewModel) {
    val context = LocalContext.current
    val properties by vm.properties.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()
    val bills by vm.bills.collectAsState()
    val history by vm.tenantHistory.collectAsState()

    var selectedPropertyId by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableIntStateOf(0) }

    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showAssignTenantDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showBillDialog by remember { mutableStateOf<Pair<RoomUnit, Tenant>?>(null) }
    var showCheckoutDialog by remember { mutableStateOf<Tenant?>(null) }
    var showEditRoomDialog by remember { mutableStateOf<RoomUnit?>(null) }

    LaunchedEffect(properties) {
        if (selectedPropertyId == null && properties.isNotEmpty()) {
            selectedPropertyId = properties.first().id
        }
    }

    val currentProperty = properties.find { it.id == selectedPropertyId }
    val currentRooms = rooms.filter { it.propertyId == selectedPropertyId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentProperty?.name ?: "Rent Manager",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (currentProperty != null && currentProperty.city.isNotBlank()) {
                            Text(
                                text = "${currentProperty.address}, ${currentProperty.city}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showAddPropertyDialog = true }) {
                        Icon(Icons.Default.AddHome, contentDescription = "Add Property", tint = Color.White)
                    }
                    IconButton(onClick = { vm.authRepo.signOut() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlueDark)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Rooms") },
                    label = { Text("Rooms") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Summary") },
                    label = { Text("Summary") }
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0 && selectedPropertyId != null) {
                FloatingActionButton(
                    onClick = { showAddRoomDialog = true },
                    containerColor = BrandBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Room")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PageBackground)
        ) {
            if (properties.isEmpty()) {
                EmptyStateView(
                    title = "No Properties Added",
                    subtitle = "Tap the button below to add your first property or building.",
                    buttonText = "Add Property",
                    onAction = { showAddPropertyDialog = true }
                )
            } else {
                when (currentTab) {
                    0 -> RoomsDashboardView(
                        properties = properties,
                        selectedPropertyId = selectedPropertyId,
                        rooms = currentRooms,
                        tenants = tenants,
                        bills = bills,
                        onSelectProperty = { selectedPropertyId = it },
                        onAssignTenant = { showAssignTenantDialog = it },
                        onGenerateBill = { room, tenant -> showBillDialog = Pair(room, tenant) },
                        onEditRoom = { showEditRoomDialog = it },
                        onDeleteRoom = { vm.deleteRoom(it.id) },
                        onCheckout = { showCheckoutDialog = it }
                    )
                    1 -> HistoryView(history = history.filter { it.propertyId == selectedPropertyId })
                    2 -> SummaryAnalyticsView(
                        rooms = currentRooms,
                        tenants = tenants.filter { it.propertyId == selectedPropertyId },
                        bills = bills.filter { it.propertyId == selectedPropertyId }
                    )
                }
            }
        }
    }

    if (showAddPropertyDialog) {
        AddPropertyDialog(
            onDismiss = { showAddPropertyDialog = false },
            onConfirm = { name, address, city, owner, phone ->
                val id = vm.addProperty(name, address, city, owner, phone)
                selectedPropertyId = id
                showAddPropertyDialog = false
            }
        )
    }

    if (showAddRoomDialog && selectedPropertyId != null) {
        AddRoomDialog(
            onDismiss = { showAddRoomDialog = false },
            onConfirm = { num, type, rent, rate ->
                vm.addRoom(selectedPropertyId!!, num, type, rent, rate)
                showAddRoomDialog = false
            }
        )
    }

    showEditRoomDialog?.let { room ->
        EditRoomDialog(
            room = room,
            onDismiss = { showEditRoomDialog = null },
            onConfirm = { num, rent, rate ->
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
            onConfirm = { name, phone, aadhaar, date, deposit, reading ->
                vm.assignTenant(room.propertyId, room.id, name, phone, aadhaar, date, deposit, reading)
                showAssignTenantDialog = null
            }
        )
    }

    showBillDialog?.let { (room, tenant) ->
        val lastBill = bills.filter { it.roomId == room.id }.maxByOrNull { it.id }
        val prevReading = lastBill?.currentMeterReading ?: tenant.initialMeterReading
        val pendingDue = vm.getCumulativePendingDue(room.id)

        GenerateBillDialog(
            room = room,
            tenant = tenant,
            prevReading = prevReading,
            previousDue = pendingDue,
            defaultMonth = vm.getPreviousMonthFormatted(),
            onDismiss = { showBillDialog = null },
            onConfirm = { month, curReading, maint, paid, mode ->
                vm.lodgeBillAndPayment(
                    propertyId = room.propertyId,
                    roomId = room.id,
                    tenantId = tenant.id,
                    month = month,
                    baseRent = room.baseRent,
                    prevUnit = prevReading,
                    curUnit = curReading,
                    rate = room.electricityRate,
                    maintenanceCharge = maint,
                    previousDue = pendingDue,
                    amountPaid = paid,
                    paymentMode = mode
                )

                val tempBill = BillRecord(
                    propertyId = room.propertyId,
                    roomId = room.id,
                    tenantId = tenant.id,
                    monthYear = month,
                    baseRent = room.baseRent,
                    prevMeterReading = prevReading,
                    currentMeterReading = curReading,
                    electricityRate = room.electricityRate,
                    maintenanceCharge = maint,
                    previousDueCarryover = pendingDue,
                    amountPaid = paid,
                    paymentMode = mode
                )
                val msg = vm.getWhatsAppReceiptText(tempBill, tenant, currentProperty ?: Property(), room)
                shareToWhatsApp(context, tenant.phone, msg)
                showBillDialog = null
            }
        )
    }

    showCheckoutDialog?.let { tenant ->
        CheckoutTenantDialog(
            tenant = tenant,
            todayDate = vm.getTodayDateFormatted(),
            onDismiss = { showCheckoutDialog = null },
            onConfirm = { date, refund ->
                vm.checkoutTenant(tenant.id, date, refund)
                showCheckoutDialog = null
            }
        )
    }
}

@Composable
fun RoomsDashboardView(
    properties: List<Property>,
    selectedPropertyId: String?,
    rooms: List<RoomUnit>,
    tenants: List<Tenant>,
    bills: List<BillRecord>,
    onSelectProperty: (String) -> Unit,
    onAssignTenant: (RoomUnit) -> Unit,
    onGenerateBill: (RoomUnit, Tenant) -> Unit,
    onEditRoom: (RoomUnit) -> Unit,
    onDeleteRoom: (RoomUnit) -> Unit,
    onCheckout: (Tenant) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScrollableTabRow(
                selectedTabIndex = properties.indexOfFirst { it.id == selectedPropertyId }.coerceAtLeast(0),
                edgePadding = 0.dp,
                divider = {},
                containerColor = Color.Transparent
            ) {
                properties.forEach { prop ->
                    Tab(
                        selected = prop.id == selectedPropertyId,
                        onClick = { onSelectProperty(prop.id) },
                        text = { Text(prop.name, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        if (rooms.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.MeetingRoom, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No rooms added in this property.", fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Tap the '+' button below to add your first room.", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(rooms) { room ->
                val tenant = tenants.find { it.roomId == room.id }
                val latestBill = bills.filter { it.roomId == room.id }.maxByOrNull { it.id }

                RoomCard(
                    room = room,
                    tenant = tenant,
                    latestBill = latestBill,
                    onAssignTenant = { onAssignTenant(room) },
                    onGenerateBill = { tenant?.let { onGenerateBill(room, it) } },
                    onEditRoom = { onEditRoom(room) },
                    onDeleteRoom = { onDeleteRoom(room) },
                    onCheckout = { tenant?.let { onCheckout(it) } }
                )
            }
        }
    }
}

@Composable
fun RoomCard(
    room: RoomUnit,
    tenant: Tenant?,
    latestBill: BillRecord?,
    onAssignTenant: () -> Unit,
    onGenerateBill: () -> Unit,
    onEditRoom: () -> Unit,
    onDeleteRoom: () -> Unit,
    onCheckout: () -> Unit
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
                            .clip(CircleShape)
                            .background(if (room.isVacant) TextMuted else SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${room.roomType} ${room.roomNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Row {
                    IconButton(onClick = onEditRoom, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                    if (room.isVacant) {
                        IconButton(onClick = onDeleteRoom, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Base Rent: ₹${room.baseRent}", fontSize = 13.sp, color = TextMuted)
                Text("Elec Rate: ₹${room.electricityRate}/unit", fontSize = 13.sp, color = TextMuted)
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = CardBorder)

            if (tenant != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(tenant.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Text("📞 ${tenant.phone}", fontSize = 12.sp, color = TextMuted)
                        Text("Moved in: ${tenant.moveInDate}", fontSize = 11.sp, color = TextMuted)
                    }

                    if (latestBill != null && latestBill.remainingDue > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DangerRed.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Due: ₹${latestBill.remainingDue}",
                                color = DangerRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onGenerateBill,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bill & Receipt", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onCheckout,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, DangerRed)
                    ) {
                        Text("Checkout", color = DangerRed, fontSize = 12.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Currently Vacant", color = TextMuted, fontSize = 13.sp)
                    Button(
                        onClick = onAssignTenant,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Assign Tenant", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

