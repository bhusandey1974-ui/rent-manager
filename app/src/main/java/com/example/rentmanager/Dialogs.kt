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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNum: String, rent: Double, elecRate: Double) -> Unit
) {
    var roomNum by remember { mutableStateOf("") }
    var rentStr by remember { mutableStateOf("") }
    var elecStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Property Unit", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomNum,
                    onValueChange = { roomNum = it },
                    label = { Text("Property / Room Number", fontFamily = FontFamily.SansSerif) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rentStr,
                    onValueChange = { rentStr = it },
                    label = { Text("Base Monthly Rent (₹)", fontFamily = FontFamily.SansSerif) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = elecStr,
                    onValueChange = { elecStr = it },
                    label = { Text("Electricity Rate per Unit (₹)", fontFamily = FontFamily.SansSerif) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rent = rentStr.toDoubleOrNull() ?: 0.0
                    val elec = elecStr.toDoubleOrNull() ?: 0.0
                    if (roomNum.isNotBlank()) onConfirm(roomNum.trim(), rent, elec)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Property", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
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
    roomNumber: String,
    currentMeterReading: Double,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, aadhaar: String, address: String, deposit: Double, meter: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var depositStr by remember { mutableStateOf("") }
    var meterStr by remember { mutableStateOf(currentMeterReading.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Tenant • Unit $roomNumber", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *", fontFamily = FontFamily.SansSerif) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number *", fontFamily = FontFamily.SansSerif) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = aadhaar,
                        onValueChange = { aadhaar = it },
                        label = { Text("Aadhaar / ID", fontFamily = FontFamily.SansSerif) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address", fontFamily = FontFamily.SansSerif) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = depositStr,
                        onValueChange = { depositStr = it },
                        label = { Text("Security Deposit (₹)", fontFamily = FontFamily.SansSerif) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = meterStr,
                        onValueChange = { meterStr = it },
                        label = { Text("Initial Meter Reading", fontFamily = FontFamily.SansSerif) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val d = depositStr.toDoubleOrNull() ?: 0.0
                        val m = meterStr.toDoubleOrNull() ?: currentMeterReading
                        onConfirm(name.trim(), phone.trim(), aadhaar.trim(), address.trim(), d, m)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Assign", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = FontFamily.SansSerif)
            }
        }
    )
}
