package com.example.rentmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.rentmanager.ui.components.BannerAdView
import com.example.rentmanager.ui.components.DeleteConfirmationDialog
import com.example.rentmanager.ui.components.EditRoomDialog
import com.example.rentmanager.ui.components.EditTenantDialog
import com.example.rentmanager.ui.components.LodgeBillDialog
import com.example.rentmanager.ui.components.MoveInDateBackfillDialog
import com.example.rentmanager.ui.components.RoomCard
import com.example.rentmanager.ui.components.RoomHistoryDialog
import java.util.Locale

private data class PendingAssignment(
    val room: Room,
    val name: String,
    val phone: String,
    val deposit: Double,
    val aadhaar: String,
    val address: String,
    val moveInMillis: Long
)

private data class PendingEdit(
    val room: Room,
    val tenant: Tenant,
    val name: String,
    val phone: String,
    val deposit: Double,
    val aadhaar: String,
    val address: String,
    val moveInMillis: Long
)

@Composable
private fun PropertyStatsCard(
    totalRooms: Int,
    occupied: Int,
    vacant: Int,
    monthlyRent: Double
) {
    val currentDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(currentDensity.density, fontScale = 1f)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppColors.SurfaceWhite,
            border = BorderStroke(0.5.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(IntrinsicSize.Min)
            ) {
                PropertyStatItem(Icons.Rounded.Home, Color(0xFF1E8E5A), "$totalRooms", "Total Rooms", Modifier.weight(22f))
                PropertyStatDivider()
                PropertyStatItem(Icons.Rounded.Group, Color(0xFF2A6FC9), "$occupied", "Occupied", Modifier.weight(18f))
                PropertyStatDivider()
                PropertyStatItem(Icons.Rounded.DoorFront, Color(0xFFE58A12), "$vacant", "Vacant", Modifier.weight(16f))
                PropertyStatDivider()
                PropertyStatItem(
                    Icons.Rounded.CurrencyRupee,
                    Color(0xFF1E8E5A),
                    String.format(Locale.ENGLISH, "%,.0f", monthlyRent),
                    "Monthly Rent",
                    Modifier.weight(24f)
                )
            }
        }
    }
}

