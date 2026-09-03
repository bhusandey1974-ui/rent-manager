package com.example.rentmanager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class LedgerFilter {
    ALL, PAID, PENDING
}

enum class RevenueTimeframe {
    YEAR_2026, LIFETIME
}

@Composable
fun RevenueView(
    vm: RentViewModel,
    modifier: Modifier = Modifier
) {
    val bills by vm.bills.collectAsState()
    val rooms by vm.rooms.collectAsState()

    var filter by remember { mutableStateOf(LedgerFilter.ALL) }
    var timeframe by remember { mutableStateOf(RevenueTimeframe.YEAR_2026) }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 2
        }
    }

    val totalCollected = if (timeframe == RevenueTimeframe.YEAR_2026) {
        vm.calculateYearlyRevenue()
    } else {
        vm.calculateLifetimeRevenue()
    }

    val rentEarnings = if (timeframe == RevenueTimeframe.YEAR_2026) {
        vm.calculateYearlyRentEarnings()
    } else {
        vm.calculateLifetimeRentEarnings()
    }

    val electricityEarnings = if (timeframe == RevenueTimeframe.YEAR_2026) {
        vm.calculateYearlyElectricityRevenue()
    } else {
        vm.calculateLifetimeElectricityRevenue()
    }

    val totalOutstandingDues = vm.calculateTotalOutstandingDues()

    val filteredBills = remember(bills, filter) {
        when (filter) {
            LedgerFilter.ALL -> bills
            LedgerFilter.PAID -> bills.filter { it.remainingDue <= 0.0 }
            LedgerFilter.PENDING -> bills.filter { it.remainingDue > 0.0 }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Executive Slate Metrics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (timeframe == RevenueTimeframe.YEAR_2026) "YEAR REVENUE (2026)" else "LIFETIME COLLECTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .padding(2.dp)
                    ) {
                        Text(
                            text = "2026",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (timeframe == RevenueTimeframe.YEAR_2026) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (timeframe == RevenueTimeframe.YEAR_2026) Color(0xFF2563EB) else Color.Transparent)
                                .clickable { timeframe = RevenueTimeframe.YEAR_2026 }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                        Text(
                            text = "All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (timeframe == RevenueTimeframe.LIFETIME) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (timeframe == RevenueTimeframe.LIFETIME) Color(0xFF2563EB) else Color.Transparent)
                                .clickable { timeframe = RevenueTimeframe.LIFETIME }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currencyFormat.format(totalCollected),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Color(0xFF334155), thickness = 0.8.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Rent Collection",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = currencyFormat.format(rentEarnings),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "Electricity",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = currencyFormat.format(electricityEarnings),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "Active Due",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = currencyFormat.format(totalOutstandingDues),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (totalOutstandingDues > 0.0) Color(0xFFFBBF24) else Color(0xFF34D399)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Ledger filter bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Billing Ledger (${filteredBills.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0F172A)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(2.dp)
            ) {
                listOf(LedgerFilter.ALL to "All", LedgerFilter.PAID to "Paid", LedgerFilter.PENDING to "Due").forEach { (f, label) ->
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (filter == f) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        color = if (filter == f) Color.White else Color(0xFF64748B),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (filter == f) Color(0xFF2563EB) else Color.Transparent)
                            .clickable { filter = f }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
                if (filteredBills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No records found",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF94A3B8)
                )
            }
        } else {
            val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredBills, key = { it.id }) { bill ->
                    val room = rooms.find { it.id == bill.roomId }
                    val roomLabel = room?.roomNumber ?: "Room"
                    val tenantName = vm.resolveTenantName(bill.tenantId, bill.roomId)

                    val elecUnits = (bill.currentMeterReading - bill.prevMeterReading).coerceAtLeast(0.0)
                    val elecTotal = elecUnits * bill.electricityRate
                    val currentPeriodCharge = bill.baseRent + elecTotal + bill.maintenanceCharge
                    val totalBilled = currentPeriodCharge + bill.previousDueCarryover

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$tenantName (Room $roomLabel)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color(0xFF0F172A)
                                )

                                if (bill.remainingDue <= 0.0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFECFDF5))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "PAID • ${bill.paymentMode}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            color = Color(0xFF059669)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "DUE: ₹${bill.remainingDue.toInt()}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Period: ${bill.monthYear}  •  ${dateFormat.format(Date(bill.timestamp))}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "Rent: ₹${bill.baseRent.toInt()}  •  Elec: ₹${elecTotal.toInt()} (${elecUnits.toInt()}u)",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFF475569)
                                    )

                                    if (bill.previousDueCarryover > 0.0) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "₹${currentPeriodCharge.toInt()} ",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = FontFamily.SansSerif,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = "+ ₹${bill.previousDueCarryover.toInt()} Due",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif,
                                                color = Color(0xFFD97706),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFFEF3C7))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = currencyFormat.format(totalBilled),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Paid: ₹${bill.amountPaid.toInt()} (${bill.paymentMode})",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFF059669)
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

