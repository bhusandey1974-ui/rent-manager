package com.example.rentmanager

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// ==========================================
// 1. ADD ROOM DIALOG
// ==========================================
@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNo: String, unitType: String, rent: Double, rate: Double) -> Unit
) {
    var roomNo by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10.0") }
    var unitType by remember { mutableStateOf("1RK") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, UICardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add New Unit", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIDarkText)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = roomNo,
                    onValueChange = { roomNo = it },
                    label = { Text("Room / Flat No (e.g. 101, A2)", fontFamily = CleanFont) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Base Rent (₹)", fontFamily = CleanFont) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate (₹/unit)", fontFamily = CleanFont) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontFamily = CleanFont, color = UIMutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val rNo = roomNo.trim()
                            val rVal = rent.toDoubleOrNull() ?: 0.0
                            val rateVal = rate.toDoubleOrNull() ?: 10.0
                            if (rNo.isNotEmpty() && rVal > 0) {
                                onConfirm(rNo, unitType, rVal, rateVal)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Unit", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. EDIT ROOM DIALOG
// ==========================================
@Composable
fun EditRoomDialog(
    room: RoomUnit,
    onDismiss: () -> Unit,
    onConfirm: (roomNo: String, rent: Double, rate: Double) -> Unit
) {
    var roomNo by remember { mutableStateOf(room.roomNumber) }
    var rent by remember { mutableStateOf(room.baseRent.toString()) }
    var rate by remember { mutableStateOf(room.electricityRate.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, UICardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Edit Room Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIDarkText)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = roomNo,
                    onValueChange = { roomNo = it },
                    label = { Text("Room / Flat No", fontFamily = CleanFont) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Base Rent (₹)", fontFamily = CleanFont) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Electricity Rate (₹/unit)", fontFamily = CleanFont) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontFamily = CleanFont, color = UIMutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val rNo = roomNo.trim()
                            val rVal = rent.toDoubleOrNull() ?: room.baseRent
                            val rateVal = rate.toDoubleOrNull() ?: room.electricityRate
                            if (rNo.isNotEmpty()) {
                                onConfirm(rNo, rVal, rateVal)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
// ==========================================
// 3. ASSIGN TENANT DIALOG
// ==========================================
@Composable
fun AssignTenantDialog(
    room: RoomUnit,
    defaultDate: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, aadhaar: String, moveIn: String, deposit: Double, initialMeter: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var moveIn by remember { mutableStateOf(defaultDate) }
    var deposit by remember { mutableStateOf("") }
    var initialMeter by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, UICardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Assign Tenant (Room ${room.roomNumber})", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIDarkText)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Name", fontFamily = CleanFont) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontFamily = CleanFont) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar / ID Card No", fontFamily = CleanFont) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = moveIn,
                    onValueChange = { moveIn = it },
                    label = { Text("Move-in Date", fontFamily = CleanFont) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deposit,
                        onValueChange = { deposit = it },
                        label = { Text("Deposit (₹)", fontFamily = CleanFont) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = initialMeter,
                        onValueChange = { initialMeter = it },
                        label = { Text("Initial Meter", fontFamily = CleanFont) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontFamily = CleanFont, color = UIMutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onConfirm(
                                    name.trim(),
                                    phone.trim(),
                                    aadhaar.trim(),
                                    moveIn.trim(),
                                    deposit.toDoubleOrNull() ?: 0.0,
                                    initialMeter.toDoubleOrNull() ?: 0.0
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Assign", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. TENANT DETAILS DIALOG
// ==========================================
@Composable
fun TenantDetailsDialog(
    tenant: Tenant,
    room: RoomUnit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onVacate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, UICardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tenant.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIDarkText)
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Room ${room.roomNumber}",
                            color = UIGreenSuccess,
                            fontFamily = CleanFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Phone: ${tenant.phone}", fontFamily = CleanFont, fontSize = 13.sp, color = UIDarkText)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Aadhaar / ID: ${if (tenant.aadhaarNumber.isBlank()) "N/A" else tenant.aadhaarNumber}", fontFamily = CleanFont, fontSize = 13.sp, color = UIDarkText)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Move-in Date: ${tenant.moveInDate}", fontFamily = CleanFont, fontSize = 13.sp, color = UIDarkText)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Security Deposit: ₹${"%,.2f".format(tenant.depositAmount)}", fontFamily = CleanFont, fontSize = 13.sp, color = UIDarkText)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Initial Meter Reading: ${tenant.initialMeterReading} units", fontFamily = CleanFont, fontSize = 13.sp, color = UIDarkText)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Edit", fontFamily = CleanFont)
                    }
                    Button(
                        onClick = onVacate,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = UIRedDanger),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Vacate", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. EDIT TENANT DIALOG
// ==========================================
@Composable
fun EditTenantDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, aadhaar: String) -> Unit
) {
    var name by remember { mutableStateOf(tenant.name) }
    var phone by remember { mutableStateOf(tenant.phone) }
    var aadhaar by remember { mutableStateOf(tenant.aadhaarNumber) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, UICardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Edit Tenant Info", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIDarkText)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", fontFamily = CleanFont) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontFamily = CleanFont) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar / ID Card", fontFamily = CleanFont) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontFamily = CleanFont, color = UIMutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onConfirm(name.trim(), phone.trim(), aadhaar.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
// ==========================================
// 6. GENERATE BILL DIALOG
// ==========================================
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
    var curReading by remember { mutableStateOf(prevReading.toString()) }
    var maintenance by remember { mutableStateOf("0") }
    var paidAmount by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }

    val cur = curReading.toDoubleOrNull() ?: prevReading
    val maint = maintenance.toDoubleOrNull() ?: 0.0
    val units = (cur - prevReading).coerceAtLeast(0.0)
    val elecCost = units * room.electricityRate
    val totalBill = room.baseRent + elecCost + maint + previousDue

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, UICardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Lodge Rent & Bill", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIDarkText)
                Text("${tenant.name} (Room ${room.roomNumber})", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Billing Month", fontFamily = CleanFont) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prevReading.toString(),
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Prev Unit", fontFamily = CleanFont) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = curReading,
                        onValueChange = { curReading = it },
                        label = { Text("Current Unit", fontFamily = CleanFont) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = maintenance,
                    onValueChange = { maintenance = it },
                    label = { Text("Maintenance / Other (₹)", fontFamily = CleanFont) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Elec: $units u @ ₹${room.electricityRate} = ₹${"%,.2f".format(elecCost)}", fontSize = 11.sp, fontFamily = CleanFont, color = UIMutedText)
                        if (previousDue > 0) {
                            Text("Previous Carryover Due: ₹${"%,.2f".format(previousDue)}", fontSize = 11.sp, fontFamily = CleanFont, color = Color(0xFFD97706))
                        }
                        Text("Total Amount: ₹${"%,.2f".format(totalBill)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIBluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = paidAmount,
                    onValueChange = { paidAmount = it },
                    label = { Text("Amount Paid Now (₹)", fontFamily = CleanFont) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cash", "UPI", "Bank").forEach { mode ->
                        val isSelected = paymentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) UIBluePrimary else Color(0xFFF1F5F9))
                                .clickable { paymentMode = mode }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                fontFamily = CleanFont,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else UIDarkText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontFamily = CleanFont, color = UIMutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val paid = paidAmount.toDoubleOrNull() ?: totalBill
                            onConfirm(month.trim(), cur, maint, paid, paymentMode)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lodge Bill", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
// ==========================================
// 7. CHECKOUT DIALOG
// ==========================================
@Composable
fun CheckoutDialog(
    tenant: Tenant,
    room: RoomUnit?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onConfirm: (vacateDate: String, refundAmount: Double) -> Unit
) {
    var vacateDate by remember { mutableStateOf(defaultDate) }
    var refundAmount by remember { mutableStateOf(tenant.depositAmount.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, UICardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Checkout Tenant", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIDarkText)
                Text("${tenant.name} (Room ${room?.roomNumber ?: ""})", fontSize = 12.sp, fontFamily = CleanFont, color = UIMutedText)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = vacateDate,
                    onValueChange = { vacateDate = it },
                    label = { Text("Vacate Date", fontFamily = CleanFont) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = refundAmount,
                    onValueChange = { refundAmount = it },
                    label = { Text("Deposit Returned (₹)", fontFamily = CleanFont) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontFamily = CleanFont, color = UIMutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val refund = refundAmount.toDoubleOrNull() ?: 0.0
                            onConfirm(vacateDate.trim(), refund)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UIRedDanger),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirm Vacate", fontFamily = CleanFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. ROOM HISTORY DIALOG
// ==========================================
@Composable
fun RoomHistoryDialog(
    room: RoomUnit,
    bills: List<BillRecord>,
    pastTenants: List<PastTenancyRecord>,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, UICardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Room ${room.roomNumber} History", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = CleanFont, color = UIDarkText)
                    IconButton(onClick = onClearHistory) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Room History", tint = UIRedDanger)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Bills (${bills.size})", fontFamily = CleanFont) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Past Tenants (${pastTenants.size})", fontFamily = CleanFont) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    if (bills.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No past bills found", color = UIMutedText, fontFamily = CleanFont)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(bills.reversed()) { bill ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(0.5.dp, UICardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(bill.monthYear, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 13.sp)
                                            Text("Paid: ₹${"%,.0f".format(bill.amountPaid)}", color = UIGreenSuccess, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 13.sp)
                                        }
                                        Text(
                                            "Rent: ₹${bill.baseRent}  •  Elec: ${bill.prevMeterReading}->${bill.currentMeterReading}",
                                            fontSize = 11.sp,
                                            fontFamily = CleanFont,
                                            color = UIMutedText
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (pastTenants.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No past tenancy records", color = UIMutedText, fontFamily = CleanFont)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(pastTenants.reversed()) { past ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(0.5.dp, UICardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(past.tenantName, fontWeight = FontWeight.Bold, fontFamily = CleanFont, fontSize = 13.sp)
                                        Text("📞 ${past.phone}", fontSize = 11.sp, fontFamily = CleanFont, color = UIMutedText)
                                        Text("${past.moveInDate}  ➔  ${past.vacateDate}", fontSize = 11.sp, fontFamily = CleanFont, color = UIMutedText)
                                        Text("Refund: ₹${"%,.0f".format(past.depositReturned)}", fontSize = 11.sp, fontWeight = FontWeight.Medium, fontFamily = CleanFont, color = UIBluePrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

