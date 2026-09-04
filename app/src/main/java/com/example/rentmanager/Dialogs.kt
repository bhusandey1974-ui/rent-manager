package com.example.rentmanager.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rentmanager.AppColors
import com.example.rentmanager.ReceiptFormatter
import com.example.rentmanager.Room
import com.example.rentmanager.Tenant
import java.util.Locale

@Composable
fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.AzureContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Apartment,
                                contentDescription = null,
                                tint = AppColors.AzurePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add New Property",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = AppColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Property / Building Name") },
                    placeholder = { Text("e.g. Green Heights") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location") },
                    placeholder = { Text("e.g. Street 4, Sector 2") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle)
                    ) {
                        Text("Cancel", color = AppColors.TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) onConfirm(name, address)
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Create Property")
                    }
                }
            }
        }
    }
}
@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNumber: String, baseRent: Double, rate: Double, initialReading: Double) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var baseRent by remember { mutableStateOf("") }
    var electricityRate by remember { mutableStateOf("10") }
    var initialReading by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.AzureContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoorFront,
                                contentDescription = null,
                                tint = AppColors.AzurePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add New Room",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = AppColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room / Flat Number") },
                    placeholder = { Text("e.g. 101, B-4") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = baseRent,
                    onValueChange = { baseRent = it },
                    label = { Text("Base Rent (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = electricityRate,
                        onValueChange = { electricityRate = it },
                        label = { Text("Elec Rate / Unit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = initialReading,
                        onValueChange = { initialReading = it },
                        label = { Text("Meter Start") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle)
                    ) {
                        Text("Cancel", color = AppColors.TextSecondary)
                    }

                    Button(
                        onClick = {
                            val rentVal = baseRent.toDoubleOrNull() ?: 0.0
                            val rateVal = electricityRate.toDoubleOrNull() ?: 10.0
                            val startVal = initialReading.toDoubleOrNull() ?: 0.0
                            if (roomNumber.isNotBlank()) {
                                onConfirm(roomNumber, rentVal, rateVal, startVal)
                            }
                        },
                        enabled = roomNumber.isNotBlank() && baseRent.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save Room")
                    }
                }
            }
        }
    }
}

