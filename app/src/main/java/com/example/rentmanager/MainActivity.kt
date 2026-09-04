package com.example.rentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(vm: RentViewModel) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var isAuthenticated by remember { mutableStateOf(auth.currentUser != null) }
    var currentTab by remember { mutableStateOf(NavigationTab.PROPERTIES) }

    var showSettingsMenu by remember { mutableStateOf(false) }
    var showResetStep1Dialog by remember { mutableStateOf(false) }
    var showResetStep2Dialog by remember { mutableStateOf(false) }

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var roomToAssignTenant by remember { mutableStateOf<RoomUnit?>(null) }
    var roomToLodgeBill by remember { mutableStateOf<RoomUnit?>(null) }
    var roomToVacate by remember { mutableStateOf<RoomUnit?>(null) }
    var roomForHistory by remember { mutableStateOf<RoomUnit?>(null) }

    val tenants by vm.tenants.collectAsState()
    val bills by vm.bills.collectAsState()

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Apartment, contentDescription = "Emblem", tint = Color(0xFF1E40AF), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Rent Manager", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (auth.currentUser != null) Color(0xFF10B981) else Color(0xFF94A3B8)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = auth.currentUser?.email ?: auth.currentUser?.phoneNumber ?: "Local Storage Mode",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        Button(
                            onClick = { showAddRoomDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.AddHomeWork, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("+ Add Property", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                        }

                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Settings", tint = Color(0xFF475569))
                        }

                        DropdownMenu(
                            expanded = showSettingsMenu,
                            onDismissRequest = { showSettingsMenu = false },
                            modifier = Modifier.background(Color.White).clip(RoundedCornerShape(14.dp)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sign Out", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFF475569)) },
                                onClick = {
                                    showSettingsMenu = false
                                    vm.signOut { isAuthenticated = false }
                                }
                            )
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            DropdownMenuItem(
                                text = { Text("Clear All Data (Reset)", color = Color(0xFFDC2626), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFDC2626)) },
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
                NavigationBar(containerColor = Color.White, tonalElevation = 3.dp) {
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.PROPERTIES,
                        onClick = { currentTab = NavigationTab.PROPERTIES },
                        icon = { Icon(Icons.Default.Apartment, contentDescription = "Properties") },
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
                        containerColor = Color(0xFF1E40AF),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Property")
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFF8FAFC))
            ) {
                if (currentTab == NavigationTab.PROPERTIES) {
                    PropertiesTabContent(
                        vm = vm,
                        onShowAddRoom = { showAddRoomDialog = true },
                        onAssignTenant = { room: RoomUnit -> roomToAssignTenant = room },
                        onLodgeBill = { room: RoomUnit -> roomToLodgeBill = room },
                        onVacate = { room: RoomUnit -> roomToVacate = room },
                        onViewHistory = { room: RoomUnit -> roomForHistory = room }
                    )
                } else {
                    RevenueView(vm = vm)
                }
            }
        }
                // Dialog Wiring
        if (showResetStep1Dialog) {
            AlertDialog(
                onDismissRequest = { showResetStep1Dialog = false },
                title = { Text("Clear All Account Data?", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                text = { Text("This will delete all rooms, tenants, and ledger records.", fontFamily = FontFamily.SansSerif, fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetStep1Dialog = false
                            showResetStep2Dialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Proceed", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) }
                },
                dismissButton = { TextButton(onClick = { showResetStep1Dialog = false }) { Text("Cancel", fontFamily = FontFamily.SansSerif) } }
            )
        }

        if (showResetStep2Dialog) {
            AlertDialog(
                onDismissRequest = { showResetStep2Dialog = false },
                title = { Text("Final Warning", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), fontFamily = FontFamily.SansSerif) },
                text = { Text("This operation cannot be reversed. Are you certain?", fontFamily = FontFamily.SansSerif, fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetStep2Dialog = false
                            vm.clearAllUserData { isAuthenticated = false }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Erase Everything", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) }
                },
                dismissButton = { TextButton(onClick = { showResetStep2Dialog = false }) { Text("Keep Data", fontFamily = FontFamily.SansSerif) } }
            )
        }

        if (showAddRoomDialog) {
            AddRoomDialog(
                onDismiss = { showAddRoomDialog = false },
                onConfirm = { rNum: String, rRent: Double, rElec: Double ->
                    vm.addRoom(rNum, rRent, rElec)
                    showAddRoomDialog = false
                }
            )
        }

        roomToAssignTenant?.let { room ->
            AssignTenantDialog(
                room = room,
                onDismiss = { roomToAssignTenant = null },
                onConfirm = { name: String, phone: String, aadhaar: String, address: String, deposit: Double, meter: Double, date: String ->
                    vm.assignTenant(room.id, name, phone, aadhaar, address, deposit, meter, date)
                    roomToAssignTenant = null
                }
            )
        }

        roomToLodgeBill?.let { room ->
            val currTenant = tenants.find { it.id == room.currentTenantId }
            val carryover = vm.getPendingDueForRoom(room.id)
            LodgeBillDialog(
                room = room,
                currentTenant = currTenant,
                previousCarryover = carryover,
                context = context,
                onDismiss = { roomToLodgeBill = null },
                onConfirm = { bill: BillRecord ->
                    vm.lodgeBill(bill)
                    roomToLodgeBill = null
                }
            )
        }

        roomToVacate?.let { room ->
            val activeTenant = tenants.find { it.id == room.currentTenantId }
            val pendingDues = vm.getPendingDueForRoom(room.id)
            VacateDialog(
                room = room,
                activeTenant = activeTenant,
                pendingDues = pendingDues,
                context = context,
                onDismiss = { roomToVacate = null },
                onConfirm = { finalReading: Double, dateStr: String ->
                    vm.vacateRoom(room.id, finalReading, dateStr)
                    roomToVacate = null
                }
            )
        }

        roomForHistory?.let { room ->
            val roomTenants = tenants.filter { it.roomId == room.id }
            val roomBills = bills.filter { it.roomId == room.id }
            RoomHistoryDialog(
                room = room,
                roomTenants = roomTenants,
                roomBills = roomBills,
                onDismiss = { roomForHistory = null }
            )
        }
    }
}
