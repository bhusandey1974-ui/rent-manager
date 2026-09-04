package com.example.rentmanager.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import com.example.rentmanager.Bill
import com.example.rentmanager.ReceiptFormatter
import com.example.rentmanager.RentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RevenueView(
    viewModel: RentViewModel,
    context: Context
) {
    val bills by viewModel.bills.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val tenants by viewModel.tenants.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") } // "All", "Paid", "Due"

    val totalRevenue = bills.sumOf { it.amountPaid }
    val rentCollection = bills.sumOf { it.baseRent }
    val electricityTotal = bills.sumOf { it.electricityAmount }
    val activeDue = bills.filter { !viewModel.wasHistoricalDueSettled(it) && it.remainingDue > 0.0 }
        .sumOf { it.remainingDue }

    val filteredBills = remember(bills, selectedFilter) {
        val sorted = bills.sortedByDescending { it.timestamp }
        when (selectedFilter) {
            "Paid" -> sorted.filter { it.remainingDue <= 0.0 || viewModel.wasHistoricalDueSettled(it) }
            "Due" -> sorted.filter { it.remainingDue > 0.0 && !viewModel.wasHistoricalDueSettled(it) }
            else -> sorted
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.ScaffoldBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Main Revenue Gradient Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, AppColors.AzureBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AppColors.AzurePrimary, AppColors.AzureDark)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "YEAR REVENUE (2026)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("2026", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "₹${String.format(Locale.ENGLISH, "%,.2f", totalRevenue)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Three Stat Columns
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Rent Collection", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text("₹${String.format(Locale.ENGLISH, "%,.2f", rentCollection)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text("Electricity", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text("₹${String.format(Locale.ENGLISH, "%,.2f", electricityTotal)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text("Active Due", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text("₹${String.format(Locale.ENGLISH, "%,.2f", activeDue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Ledger Section Title & Clean Filter Switcher
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Billing Ledger (${filteredBills.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    // Pill Filters (Zero lavender)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppColors.SurfaceWhite)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("All", "Paid", "Due").forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) AppColors.AzurePrimary else Color.Transparent)
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else AppColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
                        // Ledger Cards List
            if (filteredBills.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No billing records found.", color = AppColors.TextMuted, fontSize = 14.sp)
                    }
                }
            } else {
                items(filteredBills) { bill ->
                    val tenant = viewModel.getTenantForBill(bill)
                    val room = viewModel.getRoomForBill(bill)

                    val isDueSettled = viewModel.wasHistoricalDueSettled(bill)
                    val isAdvanceConsumed = viewModel.wasHistoricalAdvanceConsumed(bill)

                    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(bill.timestamp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top Row: Tenant Name, WhatsApp Icon & Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${tenant?.name ?: "Unknown"} (Room ${room?.roomNumber ?: "-"})",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // 1-Tap Direct WhatsApp Resend Pill
                                    IconButton(
                                        onClick = {
                                            if (tenant != null && room != null) {
                                                val msg = ReceiptFormatter.formatReceipt(
                                                    tenantName = tenant.name,
                                                    roomNumber = room.roomNumber,
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
                                                ReceiptFormatter.sendViaWhatsApp(context, tenant.phone, msg)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.WhatsAppContainer)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Chat,
                                            contentDescription = "Send WhatsApp Receipt",
                                            tint = AppColors.WhatsAppGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                // Dynamic Lifecycle Badges (Active Due vs Settled Dot)
                                when {
                                    // Past due that was settled in a subsequent bill
                                    bill.remainingDue > 0.0 && isDueSettled -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppColors.HistoryContainer)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(AppColors.HistorySettledDot)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text("Settled in next bill", fontSize = 11.sp, color = AppColors.HistoryText)
                                            }
                                        }
                                    }

                                    // Active unresolved due
                                    bill.remainingDue > 0.0 -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppColors.AmberContainer)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "DUE: ₹${String.format(Locale.ENGLISH, "%.0f", bill.remainingDue)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.AmberWarning
                                            )
                                        }
                                    }

                                    // Past advance credit that was consumed
                                    bill.remainingDue < 0.0 && isAdvanceConsumed -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppColors.HistoryContainer)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(AppColors.HistoryAdvanceDot)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text("Advance Credited", fontSize = 11.sp, color = AppColors.HistoryText)
                                            }
                                        }
                                    }

                                    // Active floating advance credit
                                    bill.remainingDue < 0.0 -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppColors.AzureContainer)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "ADVANCE: ₹${String.format(Locale.ENGLISH, "%.0f", -bill.remainingDue)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.AzurePrimary
                                            )
                                        }
                                    }

                                    // Exact fully paid
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppColors.EmeraldContainer)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "PAID · ${bill.paymentMode}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppColors.EmeraldSuccess
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Period: ${bill.billingPeriod}   ·   $dateStr",
                                fontSize = 11.sp,
                                color = AppColors.TextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Breakdown & Paid Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "Rent: ₹${String.format(Locale.ENGLISH, "%.0f", bill.baseRent)}  ·  Elec: ₹${String.format(Locale.ENGLISH, "%.0f", bill.electricityAmount)} (${String.format(Locale.ENGLISH, "%.0f", bill.unitsConsumed)}u)",
                                        fontSize = 12.sp,
                                        color = AppColors.TextSecondary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹${String.format(Locale.ENGLISH, "%,.2f", bill.totalPayable)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )
                                    Text(
                                        text = "Paid: ₹${String.format(Locale.ENGLISH, "%,.2f", bill.amountPaid)} (${bill.paymentMode})",
                                        fontSize = 11.sp,
                                        color = AppColors.EmeraldSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