@Composable
fun AssignTenantDialog(
    roomNumber: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, deposit: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.AzureContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = AppColors.AzurePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Assign Room $roomNumber",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = AppColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Name") },
                    placeholder = { Text("Full Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number (for WhatsApp)") },
                    placeholder = { Text("10-digit number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = deposit,
                    onValueChange = { deposit = it },
                    label = { Text("Security Deposit (₹)") },
                    placeholder = { Text("Optional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle)
                    ) {
                        Text("Cancel", color = AppColors.TextSecondary)
                    }

                    Button(
                        onClick = {
                            val depVal = deposit.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) onConfirm(name, phone, depVal)
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Assign Tenant")
                    }
                }
            }
        }
    }
}
@Composable
fun LodgeBillDialog(
    context: Context,
    room: Room,
    tenant: Tenant,
    previousReading: Double,
    priorDueOrAdvance: Double,
    onDismiss: () -> Unit,
    onBillLodged: (billingPeriod: String, currentReading: Double, maintenanceAmount: Double, amountPaid: Double, paymentMode: String) -> Unit
) {
    var billingPeriod by remember { mutableStateOf("Current Month") }
    var currentReadingStr by remember { mutableStateOf("") }
    var maintenanceStr by remember { mutableStateOf("0") }
    var amountPaidStr by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }

    val currReading = currentReadingStr.toDoubleOrNull() ?: previousReading
    val units = (currReading - previousReading).coerceAtLeast(0.0)
    val elecAmount = units * room.electricityRate
    val maintAmount = maintenanceStr.toDoubleOrNull() ?: 0.0
    val totalPayable = room.baseRent + elecAmount + maintAmount + priorDueOrAdvance
    val amountPaid = amountPaidStr.toDoubleOrNull() ?: 0.0
    val remainingDue = totalPayable - amountPaid

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.AzureContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = AppColors.AzurePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Lodge Bill - Room ${room.roomNumber}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = "Tenant: ${tenant.name}",
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = AppColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = billingPeriod,
                    onValueChange = { billingPeriod = it },
                    label = { Text("Billing Period / Month") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = previousReading.toString(),
                        onValueChange = {},
                        label = { Text("Previous Reading") },
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = currentReadingStr,
                        onValueChange = { currentReadingStr = it },
                        label = { Text("Current Reading") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = maintenanceStr,
                        onValueChange = { maintenanceStr = it },
                        label = { Text("Maintenance / Other (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = amountPaidStr,
                        onValueChange = { amountPaidStr = it },
                        label = { Text("Amount Paid (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.AzureContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rent + Elec (${units}u) + Maint:", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", room.baseRent + elecAmount + maintAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (priorDueOrAdvance != 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (priorDueOrAdvance > 0) "Previous Due:" else "Previous Advance:", fontSize = 12.sp, color = AppColors.TextSecondary)
                                Text("₹${String.format(Locale.ENGLISH, "%.2f", priorDueOrAdvance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = AppColors.AzureBorder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payable:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", totalPayable)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.AzurePrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remaining Due:", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", remainingDue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (remainingDue > 0) AppColors.AmberWarning else AppColors.EmeraldSuccess)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onBillLodged(billingPeriod, currReading, maintAmount, amountPaid, paymentMode)
                        val receiptMsg = ReceiptFormatter.formatReceipt(
                            tenantName = tenant.name,
                            roomNumber = room.roomNumber,
                            billingPeriod = billingPeriod,
                            paymentDateMillis = System.currentTimeMillis(),
                            previousReading = previousReading,
                            currentReading = currReading,
                            unitsConsumed = units,
                            ratePerUnit = room.electricityRate,
                            totalElectricity = elecAmount,
                            baseRent = room.baseRent,
                            totalAmount = totalPayable,
                            amountPaid = amountPaid,
                            paymentMode = paymentMode,
                            remainingDue = remainingDue
                        )
                        ReceiptFormatter.sendViaWhatsApp(context, tenant.phoneNumber, receiptMsg)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.WhatsAppGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save & Send WhatsApp Receipt", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun EditRoomDialog(
    room: Room,
    onDismiss: () -> Unit,
    onConfirm: (roomNumber: String, baseRent: Double, rate: Double, initialReading: Double) -> Unit
) {
    var roomNumber by remember { mutableStateOf(room.roomNumber) }
    var baseRent by remember { mutableStateOf(if (room.baseRent > 0) room.baseRent.toString() else "") }
    var electricityRate by remember { mutableStateOf(room.electricityRate.toString()) }
    var initialReading by remember { mutableStateOf(room.initialMeterReading.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.AzureContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoorFront,
                                contentDescription = null,
                                tint = AppColors.AzurePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Edit Room ${room.roomNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = AppColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room / Flat Number") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = baseRent,
                    onValueChange = { baseRent = it },
                    label = { Text("Base Rent (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = electricityRate,
                        onValueChange = { electricityRate = it },
                        label = { Text("Elec Rate / Unit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = initialReading,
                        onValueChange = { initialReading = it },
                        label = { Text("Meter Start") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle)
                    ) {
                        Text("Cancel", color = AppColors.TextSecondary)
                    }

                    Button(
                        onClick = {
                            val rentVal = baseRent.toDoubleOrNull() ?: room.baseRent
                            val rateVal = electricityRate.toDoubleOrNull() ?: room.electricityRate
                            val startVal = initialReading.toDoubleOrNull() ?: room.initialMeterReading
                            if (roomNumber.isNotBlank()) {
                                onConfirm(roomNumber, rentVal, rateVal, startVal)
                            }
                        },
                        enabled = roomNumber.isNotBlank() && baseRent.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
@Composable
fun EditTenantDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, deposit: Double) -> Unit
) {
    var name by remember { mutableStateOf(tenant.name) }
    var phone by remember { mutableStateOf(tenant.phoneNumber) }
    var deposit by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.AzureContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = AppColors.AzurePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Edit Tenant Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = AppColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number (for WhatsApp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = deposit,
                    onValueChange = { deposit = it },
                    label = { Text("Security Deposit (₹)") },
                    placeholder = { Text("Optional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle)
                    ) {
                        Text("Cancel", color = AppColors.TextSecondary)
                    }

                    Button(
                        onClick = {
                            val depVal = deposit.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) onConfirm(name, phone, depVal)
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
@Composable
fun DeleteConfirmationDialog(
    title: String = "Delete Room",
    message: String = "Are you sure you want to delete this room? This action cannot be undone.",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.CrimsonAlert
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle)
                    ) {
                        Text("Cancel", color = AppColors.TextSecondary)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.CrimsonAlert,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