@Composable
private fun PropertyStatItem(
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(start = 10.dp, end = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
            }
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = AppColors.TextSecondary,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun PropertyStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(AppColors.BorderSubtle.copy(alpha = 0.5f))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesView(
    vm: RentViewModel,
    onNavigateToRevenue: () -> Unit = {},
    addPropertyRequested: Boolean = false,
    onAddPropertyHandled: () -> Unit = {}
) {
    val context = LocalContext.current

    val properties by vm.properties.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()
    val selectedPropId by vm.selectedPropertyId.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    // Dialog state holders
    var showAddPropertyLocal by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var roomForAssigning by remember { mutableStateOf<Room?>(null) }
    var roomForBilling by remember { mutableStateOf<Room?>(null) }
    var roomForEditing by remember { mutableStateOf<Room?>(null) }
    var roomForDeleting by remember { mutableStateOf<Room?>(null) }
    var propertyToDelete by remember { mutableStateOf<Property?>(null) }
    var propertyDeleteStep by remember { mutableStateOf(1) }
    var roomForHistory by remember { mutableStateOf<Room?>(null) }
    var tenantForEditing by remember { mutableStateOf<Pair<Room, Tenant>?>(null) }

    // Holds a not-yet-saved tenant assignment while we ask about historical unpaid rent
    var pendingBackdatedAssignment by remember { mutableStateOf<PendingAssignment?>(null) }
    // Holds a not-yet-saved move-in-date edit while we ask about historical unpaid rent
    var pendingBackdatedEdit by remember { mutableStateOf<PendingEdit?>(null) }

    // Filter rooms by property, query, and occupancy status
    val currentRooms = rooms.filter {
        selectedPropId == null || it.propertyId == selectedPropId
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

    val occupiedCount = currentRooms.count { it.isOccupied }
    val vacantCount = currentRooms.count { !it.isOccupied }
    val duesCount = currentRooms.count { it.isOccupied && vm.getPendingDueForCurrentTenant(it.id) > 0.0 }
    val totalMonthlyRent = currentRooms.filter { it.isOccupied }.sumOf { it.baseRent }
    val chipCounts = mapOf(
        "All" to currentRooms.size,
        "Occupied" to occupiedCount,
        "Vacant" to vacantCount,
        "Dues Pending" to duesCount
    )

    Scaffold(
        containerColor = AppColors.ScaffoldBackground,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.offset(y = 8.dp),   // sits just above the banner ad
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
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Property Selector Strip: only shown when there is more than one property
            if (properties.size > 1) {
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
                        modifier = Modifier.combinedClickable(
                            onClick = { vm.setSelectedProperty(prop.id) },
                            onLongClick = {
                                if (properties.size > 1) {
                                    propertyToDelete = prop
                                    propertyDeleteStep = 1
                                }
                            }
                        )
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
                    onClick = { showAddPropertyLocal = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppColors.AzureContainer)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Property", tint = AppColors.AzurePrimary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            }

            PropertyStatsCard(
                totalRooms = currentRooms.size,
                occupied = occupiedCount,
                vacant = vacantCount,
                monthlyRent = totalMonthlyRent
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AppColors.SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search room number or tenant...",
                                fontSize = 13.sp,
                                color = AppColors.TextMuted,
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.merge(
                                TextStyle(fontSize = 13.sp, color = AppColors.TextPrimary)
                            ),
                            cursorBrush = SolidColor(AppColors.AzurePrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(AppColors.BorderSubtle)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(Icons.Rounded.Tune, contentDescription = null, tint = AppColors.TextPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Occupied", "Vacant", "Dues Pending").forEach { filterTag ->
                    val isSel = selectedFilter == filterTag
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSel) AppColors.AzureDark else AppColors.SurfaceWhite)
                            .border(
                                1.dp,
                                if (isSel) AppColors.AzureDark else AppColors.BorderSubtle,
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedFilter = filterTag }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$filterTag (${chipCounts[filterTag] ?: 0})",
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSel) Color.White else AppColors.TextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
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
                    if (searchQuery.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = AppColors.TextMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No rooms match your search.",
                                color = AppColors.TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.AzureContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Apartment,
                                    contentDescription = null,
                                    tint = AppColors.AzurePrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "No rooms yet",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Add your first room to start tracking tenants, rent, and bills.",
                                fontSize = 13.sp,
                                color = AppColors.TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { showAddRoomDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.AzurePrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Your First Room", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            onConfirmVacate = { note, depositRefunded -> vm.confirmVacateRoom(room.id, pendingDue, note, depositRefunded) },
                            onViewHistory = { roomForHistory = room },
                            onEditTenant = { tenant?.let { tenantForEditing = room to it } }
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

    if (addPropertyRequested || showAddPropertyLocal) {
        AddPropertyDialog(
            onDismiss = { showAddPropertyLocal = false; onAddPropertyHandled() },
            onConfirm = { name, address ->
                vm.addProperty(name, address)
                showAddPropertyLocal = false
                onAddPropertyHandled()
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
                if (vm.isBackdatedMoveIn(moveInMillis)) {
                    // Move-in is more than a month back — check on historical unpaid rent first
                    pendingBackdatedAssignment = PendingAssignment(room, name, phone, deposit, aadhaar, address, moveInMillis)
                    roomForAssigning = null
                } else {
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
            }
        )
    }

    pendingBackdatedAssignment?.let { pending ->
        MoveInDateBackfillDialog(
            tenantName = pending.name,
            moveInDateMillis = pending.moveInMillis,
            onDismiss = { pendingBackdatedAssignment = null },
            onConfirm = { allPaid, paidThroughMonthMillis ->
                val newTenant = vm.assignTenant(
                    roomId = pending.room.id,
                    tenantName = pending.name,
                    tenantPhone = pending.phone,
                    deposit = pending.deposit,
                    aadhaarNumber = pending.aadhaar,
                    permanentAddress = pending.address,
                    moveInDateMillis = pending.moveInMillis
                )
                if (!allPaid && paidThroughMonthMillis != null) {
                    vm.backfillUnpaidRent(pending.room.id, newTenant.id, paidThroughMonthMillis)
                }
                pendingBackdatedAssignment = null
            }
        )
    }

    roomForBilling?.let { room ->
        val tenant = tenants.find { it.id == room.currentTenantId && it.isCurrent }
        if (tenant != null) {
            val prevReading = vm.getLastRecordedMeterReading(room.id)
            val priorDue = vm.getPendingDueForCurrentTenant(room.id)
            val suggestedPeriod = vm.getSuggestedBillingPeriod(room.id)
            val outstanding = vm.getOutstandingUnpaidMonths(room.id, tenant.id)
            val existingPeriods = vm.getExistingBillingPeriods(room.id, tenant.id)

            LodgeBillDialog(
                context = context,
                vm = vm,
                room = room,
                tenant = tenant,
                previousReading = prevReading,
                priorDueOrAdvance = priorDue,
                suggestedBillingPeriod = suggestedPeriod,
                outstandingMonths = outstanding,
                existingBillingPeriods = existingPeriods,
                onDismiss = { roomForBilling = null },
                onBillLodged = { period, currReading, maint, amtPaid, mode ->
                    val bill = vm.lodgeBill(
                        roomId = room.id,
                        billingPeriod = period,
                        currentReading = currReading,
                        maintenanceAmount = maint,
                        amountPaid = amtPaid,
                        paymentMode = mode
                    )
                    roomForBilling = null
                    bill
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

    propertyToDelete?.let { prop ->
        DeleteConfirmationDialog(
            title = if (propertyDeleteStep == 1) "Remove \"${prop.name}\"?" else "Are you absolutely sure?",
            message = if (propertyDeleteStep == 1)
                "This removes the floor, its rooms, their tenants and all billing records."
            else
                "Last check: \"${prop.name}\" and everything in it will be permanently deleted. This cannot be undone.",
            onDismiss = {
                propertyToDelete = null
                propertyDeleteStep = 1
            },
            onConfirm = {
                if (propertyDeleteStep == 1) {
                    propertyDeleteStep = 2
                } else {
                    vm.deleteProperty(prop.id)
                    propertyToDelete = null
                    propertyDeleteStep = 1
                }
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
            onDismiss = { roomForHistory = null },
            onEditActiveTenant = { tenant ->
                tenantForEditing = room to tenant
                roomForHistory = null
            }
        )
    }

    tenantForEditing?.let { (room, tenant) ->
        EditTenantDialog(
            tenant = tenant,
            onDismiss = { tenantForEditing = null },
            onConfirm = { name, phone, deposit, aadhaar, address, moveInMillis ->
                val moveInChanged = moveInMillis != tenant.moveInDate
                if (moveInChanged && vm.isBackdatedMoveIn(moveInMillis)) {
                    pendingBackdatedEdit = PendingEdit(room, tenant, name, phone, deposit, aadhaar, address, moveInMillis)
                    tenantForEditing = null
                } else {
                    vm.updateTenant(
                        tenantId = tenant.id,
                        name = name,
                        phone = phone,
                        deposit = deposit,
                        aadhaarNumber = aadhaar,
                        permanentAddress = address,
                        moveInDateMillis = moveInMillis
                    )
                    tenantForEditing = null
                }
            }
        )
    }

    pendingBackdatedEdit?.let { pending ->
        MoveInDateBackfillDialog(
            tenantName = pending.name,
            moveInDateMillis = pending.moveInMillis,
            onDismiss = { pendingBackdatedEdit = null },
            onConfirm = { allPaid, paidThroughMonthMillis ->
                vm.updateTenant(
                    tenantId = pending.tenant.id,
                    name = pending.name,
                    phone = pending.phone,
                    deposit = pending.deposit,
                    aadhaarNumber = pending.aadhaar,
                    permanentAddress = pending.address,
                    moveInDateMillis = pending.moveInMillis
                )
                if (!allPaid && paidThroughMonthMillis != null) {
                    vm.backfillUnpaidRent(pending.room.id, pending.tenant.id, paidThroughMonthMillis)
                }
                pendingBackdatedEdit = null
            }
        )
    }
}


