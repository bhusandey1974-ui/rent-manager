package com.example.rentmanager.ui.components

import android.content.Context
import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.example.rentmanager.AppColors
import com.example.rentmanager.UIRedDanger
import com.example.rentmanager.UIGreenSuccess
import com.example.rentmanager.RentViewModel
import com.example.rentmanager.ReceiptFormatter
import com.example.rentmanager.Room
import com.example.rentmanager.Tenant
import com.example.rentmanager.TenantHistorySummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AssignTenantDialog(
    roomNumber: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, deposit: Double, aadhaar: String, address: String, moveInDateMillis: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var moveInDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

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

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Name *") },
                    placeholder = { Text("Full Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number (for WhatsApp) *") },
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

                Spacer(modifier = Modifier.height(8.dp))

                // NEW: Move-In Date field (read-only text field that opens a date picker)
                OutlinedTextField(
                    value = dateFormatter.format(java.util.Date(moveInDateMillis)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Move-In Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "Pick move-in date",
                                tint = AppColors.AzurePrimary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { if (it.length <= 12) aadhaar = it },
                    label = { Text("Aadhaar Number (Optional)") },
                    placeholder = { Text("Enter 12 digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Permanent Address (Optional)") },
                    placeholder = { Text("Village/Town, District, State") },
                    singleLine = false,
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(18.dp))

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
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onConfirm(name, phone, depVal, aadhaar, address, moveInDateMillis)
                            }
                        },
                        enabled = name.isNotBlank() && phone.isNotBlank(),
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

    // Date picker dialog
    if (showDatePicker) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = moveInDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { moveInDateMillis = it }
                showDatePicker = false
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDatePicker = false }) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
  }
}
@Composable
fun VacateSettlementDialog(
    tenantName: String,
    settlementAmount: Double, // positive = tenant owes you, negative = you owe tenant (advance)
    onDismiss: () -> Unit,
    onConfirm: (note: String) -> Unit
) {
    var note by remember { mutableStateOf("") }

    val isAdvance = settlementAmount < 0.0
    val displayAmount = kotlin.math.abs(settlementAmount)

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
                                .background(
                                    if (isAdvance) AppColors.AmberWarning.copy(alpha = 0.15f)
                                    else AppColors.EmeraldSuccess.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = if (isAdvance) AppColors.AmberWarning else AppColors.EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Vacate $tenantName",
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

                if (displayAmount > 0.0) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAdvance) AppColors.AmberWarning.copy(alpha = 0.1f)
                                             else AppColors.EmeraldSuccess.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isAdvance) "Refund Due to Tenant" else "Tenant Still Owes You",
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${String.format(Locale.ENGLISH, "%.2f", displayAmount)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isAdvance) AppColors.AmberWarning else AppColors.EmeraldSuccess
                            )
                            if (isAdvance) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Please refund this amount to the tenant before confirming.",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    Text(
                        text = "No pending balance for this tenant.",
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Settlement Note (Optional)") },
                    placeholder = { Text(if (isAdvance) "e.g. Refunded ₹${displayAmount.toInt()} in cash" else "e.g. Cleared final dues") },
                    singleLine = false,
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

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
                        onClick = { onConfirm(note) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Confirm Vacate")
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
    var billingPeriod by remember { 
    mutableStateOf(
        java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
    )
    }
    var currentReadingStr by remember { mutableStateOf("") }
    var maintenanceStr by remember { mutableStateOf("0") }
    var amountPaidStr by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }

    val currReading = currentReadingStr.toDoubleOrNull() ?: previousReading
    val units = (currReading - previousReading).coerceAtLeast(0.0)
    val elecAmount = units * room.electricityRate
    val maintAmount = maintenanceStr.toDoubleOrNull() ?: 0.0
    val grossPayable = room.baseRent + elecAmount + maintAmount + priorDueOrAdvance
    val amountPaid = amountPaidStr.toDoubleOrNull() ?: 0.0
    val remainingDue = grossPayable - amountPaid

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
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

                // Section 1: Period
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

                // Section 2: Meter Readings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = previousReading.toString(),
                        onValueChange = {},
                        label = { Text("Prev Meter") },
                        enabled = false,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = AppColors.BorderSubtle,
                            disabledTextColor = AppColors.TextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = currentReadingStr,
                        onValueChange = { currentReadingStr = it },
                        label = { Text("Curr Meter") },
                        placeholder = { Text(previousReading.toString()) },
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

                // Section 3: Extra Charges
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 4: Live Calculated Breakdown
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
                            Text("Base Rent:", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", room.baseRent)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Electricity (${units}u @ ₹${room.electricityRate}):", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", elecAmount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (maintAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Maintenance:", fontSize = 12.sp, color = AppColors.TextSecondary)
                                Text("₹${String.format(Locale.ENGLISH, "%.2f", maintAmount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (priorDueOrAdvance != 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (priorDueOrAdvance > 0) "Previous Due:" else "Previous Advance:", fontSize = 12.sp, color = AppColors.TextSecondary)
                                Text("₹${String.format(Locale.ENGLISH, "%.2f", priorDueOrAdvance)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = AppColors.AzureBorder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Payable:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", grossPayable)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.AzurePrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 5: Payment Received
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = amountPaidStr,
                        onValueChange = { amountPaidStr = it },
                        label = { Text("Amount Paid (₹)") },
                        placeholder = { Text(grossPayable.toInt().toString()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.weight(1.3f)
                    )

                    OutlinedTextField(
                        value = paymentMode,
                        onValueChange = { paymentMode = it },
                        label = { Text("Mode") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remaining Balance:", fontSize = 12.sp, color = AppColors.TextSecondary)
                    Text(
                        text = if (remainingDue >= 0) "₹${String.format(Locale.ENGLISH, "%.2f", remainingDue)} Due"
                               else "₹${String.format(Locale.ENGLISH, "%.2f", -remainingDue)} Advance",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingDue > 0) AppColors.AmberWarning else AppColors.EmeraldSuccess
                    )
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
                            totalAmount = grossPayable,
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
    onConfirm: (name: String, phone: String, deposit: Double, aadhaar: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf(tenant.name) }
    var phone by remember { mutableStateOf(tenant.phoneNumber) }
    var aadhaar by remember { mutableStateOf(tenant.aadhaarNumber) }
    var address by remember { mutableStateOf(tenant.permanentAddress) }
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

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tenant Name *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number (for WhatsApp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { if (it.length <= 12) aadhaar = it },
                    label = { Text("Aadhaar Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Permanent Address") },
                    singleLine = false,
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.AzurePrimary,
                        unfocusedBorderColor = AppColors.BorderSubtle,
                        focusedLabelColor = AppColors.AzurePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(18.dp))

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
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onConfirm(name, phone, depVal, aadhaar, address)
                            }
                        },
                        enabled = name.isNotBlank() && phone.isNotBlank(),
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
fun RoomHistoryDialog(
    room: Room,
    historySummaries: List<TenantHistorySummary>,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .height(620.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
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
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = AppColors.AzurePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Room ${room.roomNumber} History",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = "Tenants & Rate Audit Log",
                                fontSize = 11.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = AppColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rate Change History Section
                    if (room.rateHistory.isNotEmpty()) {
                        item {
                            Text(
                                text = "RATE MODIFICATIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.AzureContainer.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, AppColors.AzureBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    room.rateHistory.reversed().forEach { rateLog ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Rounded.TrendingUp,
                                                    contentDescription = null,
                                                    tint = AppColors.AzurePrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = dateFormat.format(Date(rateLog.timestamp)),
                                                    fontSize = 11.sp,
                                                    color = AppColors.TextSecondary
                                                )
                                            }
                                            Text(
                                                text = "Rent: ₹${rateLog.newRent.toInt()} | Elec: ₹${rateLog.newElectricityRate}/u",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Tenant History Section
                    item {
                        Text(
                            text = "TENANCY RECORDS (${historySummaries.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMuted,
                            letterSpacing = 1.sp
                        )
                    }

                    if (historySummaries.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tenant history recorded for this room.", fontSize = 13.sp, color = AppColors.TextMuted)
                            }
                        }
                    } else {
                        items(historySummaries) { itemSummary ->
                            val t = itemSummary.tenant
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (t.isCurrent) AppColors.SurfaceWhite else AppColors.ScaffoldBackground
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (t.isCurrent) AppColors.AzurePrimary.copy(alpha = 0.5f) else AppColors.BorderSubtle
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Row 1: Name & Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = t.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.TextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (t.isCurrent) AppColors.EmeraldSuccess.copy(alpha = 0.15f) else AppColors.TextMuted.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (t.isCurrent) "ACTIVE" else "VACATED",
                                                color = if (t.isCurrent) AppColors.EmeraldSuccess else AppColors.TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Row 2: Dates & Duration
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = AppColors.AzurePrimary, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val moveInStr = dateFormat.format(Date(t.moveInDate))
                                        val moveOutStr = t.moveOutDate?.let { dateFormat.format(Date(it)) } ?: "Present"
                                        Text(
                                            text = "$moveInStr → $moveOutStr (${itemSummary.daysStayed} days)",
                                            fontSize = 11.sp,
                                            color = AppColors.TextSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Row 3: Identity info
                                    Text("Phone: ${t.phoneNumber}", fontSize = 12.sp, color = AppColors.TextPrimary)
                                    if (t.aadhaarNumber.isNotBlank()) {
                                        Text("Aadhaar: [Aadhaar Redacted]", fontSize = 12.sp, color = AppColors.TextPrimary)
                                    }
                                    if (t.permanentAddress.isNotBlank()) {
                                        Text("Address: ${t.permanentAddress}", fontSize = 12.sp, color = AppColors.TextSecondary, maxLines = 2)
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = AppColors.BorderSubtle)

                                    // Row 4: Financial Summary for this tenancy
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Rent Paid", fontSize = 10.sp, color = AppColors.TextSecondary)
                                            Text("₹${String.format(Locale.ENGLISH, "%.0f", itemSummary.totalRentCollected)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                        }
                                        Column {
                                            Text("Elec Paid", fontSize = 10.sp, color = AppColors.TextSecondary)
                                            Text("₹${String.format(Locale.ENGLISH, "%.0f", itemSummary.totalElectricityCollected)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Total Collected", fontSize = 10.sp, color = AppColors.TextSecondary)
                                            Text("₹${String.format(Locale.ENGLISH, "%.0f", itemSummary.totalMoneyCollected)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.AzurePrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AppColors.BorderSubtle)
                ) {
                    Text("Close", color = AppColors.TextPrimary)
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
@Composable
fun SettingsDialog(
    vm: RentViewModel,
    onDismiss: () -> Unit,
    onSignOutSuccess: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings & Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppColors.TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentUser?.email ?: "Local Offline Mode",
                        fontSize = 14.sp,
                        color = AppColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (currentUser != null) "Cloud sync enabled" else "No cloud sync",
                    fontSize = 12.sp,
                    color = if (currentUser != null) UIGreenSuccess else AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))
                Divider()
                Spacer(modifier = Modifier.height(20.dp))

                // Sign out
                Button(
                    onClick = {
                        auth.signOut()
                        onSignOutSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.AzurePrimary
                    )
                ) {
                    Text(if (currentUser != null) "Sign Out" else "Exit Guest Mode")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Danger zone
                Text(
                    text = "Danger Zone",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = UIRedDanger
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, UIRedDanger)
                ) {
                    Text("Delete All Property Data", color = UIRedDanger)
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onConfirm = {
                vm.clearAllData(onComplete = {})
                showDeleteConfirmation = false
                onDismiss()
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Delete All Data?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = UIRedDanger
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This will permanently delete all properties, rooms, tenants, and bills from this device and the cloud. This cannot be undone.",
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = UIRedDanger)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
