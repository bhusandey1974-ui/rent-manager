package com.example.rentmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AppColors.SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PropertyStatItem(Icons.Rounded.Home, AppColors.EmeraldSuccess, "$totalRooms", "Total Rooms", Modifier.weight(1.15f))
            PropertyStatDivider()
            PropertyStatItem(Icons.Rounded.Group, Color(0xFF1E6FD9), "$occupied", "Occupied", Modifier.weight(1f))
            PropertyStatDivider()
            PropertyStatItem(Icons.Rounded.DoorFront, AppColors.AmberWarning, "$vacant", "Vacant", Modifier.weight(0.85f))
            PropertyStatDivider()
            PropertyStatItem(
                Icons.Rounded.CurrencyRupee,
                AppColors.EmeraldSuccess,
                "₹ ${String.format(Locale.ENGLISH, "%,.0f", monthlyRent)}",
                "Total Monthly Rent",
                Modifier.weight(1.5f)
            )
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
    Column(modifier = modifier.padding(horizontal = 6.dp)) {
        Box(
            modifier = Modifier
                .size(25.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            color = AppColors.TextPrimary,
            maxLines = 1
        )
        Text(
            text = label,
            fontSize = 10.sp,
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
            .height(48.dp)
            .background(AppColors.BorderSubtle.copy(alpha = 0.7f))
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
            Spacer(modifier = Modifier.height(12.dp))

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

                            
