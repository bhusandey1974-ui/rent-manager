package com.example.rentmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.rentmanager.AppColors
import com.example.rentmanager.RentViewModel
import com.example.rentmanager.Room
import com.example.rentmanager.Tenant
import com.example.rentmanager.ui.components.AddPropertyDialog
import com.example.rentmanager.ui.components.AddRoomDialog
import com.example.rentmanager.ui.components.AssignTenantDialog
import com.example.rentmanager.ui.components.DeleteConfirmationDialog
import com.example.rentmanager.ui.components.EditRoomDialog
import com.example.rentmanager.ui.components.LodgeBillDialog
import com.example.rentmanager.ui.components.RoomCard
import com.example.rentmanager.ui.components.RoomHistoryDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesView(
    vm: RentViewModel,
    onNavigateToRevenue: () -> Unit = {}
) {
    val context = LocalContext.current

    val properties by vm.properties.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()
    val selectedPropId by vm.selectedPropertyId.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    // Dialog state holders
    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var roomForAssigning by remember { mutableStateOf<Room?>(null) }
    var roomForBilling by remember { mutableStateOf<Room?>(null) }
    var roomForEditing by remember { mutableStateOf<Room?>(null) }
    var roomForDeleting by remember { mutableStateOf<Room?>(null) }
    var roomForHistory by remember { mutableStateOf<Room?>(null) }

    // Filter rooms by property, query, and occupancy status
    val currentRooms = rooms.filter {
        selectedPropId == null || it.propertyId == selectedPropId || selectedPropId == "default_property"
    }

    val filteredRooms = currentRooms.filter { room ->
        val tenant = tenants.find { it.id == room.currentTenantId && it.isCurrent }
        val matchesSearch = room.roomNumber.contains(searchQuery, ignoreCase = true) ||
                (tenant?.name?.contains(searchQuery, ignoreCase = true) == true) ||
                (tenant?.phoneNumber?.contains(searchQuery, ignoreCase = true) == true)

        val pendingDue = vm.getPendingDueForCurrentTenant(room.id)

        val matchesFilter = when (selectedFilter) {
            "Occupied" -> room.isOccupied
            "Vacant" -> !room.isOccupied
            "Dues Pending" -> room.isOccupied && pendingDue > 0.0
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        containerColor = AppColors.ScaffoldBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRoomDialog = true },
                containerColor = AppColors.AzurePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Room")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Property Selector Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                properties.forEach { prop ->
                    val isSelected = prop.id == selectedPropId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) AppColors.AzurePrimary else AppColors.SurfaceWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AppColors.AzurePrimary else AppColors.BorderSubtle
                        ),
                        modifier = Modifier.clickable { vm.setSelectedProperty(prop.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Apartment,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else AppColors.TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prop.name,
                                color = if (isSelected) Color.White else AppColors.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Add Property Button
                IconButton(
                    onClick = { showAddPropertyDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppColors.AzureContainer)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Property", tint = AppColors.AzurePrimary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search room number or tenant...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.AzurePrimary,
                    unfocusedBorderColor = AppColors.BorderSubtle,
                    focusedContainerColor = AppColors.SurfaceWhite,
                    unfocusedContainerColor = AppColors.SurfaceWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Occupied", "Vacant", "Dues Pending").forEach { filterTag ->
                    FilterChip(
                        selected = selectedFilter == filterTag,
                        onClick = { selectedFilter = filterTag },
                        label = { Text(filterTag, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.AzureContainer,
                            selectedLabelColor = AppColors.AzurePrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = AppColors.BorderSubtle,
                            selectedBorderColor = AppColors.AzurePrimary,
                            enabled = true,
                            selected = selectedFilter == filterTag
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
                        // Room Cards List
            if (filteredRooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No rooms match your search." else "No rooms added yet. Tap + to create one.",
                        color = AppColors.TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRooms, key = { it.id }) { room ->
                        val tenant = tenants.find { it.id == room.currentTenantId && it.isCurrent }
                        val pendingDue = vm.getPendingDueForCurrentTenant(room.id)

                        RoomCard(
                            room = room,
                            tenant = tenant,
                            pendingDue = pendingDue,
                            onCardClick = { roomForHistory = room },
                            onAssignTenant = { roomForAssigning = room },
                            onLodgeBill = { roomForBilling = room },
                            onEditRoom = { roomForEditing = room },
                            onDeleteRoom = { roomForDeleting = room },
                            onVacateRoom = { vm.vacateRoom(room.id) },
                            onViewHistory = { roomForHistory = room }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
        // ==========================================
    // MODAL DIALOGS HOOKUP
    // ==========================================

    if (showAddPropertyDialog) {
        AddPropertyDialog(
            onDismiss = { showAddPropertyDialog = false },
            onConfirm = { name, address ->
                vm.addProperty(name, address)
                showAddPropertyDialog = false
            }
        )
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            onDismiss = { showAddRoomDialog = false },
            onConfirm = { roomNum, rent, rate, startReading ->
                vm.addRoom(roomNum, rent, rate, startReading)
                showAddRoomDialog = false
            }
        )
    }

    roomForAssigning?.let { room ->
        AssignTenantDialog(
            roomNumber = room.roomNumber,
            onDismiss = { roomForAssigning = null },
            onConfirm = { name, phone, deposit, aadhaar, address, moveInMillis ->
                vm.assignTenant(
                    roomId = room.id,
                    tenantName = name,
                    tenantPhone = phone,
                    deposit = deposit,
                    aadhaarNumber = aadhaar,
                    permanentAddress = address,
                    moveInDateMillis = moveInMillis
                )
                roomForAssigning = null
            }
        )
    }

    roomForBilling?.let { room ->
        val tenant = tenants.find { it.id == room.currentTenantId && it.isCurrent }
        if (tenant != null) {
            val prevReading = vm.getLastRecordedMeterReading(room.id)
            val priorDue = vm.getPendingDueForCurrentTenant(room.id)

            LodgeBillDialog(
                context = context,
                room = room,
                tenant = tenant,
                previousReading = prevReading,
                priorDueOrAdvance = priorDue,
                onDismiss = { roomForBilling = null },
                onBillLodged = { period, currReading, maint, amtPaid, mode ->
                    vm.lodgeBill(
                        roomId = room.id,
                        billingPeriod = period,
                        currentReading = currReading,
                        maintenanceAmount = maint,
                        amountPaid = amtPaid,
                        paymentMode = mode
                    )
                    roomForBilling = null
                }
            )
        }
    }

    roomForEditing?.let { room ->
        EditRoomDialog(
            room = room,
            onDismiss = { roomForEditing = null },
            onConfirm = { num, rent, rate, initialMeter ->
                vm.updateRoom(room.id, num, rent, rate, initialMeter)
                roomForEditing = null
            }
        )
    }

    roomForDeleting?.let { room ->
        DeleteConfirmationDialog(
            title = "Delete Room ${room.roomNumber}?",
            message = "This will permanently remove this room and its active billing links. Past billing records are preserved.",
            onDismiss = { roomForDeleting = null },
            onConfirm = {
                vm.deleteRoom(room.id)
                roomForDeleting = null
            }
        )
    }

    roomForHistory?.let { room ->
        val tenancyRecords = vm.getRoomTenancyHistory(room.id)
        RoomHistoryDialog(
            room = room,
            historySummaries = tenancyRecords,
            onDismiss = { roomForHistory = null }
        )
    }
}

