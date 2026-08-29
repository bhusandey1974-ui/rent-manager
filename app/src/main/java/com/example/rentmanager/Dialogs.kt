package com.example.rentmanager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DlgBlue = Color(0xFF0284C7)
private val DlgDark = Color(0xFF0F172A)
private val DlgMuted = Color(0xFF64748B)
private val DlgGreen = Color(0xFF10B981)
private val DlgRed = Color(0xFFEF4444)
private val DlgFont = FontFamily.SansSerif

@Composable
fun TenantDetailsDialog(
    tenant: Tenant,
    room: RoomUnit,
    onDismiss: () -> Unit,
    onVacate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tenant Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = DlgFont,
                color = DlgDark
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tenant.name.take(1).uppercase(),
                            color = DlgBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            fontFamily = DlgFont
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(tenant.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFamily = DlgFont, color = DlgDark)
                        Text("Assigned to Room ${room.roomNumber}", fontSize = 13.sp, fontFamily = DlgFont, color = DlgMuted)
                    }
                }

                Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)

                TenantInfoRow(icon = Icons.Default.Phone, label = "Phone", value = tenant.phone)
                TenantInfoRow(icon = Icons.Default.CalendarToday, label = "Move-In Date", value = tenant.moveInDate)
                TenantInfoRow(icon = Icons.Default.Savings, label = "Security Deposit", value = "₹${"%,.2f".format(tenant.securityDeposit)}")
                TenantInfoRow(icon = Icons.Default.ElectricMeter, label = "Initial Meter", value = "${tenant.initialMeterReading} kWh")

                Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)

                Text("Tenancy Records", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = DlgFont, color = DlgDark)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFEDF2F7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• Assigned on: ${tenant.moveInDate}", fontSize = 12.sp, fontFamily = DlgFont, color = DlgDark)
                        Text("• Base Room Rent: ₹${room.baseRent}/mo", fontSize = 12.sp, fontFamily = DlgFont, color = DlgMuted)
                        Text("• Electricity Unit Rate: ₹${room.electricityRate}/kWh", fontSize = 12.sp, fontFamily = DlgFont, color = DlgMuted)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Close", fontFamily = DlgFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onVacate,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DlgRed)
            ) {
                Text("Vacate Tenant", color = DlgRed, fontFamily = DlgFont, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun TenantInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = DlgMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = DlgMuted, fontSize = 13.sp, fontFamily = DlgFont)
        }
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, fontFamily = DlgFont, color = DlgDark)
    }
}

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNum: String, baseRent: Double, elecRate: Double) -> Unit
) {
    var roomNum by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = roomNum,
                    onValueChange = { roomNum = it },
                    label = { Text("Room No (e.g. 101, 01)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate/Unit (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rent.toDoubleOrNull() ?: 0.0
                    val e = rate.toDoubleOrNull() ?: 10.0
                    if (roomNum.isNotBlank() && r > 0) {
                        onConfirm(roomNum, r, e)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Save Room", fontWeight = FontWeight.Bold, fontFamily = DlgFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont)
            }
        }
    )
}
@Composable
fun AssignTenantDialog(
    room: RoomUnit,
    todayDate: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, aadhaar: String, date: String, deposit: Double, initialMeter: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayDate) }
    var deposit by remember { mutableStateOf("0") }
    var reading by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Tenant to Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Name", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar Number", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Move-In Date", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deposit,
                    onValueChange = { deposit = it },
                    label = { Text("Security Deposit (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reading,
                    onValueChange = { reading = it },
                    label = { Text("Initial Meter Reading (kWh)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dep = deposit.toDoubleOrNull() ?: 0.0
                    val meter = reading.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name, phone, aadhaar, date, dep, meter)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Assign Tenant", fontWeight = FontWeight.Bold, fontFamily = DlgFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont)
            }
        }
    )
}

