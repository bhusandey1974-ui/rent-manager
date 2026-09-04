package com.example.rentmanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

fun calculateDaysLived(moveIn: String, moveOut: String?): Long {
    val formats = listOf(
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    )
    for (fmt in formats) {
        try {
            val inDate = fmt.parse(moveIn) ?: continue
            val outDate = if (!moveOut.isNullOrBlank()) fmt.parse(moveOut) ?: Date() else Date()
            val diff = outDate.time - inDate.time
            return (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
        } catch (_: Exception) {}
    }
    return 1
}

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNum: String, baseRent: Double, elecRate: Double) -> Unit
) {
    var roomNum by remember { mutableStateOf("") }
    var baseRentStr by remember { mutableStateOf("") }
    var elecRateStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Property Room", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomNum,
                    onValueChange = { roomNum = it },
                    label = { Text("Room Number (e.g. Room 101)", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = baseRentStr,
                    onValueChange = { baseRentStr = it },
                    label = { Text("Monthly Base Rent (₹)", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = elecRateStr,
                    onValueChange = { elecRateStr = it },
                    label = { Text("Electricity Rate per Unit (₹)", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = baseRentStr.toDoubleOrNull() ?: 0.0
                    val e = elecRateStr.toDoubleOrNull() ?: 0.0
                    if (roomNum.isNotBlank()) onConfirm(roomNum.trim(), r, e)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
            ) {
                Text("Save Room", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = FontFamily.SansSerif)
            }
        }
    )
}
@Composable
fun AssignTenantDialog(
    room: RoomUnit,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, aadhaar: String, address: String, deposit: Double, initialReading: Double, dateStr: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var depositStr by remember { mutableStateOf("") }
    var initMeterStr by remember { mutableStateOf(room.lastMeterReading.toInt().toString()) }

    val cal = Calendar.getInstance()
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysLeft = (maxDays - currentDay + 1).coerceAtLeast(1)
    val proratedRent = ((room.baseRent / maxDays) * daysLeft).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF1E40AF), modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Assign Tenant", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
                    Text("Room ${room.roomNumber} • Base ₹${room.baseRent.toInt()}/mo", fontSize = 12.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF64748B))
                }
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (currentDay > 1) {
                    item {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚡ Mid-Month Move-In (Day $currentDay of $maxDays)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF),
                                    fontFamily = FontFamily.SansSerif
                                )
                                Text(
                                    text = "Suggested prorated rent for $daysLeft days: ₹$proratedRent",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155),
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tenant Full Name *", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number (WhatsApp) *", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = aadhaar,
                        onValueChange = { aadhaar = it },
                        label = { Text("Aadhaar / National ID", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Permanent Address", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = depositStr,
                            onValueChange = { depositStr = it },
                            label = { Text("Deposit (₹)", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = initMeterStr,
                            onValueChange = { initMeterStr = it },
                            label = { Text("Start Meter", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.ElectricMeter, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val d = depositStr.toDoubleOrNull() ?: 0.0
                        val m = initMeterStr.toDoubleOrNull() ?: room.lastMeterReading
                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                        onConfirm(name.trim(), phone.trim(), aadhaar.trim(), address.trim(), d, m, dateStr)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
            ) {
                Text("Save & Assign", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = FontFamily.SansSerif)
            }
        }
    )
}
@Composable
fun LodgeBillDialog(
    room: RoomUnit,
    currentTenant: Tenant?,
    previousCarryover: Double,
    context: Context,
    onDismiss: () -> Unit,
    onConfirm: (BillRecord) -> Unit
) {
    val defaultMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
    var monthYear by remember { mutableStateOf(defaultMonthYear) }
    var baseRentStr by remember { mutableStateOf(room.baseRent.toInt().toString()) }
    var currMeterStr by remember { mutableStateOf("") }
    var maintenanceStr by remember { mutableStateOf("0") }
    var paymentMode by remember { mutableStateOf("Cash") }

    val currReading = currMeterStr.toDoubleOrNull() ?: room.lastMeterReading
    val elecUnits = (currReading - room.lastMeterReading).coerceAtLeast(0.0)
    val elecAmount = elecUnits * room.electricityRate
    val currentBaseRent = baseRentStr.toDoubleOrNull() ?: 0.0
    val maintenance = maintenanceStr.toDoubleOrNull() ?: 0.0

    val currentPeriodCharge = currentBaseRent + elecAmount + maintenance
    val totalPayable = currentPeriodCharge + previousCarryover

    var amountPaidStr by remember { mutableStateOf(totalPayable.coerceAtLeast(0.0).toInt().toString()) }
    val amountPaid = amountPaidStr.toDoubleOrNull() ?: 0.0
    val netRemainingBalance = totalPayable - amountPaid

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF1E40AF), modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Lodge Bill • Room ${room.roomNumber}", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
                    Text("Tenant: ${currentTenant?.name ?: "Occupant"}", fontSize = 12.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF1E40AF), fontWeight = FontWeight.Medium)
                }
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (previousCarryover > 0.0) {
                    item {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Previous Unpaid Due: ₹${previousCarryover.toInt()} added", color = Color(0xFF92400E), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
                            }
                        }
                    }
                } else if (previousCarryover < 0.0) {
                    item {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Advance Credit: ₹${(-previousCarryover).toInt()} auto-deducted", color = Color(0xFF065F46), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = monthYear,
                        onValueChange = { monthYear = it },
                        label = { Text("Billing Cycle / Month", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = baseRentStr,
                            onValueChange = { baseRentStr = it },
                            label = { Text("Base Rent (₹)", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = currMeterStr,
                            onValueChange = { currMeterStr = it },
                            label = { Text("Current Meter", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.ElectricMeter, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                            placeholder = { Text("Prev: ${room.lastMeterReading.toInt()}", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = maintenanceStr,
                        onValueChange = { maintenanceStr = it },
                        label = { Text("Maintenance / Water Charges (₹)", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Handyman, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Rent Amount:", fontSize = 12.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                Text("₹${currentBaseRent.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155), fontFamily = FontFamily.SansSerif)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Electricity (${elecUnits.toInt()}u × ₹${room.electricityRate.toInt()}):", fontSize = 12.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                Text("₹${elecAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155), fontFamily = FontFamily.SansSerif)
                            }
                            if (maintenance > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Other Charges:", fontSize = 12.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                    Text("+₹${maintenance.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155), fontFamily = FontFamily.SansSerif)
                                }
                            }
                            if (previousCarryover != 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Adjustment:", fontSize = 12.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                    Text(
                                        text = if (previousCarryover > 0) "+₹${previousCarryover.toInt()}" else "-₹${(-previousCarryover).toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (previousCarryover > 0) Color(0xFFB45309) else Color(0xFF059669),
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE2E8F0))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Gross Payable:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontFamily = FontFamily.SansSerif)
                                Text("₹${totalPayable.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF), fontFamily = FontFamily.SansSerif)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = amountPaidStr,
                        onValueChange = { amountPaidStr = it },
                        label = { Text("Amount Paid Now (₹) *", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("Cash", Icons.Default.AttachMoney, "Cash"),
                            Triple("UPI", Icons.Default.QrCode, "UPI"),
                            Triple("Bank", Icons.Default.AccountBalance, "Bank")
                        ).forEach { (mode, icon, _) ->
                            val isSelected = paymentMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF1E40AF) else Color(0xFFE2E8F0)),
                                modifier = Modifier.weight(1f).clickable { paymentMode = mode }
                            ) {
                                Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, tint = if (isSelected) Color(0xFF1E40AF) else Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(mode, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                }
                            }
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = if (netRemainingBalance > 0.0) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, if (netRemainingBalance > 0.0) Color(0xFFFECACA) else Color(0xFFA7F3D0))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (netRemainingBalance > 0.0) "Remaining Unpaid Due:" else if (netRemainingBalance < 0.0) "Advance Carried Forward:" else "Status:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = if (netRemainingBalance > 0.0) Color(0xFF991B1B) else Color(0xFF065F46)
                            )
                            Text(
                                text = if (netRemainingBalance > 0.0) "₹${netRemainingBalance.toInt()}" else if (netRemainingBalance < 0.0) "₹${(-netRemainingBalance).toInt()}" else "Fully Settled ✓",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = if (netRemainingBalance > 0.0) Color(0xFFDC2626) else Color(0xFF059669)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bill = BillRecord(
                        id = UUID.randomUUID().toString(),
                        roomId = room.id,
                        tenantId = room.currentTenantId,
                        monthYear = monthYear.trim(),
                        baseRent = currentBaseRent,
                        prevMeterReading = room.lastMeterReading,
                        currentMeterReading = currReading,
                        electricityRate = room.electricityRate,
                        maintenanceCharge = maintenance,
                        previousDueCarryover = previousCarryover,
                        amountPaid = amountPaid,
                        remainingDue = netRemainingBalance,
                        paymentMode = paymentMode,
                        timestamp = System.currentTimeMillis()
                    )

                    onConfirm(bill)

                    val message = buildString {
                        appendLine("🏠 *RENT & ELECTRICITY RECEIPT*")
                        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                        appendLine("👤 *Tenant:* ${currentTenant?.name ?: "Occupant"} (Room ${room.roomNumber})")
                        appendLine("📅 *Billing Period:* $monthYear")
                        appendLine("🏢 *Base Rent:* ₹${currentBaseRent.toInt()}")
                        appendLine("⚡ *Electricity (${elecUnits.toInt()} units):* ₹${elecAmount.toInt()}")
                        if (maintenance > 0.0) {
                            appendLine("🛠️ *Maintenance/Other:* ₹${maintenance.toInt()}")
                        }
                        if (previousCarryover > 0.0) {
                            appendLine("⚠️ *Previous Carryover Due:* ₹${previousCarryover.toInt()}")
                        } else if (previousCarryover < 0.0) {
                            appendLine("🟢 *Advance Credit Adjusted:* -₹${(-previousCarryover).toInt()}")
                        }
                        appendLine("🧾 *Total Net Billed:* ₹${totalPayable.toInt()}")
                        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                        appendLine("✅ *Amount Paid Today:* ₹${amountPaid.toInt()} ($paymentMode)")
                        if (netRemainingBalance > 0.0) {
                            appendLine("⚠️ *Pending Balance Due:* ₹${netRemainingBalance.toInt()}")
                        } else if (netRemainingBalance < 0.0) {
                            appendLine("🟢 *Advance Balance Carried Forward:* ₹${(-netRemainingBalance).toInt()}")
                        } else {
                            appendLine("🎉 *Balance Status:* Fully Cleared ✓")
                        }
                        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                        appendLine("Thank you!")
                    }

                    val cleanPhone = currentTenant?.phoneNumber?.replace("+", "")?.replace(" ", "") ?: ""
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Bill saved successfully.", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lodge & Send WhatsApp", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = FontFamily.SansSerif)
            }
        }
    )
}
@Composable
fun VacateDialog(
    room: RoomUnit,
    activeTenant: Tenant?,
    pendingDues: Double,
    context: Context,
    onDismiss: () -> Unit,
    onConfirm: (finalReading: Double, dateStr: String) -> Unit
) {
    var finalMeterStr by remember { mutableStateOf(room.lastMeterReading.toInt().toString()) }
    var damageStr by remember { mutableStateOf("0") }
    var damageNotes by remember { mutableStateOf("") }

    val fReading = finalMeterStr.toDoubleOrNull() ?: room.lastMeterReading
    val finalUnits = (fReading - room.lastMeterReading).coerceAtLeast(0.0)
    val finalElecCost = finalUnits * room.electricityRate
    val damageDeductions = damageStr.toDoubleOrNull() ?: 0.0

    val deposit = activeTenant?.depositAmount ?: 0.0
    val totalDeductions = (if (pendingDues > 0) pendingDues else 0.0) + finalElecCost + damageDeductions
    val advanceAdjustment = if (pendingDues < 0) -pendingDues else 0.0
    val effectiveDeposit = deposit + advanceAdjustment
    val finalRefund = effectiveDeposit - totalDeductions

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vacate Room ${room.roomNumber} & Settle", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Archive ${activeTenant?.name ?: "tenant"} and generate itemized statement.", fontSize = 13.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                }

                item {
                    OutlinedTextField(
                        value = finalMeterStr,
                        onValueChange = { finalMeterStr = it },
                        label = { Text("Final Meter Reading (Prev: ${room.lastMeterReading.toInt()})", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.ElectricMeter, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = damageStr,
                        onValueChange = { damageStr = it },
                        label = { Text("Damage Deductions (₹)", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Handyman, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = damageNotes,
                        onValueChange = { damageNotes = it },
                        label = { Text("Damage / Repair Remarks", fontFamily = FontFamily.SansSerif, fontSize = 12.sp) },
                        placeholder = { Text("e.g. Wall painting, broken lock", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚡ Unbilled Electricity: ₹${finalElecCost.toInt()} (${finalUnits.toInt()}u)", fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                            if (pendingDues != 0.0) {
                                Text(
                                    text = if (pendingDues > 0) "⚠️ Outstanding Dues: ₹${pendingDues.toInt()}" else "🟢 Advance Credit: ₹${(-pendingDues).toInt()}",
                                    fontSize = 12.sp,
                                    color = if (pendingDues > 0) Color(0xFFB45309) else Color(0xFF059669),
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                            if (damageDeductions > 0.0) {
                                Text("🛠️ Damage / Repairs: ₹${damageDeductions.toInt()}", fontSize = 12.sp, color = Color(0xFFDC2626), fontFamily = FontFamily.SansSerif)
                            }
                            Text("💰 Security Deposit Held: ₹${deposit.toInt()}", fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Refund Status:", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.SansSerif)
                                Text(
                                    text = if (finalRefund >= 0) "₹${finalRefund.toInt()} (Refund to Tenant)" else "₹${(-finalRefund).toInt()} (Tenant Owes)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    color = if (finalRefund >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                    onConfirm(fReading, dateStr)

                    val statement = buildString {
                        appendLine("🏁 *FINAL MOVE-OUT & SETTLEMENT STATEMENT*")
                        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                        appendLine("👤 *Tenant:* ${activeTenant?.name} (Room ${room.roomNumber})")
                        appendLine("🗓️ *Move-In:* ${activeTenant?.moveInDate}")
                        appendLine("🗓️ *Move-Out:* $dateStr")
                        appendLine("⚡ *Final Meter Reading:* ${fReading.toInt()} (${finalUnits.toInt()}u)")
                        appendLine("⚡ *Final Electricity Charge:* ₹${finalElecCost.toInt()}")
                        if (pendingDues > 0) {
                            appendLine("⚠️ *Unpaid Rent / Dues:* ₹${pendingDues.toInt()}")
                        } else if (pendingDues < 0) {
                            appendLine("🟢 *Advance Rent Credit:* -₹${(-pendingDues).toInt()}")
                        }
                        if (damageDeductions > 0.0) {
                            appendLine("🛠️ *Damage Deductions:* ₹${damageDeductions.toInt()}")
                            if (damageNotes.isNotBlank()) {
                                appendLine("   _Reason: ${damageNotes.trim()}_")
                            }
                        }
                        appendLine("💰 *Security Deposit Held:* ₹${deposit.toInt()}")
                        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                        if (finalRefund >= 0) {
                            appendLine("🟢 *Security Deposit Refund to Tenant:* ₹${finalRefund.toInt()}")
                        } else {
                            appendLine("🔴 *Pending Balance Recovery from Tenant:* ₹${(-finalRefund).toInt()}")
                        }
                        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
                        appendLine("Thank you for your tenancy!")
                    }

                    val cleanPhone = activeTenant?.phoneNumber?.replace("+", "")?.replace(" ", "") ?: ""
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(statement)}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm & Settle", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = FontFamily.SansSerif)
            }
        }
    )
}
@Composable
fun RoomHistoryDialog(
    room: RoomUnit,
    roomTenants: List<Tenant>,
    roomBills: List<BillRecord>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedHistoryTab by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        title = {
            Text("Room ${room.roomNumber} History", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedHistoryTab == 1) Color.White else Color.Transparent)
                            .clickable { selectedHistoryTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Past Tenants (${roomTenants.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedHistoryTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            color = if (selectedHistoryTab == 1) Color(0xFF1E40AF) else Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedHistoryTab == 0) Color.White else Color.Transparent)
                            .clickable { selectedHistoryTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bills (${roomBills.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedHistoryTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            color = if (selectedHistoryTab == 0) Color(0xFF1E40AF) else Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedHistoryTab == 1) {
                    if (roomTenants.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            Text("No past tenants recorded.", color = Color(0xFF94A3B8), fontFamily = FontFamily.SansSerif, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(roomTenants) { tenant ->
                                val tenantBills = roomBills.filter { it.tenantId == tenant.id }
                                val totalRentPaid = tenantBills.sumOf { it.baseRent }
                                val totalElecPaid = tenantBills.sumOf {
                                    val u = (it.currentMeterReading - it.prevMeterReading).coerceAtLeast(0.0)
                                    u * it.electricityRate
                                }
                                val totalPaidSum = tenantBills.sumOf { it.amountPaid }
                                val daysLived = calculateDaysLived(tenant.moveInDate, tenant.moveOutDate)

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shadowElevation = 1.dp
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(tenant.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(if (tenant.isActive) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = if (tenant.isActive) "Active" else "Past Tenant",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.SansSerif,
                                                    color = if (tenant.isActive) Color(0xFF1E40AF) else Color(0xFF64748B)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(tenant.phoneNumber, fontSize = 12.sp, color = Color(0xFF475569), fontFamily = FontFamily.SansSerif)
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                FilledTonalIconButton(
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tenant.phoneNumber}"))
                                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                                    },
                                                    modifier = Modifier.size(28.dp),
                                                    shape = CircleShape
                                                ) {
                                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF1E40AF), modifier = Modifier.size(14.dp))
                                                }

                                                FilledTonalIconButton(
                                                    onClick = {
                                                        val cleanPhone = tenant.phoneNumber.replace("+", "").replace(" ", "")
                                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
                                                        try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (_: Exception) {}
                                                    },
                                                    modifier = Modifier.size(28.dp),
                                                    shape = CircleShape
                                                ) {
                                                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = Color(0xFFF8FAFC),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Entry: ${tenant.moveInDate}", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                                    Text("Exit: ${tenant.moveOutDate ?: "Present"}", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text("Duration: $daysLived days lived", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF), fontFamily = FontFamily.SansSerif)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column {
                                                Text("Total Paid", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.SansSerif)
                                                Text("₹${totalPaidSum.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontFamily = FontFamily.SansSerif)
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Rent: ₹${totalRentPaid.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155), fontFamily = FontFamily.SansSerif)
                                                Text("Electricity: ₹${totalElecPaid.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155), fontFamily = FontFamily.SansSerif)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedHistoryTab == 0) {
                    if (roomBills.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            Text("No bills found.", color = Color(0xFF94A3B8), fontFamily = FontFamily.SansSerif, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(roomBills) { bill ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(bill.monthYear, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF0F172A))
                                            Text("₹${bill.amountPaid.toInt()} (${bill.paymentMode})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF059669), fontFamily = FontFamily.SansSerif)
                                        }
                                        val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
                                        val elecCost = units * bill.electricityRate
                                        Text(
                                            "Rent: ₹${bill.baseRent.toInt()}  •  Elec: ₹${elecCost.toInt()} (${units.toInt()}u)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, color = Color(0xFF1E40AF))
            }
        }
    )
}
