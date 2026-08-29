package com.example.rentmanager

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Clean Modern Theme Colors
val BluePrimary = Color(0xFF0088FF)
val BlueGradientStart = Color(0xFF1EAEFF)
val BlueGradientEnd = Color(0xFF007AEB)
val AppBg = Color(0xFFFBFDFF)
val CardBorder = Color(0xFFEDF2F7)
val TextDark = Color(0xFF1E293B)
val TextMuted = Color(0xFF94A3B8)
val GreenSuccess = Color(0xFF10B981)
val RedDanger = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentManagerMainApp(vm: RentViewModel) {
    val context = LocalContext.current
    val properties by vm.properties.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()
    val bills by vm.bills.collectAsState()

    var selectedPropertyId by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableIntStateOf(0) } // 0: Properties, 1: Revenue

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
    val currentTenants = tenants.filter { it.propertyId == selectedPropertyId }
    val currentBills = bills.filter { it.propertyId == selectedPropertyId }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BluePrimary),
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
                                text = if (currentTab == 0) (currentProperty?.name ?: "Rent Manager") else "Revenue & Analytics",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = if (currentTab == 0) "${currentRooms.size} Units Registered" else "Lifetime Ledger",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE))
                            .clickable { showAddPropertyDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DomainAdd,
                            contentDescription = "Add Property",
                            tint = BluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
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
                        icon = Icons.Default.AutoGraph,
                        onClick = { currentTab = 1 }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                FloatingActionButton(
                    onClick = {
                        if (properties.isEmpty()) {
                            showAddPropertyDialog = true
                        } else {
                            showAddRoomDialog = true
                        }
                    },
                    containerColor = BluePrimary,
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
                .background(AppBg)
        ) {
            when (currentTab) {
                0 -> {
                    if (currentRooms.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, CardBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp, horizontal = 24.dp),
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
                                            tint = BluePrimary,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
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
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (properties.size > 1) {
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
                                                onClick = { selectedPropertyId = prop.id },
                                                text = { Text(prop.name, fontWeight = FontWeight.Bold) }
                                            )
                                        }
                                    }
                                }
                            }

                            items(currentRooms) { room ->
                                val tenant = tenants.find { it.roomId == room.id }
                                val latestBill = bills.filter { it.roomId == room.id }.maxByOrNull { it.id }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, CardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
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
                                                        .background(if (room.isVacant) Color(0xFFCBD5E1) else GreenSuccess)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "${room.roomType} ${room.roomNumber}",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                            }

                                            Row {
                                                IconButton(onClick = { showEditRoomDialog = room }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(17.dp))
                                                }
                                                if (room.isVacant) {
                                                    IconButton(onClick = { vm.deleteRoom(room.id) }, modifier = Modifier.size(32.dp)) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedDanger, modifier = Modifier.size(17.dp))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Rent: ₹${room.baseRent}", fontSize = 13.sp, color = TextMuted)
                                            Text("Elec: ₹${room.electricityRate}/unit", fontSize = 13.sp, color = TextMuted)
                                        }

                                        Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = CardBorder)

                                        if (tenant != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(tenant.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                                                    Text("📞 ${tenant.phone}", fontSize = 12.sp, color = TextMuted)
                                                }

                                                if (latestBill != null && latestBill.remainingDue > 0) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = RedDanger.copy(alpha = 0.1f)
                                                    ) {
                                                        Text(
                                                            text = "Due: ₹${latestBill.remainingDue}",
                                                            color = RedDanger,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { showBillDialog = Pair(room, tenant) },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                                                ) {
                                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Bill & Receipt", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                }

                                                OutlinedButton(
                                                    onClick = { showCheckoutDialog = tenant },
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, RedDanger)
                                                ) {
                                                    Text("Checkout", color = RedDanger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
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
                    maintenance = maint,
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
                .size(width = 62.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) Color(0xFFE0F2FE) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) BluePrimary else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) BluePrimary else TextMuted
        )
    }
}
@Composable
fun RevenueView(
    bills: List<BillRecord>,
    rooms: List<RoomUnit>,
    tenants: List<Tenant>,
    property: Property,
    onShareWhatsApp: (BillRecord, Tenant, RoomUnit) -> Unit
) {
    var ledgerFilter by remember { mutableStateOf("All") }

    val totalCollected = bills.sumOf { it.amountPaid }
    val totalRent = bills.sumOf { it.baseRent }
    val totalElec = bills.sumOf { it.totalElectricityCharge }
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
        // Lifetime Revenue Blue Gradient Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(BlueGradientStart, BlueGradientEnd)
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
                                text = "LIFETIME REVENUE COLLECTION",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Icon(
                                imageVector = Icons.Default.AutoGraph,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "₹${"%.2f".format(totalCollected)}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rent Earnings", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${"%.2f".format(totalRent)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Electricity", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${"%.2f".format(totalElec)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Total Due", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${"%.2f".format(totalDue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Current Year Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorder)
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
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEDE9FE)
                        ) {
                            Text(
                                text = "Live",
                                color = Color(0xFF7C3AED),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        YearStatBox(modifier = Modifier.weight(1f), label = "Base Rent Billed", value = "₹${"%.2f".format(totalRent)}", valueColor = TextDark)
                        YearStatBox(modifier = Modifier.weight(1f), label = "Electricity Charges", value = "₹${"%.2f".format(totalElec)}", valueColor = BluePrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        YearStatBox(modifier = Modifier.weight(1f), label = "Total Invoiced", value = "₹${"%.2f".format(totalRent + totalElec)}", valueColor = BluePrimary)
                        YearStatBox(modifier = Modifier.weight(1f), label = "Collected", value = "₹${"%.2f".format(totalCollected)}", valueColor = GreenSuccess)
                    }
                }
            }
        }

        // Ledger Header with Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Billing Ledger (${filteredBills.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Paid", "Pending").forEach { filter ->
                        val isSelected = ledgerFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BluePrimary else Color(0xFFF1F5F9))
                                .clickable { ledgerFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextDark
                            )
                        }
                    }
                }
            }
        }

        // Payment History: Newest paid bill appears on top
        if (filteredBills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No billing entries found.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredBills.reversed()) { bill ->
                val room = rooms.find { it.id == bill.roomId }
                val tenant = tenants.find { it.id == bill.tenantId }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(bill.monthYear, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Room ${room?.roomNumber ?: ""} • ${tenant?.name ?: "Tenant"}", fontSize = 12.sp, color = TextMuted)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${bill.totalBillAmount}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BluePrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (bill.remainingDue <= 0) "Paid: ₹${bill.amountPaid}" else "Due: ₹${bill.remainingDue}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (bill.remainingDue <= 0) GreenSuccess else RedDanger
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = CardBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Units: ${bill.unitsConsumed} (${bill.prevMeterReading} → ${bill.currentMeterReading})", fontSize = 12.sp, color = TextMuted)
                            if (tenant != null && room != null) {
                                TextButton(
                                    onClick = { onShareWhatsApp(bill, tenant, room) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 11.sp, color = GreenSuccess, fontWeight = FontWeight.Bold)
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
fun YearStatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
