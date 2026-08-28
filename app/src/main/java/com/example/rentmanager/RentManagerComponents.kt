package com.example.rentmanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryView(history: List<TenantHistoryRecord>) {
    if (history.isEmpty()) {
        EmptyStateView(
            title = "No Past Tenant Records",
            subtitle = "When tenants checkout, their complete stay details and lifetime payment histories will appear here.",
            buttonText = null,
            onAction = {}
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            Text("Stayed: ${item.formattedDuration}", fontSize = 12.sp, color = BrandBlue, fontWeight = FontWeight.SemiBold)
                        }
                        Text("📞 ${item.phone} • Aadhaar: ${item.aadhaarNo.ifBlank { "N/A" }}", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid: ₹${item.totalRentPaidLifetime}", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            Text("Deposit Refunded: ₹${item.depositRefunded}", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryAnalyticsView(rooms: List<RoomUnit>, tenants: List<Tenant>, bills: List<BillRecord>) {
    val totalRooms = rooms.size
    val occupiedRooms = rooms.count { !it.isVacant }
    val totalCollected = bills.sumOf { it.amountPaid }
    val totalPending = bills.filter { !it.isPaid }.sumOf { it.remainingDue }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Overview Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Occupancy", "$occupiedRooms / $totalRooms", BrandBlue, Modifier.weight(1f))
            StatCard("Active Tenants", "${tenants.size}", SuccessGreen, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total Collected", "₹$totalCollected", SuccessGreen, Modifier.weight(1f))
            StatCard("Pending Dues", "₹$totalPending", DangerRed, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun AddPropertyDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Property", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Property/Building Name") }, singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, singleLine = true)
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, address, city, owner, phone) }) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddRoomDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, Double) -> Unit) {
    var roomNo by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("Room") }
    var rent by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Unit / Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = roomNo, onValueChange = { roomNo = it }, label = { Text("Room/Flat No (e.g. 101)") }, singleLine = true)
                OutlinedTextField(value = roomType, onValueChange = { roomType = it }, label = { Text("Type (Room / 1BHK / Flat)") }, singleLine = true)
                OutlinedTextField(value = rent, onValueChange = { rent = it }, label = { Text("Monthly Base Rent (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Elec Rate / Unit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (roomNo.isNotBlank() && rent.isNotBlank()) {
                    onConfirm(roomNo, roomType, rent.toDoubleOrNull() ?: 0.0, rate.toDoubleOrNull() ?: 0.0)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditRoomDialog(room: RoomUnit, onDismiss: () -> Unit, onConfirm: (String, Double, Double) -> Unit) {
    var roomNo by remember { mutableStateOf(room.roomNumber) }
    var rent by remember { mutableStateOf(room.baseRent.toString()) }
    var rate by remember { mutableStateOf(room.electricityRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Room ${room.roomNumber}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = roomNo, onValueChange = { roomNo = it }, label = { Text("Room Number") }, singleLine = true)
                OutlinedTextField(value = rent, onValueChange = { rent = it }, label = { Text("Base Rent (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Elec Rate / Unit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(roomNo, rent.toDoubleOrNull() ?: room.baseRent, rate.toDoubleOrNull() ?: room.electricityRate)
            }) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AssignTenantDialog(room: RoomUnit, todayDate: String, onDismiss: () -> Unit, onConfirm: (String, String, String, String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var moveInDate by remember { mutableStateOf(todayDate) }
    var deposit by remember { mutableStateOf("") }
    var meterReading by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Tenant to ${room.roomNumber}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tenant Full Name") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar / ID (Optional)") }, singleLine = true)
                OutlinedTextField(value = moveInDate, onValueChange = { moveInDate = it }, label = { Text("Move-in Date") }, singleLine = true)
                OutlinedTextField(value = deposit, onValueChange = { deposit = it }, label = { Text("Security Deposit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = meterReading, onValueChange = { meterReading = it }, label = { Text("Starting Meter Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    onConfirm(name, phone, aadhaar, moveInDate, deposit.toDoubleOrNull() ?: 0.0, meterReading.toDoubleOrNull() ?: 0.0)
                }
            }) { Text("Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
    onConfirm: (String, Double, Double, Double, String) -> Unit
) {
    var month by remember { mutableStateOf(defaultMonth) }
    var curReading by remember { mutableStateOf("") }
    var maintenance by remember { mutableStateOf("0") }
    var amountPaid by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }

    val curUnitNum = curReading.toDoubleOrNull() ?: prevReading
    val unitsUsed = (curUnitNum - prevReading).coerceAtLeast(0.0)
    val elecCost = unitsUsed * room.electricityRate
    val maintNum = maintenance.toDoubleOrNull() ?: 0.0
    val totalBill = room.baseRent + elecCost + maintNum + previousDue

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lodge Bill for ${tenant.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Billing Month") }, singleLine = true)
                Text("Previous Meter Reading: $prevReading", fontSize = 12.sp, color = TextMuted)
                OutlinedTextField(value = curReading, onValueChange = { curReading = it }, label = { Text("Current Meter Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = maintenance, onValueChange = { maintenance = it }, label = { Text("Maintenance / Other (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)

                Surface(
                    color = PageBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Units Used: $unitsUsed (₹$elecCost)", fontSize = 12.sp, color = TextDark)
                        Text("Previous Due: ₹$previousDue", fontSize = 12.sp, color = DangerRed)
                        Text("Total Amount: ₹$totalBill", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandBlueDark)
                    }
                }

                OutlinedTextField(value = amountPaid, onValueChange = { amountPaid = it }, label = { Text("Amount Paid Now (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = paymentMode, onValueChange = { paymentMode = it }, label = { Text("Mode (Cash / UPI / Bank)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(month, curUnitNum, maintNum, amountPaid.toDoubleOrNull() ?: 0.0, paymentMode)
            }) { Text("Save & Send Receipt") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CheckoutTenantDialog(tenant: Tenant, todayDate: String, onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var moveOutDate by remember { mutableStateOf(todayDate) }
    var refund by remember { mutableStateOf(tenant.securityDeposit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checkout ${tenant.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Deposit Collected: ₹${tenant.securityDeposit}", fontSize = 13.sp, color = TextDark)
                OutlinedTextField(value = moveOutDate, onValueChange = { moveOutDate = it }, label = { Text("Move-out Date") }, singleLine = true)
                OutlinedTextField(value = refund, onValueChange = { refund = it }, label = { Text("Deposit Refund Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(moveOutDate, refund.toDoubleOrNull() ?: 0.0) },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
            ) { Text("Confirm Checkout") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EmptyStateView(title: String, subtitle: String, buttonText: String?, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Apartment, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
        Spacer(modifier = Modifier.height(6.dp))
        Text(subtitle, fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center)
        if (buttonText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) {
                Text(buttonText)
            }
        }
    }
}

fun shareToWhatsApp(context: Context, phone: String, message: String) {
    try {
        val cleanPhone = phone.replace("+", "").replace(" ", "")
        val formattedNumber = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(message)}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
    }
}

