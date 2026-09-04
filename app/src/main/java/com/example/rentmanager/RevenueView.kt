package com.example.rentmanager.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import com.example.rentmanager.ReceiptFormatter
import com.example.rentmanager.RentViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueView(vm: RentViewModel) {
    val context = LocalContext.current
    val bills by vm.bills.collectAsState()

    var isCurrentYearOnly by remember { mutableStateOf(true) }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val revenueSummary = vm.getRevenueSummary(forCurrentYearOnly = isCurrentYearOnly)
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }

    val filteredBills = remember(bills, isCurrentYearOnly) {
        if (isCurrentYearOnly) {
            val cal = Calendar.getInstance()
            bills.filter { b ->
                cal.timeInMillis = b.timestamp
                cal.get(Calendar.YEAR) == currentYear
            }.sortedByDescending { it.timestamp }
        } else {
            bills.sortedByDescending { it.timestamp }
        }
    }

    Scaffold(
        containerColor = AppColors.ScaffoldBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header & Timeframe Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Financial Ledger",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isCurrentYearOnly,
                        onClick = { isCurrentYearOnly = true },
                        label = { Text("Year $currentYear", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.AzurePrimary,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = !isCurrentYearOnly,
                        onClick = { isCurrentYearOnly = false },
                        label = { Text("Lifetime", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.AzurePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Total Collections Hero Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.AzurePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCurrentYearOnly) "COLLECTIONS ($currentYear)" else "LIFETIME COLLECTIONS",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹${String.format(Locale.ENGLISH, "%,.2f", revenueSummary.totalCollected)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-metrics Grid (Rent, Electricity, Active Dues)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Rent Inflow",
                    amount = revenueSummary.rentCollected,
                    icon = Icons.Rounded.Home,
                    accentColor = AppColors.AzurePrimary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Electricity",
                    amount = revenueSummary.electricityCollected,
                    icon = Icons.Rounded.Bolt,
                    accentColor = AppColors.AmberWarning,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Active Dues",
                    amount = revenueSummary.activeDues,
                    icon = Icons.Rounded.WarningAmber,
                    accentColor = AppColors.CrimsonAlert,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "BILLING HISTORY & RECEIPTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
                        // Billing History List
            if (filteredBills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCurrentYearOnly) "No billing records found for $currentYear." else "No billing records found.",
                        color = AppColors.TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredBills, key = { it.id }) { bill ->
                        val tenant = vm.getTenantForBill(bill)
                        val room = vm.getRoomForBill(bill)

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                            border = BorderStroke(1.dp, AppColors.BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header: Room, Period, and WhatsApp Share Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.DoorFront,
                                            contentDescription = null,
                                            tint = AppColors.AzurePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Room ${room?.roomNumber ?: bill.roomId} • ${bill.billingPeriod}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.TextPrimary
                                        )
                                    }

                                    // WhatsApp Share Action
                                    if (tenant != null && tenant.phoneNumber.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                val receiptMsg = ReceiptFormatter.formatReceipt(
                                                    tenantName = tenant.name,
                                                    roomNumber = room?.roomNumber ?: bill.roomId,
                                                    billingPeriod = bill.billingPeriod,
                                                    paymentDateMillis = bill.timestamp,
                                                    previousReading = bill.previousReading,
                                                    currentReading = bill.currentReading,
                                                    unitsConsumed = bill.unitsConsumed,
                                                    ratePerUnit = bill.electricityRate,
                                                    totalElectricity = bill.electricityAmount,
                                                    baseRent = bill.baseRent,
                                                    totalAmount = bill.totalPayable,
                                                    amountPaid = bill.amountPaid,
                                                    paymentMode = bill.paymentMode,
                                                    remainingDue = bill.remainingDue
                                                )
                                                ReceiptFormatter.sendViaWhatsApp(context, tenant.phoneNumber, receiptMsg)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ReceiptLong,
                                                contentDescription = "Share WhatsApp Receipt",
                                                tint = AppColors.WhatsAppGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Date & Tenant Subheader
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tenant: ${tenant?.name ?: "Unknown"}",
                                        fontSize = 11.sp,
                                        color = AppColors.TextSecondary
                                    )
                                    Text(
                                        text = dateFormat.format(Date(bill.timestamp)),
                                        fontSize = 11.sp,
                                        color = AppColors.TextMuted
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = AppColors.BorderSubtle)

                                // Financial Breakdown Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Rent + Elec", fontSize = 10.sp, color = AppColors.TextSecondary)
                                        Text(
                                            text = "₹${bill.baseRent.toInt()} + ₹${bill.electricityAmount.toInt()}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AppColors.TextPrimary
                                        )
                                    }

                                    Column {
                                        Text("Paid (${bill.paymentMode})", fontSize = 10.sp, color = AppColors.TextSecondary)
                                        Text(
                                            text = "₹${String.format(Locale.ENGLISH, "%.0f", bill.amountPaid)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.EmeraldSuccess
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Status", fontSize = 10.sp, color = AppColors.TextSecondary)
                                        if (bill.remainingDue > 0) {
                                            Text(
                                                text = "₹${String.format(Locale.ENGLISH, "%.0f", bill.remainingDue)} Due",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.CrimsonAlert
                                            )
                                        } else {
                                            Text(
                                                text = "Settled",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.EmeraldSuccess
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        border = BorderStroke(1.dp, AppColors.BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "₹${String.format(Locale.ENGLISH, "%.0f", amount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
        }
    }
}
