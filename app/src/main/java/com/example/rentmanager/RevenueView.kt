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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Savings
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
import com.example.rentmanager.Bill
import com.example.rentmanager.ReceiptFormatter
import com.example.rentmanager.RentViewModel
import com.example.rentmanager.ui.components.RoomWiseBreakdownDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class MonthGroup(
    val year: Int,
    val monthIndex: Int,
    val monthName: String,
    val bills: List<Bill>,
    val totalCollected: Double
)

private data class YearGroup(
    val year: Int,
    val months: List<MonthGroup>,
    val totalCollected: Double
)

/** (year, month 0-11) for a bill — falls back to the bill's save timestamp if
 *  billingPeriod isn't a clean "Month yyyy" string (e.g. a manually-typed combined label). */
private fun parseBillYearMonth(bill: Bill): Pair<Int, Int> {
    return try {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        val parsed = sdf.parse(bill.billingPeriod.trim())
        if (parsed != null) {
            val cal = Calendar.getInstance()
            cal.time = parsed
            cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
        } else {
            throw IllegalArgumentException("unparseable")
        }
    } catch (e: Exception) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = bill.timestamp
        cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }
}

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

    // Every room's bills clubbed together by month, and every month clubbed under its year.
    val yearGroups = remember(filteredBills) {
        val monthNameFmt = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val byYear = filteredBills.groupBy { parseBillYearMonth(it).first }
        byYear.keys.sortedDescending().map { year ->
            val billsThisYear = byYear[year] ?: emptyList()
            val byMonth = billsThisYear.groupBy { parseBillYearMonth(it).second }
            val months = (0..11).map { monthIdx ->
                val cal = Calendar.getInstance()
                cal.clear()
                cal.set(year, monthIdx, 1)
                val billsForMonth = (byMonth[monthIdx] ?: emptyList()).sortedByDescending { it.timestamp }
                MonthGroup(
                    year = year,
                    monthIndex = monthIdx,
                    monthName = monthNameFmt.format(cal.time),
                    bills = billsForMonth,
                    totalCollected = billsForMonth.sumOf { it.amountPaid }
                )
            }
            YearGroup(
                year = year,
                months = months,
                totalCollected = billsThisYear.sumOf { it.amountPaid }
            )
        }
    }

    var expandedYears by remember { mutableStateOf(setOf<Int>()) }
    var expandedMonthKeys by remember { mutableStateOf(setOf<String>()) }

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

            RevenueCollectionsCard(
                vm = vm,
                year = currentYear,
                totalCollections = revenueSummary.totalCollected,
                rentTotal = revenueSummary.rentCollected,
                electricityTotal = revenueSummary.electricityCollected,
                duesTotal = revenueSummary.activeDues,
                advanceTotal = vm.getTotalAdvance(),
                forCurrentYearOnly = isCurrentYearOnly
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "BILLING HISTORY & RECEIPTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (yearGroups.isEmpty()) {
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
                    items(yearGroups, key = { it.year }) { yearGroup ->
                        YearGroupCard(
                            yearGroup = yearGroup,
                            isExpanded = expandedYears.contains(yearGroup.year),
                            onToggleYear = {
                                expandedYears = if (expandedYears.contains(yearGroup.year))
                                    expandedYears - yearGroup.year
                                else
                                    expandedYears + yearGroup.year
                            },
                            expandedMonthKeys = expandedMonthKeys,
                            onToggleMonth = { key ->
                                expandedMonthKeys = if (expandedMonthKeys.contains(key))
                                    expandedMonthKeys - key
                                else
                                    expandedMonthKeys + key
                            },
                            vm = vm,
                            context = context,
                            dateFormat = dateFormat
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun YearGroupCard(
    yearGroup: YearGroup,
    isExpanded: Boolean,
    onToggleYear: () -> Unit,
    expandedMonthKeys: Set<String>,
    onToggleMonth: (String) -> Unit,
    vm: RentViewModel,
    context: Context,
    dateFormat: SimpleDateFormat
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        border = BorderStroke(1.dp, AppColors.BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleYear() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.ChevronRight,
                        contentDescription = if (isExpanded) "Collapse year" else "Expand year",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${yearGroup.year}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }
                Text(
                    text = "₹${String.format(Locale.ENGLISH, "%,.0f", yearGroup.totalCollected)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.EmeraldSuccess
                )
            }

            if (isExpanded) {
                Divider(color = AppColors.BorderSubtle)
                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                    yearGroup.months.forEach { monthGroup ->
                        val monthKey = "${monthGroup.year}-${monthGroup.monthIndex}"
                        MonthRow(
                            monthGroup = monthGroup,
                            isExpanded = expandedMonthKeys.contains(monthKey),
                            onToggle = { onToggleMonth(monthKey) },
                            vm = vm,
                            context = context,
                            dateFormat = dateFormat
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthRow(
    monthGroup: MonthGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    vm: RentViewModel,
    context: Context,
    dateFormat: SimpleDateFormat
) {
    val hasBills = monthGroup.bills.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = hasBills) { onToggle() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasBills && isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = if (hasBills) AppColors.TextSecondary else AppColors.TextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = monthGroup.monthName,
                    fontSize = 13.sp,
                    color = if (hasBills) AppColors.TextPrimary else AppColors.TextMuted,
                    fontWeight = if (hasBills) FontWeight.Medium else FontWeight.Normal
                )
            }
            Text(
                text = "₹${String.format(Locale.ENGLISH, "%,.0f", monthGroup.totalCollected)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (hasBills) AppColors.TextPrimary else AppColors.TextMuted
            )
        }

        if (isExpanded && hasBills) {
            Column(modifier = Modifier.padding(start = 22.dp, end = 14.dp, bottom = 8.dp)) {
                monthGroup.bills.forEach { bill ->
                    BillDetailRow(bill = bill, vm = vm, context = context, dateFormat = dateFormat)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun BillDetailRow(
    bill: Bill,
    vm: RentViewModel,
    context: Context,
    dateFormat: SimpleDateFormat
) {
    val tenant = vm.getTenantForBill(bill)
    val room = vm.getRoomForBill(bill)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AppColors.ScaffoldBackground,
        border = BorderStroke(1.dp, AppColors.BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Room ${room?.roomNumber ?: bill.roomId} • ${tenant?.name ?: "Unknown"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = dateFormat.format(Date(bill.timestamp)),
                            fontSize = 10.sp,
                            color = AppColors.TextMuted
                        )
                    }
                }
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
                                maintenanceAmount = bill.maintenanceAmount,
                                totalAmount = bill.totalPayable,
                                amountPaid = bill.amountPaid,
                                paymentMode = bill.paymentMode,
                                remainingDue = bill.remainingDue
                            )
                            ReceiptFormatter.sendViaWhatsApp(context, tenant.phoneNumber, receiptMsg)
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = "Share WhatsApp Receipt",
                            tint = AppColors.WhatsAppGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = AppColors.BorderSubtle)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (bill.maintenanceAmount > 0) "Rent + Elec + Maint" else "Rent + Elec",
                        fontSize = 9.sp,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = if (bill.maintenanceAmount > 0)
                            "₹${bill.baseRent.toInt()} + ₹${bill.electricityAmount.toInt()} + ₹${bill.maintenanceAmount.toInt()}"
                        else
                            "₹${bill.baseRent.toInt()} + ₹${bill.electricityAmount.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextPrimary
                    )
                }
                Column {
                    Text("Paid (${bill.paymentMode})", fontSize = 9.sp, color = AppColors.TextSecondary)
                    Text(
                        text = "₹${String.format(Locale.ENGLISH, "%.0f", bill.amountPaid)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.EmeraldSuccess
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", fontSize = 9.sp, color = AppColors.TextSecondary)
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

@Composable
fun RevenueCollectionsCard(
    vm: RentViewModel,
    year: Int,
    totalCollections: Double,
    rentTotal: Double,
    electricityTotal: Double,
    duesTotal: Double,
    advanceTotal: Double,
    forCurrentYearOnly: Boolean
) {
    var activeCategory by remember { mutableStateOf<String?>(null) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AppColors.SurfaceWhite,
        border = BorderStroke(1.dp, AppColors.BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collections ($year)",
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary
                )
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "₹${String.format(Locale.ENGLISH, "%,.0f", totalCollections)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = AppColors.BorderSubtle)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RevenueStatBox(
                    icon = Icons.Rounded.Home,
                    label = "Rent",
                    amount = rentTotal,
                    color = AppColors.AzurePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { activeCategory = "rent" }
                )
                RevenueStatBox(
                    icon = Icons.Rounded.Bolt,
                    label = "Electricity",
                    amount = electricityTotal,
                    color = AppColors.AmberWarning,
                    modifier = Modifier.weight(1f),
                    onClick = { activeCategory = "electricity" }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RevenueStatBox(
                    icon = Icons.Rounded.WarningAmber,
                    label = "Dues",
                    amount = duesTotal,
                    color = AppColors.CrimsonAlert,
                    modifier = Modifier.weight(1f),
                    onClick = { activeCategory = "dues" }
                )
                RevenueStatBox(
                    icon = Icons.Rounded.Savings,
                    label = "Advance",
                    amount = advanceTotal,
                    color = AppColors.EmeraldSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = { activeCategory = "advance" }
                )
            }
        }
    }

    activeCategory?.let { category ->
        val items = vm.getRoomWiseBreakdown(category, forCurrentYearOnly)
        val (title, color) = when (category) {
            "rent" -> "Rent Collected" to AppColors.AzurePrimary
            "electricity" -> "Electricity Collected" to AppColors.AmberWarning
            "dues" -> "Pending Dues" to AppColors.CrimsonAlert
            "advance" -> "Advance Owed" to AppColors.EmeraldSuccess
            else -> "" to AppColors.TextPrimary
        }
        RoomWiseBreakdownDialog(
            title = title,
            items = items,
            accentColor = color,
            onDismiss = { activeCategory = null }
        )
    }
}

@Composable
private fun RevenueStatBox(
    icon: ImageVector,
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AppColors.ScaffoldBackground,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${String.format(Locale.ENGLISH, "%,.0f", amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
                
