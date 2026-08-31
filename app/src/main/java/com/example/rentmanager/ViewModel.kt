package com.example.rentmanager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PropertiesView(
    rooms: List<RoomUnit>,
    tenants: List<Tenant>,
    bills: List<BillRecord>,
    vm: RentViewModel,
    onAddRoom: () -> Unit,
    onEditRoom: (RoomUnit) -> Unit,
    onDeleteRoom: (RoomUnit) -> Unit,
    onAssignTenant: (RoomUnit) -> Unit,
    onTenantClick: (Tenant, RoomUnit) -> Unit,
    onLodgeBill: (RoomUnit, Tenant) -> Unit,
    onVacate: (Tenant) -> Unit,
    onHistory: (RoomUnit) -> Unit
) {
    if (rooms.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, UICardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddHomeWork, contentDescription = null, tint = UIBluePrimary, modifier = Modifier.size(32.dp))
                    }
                    Text(
                        text = "No Units Added Yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CleanFont,
                        color = UIDarkText
                    )
                    Text(
                        text = "Start by registering your rental rooms with rent & electricity charges.",
                        fontSize = 13.sp,
                        fontFamily = CleanFont,
                        color = UIMutedText,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onAddRoom,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
                    ) {
                        Text("Add First Unit", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(rooms) { room ->
                val tenant = tenants.find { it.roomId == room.id && it.isActive }
                val pendingDue = vm.getCumulativePendingDue(room.id)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, UICardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (tenant != null) UIRedDanger else UIGreenSuccess)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Room ${room.roomNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    fontFamily = CleanFont,
                                    color = UIDarkText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = { onEditRoom(room) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Room", tint = UIMutedText, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { onDeleteRoom(room) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = UIRedDanger.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(
                                text = "₹${"%,.2f".format(room.baseRent)}/mo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = CleanFont,
                                color = UIBluePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (tenant != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onTenantClick(tenant, room) },
                                color = Color(0xFFF1F5F9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tenant.name.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = CleanFont,
                                                color = UIBluePrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = tenant.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                fontFamily = CleanFont,
                                                color = UIDarkText
                                            )
                                            Text(
                                                text = "📞 ${tenant.phone}  •  In: ${tenant.moveInDate}",
                                                fontSize = 11.sp,
                                                fontFamily = CleanFont,
                                                color = UIMutedText
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = "View Profile", tint = UIMutedText)
                                }
                            }

                            if (pendingDue > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⚠️ Unpaid Due: ₹${"%,.2f".format(pendingDue)}",
                                            color = Color(0xFFD97706),
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = CleanFont,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onHistory(room) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, UICardBorder)
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = UIDarkText)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("History", fontSize = 12.sp, fontFamily = CleanFont, color = UIDarkText)
                                }

                                Button(
                                    onClick = { onLodgeBill(room, tenant) },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lodge Bill", fontSize = 12.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onVacate(tenant) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, UICardBorder)
                                ) {
                                    Text("Vacate", fontSize = 12.sp, fontFamily = CleanFont, color = UIRedDanger)
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🟢 Unit is Vacant", color = UIGreenSuccess, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 13.sp)
                                    Text("Rate: ₹${room.electricityRate}/u", color = UIGreenSuccess, fontFamily = CleanFont, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onHistory(room) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, UICardBorder)
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = UIDarkText)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("History", fontSize = 12.sp, fontFamily = CleanFont, color = UIDarkText)
                                }

                                Button(
                                    onClick = { onAssignTenant(room) },
                                    modifier = Modifier.weight(2f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Assign Tenant", fontSize = 13.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold)
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
fun RevenueView(
    bills: List<BillRecord>,
    rooms: List<RoomUnit>,
    tenants: List<Tenant>,
    onClearAll: () -> Unit,
    onShareWhatsApp: (BillRecord, Tenant, RoomUnit) -> Unit
) {
    var ledgerFilter by remember { mutableStateOf("All") }

    val totalCollected = bills.sumOf { it.amountPaid }
    val totalRent = bills.sumOf { it.baseRent }
    val totalElec = bills.sumOf { (it.currentMeterReading - it.prevMeterReading).coerceAtLeast(0.0) * it.electricityRate }
    val totalDue = bills.sumOf { it.remainingDue }

    val filteredBills = remember(bills, ledgerFilter) {
        when (ledgerFilter) {
            "Paid" -> bills.filter { it.remainingDue <= 0 }
            "Pending" -> bills.filter { it.remainingDue > 0 }
            else -> bills
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(UIBlueGradientStart, UIBlueGradientEnd)))
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIFETIME REVENUE COLLECTION",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontFamily = CleanFont,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            IconButton(onClick = onClearAll, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Clear All Revenue", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "₹${"%,.2f".format(totalCollected)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = CleanFont,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rent Earnings", fontSize = 12.sp, fontFamily = CleanFont, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${"%,.2f".format(totalRent)}", fontSize = 14.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Electricity", fontSize = 12.sp, fontFamily = CleanFont, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${"%,.2f".format(totalElec)}", fontSize = 14.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Total Due", fontSize = 12.sp, fontFamily = CleanFont, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${"%,.2f".format(totalDue)}", fontSize = 14.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, UICardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Year Breakdown",
                            fontWeight = FontWeight.Bold,
                            fontFamily = CleanFont,
                            fontSize = 16.sp,
                            color = UIDarkText
                        )
                        IconButton(onClick = onClearAll, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Stats", tint = Color(0xFF7C3AED))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        YearStatBox(modifier = Modifier.weight(1f), label = "Base Rent Billed", value = "₹${"%,.2f".format(totalRent)}", valueColor = UIDarkText)
                        YearStatBox(modifier = Modifier.weight(1f), label = "Electricity Charges", value = "₹${"%,.2f".format(totalElec)}", valueColor = UIBluePrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        YearStatBox(modifier = Modifier.weight(1f), label = "Total Invoiced", value = "₹${"%,.2f".format(totalRent + totalElec)}", valueColor = UIBluePrimary)
                        YearStatBox(modifier = Modifier.weight(1f), label = "Collected", value = "₹${"%,.2f".format(totalCollected)}", valueColor = UIGreenSuccess)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Billing Ledger (${filteredBills.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CleanFont,
                    color = UIDarkText
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Paid", "Pending").forEach { filter ->
                        val isSelected = ledgerFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) UIBluePrimary else Color(0xFFF1F5F9))
                                .clickable { ledgerFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontFamily = CleanFont,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else UIDarkText
                            )
                        }
                    }
                }
            }
        }

        if (filteredBills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, UICardBorder)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("No billing entries found.", color = UIMutedText, fontFamily = CleanFont, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredBills.reversed()) { bill ->
                val room = rooms.find { it.id == bill.roomId }
                val tenant = tenants.find { it.id == bill.tenantId }
                val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
                val totalBillAmount = bill.baseRent + (units * bill.electricityRate) + bill.maintenanceCharge + bill.previousDueCarryover

                val formattedPaymentDate = remember(bill.timestamp) {
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(bill.timestamp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, UICardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "${tenant?.name ?: "Tenant"} (Room ${room?.roomNumber ?: ""})",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CleanFont,
                                    fontSize = 16.sp,
                                    color = UIDarkText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Billing Month: ${bill.monthYear}",
                                    fontSize = 12.sp,
                                    fontFamily = CleanFont,
                                    color = UIMutedText
                                )
                                Text(
                                    text = "Paid on: $formattedPaymentDate",
                                    fontSize = 11.sp,
                                    fontFamily = CleanFont,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (bill.remainingDue <= 0) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (bill.remainingDue <= 0) "PAID \u2705 (${bill.paymentMode})" else "DUE: ₹${"%.2f".format(bill.remainingDue)}",
                                    fontSize = 11.sp,
                                    fontFamily = CleanFont,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bill.remainingDue <= 0) UIGreenSuccess else Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = UICardBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Rent: ₹${bill.baseRent}  •  Elec: ₹${"%.2f".format(units * bill.electricityRate)}", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                                Text("Units: $units (${bill.prevMeterReading} -> ${bill.currentMeterReading})", fontSize = 11.sp, fontFamily = CleanFont, color = UIMutedText)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${"%,.2f".format(totalBillAmount)}", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 16.sp, color = UIBluePrimary)
                                Text("Paid: ₹${"%,.2f".format(bill.amountPaid)} (${bill.paymentMode})", fontSize = 11.sp, fontFamily = CleanFont, color = UIGreenSuccess)
                            }
                        }

                        if (tenant != null && room != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onShareWhatsApp(bill, tenant, room) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = UIGreenSuccess, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp Receipt", fontSize = 12.sp, fontFamily = CleanFont, color = UIGreenSuccess, fontWeight = FontWeight.Bold)
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
fun YearStatBox(modifier: Modifier = Modifier, label: String, value: String, valueColor: Color) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, fontFamily = CleanFont, color = UIMutedText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
