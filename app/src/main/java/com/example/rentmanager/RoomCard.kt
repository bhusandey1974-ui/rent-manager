package com.example.rentmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rentmanager.AppColors
import com.example.rentmanager.R
import com.example.rentmanager.ReceiptFormatter
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
    onConfirmVacate: (note: String, depositRefunded: Boolean) -> Unit,
    onViewHistory: () -> Unit,
    onEditTenant: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }
    var showVacateConfirm by remember { mutableStateOf(false) }

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
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(112.dp)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(
                        id = if (room.isOccupied) R.drawable.room_settled else R.drawable.room_empty
                    ),
                    contentDescription = if (room.isOccupied) "Occupied room" else "Vacant room",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "#${room.roomNumber}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 12.dp, end = 10.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Room ${room.roomNumber}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusColor = if (room.isOccupied) AppColors.EmeraldSuccess else AppColors.AmberWarning
                        val statusContainer = if (room.isOccupied) AppColors.EmeraldContainer else AppColors.AmberContainer
                        val statusLabel = if (room.isOccupied) "Occupied" else "Vacant"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = statusContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = statusLabel,
                                    color = statusColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (room.isOccupied && tenant != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tenant.name,
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tenant.phoneNumber,
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "No tenant assigned",
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (room.isOccupied) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.SlateBackground)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (pendingDue > 0) "Rent Due" else "Rent Status",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                                Text(
                                    text = if (pendingDue > 0)
                                        "₹${String.format(Locale.ENGLISH, "%.0f", pendingDue)} pending"
                                    else "All bills settled",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                            }
                            if (pendingDue > 0) {
                                if (tenant != null && tenant.phoneNumber.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            val reminderMsg = ReceiptFormatter.formatRentReminder(
                                                tenantName = tenant.name,
                                                roomNumber = room.roomNumber,
                                                pendingDue = pendingDue
                                            )
                                            ReceiptFormatter.sendViaWhatsApp(context, tenant.phoneNumber, reminderMsg)
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.NotificationsActive,
                                            contentDescription = "Send rent reminder via WhatsApp",
                                            tint = AppColors.CrimsonAlert,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppColors.CrimsonAlert
                                ) {
                                    Text(
                                        text = "Due",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppColors.AzureDark
                                ) {
                                    Text(
                                        text = "\u2713 Settled",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.AmberContainer)
                                .clickable { onAssignTenant() }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .border(BorderStroke(1.5.dp, AppColors.TextPrimary), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = AppColors.TextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Add Tenant",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = "Assign a tenant to this room",
                                    fontSize = 11.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { showDetails = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Room options",
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
            onVacateRoom = { showDetails = false; showVacateConfirm = true },
            onViewHistory = { showDetails = false; onViewHistory() },
            onEditTenant = { showDetails = false; onEditTenant() }
        )
    }

    if (showVacateConfirm) {
        VacateSettlementDialog(
            tenantName = tenant?.name ?: "Tenant",
            settlementAmount = pendingDue,
            securityDeposit = tenant?.securityDeposit ?: 0.0,
            onDismiss = { showVacateConfirm = false },
            onConfirm = { note, depositRefunded ->
                showVacateConfirm = false
                if (tenant != null && tenant.phoneNumber.isNotBlank()) {
                    val receiptMsg = ReceiptFormatter.formatVacateReceipt(
                        tenantName = tenant.name,
                        roomNumber = room.roomNumber,
                        securityDeposit = tenant.securityDeposit,
                        depositRefunded = depositRefunded,
                        settlementAmount = pendingDue,
                        settlementNote = note
                    )
                    ReceiptFormatter.sendViaWhatsApp(context, tenant.phoneNumber, receiptMsg)
                }
                onConfirmVacate(note, depositRefunded)
            }
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
    onViewHistory: () -> Unit,
    onEditTenant: () -> Unit = {}
) {
    
