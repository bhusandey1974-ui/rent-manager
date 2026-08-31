package com.example.rentmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import java.net.URLEncoder

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

    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()
    val bills by vm.bills.collectAsState()
    val pastTenancies by vm.pastTenancies.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) }

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
                                fontSize = 20.sp,
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
                                .clip(RoundedCornerShape(12.dp))
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
                    Icon(Icons.Default.Add, contentDescription = "Add Room", modifier = Modifier.size(28.dp))
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
            text = { Text("Are you sure you want to delete Room ${room.roomNumber}? All tenant and billing records associated with this room will be permanently removed.", fontFamily = CleanFont) },
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
            text = { Text("Are you sure you want to clear all billing and occupancy history for Room ${room.roomNumber}?", fontFamily = CleanFont) },
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
