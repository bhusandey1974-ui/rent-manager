package com.example.rentmanager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoomCard(
    room: RoomUnit,
    tenant: Tenant?,
    pendingBalance: Double,
    onLodgeBill: () -> Unit,
    onVacate: () -> Unit,
    onAssignTenant: () -> Unit,
    onViewHistory: () -> Unit
) {
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
                            Text(
                                text = tenant.phoneNumber,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ElectricMeter, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Last Meter: ${room.lastMeterReading.toInt()} units",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onLodgeBill,
                        modifier = Modifier.weight(1.2f).height(38.dp),
                        shape = RoundedCornerShape(9.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Lodge Bill", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                    }

                    OutlinedButton(
                        onClick = onVacate,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(9.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Text("Vacate", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                    }

                    FilledTonalIconButton(
                        onClick = onViewHistory,
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(9.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF475569)
                        )
                    ) {
                        Icon(Icons.Default.History, contentDescription = "History", modifier = Modifier.size(18.dp))
                    }
                }
            } else {
                Text(
                    text = "Ready for occupancy",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAssignTenant,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(9.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Assign Tenant", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                    }

                    OutlinedButton(
                        onClick = onViewHistory,
                        modifier = Modifier.weight(0.8f).height(38.dp),
                        shape = RoundedCornerShape(9.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text("History", fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                    }
                }
            }
        }
    }
}

