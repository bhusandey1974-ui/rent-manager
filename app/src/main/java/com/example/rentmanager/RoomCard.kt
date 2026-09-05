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
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rentmanager.AppColors
import com.example.rentmanager.Room
import com.example.rentmanager.Tenant
import java.text.SimpleDateFormat
import java.util.Date
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
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetails = true },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        border = BorderStroke(1.dp, AppColors.BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.AzureContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = AppColors.AzurePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Room ${room.roomNumber}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                if (room.isOccupied && tenant != null) {
                    Text(
                        text = "${tenant.name} • ${tenant.phoneNumber}",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                } else {
                    Text(
                        text = "Vacant",
                        fontSize = 12.sp,
                        color = AppColors.AmberWarning
                    )
                }
            }

            if (room.isOccupied) {
                if (pendingDue > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.CrimsonAlert.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%.0f", pendingDue)} Due",
                            color = AppColors.CrimsonAlert,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.EmeraldContainer
                    ) {
                        Text(
                            text = "Settled",
                            color = AppColors.EmeraldSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.AmberContainer
                ) {
                    Text(
                        text = "Vacant",
                        color = AppColors.AmberWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showDetails) {
        RoomDetailsDialog(
            room = room,
            tenant = tenant,
            pendingDue = pendingDue,
            onDismiss = { showDetails = false },
            onAssignTenant = { showDetails = false; onAssignTenant() },
            onLodgeBill = { showDetails = false; onLodgeBill() },
            onEditRoom = { showDetails = false; onEditRoom() },
            onDeleteRoom = { showDetails = false; onDeleteRoom() },
            onVacateRoom = { showDetails = false; onVacateRoom() },
            onViewHistory = { showDetails = false; onViewHistory() }
        )
    }
}

@Composable
fun RoomDetailsDialog(
    room: Room,
    tenant: Tenant?,
    pendingDue: Double,
    onDismiss: () -> Unit,
    onAssignTenant: () -> Unit,
    onLodgeBill: () -> Unit,
    onEditRoom: () -> Unit,
    onDeleteRoom: () -> Unit,
    onVacateRoom: () -> Unit,
    onViewHistory: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Room ${room.roomNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "₹${room.baseRent.toInt()} / mo",
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rate: ₹${room.electricityRate}/unit | Initial Meter: ${room.initialMeterReading}",
                        fontSize = 12.sp,
                        color = AppColors.TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = AppColors.BorderSubtle)
                Spacer(modifier = Modifier.height(16.dp))

                if (room.isOccupied && tenant != null) {
                    Text(
                        text = "Tenant Details",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("Name", tenant.name)
                    DetailRow("Phone", tenant.phoneNumber)
                    DetailRow("Aadhaar", tenant.aadhaarNumber.ifBlank { "Not provided" })
                    DetailRow("Address", tenant.permanentAddress.ifBlank { "Not provided" })
                    DetailRow("Move-In Date", dateFormatter.format(Date(tenant.moveInDate)))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (pendingDue > 0)
                            "Due: ₹${String.format(Locale.ENGLISH, "%.0f", pendingDue)}"
                        else "All Settled",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pendingDue > 0) AppColors.CrimsonAlert else AppColors.EmeraldSuccess
                    )
                } else {
                    Text(
                        text = "This room is currently vacant.",
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = AppColors.BorderSubtle)
                Spacer(modifier = Modifier.height(16.dp))

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
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lodge Bill", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = onAssignTenant,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.AzurePrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Assign Tenant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onViewHistory,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.History, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("History", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditRoom,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }

                    if (room.isOccupied) {
                        OutlinedButton(
                            onClick = onVacateRoom,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AppColors.CrimsonAlert.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Vacate", fontSize = 12.sp, color = AppColors.CrimsonAlert, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDeleteRoom,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AppColors.CrimsonAlert.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.DeleteOutline, contentDescription = null, tint = AppColors.CrimsonAlert, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", fontSize = 12.sp, color = AppColors.CrimsonAlert)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(text = "$label: ", fontSize = 12.sp, color = AppColors.TextMuted, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 12.sp, color = AppColors.TextSecondary)
    }
}
