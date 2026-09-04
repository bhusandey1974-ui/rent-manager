package com.example.rentmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import com.example.rentmanager.Room
import com.example.rentmanager.Tenant
import java.util.Locale

@Composable
fun RoomCard(
    room: Room,
    tenant: Tenant?,
    pendingDue: Double,
    onLodgeBillClick: () -> Unit,
    onAssignTenantClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onCallTenantClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        border = BorderStroke(1.dp, AppColors.BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Room Identity & Occupancy Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Soft Residential Home Badge (No lavender)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (room.isOccupied) AppColors.RoomOccupiedContainer 
                                else AppColors.RoomVacantContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "Room ${room.roomNumber}",
                            tint = if (room.isOccupied) AppColors.RoomOccupiedIcon else AppColors.RoomVacantIcon,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Room ${room.roomNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "Rent: ₹${String.format(Locale.ENGLISH, "%.0f", room.baseRent)}/mo",
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                // Occupancy Tag
                val tagBg = if (room.isOccupied) AppColors.EmeraldContainer else AppColors.SlateBackground
                val tagColor = if (room.isOccupied) AppColors.EmeraldSuccess else AppColors.TextSecondary
                val tagBorder = if (room.isOccupied) AppColors.EmeraldBorder else AppColors.BorderSubtle

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(tagBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (room.isOccupied) "Occupied" else "Vacant",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tagColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tenant Details or Vacant Prompt
            if (room.isOccupied && tenant != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.ScaffoldBackground)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = tenant.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary
                        )
                        if (pendingDue > 0.0) {
                            Text(
                                text = "Due: ₹${String.format(Locale.ENGLISH, "%.2f", pendingDue)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.AmberWarning
                            )
                        } else if (pendingDue < 0.0) {
                            Text(
                                text = "Advance: ₹${String.format(Locale.ENGLISH, "%.2f", -pendingDue)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.AzurePrimary
                            )
                        } else {
                            Text(
                                text = "All dues cleared",
                                fontSize = 13.sp,
                                color = AppColors.EmeraldSuccess
                            )
                        }
                    }

                    // Call Button
                    if (tenant.phone.isNotBlank()) {
                        IconButton(
                            onClick = { onCallTenantClick(tenant.phone) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppColors.SurfaceWhite)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Call,
                                contentDescription = "Call ${tenant.name}",
                                tint = AppColors.AzurePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No tenant currently assigned to this room.",
                    fontSize = 13.sp,
                    color = AppColors.TextMuted,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (room.isOccupied) {
                    Button(
                        onClick = onLodgeBillClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Lodge Bill", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onAssignTenantClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Assign Tenant", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                OutlinedButton(
                    onClick = onHistoryClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AppColors.BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextSecondary)
                ) {
                    Text(text = "History", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
