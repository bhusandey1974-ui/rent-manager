package com.example.rentmanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentManagerMainApp(vm: RentViewModel) {
    val context = LocalContext.current
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()
    val bills by vm.bills.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) }

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showAssignTenantDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showBillDialog by remember { mutableStateOf<Pair<RoomUnit, Tenant>?>(null) }
    var showCheckoutDialog by remember { mutableStateOf<Tenant?>(null) }
    var showEditRoomDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showRoomHistoryDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showTenantDetailsDialog by remember { mutableStateOf<Pair<Tenant, RoomUnit>?>(null) }
    var showEditTenantDialog by remember { mutableStateOf<Tenant?>(null) }

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
        RoomHistoryDialog(
            room = room,
            bills = roomBills,
            onDismiss = { showRoomHistoryDialog = null }
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
            // Header: Status, Room name, actions, Rent
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
                // Compact Clickable Tenant Pill (Opens Details & History)
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

                // Action Buttons
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
                            Icon(Icons.Default.QueryStats, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEDE9FE)
                        ) {
                            Text(
                                text = "Live",
                                color = Color(0xFF7C3AED),
                                fontSize = 11.sp,
                                fontFamily = CleanFont,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
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
