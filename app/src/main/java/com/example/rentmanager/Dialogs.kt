package com.example.rentmanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNum: String, baseRent: Double, elecRate: Double) -> Unit
) {
    var roomNum by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room", fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = roomNum,
                    onValueChange = { roomNum = it },
                    label = { Text("Room No (e.g. 101, 01)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate/Unit (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Save Room", fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont, fontSize = 15.sp)
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
        title = { Text("Assign Tenant to Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Name", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar Number", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Move-In Date", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deposit,
                    onValueChange = { deposit = it },
                    label = { Text("Security Deposit (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reading,
                    onValueChange = { reading = it },
                    label = { Text("Initial Meter Reading (kWh)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Assign Tenant", fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont, fontSize = 15.sp)
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
        title = { Text("Lodge Bill - Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Billing Month", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Base Rent: ₹${room.baseRent}", fontWeight = FontWeight.Bold, fontFamily = DlgFont, color = DlgDark, fontSize = 14.sp)
                Text("Previous Reading: $prevReading kWh", color = DlgMuted, fontFamily = DlgFont, fontSize = 13.sp)

                OutlinedTextField(
                    value = curReadingStr,
                    onValueChange = { curReadingStr = it },
                    label = { Text("Current Meter Reading (kWh)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Units: $units = ₹${"%.2f".format(elecCharge)} (@ ₹${room.electricityRate}/unit)", fontSize = 13.sp, fontFamily = DlgFont, color = DlgBlue)

                OutlinedTextField(
                    value = maintStr,
                    onValueChange = { maintStr = it },
                    label = { Text("Maintenance / Other (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (previousDue > 0) {
                    Text("Previous Overdue: ₹$previousDue", color = DlgRed, fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 13.sp)
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Payable:", fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 15.sp, color = DlgDark)
                        Text("₹${"%,.2f".format(totalBill)}", fontWeight = FontWeight.ExtraBold, fontFamily = DlgFont, fontSize = 17.sp, color = DlgBlue)
                    }
                }

                OutlinedTextField(
                    value = paidStr,
                    onValueChange = { paidStr = it },
                    label = { Text("Amount Paid Now (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    placeholder = { Text("Enter ₹${"%,.2f".format(totalBill)}", fontFamily = DlgFont) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Save & Open WhatsApp", fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont, fontSize = 15.sp)
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
        title = { Text("Edit Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = roomNum,
                    onValueChange = { roomNum = it },
                    label = { Text("Room No", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate/Unit (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont, fontSize = 15.sp)
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
        title = { Text("Vacate Tenant: ${tenant.name}", fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Security Deposit: ₹${tenant.securityDeposit}", color = DlgDark, fontFamily = DlgFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                OutlinedTextField(
                    value = checkoutDate,
                    onValueChange = { checkoutDate = it },
                    label = { Text("Vacate Date", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = refundStr,
                    onValueChange = { refundStr = it },
                    label = { Text("Refund Amount (₹)", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgRed)
            ) {
                Text("Confirm Vacate", fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont, fontSize = 15.sp)
            }
        }
    )
}

@Composable
fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String, city: String, owner: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Property", fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Property Name", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text("Owner Name", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontFamily = DlgFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = DlgFont, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, address, city, owner, phone)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)
            ) {
                Text("Save Property", fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DlgMuted, fontFamily = DlgFont, fontSize = 15.sp)
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
        title = { Text("Room ${room.roomNumber} History", fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = DlgFont, color = DlgDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (bills.isEmpty()) {
                    Text("No billing history found for this room.", color = DlgMuted, fontFamily = DlgFont, fontSize = 14.sp)
                } else {
                    bills.reversed().forEach { bill ->
                        val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
                        val totalBill = bill.baseRent + (units * bill.electricityRate) + bill.maintenanceCharge + bill.previousDueCarryover
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(bill.monthYear, fontWeight = FontWeight.Bold, fontFamily = DlgFont, fontSize = 15.sp)
                                    Text("Paid: ₹${bill.amountPaid}", color = DlgGreen, fontFamily = DlgFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text("Units: $units (Total: ₹$totalBill)", fontSize = 13.sp, fontFamily = DlgFont, color = DlgMuted)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = DlgBlue)) {
                Text("Close", fontFamily = DlgFont, fontSize = 15.sp)
            }
        }
    )
}
