package com.example.rentmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
    onCardClick: () -> Unit,
    onAssignTenant: () -> Unit,
    onLodgeBill: () -> Unit,
    onEditRoom: () -> Unit,
    onDeleteRoom: () -> Unit,
    onVacateRoom: () -> Unit,
    onViewHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        border = BorderStroke(1.dp, AppColors.BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Room Number & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.AzureContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = room.roomNumber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.AzurePrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Room ${room.roomNumber}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "₹${room.baseRent.toInt()} / mo",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (room.isOccupied) AppColors.EmeraldContainer else AppColors.AmberContainer
                    ) {
                        Text(
                            text = if (room.isOccupied) "Occupied" else "Vacant",
                            color = if (room.isOccupied) AppColors.EmeraldDark else AppColors.AmberDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onViewHistory, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "Room History",
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onEditRoom, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Room",
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDeleteRoom, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete Room",
                            tint = AppColors.CrimsonAlert,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tenant Info or Unoccupied State
            if (room.isOccupied && tenant != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.ScaffoldBackground),
                    border = BorderStroke(1.dp, AppColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.AzurePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = AppColors.AzurePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tenant.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = tenant.phoneNumber,
                                    fontSize = 11.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }

                        if (pendingDue > 0) {
                            Text(
                                text = "Due: ₹${String.format(Locale.ENGLISH, "%.0f", pendingDue)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.CrimsonAlert
                            )
                        } else {
                            Text(
                                text = "All Settled",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.EmeraldSuccess
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rate: ₹${room.electricityRate}/unit | Initial Meter: ${room.initialMeterReading}",
                        fontSize = 12.sp,
                        color = AppColors.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = AppColors.BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (room.isOccupied) {
                    Button(
                        onClick = onLodgeBill,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1.3f).height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lodge Bill", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onVacateRoom,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.CrimsonAlert.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text("Vacate", fontSize = 12.sp, color = AppColors.CrimsonAlert, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onAssignTenant,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Assign Tenant", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
