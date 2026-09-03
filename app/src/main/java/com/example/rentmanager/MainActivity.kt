package com.example.rentmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: RentViewModel = viewModel()
            RentManagerMainApp(vm)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentManagerMainApp(vm: RentViewModel) {
    val context = LocalContext.current
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }

    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()
    val bills by vm.bills.collectAsState()
    val pastTenancies by vm.pastTenancies.collectAsState()

    var isAuthenticated by remember { mutableStateOf(auth.currentUser != null) }
    var currentTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        auth.currentUser?.let { user ->
            vm.loadCloudData(user.uid)
        }
    }

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showEditRoomDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showDeleteRoomConfirm by remember { mutableStateOf<RoomUnit?>(null) }
    var showAssignTenantDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showTenantDetailsDialog by remember { mutableStateOf<Pair<Tenant, RoomUnit>?>(null) }
    var showEditTenantDialog by remember { mutableStateOf<Tenant?>(null) }
    var showBillDialog by remember { mutableStateOf<Pair<RoomUnit, Tenant>?>(null) }
    var showCheckoutDialog by remember { mutableStateOf<Tenant?>(null) }
    var showRoomHistoryDialog by remember { mutableStateOf<RoomUnit?>(null) }
    var showClearHistoryConfirm by remember { mutableStateOf<RoomUnit?>(null) }
    var showResetAllConfirm by remember { mutableStateOf(false) }

    val defaultProperty = remember { Property(name = "Main Complex", address = "Building 1") }

    if (!isAuthenticated) {
        AuthView(
            onLoginSuccess = { email, password, isRegister ->
                if (isRegister) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            val uid = result.user?.uid.orEmpty()
                            isAuthenticated = true
                            vm.loadCloudData(uid)
                        }
                        .addOnFailureListener { err ->
                            Toast.makeText(context, err.localizedMessage ?: "Registration failed", Toast.LENGTH_LONG).show()
                        }
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            val uid = result.user?.uid.orEmpty()
                            isAuthenticated = true
                            vm.loadCloudData(uid)
                        }
                        .addOnFailureListener { err ->
                            Toast.makeText(context, err.localizedMessage ?: "Login failed", Toast.LENGTH_LONG).show()
                        }
                }
            },
            onSkipOffline = {
                isAuthenticated = true
            }
        )
    } else {
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(UIBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (currentTab == 0) "Rent Manager" else "Revenue & Ledger",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CleanFont,
                                    color = UIDarkText
                                )
                                Text(
                                    text = if (currentTab == 0) "${rooms.size} Units Registered" else "Lifetime Ledger",
                                    fontSize = 12.sp,
                                    fontFamily = CleanFont,
                                    color = UIMutedText
                                )
                            }
                        }

                        if (currentTab == 0) {
                            IconButton(
                                onClick = { showAddRoomDialog = true },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DomainAdd,
                                    contentDescription = "Add Unit",
                                    tint = UIBluePrimary
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.Domain, contentDescription = "Properties") },
                        label = { Text("Properties", fontFamily = CleanFont, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UIBluePrimary,
                            selectedTextColor = UIBluePrimary,
                            indicatorColor = Color(0xFFE0F2FE),
                            unselectedIconColor = UIMutedText,
                            unselectedTextColor = UIMutedText
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Revenue") },
                        label = { Text("Revenue", fontFamily = CleanFont, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UIBluePrimary,
                            selectedTextColor = UIBluePrimary,
                            indicatorColor = Color(0xFFE0F2FE),
                            unselectedIconColor = UIMutedText,
                            unselectedTextColor = UIMutedText
                        )
                    )
                }
            },
            floatingActionButton = {
                if (currentTab == 0) {
                    FloatingActionButton(
                        onClick = { showAddRoomDialog = true },
                        containerColor = UIBluePrimary,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Room", modifier = Modifier.size(26.dp))
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    label = "TabTransition"
                ) { target ->
                    if (target == 0) {
                        PropertiesView(
                            rooms = rooms,
                            tenants = tenants,
                            bills = bills,
                            vm = vm,
                            onAddRoom = { showAddRoomDialog = true },
                            onEditRoom = { r: RoomUnit -> showEditRoomDialog = r },
                            onDeleteRoom = { r: RoomUnit -> showDeleteRoomConfirm = r },
                            onAssignTenant = { r: RoomUnit -> showAssignTenantDialog = r },
                            onTenantClick = { t: Tenant, r: RoomUnit -> showTenantDetailsDialog = Pair(t, r) },
                            onLodgeBill = { r: RoomUnit, t: Tenant -> showBillDialog = Pair(r, t) },
                            onVacate = { t: Tenant -> showCheckoutDialog = t },
                            onHistory = { r: RoomUnit -> showRoomHistoryDialog = r }
                        )
                    } else {
                        RevenueView(
                            bills = bills,
                            rooms = rooms,
                            tenants = tenants,
                            onClearAll = { showResetAllConfirm = true },
                            onShareWhatsApp = { bill: BillRecord, tenant: Tenant, room: RoomUnit ->
                                val text = vm.getWhatsAppReceiptText(bill, tenant, defaultProperty, room)
                                val encodedText = URLEncoder.encode(text, "UTF-8")
                                val cleanNumber = tenant.phone.replace("+", "").replace(" ", "").replace("-", "")
                                val finalNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
                                val uri = "https://wa.me/$finalNumber?text=$encodedText"

                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not launch WhatsApp", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
        if (showAddRoomDialog) {
        AddRoomDialog(
            onDismiss = { showAddRoomDialog = false },
            onConfirm = { roomNo, unitType, rent, rate ->
                vm.addRoom(defaultProperty.id, roomNo, unitType, rent, rate)
                showAddRoomDialog = false
            }
        )
    }

    showEditRoomDialog?.let { room ->
        EditRoomDialog(
            room = room,
            onDismiss = { showEditRoomDialog = null },
            onConfirm = { roomNo, rent, rate ->
                vm.editRoom(room.id, roomNo, rent, rate)
                showEditRoomDialog = null
            }
        )
    }

    showDeleteRoomConfirm?.let { room ->
        AlertDialog(
            onDismissRequest = { showDeleteRoomConfirm = null },
            title = { Text("Delete Room", fontWeight = FontWeight.Bold, fontFamily = CleanFont) },
            text = { Text("Are you sure you want to delete Room ${room.roomNumber}? All associated records will be removed.", fontFamily = CleanFont) },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteRoom(room.id)
                        showDeleteRoomConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UIRedDanger)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteRoomConfirm = null }) {
                    Text("Cancel", fontFamily = CleanFont)
                }
            }
        )
    }

    showAssignTenantDialog?.let { room ->
        AssignTenantDialog(
            room = room,
            defaultDate = vm.getTodayDateFormatted(),
            onDismiss = { showAssignTenantDialog = null },
            onConfirm = { name, phone, aadhaar, moveIn, deposit, initialMeter ->
                vm.assignTenant(defaultProperty.id, room.id, name, phone, aadhaar, moveIn, deposit, initialMeter)
                showAssignTenantDialog = null
            }
        )
    }

    showTenantDetailsDialog?.let { pair ->
        val t = pair.first
        val r = pair.second
        TenantDetailsDialog(
            tenant = t,
            room = r,
            onDismiss = { showTenantDetailsDialog = null },
            onEdit = {
                showTenantDetailsDialog = null
                showEditTenantDialog = t
            },
            onVacate = {
                showTenantDetailsDialog = null
                showCheckoutDialog = t
            }
        )
    }

    showEditTenantDialog?.let { tenant ->
        EditTenantDialog(
            tenant = tenant,
            onDismiss = { showEditTenantDialog = null },
            onConfirm = { name, phone, aadhaar ->
                vm.editTenant(tenant.id, name, phone, aadhaar)
                showEditTenantDialog = null
            }
        )
    }

    showBillDialog?.let { pair ->
        val room = pair.first
        val tenant = pair.second
        val lastBill = bills.filter { it.roomId == room.id }.maxByOrNull { it.timestamp }
        val prevReading = lastBill?.currentMeterReading ?: tenant.initialMeterReading
        val carriedDue = vm.getCumulativePendingDue(room.id)

        GenerateBillDialog(
            room = room,
            tenant = tenant,
            prevReading = prevReading,
            previousDue = carriedDue,
            defaultMonth = vm.getPreviousMonthFormatted(),
            onDismiss = { showBillDialog = null },
            onConfirm = { month, curReading, maint, paid, mode ->
                vm.lodgeBillAndPayment(
                    propertyId = defaultProperty.id,
                    roomId = room.id,
                    tenantId = tenant.id,
                    month = month,
                    baseRent = room.baseRent,
                    prevUnit = prevReading,
                    curUnit = curReading,
                    rate = room.electricityRate,
                    maintenance = maint,
                    previousDue = carriedDue,
                    amountPaid = paid,
                    paymentMode = mode
                )
                showBillDialog = null
            }
        )
    }

    showCheckoutDialog?.let { tenant ->
        val room = rooms.find { it.id == tenant.roomId }
        CheckoutDialog(
            tenant = tenant,
            room = room,
            defaultDate = vm.getTodayDateFormatted(),
            onDismiss = { showCheckoutDialog = null },
            onConfirm = { vacateDate, refund ->
                vm.checkoutTenant(tenant.id, vacateDate, refund)
                showCheckoutDialog = null
            }
        )
    }

    showRoomHistoryDialog?.let { room ->
        val roomBills = bills.filter { it.roomId == room.id }
        val pastTenants = pastTenancies.filter { it.roomId == room.id }
        RoomHistoryDialog(
            room = room,
            bills = roomBills,
            pastTenants = pastTenants,
            onClearHistory = { showClearHistoryConfirm = room },
            onDismiss = { showRoomHistoryDialog = null }
        )
    }

    showClearHistoryConfirm?.let { room ->
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = null },
            title = { Text("Clear Room History", fontWeight = FontWeight.Bold, fontFamily = CleanFont) },
            text = { Text("Clear all billing and tenant history for Room ${room.roomNumber}?", fontFamily = CleanFont) },
            confirmButton = {
                Button(
                    onClick = {
                        vm.clearRoomHistory(room.id)
                        showClearHistoryConfirm = null
                        showRoomHistoryDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UIRedDanger)
                ) {
                    Text("Clear", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = null }) {
                    Text("Cancel", fontFamily = CleanFont)
                }
            }
        )
    }

    if (showResetAllConfirm) {
        AlertDialog(
            onDismissRequest = { showResetAllConfirm = false },
            title = { Text("Reset Ledger", fontWeight = FontWeight.Bold, fontFamily = CleanFont) },
            text = { Text("Are you sure you want to wipe all past ledger records?", fontFamily = CleanFont) },
            confirmButton = {
                Button(
                    onClick = {
                        vm.resetAllRevenueData()
                        showResetAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UIRedDanger)
                ) {
                    Text("Reset All", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllConfirm = false }) {
                    Text("Cancel", fontFamily = CleanFont)
                }
            }
        )
    }
}
// ==========================================
// PROPERTIES VIEW
// ==========================================

