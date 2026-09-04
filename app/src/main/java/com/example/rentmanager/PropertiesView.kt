package com.example.rentmanager

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddHomeWork
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.ui.components.AddRoomDialog
import com.example.rentmanager.ui.components.AssignTenantDialog
import com.example.rentmanager.ui.components.DeleteConfirmationDialog
import com.example.rentmanager.ui.components.LodgeBillDialog

enum class PropertyFilter { ALL, OCCUPIED, VACANT, HAS_DUES }

@Composable
fun PropertiesTabContent(
    vm: RentViewModel,
    onShowAddRoom: () -> Unit,
    onAssignTenant: (Room) -> Unit,
    onLodgeBill: (Room) -> Unit,
    onVacate: (Room) -> Unit,
    onViewHistory: (Room) -> Unit
) {
    val rooms by vm.rooms.collectAsState()
    val tenants by vm.tenants.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(PropertyFilter.ALL) }

    val filteredRooms = remember(rooms, activeFilter, searchQuery, tenants) {
        rooms.filter { room ->
            val matchesQuery = room.roomNumber.contains(searchQuery, ignoreCase = true) ||
                tenants.find { it.id == room.currentTenantId }?.name?.contains(searchQuery, ignoreCase = true) == true

            val matchesFilter = when (activeFilter) {
                PropertyFilter.ALL -> true
                PropertyFilter.OCCUPIED -> room.isOccupied
                PropertyFilter.VACANT -> !room.isOccupied
                PropertyFilter.HAS_DUES -> vm.getPendingDueForCurrentTenant(room.id) > 0.0
            }

            matchesQuery && matchesFilter
        }
    }
        if (rooms.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = Color(0xFF1E40AF),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "No Rooms Added Yet",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add your property rooms to begin tracking tenants, meter readings, and collections.",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onShowAddRoom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                    ) {
                        Icon(Icons.Default.AddHomeWork, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Your First Room",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search room number or tenant...",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E40AF),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = activeFilter == PropertyFilter.ALL,
                                onClick = { activeFilter = PropertyFilter.ALL },
                                label = { Text("All (${rooms.size})", fontSize = 12.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E40AF),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            val occupiedCount = rooms.count { it.isOccupied }
                            FilterChip(
                                selected = activeFilter == PropertyFilter.OCCUPIED,
                                onClick = { activeFilter = PropertyFilter.OCCUPIED },
                                label = { Text("Occupied ($occupiedCount)", fontSize = 12.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E40AF),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            val vacantCount = rooms.count { !it.isOccupied }
                            FilterChip(
                                selected = activeFilter == PropertyFilter.VACANT,
                                onClick = { activeFilter = PropertyFilter.VACANT },
                                label = { Text("Vacant ($vacantCount)", fontSize = 12.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E40AF),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            val dueCount = rooms.count { vm.getPendingDueForCurrentTenant(it.id) > 0.0 }
                            FilterChip(
                                selected = activeFilter == PropertyFilter.HAS_DUES,
                                onClick = { activeFilter = PropertyFilter.HAS_DUES },
                                label = { Text("Dues Pending ($dueCount)", fontSize = 12.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFDC2626),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
            ) {
                items(filteredRooms, key = { it.id }) { room ->
                    val tenant = tenants.find { it.id == room.currentTenantId }
                    val pendingBalance = vm.getPendingDueForCurrentTenant(room.id)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (room.isOccupied) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.MeetingRoom,
                                                contentDescription = null,
                                                tint = if (room.isOccupied) Color(0xFF1E40AF) else Color(0xFF64748B),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Room ${room.roomNumber}",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "₹${room.baseRent.toInt()}/mo • ₹${room.electricityRate.toInt()}/u",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                }

                                if (room.isOccupied) {
                                    if (pendingBalance > 0.0) {
                                        Surface(color = Color(0xFFFEF2F2), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color(0xFFFECACA))) {
                                            Text(
                                                text = "Due: ₹${pendingBalance.toInt()}",
                                                color = Color(0xFFDC2626),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else if (pendingBalance < 0.0) {
                                        Surface(color = Color(0xFFECFDF5), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color(0xFFA7F3D0))) {
                                            Text(
                                                text = "Advance: ₹${(-pendingBalance).toInt()}",
                                                color = Color(0xFF059669),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = "Settled ✓",
                                                color = Color(0xFF475569),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                        Text(
                                            text = "Vacant",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                                                        Spacer(modifier = Modifier.height(14.dp))

                            if (room.isOccupied && tenant != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E40AF), modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = tenant.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontFamily = FontFamily.SansSerif,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Ready for occupancy",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = FontFamily.SansSerif,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (room.isOccupied) {
                                    Button(
                                        onClick = { onLodgeBill(room) },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                                    ) {
                                        Text("Lodge Bill", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    OutlinedButton(
                                        onClick = { onVacate(room) },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
                                    ) {
                                        Text("Vacate", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                } else {
                                    Button(
                                        onClick = { onAssignTenant(room) },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                                    ) {
                                        Text("Assign Tenant", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                IconButton(
                                    onClick = { onViewHistory(room) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = "View History",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
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
fun PropertiesView(
    vm: RentViewModel,
    onNavigateToRevenue: () -> Unit = {}
) {
    val context = LocalContext.current
    val tenants by vm.tenants.collectAsState()

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var roomForAssigning by remember { mutableStateOf<Room?>(null) }
    var roomForBilling by remember { mutableStateOf<Room?>(null) }
    var roomToVacate by remember { mutableStateOf<Room?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        PropertiesTabContent(
            vm = vm,
            onShowAddRoom = { showAddRoomDialog = true },
            onAssignTenant = { room: Room -> roomForAssigning = room },
            onLodgeBill = { room: Room -> roomForBilling = room },
            onVacate = { room: Room -> roomToVacate = room },
            onViewHistory = { room: Room -> onNavigateToRevenue() }
        )

        if (showAddRoomDialog) {
            AddRoomDialog(
                onDismiss = { showAddRoomDialog = false },
                onConfirm = { roomNumber, baseRent, rate, initialReading ->
                    vm.addRoom(roomNumber, baseRent, rate, initialReading)
                    showAddRoomDialog = false
                }
            )
        }

        roomForAssigning?.let { room ->
            AssignTenantDialog(
                roomNumber = room.roomNumber,
                onDismiss = { roomForAssigning = null },
                onConfirm = { name, phone, deposit ->
                    vm.assignTenant(room.id, name, phone, deposit)
                    roomForAssigning = null
                }
            )
        }

        roomForBilling?.let { room ->
            val tenant = tenants.find { it.id == room.currentTenantId }
            if (tenant != null) {
                val prevReading = vm.getLastRecordedMeterReading(room.id)
                val priorBalance = vm.getPendingDueForCurrentTenant(room.id)

                LodgeBillDialog(
                    context = context,
                    room = room,
                    tenant = tenant,
                    previousReading = prevReading,
                    priorDueOrAdvance = priorBalance,
                    onDismiss = { roomForBilling = null },
                    onBillLodged = { period, currReading, maint, paid, mode ->
                        vm.lodgeBill(room.id, period, currReading, maint, paid, mode)
                        roomForBilling = null
                    }
                )
            }
        }

        roomToVacate?.let { room ->
            DeleteConfirmationDialog(
                title = "Vacate Room ${room.roomNumber}",
                message = "Are you sure you want to mark this room as vacant? The active tenancy will end.",
                onDismiss = { roomToVacate = null },
                onConfirm = {
                    vm.vacateRoom(room.id)
                    roomToVacate = null
                }
            )
        }
    }
}