@Composable
fun GenerateBillDialog(
    room: RoomUnit,
    tenant: Tenant,
    prevReading: Double,
    previousDue: Double,
    defaultMonth: String,
    onDismiss: () -> Unit,
    onConfirm: (month: String, curReading: Double, maint: Double, paid: Double, mode: String) -> Unit
) {
    var month by remember { mutableStateOf(defaultMonth) }
    var curReadingStr by remember { mutableStateOf("") }
    var maintStr by remember { mutableStateOf("0") }
    var paidStr by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("UPI / Online") }

    val curReading = curReadingStr.toDoubleOrNull() ?: prevReading
    val units = (curReading - prevReading).coerceAtLeast(0.0)
    val elecCharge = units * room.electricityRate
    val maint = maintStr.toDoubleOrNull() ?: 0.0
    val totalBill = room.baseRent + elecCharge + maint + previousDue

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lodge Bill - Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Billing Month", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Base Rent: ₹${room.baseRent}", fontWeight = FontWeight.Bold, fontFamily = DlgFont, color = DlgDark, fontSize = 14.sp)
                Text("Previous Reading: $prevReading kWh", color = DlgMuted, fontFamily = DlgFont, fontSize = 12.sp)

                OutlinedTextField(
                    value = curReadingStr,
                    onValueChange = { curReadingStr = it },
                    label = { Text("Current Meter Reading (kWh)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Units: $units = ₹${"%.2f".format(elecCharge)} (@ ₹${room.electricityRate}/unit)", fontSize = 12.sp, fontFamily = DlgFont, color = DlgBlue)

                OutlinedTextField(
                    value = maintStr,
                    onValueChange = { maintStr = it },
                    label = { Text("Maintenance / Other (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (previousDue > 0) {
                    Text("Previous Overdue: ₹$previousDue", color = DlgRed, fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 12.sp)
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Payable:", fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 14.sp, color = DlgDark)
                        Text("₹${"%,.2f".format(totalBill)}", fontWeight = FontWeight.ExtraBold, fontFamily = DlgFont, fontSize = 16.sp, color = DlgBlue)
                    }
                }

                OutlinedTextField(
                    value = paidStr,
                    onValueChange = { paidStr = it },
                    label = { Text("Amount Paid Now (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    placeholder = { Text("Enter ₹${"%,.2f".format(totalBill)}") },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = paidStr.toDoubleOrNull() ?: 0.0
                    if (curReading >= prevReading) {
                        onConfirm(month, curReading, maint, p, mode)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Save Bill", fontWeight = FontWeight.Bold, fontFamily = DlgFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont)
            }
        }
    )
}

@Composable
fun EditRoomDialog(
    room: RoomUnit,
    onDismiss: () -> Unit,
    onConfirm: (roomNum: String, baseRent: Double, elecRate: Double) -> Unit
) {
    var roomNum by remember { mutableStateOf(room.roomNumber) }
    var rent by remember { mutableStateOf(room.baseRent.toString()) }
    var rate by remember { mutableStateOf(room.electricityRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = roomNum,
                    onValueChange = { roomNum = it },
                    label = { Text("Room No", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate/Unit (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rent.toDoubleOrNull() ?: room.baseRent
                    val e = rate.toDoubleOrNull() ?: room.electricityRate
                    onConfirm(roomNum, r, e)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontFamily = DlgFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont)
            }
        }
    )
}

@Composable
fun CheckoutTenantDialog(
    tenant: Tenant,
    todayDate: String,
    onDismiss: () -> Unit,
    onConfirm: (date: String, refundAmount: Double) -> Unit
) {
    var checkoutDate by remember { mutableStateOf(todayDate) }
    var refundStr by remember { mutableStateOf(tenant.securityDeposit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vacate Tenant: ${tenant.name}", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Security Deposit: ₹${tenant.securityDeposit}", color = DlgDark, fontFamily = DlgFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                OutlinedTextField(
                    value = checkoutDate,
                    onValueChange = { checkoutDate = it },
                    label = { Text("Vacate Date", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = refundStr,
                    onValueChange = { refundStr = it },
                    label = { Text("Refund Amount (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val refund = refundStr.toDoubleOrNull() ?: 0.0
                    onConfirm(checkoutDate, refund)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgRed)
            ) {
                Text("Confirm Vacate", fontWeight = FontWeight.Bold, fontFamily = DlgFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont)
            }
        }
    )
}

@Composable
fun RoomHistoryDialog(
    room: RoomUnit,
    bills: List<BillRecord>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Room ${room.roomNumber} History", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (bills.isEmpty()) {
                    Text("No billing history found for this room.", color = DlgMuted, fontFamily = DlgFont, fontSize = 13.sp)
                } else {
                    bills.reversed().forEach { bill ->
                        val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
                        val totalBill = bill.baseRent + (units * bill.electricityRate) + bill.maintenanceCharge + bill.previousDueCarryover
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(bill.monthYear, fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 14.sp)
                                    Text("Paid: ₹${bill.amountPaid}", color = DlgGreen, fontFamily = DlgFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("Units: $units (Total: ₹$totalBill)", fontSize = 12.sp, fontFamily = DlgFont, color = DlgMuted)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)) {
                Text("Close", fontFamily = DlgFont, fontSize = 14.sp)
            }
        }
    )
}