@Composable
fun PropertiesView(
    rooms: List<RoomUnit>,
    tenants: List<Tenant>,
    bills: List<BillRecord>,
    vm: RentViewModel,
    onAddRoom: () -> Unit,
    onEditRoom: (RoomUnit) -> Unit,
    onDeleteRoom: (RoomUnit) -> Unit,
    onAssignTenant: (RoomUnit) -> Unit,
    onTenantClick: (Tenant, RoomUnit) -> Unit,
    onLodgeBill: (RoomUnit, Tenant) -> Unit,
    onVacate: (Tenant) -> Unit,
    onHistory: (RoomUnit) -> Unit
) {
    if (rooms.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, UICardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddHomeWork, contentDescription = null, tint = UIBluePrimary, modifier = Modifier.size(28.dp))
                    }
                    Text(
                        text = "No Units Added Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CleanFont,
                        color = UIDarkText
                    )
                    Text(
                        text = "Start by registering your rental rooms with rent & electricity charges.",
                        fontSize = 13.sp,
                        fontFamily = CleanFont,
                        color = UIMutedText,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onAddRoom,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
                    ) {
                        Text("Add First Unit", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
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
                val tenant = tenants.find { it.roomId == room.id && it.isActive }
                val pendingDue = vm.getCumulativePendingDue(room.id)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(if (tenant != null) UIRedDanger else UIGreenSuccess)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Room ${room.roomNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    fontFamily = CleanFont,
                                    color = UIDarkText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { onEditRoom(room) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Room", tint = UIMutedText, modifier = Modifier.size(15.dp))
                                }
                                IconButton(onClick = { onDeleteRoom(room) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = UIRedDanger.copy(alpha = 0.6f), modifier = Modifier.size(15.dp))
                                }

                                if (pendingDue > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFFFBEB))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B))
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Due: ₹${"%,.0f".format(pendingDue)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = CleanFont,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "₹${"%,.2f".format(room.baseRent)}/mo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = CleanFont,
                                color = UIBluePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (tenant != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onTenantClick(tenant, room) },
                                color = Color(0xFFF8FAFC)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tenant.name.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = CleanFont,
                                                color = UIBluePrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = tenant.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                fontFamily = CleanFont,
                                                color = UIDarkText
                                            )
                                            Text(
                                                text = "📞 ${tenant.phone}  •  In: ${tenant.moveInDate}",
                                                fontSize = 11.sp,
                                                fontFamily = CleanFont,
                                                color = UIMutedText
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = "View Profile", tint = UIMutedText)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onHistory(room) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, UICardBorder)
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp), tint = UIDarkText)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("History", fontSize = 12.sp, fontFamily = CleanFont, color = UIDarkText)
                                }

                                Button(
                                    onClick = { onLodgeBill(room, tenant) },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lodge Bill", fontSize = 12.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onVacate(tenant) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, UICardBorder)
                                ) {
                                    Text("Vacate", fontSize = 12.sp, fontFamily = CleanFont, color = UIRedDanger)
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🟢 Unit is Vacant", color = UIGreenSuccess, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 12.sp)
                                    Text("Rate: ₹${room.electricityRate}/u", color = UIGreenSuccess, fontFamily = CleanFont, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onHistory(room) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, UICardBorder)
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp), tint = UIDarkText)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("History", fontSize = 12.sp, fontFamily = CleanFont, color = UIDarkText)
                                }

                                Button(
                                    onClick = { onAssignTenant(room) },
                                    modifier = Modifier.weight(2f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Assign Tenant", fontSize = 12.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
// ==========================================
// REVENUE VIEW
// ==========================================

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(UIBlueGradientStart, UIBlueGradientEnd)))
                        .padding(20.dp)
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

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "₹${"%,.2f".format(totalCollected)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = CleanFont,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rent Earnings", fontSize = 11.sp, fontFamily = CleanFont, color = Color.White.copy(alpha = 0.85f))
                                Text("₹${"%,.2f".format(totalRent)}", fontSize = 13.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Electricity", fontSize = 11.sp, fontFamily = CleanFont, color = Color.White.copy(alpha = 0.85f))
                                Text("₹${"%,.2f".format(totalElec)}", fontSize = 13.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Total Due", fontSize = 11.sp, fontFamily = CleanFont, color = Color.White.copy(alpha = 0.85f))
                                Text("₹${"%,.2f".format(totalDue)}", fontSize = 13.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Billing Ledger (${filteredBills.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CleanFont,
                    color = UIDarkText
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Paid", "Pending").forEach { filter ->
                        val isSelected = ledgerFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) UIBluePrimary else Color(0xFFF1F5F9))
                                .clickable { ledgerFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
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
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, UICardBorder)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp), contentAlignment = Alignment.Center) {
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

                val formattedPaymentDate = remember(bill.timestamp) {
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(bill.timestamp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, UICardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "${tenant?.name ?: "Tenant"} (Room ${room?.roomNumber ?: ""})",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CleanFont,
                                    fontSize = 15.sp,
                                    color = UIDarkText
                                )
                                Text(
                                    text = "Billing Month: ${bill.monthYear}",
                                    fontSize = 12.sp,
                                    fontFamily = CleanFont,
                                    color = UIMutedText
                                )
                                Text(
                                    text = "Paid on: $formattedPaymentDate",
                                    fontSize = 11.sp,
                                    fontFamily = CleanFont,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (bill.remainingDue <= 0) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (bill.remainingDue <= 0) "PAID \u2705 (${bill.paymentMode})" else "DUE: ₹${"%.0f".format(bill.remainingDue)}",
                                    fontSize = 11.sp,
                                    fontFamily = CleanFont,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bill.remainingDue <= 0) UIGreenSuccess else Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = UICardBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Rent: ₹${bill.baseRent}  •  Elec: ₹${"%.2f".format(units * bill.electricityRate)}", fontSize = 11.sp, fontFamily = CleanFont, color = UIMutedText)
                                Text("Units: $units (${bill.prevMeterReading} -> ${bill.currentMeterReading})", fontSize = 11.sp, fontFamily = CleanFont, color = UIMutedText)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${"%,.2f".format(totalBillAmount)}", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 15.sp, color = UIBluePrimary)
                                Text("Paid: ₹${"%,.2f".format(bill.amountPaid)} (${bill.paymentMode})", fontSize = 11.sp, fontFamily = CleanFont, color = UIGreenSuccess)
                            }
                        }

                        if (tenant != null && room != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onShareWhatsApp(bill, tenant, room) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = UIGreenSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp Receipt", fontSize = 12.sp, fontFamily = CleanFont, color = UIGreenSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
