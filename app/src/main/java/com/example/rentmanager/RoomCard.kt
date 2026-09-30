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
import androidx.compose.foundation.layout.heightIn
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Reference sizes (dp, on a 360dp-wide screen):
        //   occupied card ~ 338 x 106  -> inner row 92 + 7 padding top/bottom
        //   vacant card   ~ 338 x  99  -> inner row 85 + 7 padding top/bottom
        //   photo 85 wide, 14 corner radius; gap between cards 10
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp)
                .height(if (room.isOccupied) 92.dp else 85.dp)
        ) {
            // ---- Photo
            Box(
                modifier = Modifier
                    .width(85.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(
                        id = if (room.isOccupied) R.drawable.room_settled else R.drawable.room_empty
                    ),
                    contentDescription = if (room.isOccupied) "Occupied room" else "Vacant room",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#${room.roomNumber}",
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ---- Content (title row / tenant info / bottom row, spread evenly)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 10.dp, end = 2.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title + status pill + chevron
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Room ${room.roomNumber}",
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 1
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
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = statusLabel,
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Tenant info
                if (room.isOccupied && tenant != null) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tenant.name,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                color = AppColors.TextSecondary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tenant.phoneNumber,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                color = AppColors.TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "No tenant assigned",
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            color = AppColors.TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Bottom row: rent-due box (occupied) or Add Tenant box (vacant) + kebab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (room.isOccupied) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(33.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.SlateBackground)
                                .padding(horizontal = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Rent Due",
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    color = AppColors.TextSecondary,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (pendingDue > 0)
                                        "₹${String.format(Locale.ENGLISH, "%.0f", pendingDue)} pending"
                                    else nextRentDueLabel(tenant?.moveInDate ?: System.currentTimeMillis()),
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary,
                                    maxLines = 1
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
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.NotificationsActive,
                                            contentDescription = "Send rent reminder via WhatsApp",
                                            tint = AppColors.CrimsonAlert,
                                            modifier = Modifier.size(16.dp)
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
                                        lineHeight = 14.sp,
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
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF4E2))
                                .clickable { onAssignTenant() }
                                .padding(horizontal = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(BorderStroke(1.5.dp, AppColors.TextPrimary), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = AppColors.TextPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = "Add Tenant",
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Assign a tenant to this room",
                                    fontSize = 10.5.sp,
                                    lineHeight = 13.sp,
                                    color = AppColors.TextSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(
                        onClick = { showDetails = true },
                        modifier = Modifier.size(28.dp)
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
            
