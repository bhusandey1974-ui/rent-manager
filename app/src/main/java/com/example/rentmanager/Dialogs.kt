package com.example.rentmanager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNo: String, unitType: String, rent: Double, elecRate: Double) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var unitType by remember { mutableStateOf("1 RK") }
    var rentStr by remember { mutableStateOf("") }
    var elecRateStr by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
        title = {
            Text("Add Rental Unit", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 20.sp, color = UIDarkText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room / Flat Number", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = unitType,
                    onValueChange = { unitType = it },
                    label = { Text("Unit Type (e.g. 1 BHK, 1 RK, Shop)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rentStr,
                    onValueChange = { rentStr = it },
                    label = { Text("Monthly Base Rent (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = elecRateStr,
                    onValueChange = { elecRateStr = it },
                    label = { Text("Electricity Rate (₹ / Unit)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rentStr.toDoubleOrNull() ?: 0.0
                    val rate = elecRateStr.toDoubleOrNull() ?: 10.0
                    if (roomNumber.isNotBlank()) {
                        onConfirm(roomNumber, unitType, r, rate)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Add Unit", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}

@Composable
fun EditRoomDialog(
    room: RoomUnit,
    onDismiss: () -> Unit,
    onConfirm: (roomNo: String, rent: Double, elecRate: Double) -> Unit
) {
    var roomNumber by remember { mutableStateOf(room.roomNumber) }
    var rentStr by remember { mutableStateOf(room.baseRent.toString()) }
    var elecRateStr by remember { mutableStateOf(room.electricityRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
        title = {
            Text("Edit Room Details", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 20.sp, color = UIDarkText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room / Flat Number", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rentStr,
                    onValueChange = { rentStr = it },
                    label = { Text("Base Rent (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = elecRateStr,
                    onValueChange = { elecRateStr = it },
                    label = { Text("Electricity Rate (₹ / Unit)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rentStr.toDoubleOrNull() ?: room.baseRent
                    val rate = elecRateStr.toDoubleOrNull() ?: room.electricityRate
                    if (roomNumber.isNotBlank()) {
                        onConfirm(roomNumber, r, rate)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Save Changes", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}
@Composable
fun AssignTenantDialog(
    room: RoomUnit,
    defaultDate: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, aadhaar: String, moveIn: String, deposit: Double, initialReading: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var moveInDate by remember { mutableStateOf(defaultDate) }
    var depositStr by remember { mutableStateOf("0") }
    var initialMeterStr by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
        title = {
            Text("Assign to Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 18.sp, color = UIDarkText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Full Name", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar / National ID (Optional)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = moveInDate,
                    onValueChange = { moveInDate = it },
                    label = { Text("Move-In Date", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = depositStr,
                    onValueChange = { depositStr = it },
                    label = { Text("Security Deposit (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = initialMeterStr,
                    onValueChange = { initialMeterStr = it },
                    label = { Text("Initial Meter Reading (Units)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dep = depositStr.toDoubleOrNull() ?: 0.0
                    val meter = initialMeterStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name, phone, aadhaar, moveInDate, dep, meter)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Confirm Assignment", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}

@Composable
fun TenantDetailsDialog(
    tenant: Tenant,
    room: RoomUnit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onVacate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = UIBluePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tenant Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        fontFamily = CleanFont,
                        color = UIDarkText
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Tenant",
                        tint = UIBluePrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Name: ${tenant.name}",
                    fontSize = 16.sp,
                    fontFamily = CleanFont,
                    fontWeight = FontWeight.Bold,
                    color = UIDarkText
                )
                Text(
                    text = "Phone: ${tenant.phone}",
                    fontSize = 14.sp,
                    fontFamily = CleanFont,
                    color = UIDarkText
                )
                if (tenant.aadhaarNumber.isNotBlank()) {
                    Text(
                        text = "Aadhaar / ID: ${tenant.aadhaarNumber}",
                        fontSize = 14.sp,
                        fontFamily = CleanFont,
                        color = UIDarkText
                    )
                }
                Text(
                    text = "Move-in: ${tenant.moveInDate}",
                    fontSize = 14.sp,
                    fontFamily = CleanFont,
                    color = UIDarkText
                )
                Text(
                    text = "Security Deposit: ₹${"%,.2f".format(tenant.securityDeposit)}",
                    fontSize = 14.sp,
                    fontFamily = CleanFont,
                    fontWeight = FontWeight.Bold,
                    color = UIGreenSuccess
                )
                Text(
                    text = "Initial Meter: ${tenant.initialMeterReading} units",
                    fontSize = 14.sp,
                    fontFamily = CleanFont,
                    color = UIDarkText
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Done", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onEdit,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, UIBluePrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = UIBluePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Profile", color = UIBluePrimary, fontFamily = CleanFont, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun EditTenantDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, aadhaar: String) -> Unit
) {
    var name by remember { mutableStateOf(tenant.name) }
    var phone by remember { mutableStateOf(tenant.phone) }
    var aadhaar by remember { mutableStateOf(tenant.aadhaarNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
        title = {
            Text("Edit Tenant Info", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 18.sp, color = UIDarkText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Name", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar / ID", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name, phone, aadhaar)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Save", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
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
    var mode by remember { mutableStateOf("Cash") }

    val curReading = curReadingStr.toDoubleOrNull() ?: prevReading
    val units = (curReading - prevReading).coerceAtLeast(0.0)
    val elecCharge = units * room.electricityRate
    val maint = maintStr.toDoubleOrNull() ?: 0.0
    val totalBill = room.baseRent + elecCharge + maint + previousDue

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = UIBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Lodge Bill - Room ${room.roomNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = CleanFont,
                        color = UIDarkText
                    )
                    Text(
                        text = "Tenant: ${tenant.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = CleanFont,
                        color = UIBluePrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Billing Month", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = prevReading.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Previous Meter Reading", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = curReadingStr,
                    onValueChange = { curReadingStr = it },
                    label = { Text("Current Reading (Enter manually)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maintStr,
                    onValueChange = { maintStr = it },
                    label = { Text("Maintenance / Other Charges (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    color = Color(0xFFF0F9FF),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Units Consumed: $units units (@ ₹${room.electricityRate}/unit)",
                            fontSize = 13.sp,
                            fontFamily = CleanFont,
                            color = UIDarkText
                        )
                        if (previousDue > 0) {
                            Text(
                                text = "⚠️ Carried Due: ₹${"%,.2f".format(previousDue)}",
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.Bold,
                                fontFamily = CleanFont,
                                fontSize = 13.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount Due:",
                                fontWeight = FontWeight.Bold,
                                fontFamily = CleanFont,
                                fontSize = 15.sp,
                                color = UIDarkText
                            )
                            Text(
                                text = "₹${"%,.2f".format(totalBill)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = CleanFont,
                                fontSize = 18.sp,
                                color = UIBluePrimary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = paidStr,
                    onValueChange = { paidStr = it },
                    label = { Text("Amount Paid Now (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Payment Mode",
                    fontSize = 13.sp,
                    fontFamily = CleanFont,
                    fontWeight = FontWeight.SemiBold,
                    color = UIDarkText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cash", "UPI", "Bank", "Cheque").forEach { option ->
                        val isSelected = mode == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) UIBluePrimary else Color(0xFFF1F5F9))
                                .clickable { mode = option }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontFamily = CleanFont,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else UIDarkText
                            )
                        }
                    }
                }
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
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Create & Save", fontWeight = FontWeight.Bold, fontFamily = CleanFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}

@Composable
fun CheckoutDialog(
    tenant: Tenant,
    room: RoomUnit?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onConfirm: (vacateDate: String, refund: Double) -> Unit
) {
    var vacateDate by remember { mutableStateOf(defaultDate) }
    var refundStr by remember { mutableStateOf(tenant.securityDeposit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
        title = {
            Text("Vacate Room ${room?.roomNumber ?: ""}", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 18.sp, color = UIDarkText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Tenant: ${tenant.name}", fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 15.sp, color = UIDarkText)
                Text("Security Deposit Held: ₹${"%,.2f".format(tenant.securityDeposit)}", fontSize = 13.sp, fontFamily = CleanFont, color = UIGreenSuccess, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = vacateDate,
                    onValueChange = { vacateDate = it },
                    label = { Text("Vacate / Exit Date", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = refundStr,
                    onValueChange = { refundStr = it },
                    label = { Text("Security Deposit Refunded (₹)", fontFamily = CleanFont, fontSize = 14.sp) },
                    textStyle = TextStyle(fontFamily = CleanFont, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ref = refundStr.toDoubleOrNull() ?: 0.0
                    onConfirm(vacateDate, ref)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIRedDanger)
            ) {
                Text("Confirm Vacate", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIMutedText, fontFamily = CleanFont)
            }
        }
    )
}

@Composable
fun RoomHistoryDialog(
    room: RoomUnit,
    bills: List<BillRecord>,
    pastTenants: List<PastTenancyRecord>,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Room ${room.roomNumber} History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = CleanFont,
                    color = UIDarkText
                )
                if (bills.isNotEmpty() || pastTenants.isNotEmpty()) {
                    IconButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = UIRedDanger
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (pastTenants.isNotEmpty()) {
                    Text(
                        text = "Past Occupants",
                        fontWeight = FontWeight.Bold,
                        fontFamily = CleanFont,
                        fontSize = 15.sp,
                        color = UIDarkText
                    )
                    pastTenants.reversed().forEach { past ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(past.tenantName, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 14.sp, color = UIDarkText)
                                Text("📞 ${past.tenantPhone}", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                                Text("🗓 ${past.moveInDate} → ${past.vacateDate} (${past.totalDaysStayed} days)", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)
                                Text("💰 Total Paid: ₹${"%,.2f".format(past.totalPaid)}", fontSize = 12.sp, fontFamily = CleanFont, fontWeight = FontWeight.Bold, color = UIGreenSuccess)
                            }
                        }
                    }
                    Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                }

                Text(
                    text = "Billing Records",
                    fontWeight = FontWeight.Bold,
                    fontFamily = CleanFont,
                    fontSize = 15.sp,
                    color = UIDarkText
                )
                if (bills.isEmpty()) {
                    Text("No billing history found for this room.", color = UIMutedText, fontFamily = CleanFont, fontSize = 13.sp)
                } else {
                    bills.reversed().forEach { bill ->
                        val units = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
                        val totalBill = bill.baseRent + (units * bill.electricityRate) + bill.maintenanceCharge + bill.previousDueCarryover
                        val billPaymentDate = remember(bill.timestamp) {
                            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(bill.timestamp))
                        }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(bill.monthYear, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 14.sp)
                                    Text("Paid: ₹${bill.amountPaid}", color = UIGreenSuccess, fontFamily = CleanFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("Paid on: $billPaymentDate  •  Units: $units (Total: ₹$totalBill)", fontSize = 11.sp, fontFamily = CleanFont, color = UIMutedText)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text("Close", fontFamily = CleanFont, fontSize = 14.sp)
            }
        }
    )
}

