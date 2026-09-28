package com.example.rentmanager.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import com.example.rentmanager.RentViewModel
import com.example.rentmanager.Withdrawal
import java.text.SimpleDateFormat
import java.util.Locale

private fun withdrawalMoney(v: Double): String =
    if (v % 1.0 == 0.0) String.format(Locale.ENGLISH, "%,.0f", v)
    else String.format(Locale.ENGLISH, "%,.2f", v)

@Composable
fun WithdrawalsCard(
    vm: RentViewModel,
    totalCollected: Double,
    forCurrentYearOnly: Boolean
) {
    val allWithdrawals by vm.withdrawals.collectAsState()
    val scoped = remember(allWithdrawals, forCurrentYearOnly) {
        vm.getWithdrawalsList(forCurrentYearOnly)
    }
    val totalWithdrawn = remember(scoped) { scoped.sumOf { it.amount } }
    val netCash = totalCollected - totalWithdrawn

    var showAdd by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        border = BorderStroke(1.dp, AppColors.BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NET CASH AVAILABLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "₹${withdrawalMoney(netCash)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netCash < 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "WITHDRAWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "₹${withdrawalMoney(totalWithdrawn)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAdd = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.AzurePrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text(" Withdraw")
                }
                OutlinedButton(
                    onClick = { showHistory = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("History (${scoped.size})")
                }
            }
        }
    }

    if (showAdd) {
        AddWithdrawalDialog(
            onDismiss = { showAdd = false },
            onSave = { amount, recipient, purpose ->
                vm.addWithdrawal(amount, recipient, purpose)
                showAdd = false
            }
        )
    }

    if (showHistory) {
        WithdrawalHistoryDialog(
            withdrawals = scoped,
            onDelete = { id -> vm.deleteWithdrawal(id) },
            onDismiss = { showHistory = false }
        )
    }
}

@Composable
private fun AddWithdrawalDialog(
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    val amount = amountText.toDoubleOrNull()
    val canSave = amount != null && amount > 0.0 && recipient.isNotBlank() && purpose.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Withdrawal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Given to (whom)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Reason (why)") }
                )
                Text(
                    text = "Date and time are recorded automatically.",
                    fontSize = 11.sp,
                    color = AppColors.TextMuted
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (canSave) onSave(amount ?: 0.0, recipient, purpose) },
                enabled = canSave
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun WithdrawalHistoryDialog(
    withdrawals: List<Withdrawal>,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH) }
    var pendingDelete by remember { mutableStateOf<Withdrawal?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdrawal History", fontWeight = FontWeight.Bold) },
        text = {
            if (withdrawals.isEmpty()) {
                Text("No withdrawals recorded.", color = AppColors.TextMuted, fontSize = 13.sp)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    items(withdrawals, key = { it.id }) { w ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "To: ${w.recipient}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = w.purpose,
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                                Text(
                                    text = dateFmt.format(java.util.Date(w.timestamp)),
                                    fontSize = 11.sp,
                                    color = AppColors.TextMuted
                                )
                            }
                            Text(
                                text = "₹${withdrawalMoney(w.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AppColors.TextPrimary
                            )
                            IconButton(onClick = { pendingDelete = w }) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Delete withdrawal",
                                    tint = Color(0xFFD32F2F)
                                )
                            }
                        }
                        Divider(color = AppColors.BorderSubtle)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )

    pendingDelete?.let { w ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this withdrawal?") },
            text = { Text("₹${withdrawalMoney(w.amount)} to ${w.recipient}. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(w.id)
                    pendingDelete = null
                }) { Text("Delete", color = Color(0xFFD32F2F)) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}
