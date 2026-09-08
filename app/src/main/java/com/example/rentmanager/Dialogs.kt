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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.rentmanager.Bill
import com.example.rentmanager.BillingConvention
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
fun RoomWiseBreakdownDialog(
    title: String,
    items: List<com.example.rentmanager.RoomWiseAmount>,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    val total = items.sumOf { it.amount }

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
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = AppColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Room-wise breakdown",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data for this category.",
                            fontSize = 13.sp,
                            color = AppColors.TextMuted
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                    ) {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Room ${item.roomNumber}",
                                    fontSize = 14.sp,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = "₹${String.format(Locale.ENGLISH, "%.0f", item.amount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentColor
                                )
                            }
                            Divider(color = AppColors.BorderSubtle)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%.0f", total)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
fun LodgeBillDialog(
    context: Context,
    room: Room,
    tenant: Tenant,
    previousReading: Double,
    priorDueOrAdvance: Double,
    suggestedBillingPeriod: String,
    outstandingMonths: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onBillLodged: (billingPeriod: String, currentReading: Double, maintenanceAmount: Double, amountPaid: Double, paymentMode: String) -> Bill
) {
    var billingPeriod by remember { mutableStateOf(suggestedBillingPeriod) }
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
                    
                    
