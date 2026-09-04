package com.example.rentmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.ui.components.AddPropertyDialog
import com.example.rentmanager.ui.components.AddRoomDialog
import com.example.rentmanager.ui.components.AssignTenantDialog
import com.example.rentmanager.ui.components.DeleteConfirmationDialog
import com.example.rentmanager.ui.components.EditRoomDialog
import com.example.rentmanager.ui.components.EditTenantDialog
import com.example.rentmanager.ui.components.LodgeBillDialog
import com.example.rentmanager.ui.components.RoomCard
import com.example.rentmanager.ui.screens.RevenueView

class MainActivity : ComponentActivity() {
    private val viewModel: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp(viewModel)
        }
    }
}

@Composable
fun MainApp(viewModel: RentViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("Properties") } // "Properties" or "Revenue"

    // Dialog State Trackers
    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }

    var roomForBilling by remember { mutableStateOf<Room?>(null) }
    var roomForAssigning by remember { mutableStateOf<Room?>(null) }
    var roomToEdit by remember { mutableStateOf<Room?>(null) }
    var roomToDelete by remember { mutableStateOf<Room?>(null) }
    var tenantToEdit by remember { mutableStateOf<Tenant?>(null) }

    val properties by viewModel.properties.collectAsState()
    val selectedPropId by viewModel.selectedPropertyId.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val tenants by viewModel.tenants.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterTab by remember { mutableStateOf("All") } // "All", "Occupied", "Vacant", "Dues Pending"

    val displayedRooms = remember(rooms, selectedPropId, searchQuery, filterTab, tenants) {
        rooms.filter { r ->
            (selectedPropId == null || r.propertyId == selectedPropId) &&
            (searchQuery.isBlank() || r.roomNumber.contains(searchQuery, ignoreCase = true) ||
             tenants.find { it.id == r.currentTenantId }?.name?.contains(searchQuery, ignoreCase = true) == true)
        }.filter { r ->
            when (filterTab) {
                "Occupied" -> r.isOccupied
                "Vacant" -> !r.isOccupied
                "Dues Pending" -> r.isOccupied && viewModel.getPendingDueForCurrentTenant(r.id) > 0.0
                else -> true
            }
        }
    }

    Scaffold(
        containerColor = AppColors.ScaffoldBackground,
        floatingActionButton = {
            if (currentTab == "Properties") {
                FloatingActionButton(
                    onClick = { showAddRoomDialog = true },
                    containerColor = AppColors.AzurePrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Room", modifier = Modifier.size(28.dp))
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.SurfaceWhite,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == "Properties",
                    onClick = { currentTab = "Properties" },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Properties") },
                    label = { Text("Properties", fontWeight = if (currentTab == "Properties") FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.AzurePrimary,
                        selectedTextColor = AppColors.AzurePrimary,
                        indicatorColor = AppColors.AzureContainer,
                        unselectedIconColor = AppColors.TextSecondary,
                        unselectedTextColor = AppColors.TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "Revenue",
                    onClick = { currentTab = "Revenue" },
                    icon = { Icon(Icons.Rounded.Assessment, contentDescription = "Revenue") },
                    label = { Text("Revenue", fontWeight = if (currentTab == "Revenue") FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.AzurePrimary,
                        selectedTextColor = AppColors.AzurePrimary,
                        indicatorColor = AppColors.AzureContainer,
                        unselectedIconColor = AppColors.TextSecondary,
                        unselectedTextColor = AppColors.TextSecondary
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (currentTab == "Properties") {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Bar Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.SurfaceWhite)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.AzureContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Apartment,
                                    contentDescription = "Logo",
                                    tint = AppColors.AzurePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Rent Manager",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = "Local Storage Mode",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }

                        // Add Property Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppColors.AzurePrimary)
                                .clickable { showAddPropertyDialog = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Domain, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Add Property", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                                        // Search Bar
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search room number or tenant...", color = AppColors.TextMuted, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = AppColors.TextMuted) },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.AzurePrimary,
                                unfocusedBorderColor = AppColors.BorderSubtle,
                                unfocusedContainerColor = AppColors.SurfaceWhite,
                                focusedContainerColor = AppColors.SurfaceWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Filter Chips Row
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val countAll = rooms.size
                        val countOcc = rooms.count { it.isOccupied }
                        val countVac = rooms.count { !it.isOccupied }
                        val countDues = rooms.count { it.isOccupied && viewModel.getPendingDueForCurrentTenant(it.id) > 0.0 }

                        val filters = listOf(
                            "All ($countAll)",
                            "Occupied ($countOcc)",
                            "Vacant ($countVac)",
                            "Dues Pending ($countDues)"
                        )

                        items(filters) { label ->
                            val key = label.substringBefore(" (")
                            val isSelected = filterTab == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) AppColors.AzurePrimary else AppColors.SurfaceWhite)
                                    .border(1.dp, if (isSelected) Color.Transparent else AppColors.BorderSubtle, RoundedCornerShape(20.dp))
                                    .clickable { filterTab = key }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else AppColors.TextSecondary
                                )
                            }
                        }
                    }

                    // Rooms List
                    if (displayedRooms.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No rooms match this criteria.", color = AppColors.TextMuted, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                            items(displayedRooms, key = { it.id }) { room ->
                                val tenant = tenants.find { it.id == room.currentTenantId }
                                val pendingDue = viewModel.getPendingDueForCurrentTenant(room.id)

                                RoomCard(
                                    room = room,
                                    tenant = tenant,
                                    pendingDue = pendingDue,
                                    onLodgeBillClick = { roomForBilling = room },
                                    onAssignTenantClick = { roomForAssigning = room },
                                    onHistoryClick = { currentTab = "Revenue" },
                                    onCallTenantClick = { phone ->
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(dialIntent)
                                    },
                                    onEditRoomClick = { roomToEdit = room },
                                    onDeleteRoomClick = { roomToDelete = room },
                                    onEditTenantClick = { tenantToEdit = tenant }
                                )
                            }
                        }
                    }
                }
            } else {
                RevenueView(viewModel = viewModel, context = context)
            }

            // ==========================================
            // DIALOG RENDERING
            // ==========================================

            // Add Property Modal
            if (showAddPropertyDialog) {
                AddPropertyDialog(
                    onDismiss = { showAddPropertyDialog = false },
                    onConfirm = { name, address ->
                        viewModel.addProperty(name, address)
                        showAddPropertyDialog = false
                    }
                )
            }

            // Add Room Modal
            if (showAddRoomDialog) {
                AddRoomDialog(
                    onDismiss = { showAddRoomDialog = false },
                    onConfirm = { roomNumber, baseRent, rate, initialReading ->
                        viewModel.addRoom(roomNumber, baseRent, rate, initialReading)
                        showAddRoomDialog = false
                    }
                )
            }

            // Assign Tenant Modal
            roomForAssigning?.let { room ->
                AssignTenantDialog(
                    roomNumber = room.roomNumber,
                    onDismiss = { roomForAssigning = null },
                    onConfirm = { name, phone, deposit ->
                        viewModel.assignTenant(room.id, name, phone, deposit)
                        roomForAssigning = null
                    }
                )
            }

            // Lodge Bill Modal (with WhatsApp direct share)
            roomForBilling?.let { room ->
                val tenant = tenants.find { it.id == room.currentTenantId }
                if (tenant != null) {
                    val prevReading = viewModel.getLastRecordedMeterReading(room.id)
                    val priorBalance = viewModel.getPendingDueForCurrentTenant(room.id)

                    LodgeBillDialog(
                        context = context,
                        room = room,
                        tenant = tenant,
                        previousReading = prevReading,
                        priorDueOrAdvance = priorBalance,
                        onDismiss = { roomForBilling = null },
                        onBillLodged = { period, currReading, maint, paid, mode ->
                            viewModel.lodgeBill(room.id, period, currReading, maint, paid, mode)
                            roomForBilling = null
                        }
                    )
                }
            }

            // Edit Room Modal
            roomToEdit?.let { room ->
                EditRoomDialog(
                    room = room,
                    onDismiss = { roomToEdit = null },
                    onConfirm = { roomNumber, baseRent, rate, initialReading ->
                        viewModel.updateRoom(room.id, roomNumber, baseRent, rate, initialReading)
                        roomToEdit = null
                    }
                )
            }

            // Delete Room Modal
            roomToDelete?.let { room ->
                DeleteConfirmationDialog(
                    title = "Delete Room ${room.roomNumber}",
                    message = "Are you sure you want to delete this room? This action cannot be undone.",
                    onDismiss = { roomToDelete = null },
                    onConfirm = {
                        viewModel.deleteRoom(room.id)
                        roomToDelete = null
                    }
                )
            }

            // Edit Tenant Modal
            tenantToEdit?.let { tenant ->
                EditTenantDialog(
                    tenant = tenant,
                    onDismiss = { tenantToEdit = null },
                    onConfirm = { name, phone, deposit ->
                        viewModel.updateTenant(tenant.id, name, phone, deposit)
                        tenantToEdit = null
                    }
                )
            }
        }
    }
}
