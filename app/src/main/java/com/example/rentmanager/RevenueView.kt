package com.example.rentmanager.ui.screens

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FindInPage
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Handyman
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.rentmanager.AppColors
import com.example.rentmanager.Bill
import com.example.rentmanager.ReceiptFormatter
import com.example.rentmanager.RentViewModel
import com.example.rentmanager.ui.components.RoomWiseBreakdownDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private class Ref(val s: Float) {
    fun dp(px: Number) = (px.toFloat() * s).dp
    fun sp(px: Number) = (px.toFloat() * s).sp
}

@Composable
private fun rememberRef(): Ref {
    val w = LocalConfiguration.current.screenWidthDp
    return remember(w) { Ref(w / 841f) }
}

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

/** "Aug 2026" for the month a bill is FOR (not the day it was paid). */
private fun billMonthLabel(bill: Bill): String {
    val (year, month) = parseBillYearMonth(bill)
    val cal = Calendar.getInstance()
    cal.clear()
    cal.set(year, month, 1)
    return SimpleDateFormat("MMM yyyy", Locale.ENGLISH).format(cal.time)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueView(vm: RentViewModel, onAddRecord: () -> Unit = {}) {
    val context = LocalContext.current
    val bills by vm.bills.collectAsState()
    val rooms by vm.rooms.collectAsState()
    var isCurrentYearOnly by remember { mutableStateOf(true) }
    var categoryFilter by remember { mutableStateOf("All") }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var modeFilter by remember { mutableStateOf<String?>(null) }
    var showModeMenu by remember { mutableStateOf(false) }
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

    // Counts shown under each category box
    val rentCount = filteredBills.count { it.baseRent > 0 }
    val electricityCount = filteredBills.count { it.electricityAmount > 0 }
    val maintenanceCount = filteredBills.count { it.maintenanceAmount > 0 }
    val duesCount = filteredBills.count { it.remainingDue > 0 }
    val advanceCount = filteredBills.count { it.amountPaid > it.totalPayable }

    // "% from last year"
    val thisYearTotal = remember(bills) { bills.filter { parseBillYearMonth(it).first == currentYear }.sumOf { it.amountPaid } }
    val lastYearTotal = remember(bills) { bills.filter { parseBillYearMonth(it).first == currentYear - 1 }.sumOf { it.amountPaid } }
    val growthUp = thisYearTotal >= lastYearTotal
    val growthText: String? = when {
        !isCurrentYearOnly -> null
        lastYearTotal > 0 -> "${kotlin.math.abs(((thisYearTotal - lastYearTotal) / lastYearTotal * 100).roundToInt())}% from last year"
        thisYearTotal > 0 -> "First year of records"
        else -> "0% from last year"
    }

    val paymentModes = remember(bills) { bills.map { it.paymentMode.toString() }.distinct().sorted() }

    val visibleBills = remember(filteredBills, categoryFilter, searchQuery, modeFilter) {
        filteredBills
            .filter { b ->
                when (categoryFilter) {
                    "Rent" -> b.baseRent > 0
                    "Electricity" -> b.electricityAmount > 0
                    "Maintenance" -> b.maintenanceAmount > 0
                    "Dues" -> b.remainingDue > 0
                    "Advance" -> b.amountPaid > b.totalPayable
                    else -> true
                }
            }
            .filter { modeFilter == null || it.paymentMode.toString() == modeFilter }
            .filter { b ->
                searchQuery.isBlank() || run {
                    val r = vm.getRoomForBill(b)
                    val t = vm.getTenantForBill(b)
                    (r?.roomNumber?.contains(searchQuery, ignoreCase = true) == true) ||
                        (t?.name?.contains(searchQuery, ignoreCase = true) == true)
                }
            }
    }

    // Every room's bills clubbed together by month, and every month clubbed under its year.
    val yearGroups = remember(visibleBills) {
        val monthNameFmt = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val byYear = visibleBills.groupBy { parseBillYearMonth(it).first }
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

    // Each room's single most recent bill, in room order — a quick "what's the latest" snapshot.
    val recentBillsByRoom = remember(visibleBills, rooms) {
        rooms.mapNotNull { room ->
            visibleBills.filter { it.roomId == room.id }.maxByOrNull { it.timestamp }
        }
    }

    val monthlyTotals = remember(filteredBills, currentYear) {
        val arr = DoubleArray(12)
        filteredBills.forEach { b ->
            val (y, m) = parseBillYearMonth(b)
            if (y == currentYear) arr[m] += b.amountPaid
        }
        arr.toList()
    }

    var expandedYears by remember { mutableStateOf(setOf<Int>()) }
    var expandedMonthKeys by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        containerColor = AppColors.ScaffoldBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---- Title + Year/Lifetime toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Financial Ledger",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Track your income, expenses and dues",
                            fontSize = 11.sp,
                            color = AppColors.TextSecondary,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.SurfaceWhite,
                        border = BorderStroke(1.dp, AppColors.BorderSubtle)
                    ) {
                        Row {
                            listOf(true to "Year $currentYear", false to "Lifetime").forEach { (isYear, label) ->
                                val sel = isCurrentYearOnly == isYear
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (sel) AppColors.AzureDark else Color.Transparent)
                                        .clickable { isCurrentYearOnly = isYear }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false,
                                        color = if (sel) Color.White else AppColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---- Collections card
            item {
                RevenueCollectionsCard(
                    vm = vm,
                    year = currentYear,
                    totalCollections = revenueSummary.totalCollected,
                    rentTotal = revenueSummary.rentCollected,
                    electricityTotal = revenueSummary.electricityCollected,
                    maintenanceTotal = revenueSummary.maintenanceCollected,
                    duesTotal = revenueSummary.activeDues,
                    advanceTotal = vm.getTotalAdvance(),
                    rentCount = rentCount,
                    electricityCount = electricityCount,
                    maintenanceCount = maintenanceCount,
                    duesCount = duesCount,
                    advanceCount = advanceCount,
                    growthText = growthText,
                    growthUp = growthUp,
                    monthlyTotals = monthlyTotals,
                    forCurrentYearOnly = isCurrentYearOnly
                )
            }

            // ---- Billing History header (title, search, filter, Add)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(
                        LocalDensity provides Density(LocalDensity.current.density, fontScale = 1f)
                    ) {
                        Text(
                            text = "Billing History & Receipts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    HeaderIconButton(
                        icon = if (showSearch) Icons.Rounded.Close else Icons.Rounded.Search,
                        active = showSearch,
                        onClick = {
                            showSearch = !showSearch
                            if (!showSearch) searchQuery = ""
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        HeaderIconButton(
                            icon = Icons.Rounded.FilterList,
                            active = modeFilter != null,
                            onClick = { showModeMenu = true }
                        )
                        DropdownMenu(
                            expanded = showModeMenu,
                            onDismissRequest = { showModeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All payment modes", fontSize = 13.sp) },
                                onClick = { modeFilter = null; showModeMenu = false }
                            )
                            paymentModes.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode, fontSize = 13.sp) },
                                    onClick = { modeFilter = mode; showModeMenu = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AppColors.AzureDark,
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAddRecord() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }

            // ---- Search field (only when the search button is active)
            if (showSearch) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AppColors.SurfaceWhite,
                        border = BorderStroke(1.dp, AppColors.BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text("Search room or tenant...", fontSize = 13.sp, color = AppColors.TextMuted, maxLines = 1)
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.merge(
                                        TextStyle(fontSize = 13.sp, color = AppColors.TextPrimary)
                                    ),
                                    cursorBrush = SolidColor(AppColors.AzurePrimary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // ---- Category chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "Rent", "Electricity", "Maintenance", "Dues", "Advance").forEach { tag ->
                        val isSel = categoryFilter == tag
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSel) AppColors.AzureDark else AppColors.SurfaceWhite)
                                .border(
                                    1.dp,
                                    if (isSel) AppColors.AzureDark else AppColors.BorderSubtle,
                                    RoundedCornerShape(50)
                                )
                                .clickable { categoryFilter = tag }
                                .padding(horizontal = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSel) Color.White else AppColors.TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // ---- Bills / empty state
            if (yearGroups.isEmpty()) {
                item {
                    EmptyBillingState(
                        message = when {
                            filteredBills.isNotEmpty() -> "No records match your filters."
                            isCurrentYearOnly -> "No billing records found for $currentYear."
                            else -> "No billing records found."
                        },
                        showAddButton = filteredBills.isEmpty(),
                        onAddRecord = onAddRecord
                    )
                }
            } else {
                if (recentBillsByRoom.isNotEmpty()) {
                    item {
                        Text(
                            text = "RECENT BY ROOM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    items(recentBillsByRoom, key = { "recent-${it.id}" }) { bill ->
                        BillDetailRow(bill = bill, vm = vm, context = context, dateFormat = dateFormat)
                    }
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Divider(color = AppColors.BorderSubtle)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "BY YEAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }
                }
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
            }
            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun HeaderIconButton(icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (active) AppColors.AzureContainer else AppColors.SurfaceWhite,
        border = BorderStroke(1.dp, if (active) AppColors.AzurePrimary else AppColors.BorderSubtle),
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = AppColors.TextPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun EmptyBillingState(message: String, showAddButton: Boolean, onAddRecord: () -> Unit) {
    val borderColor = AppColors.AzureBorder
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )
                )
            }
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AppColors.AzureContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FindInPage,
                contentDescription = null,
                tint = AppColors.AzureDark,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Add rent or utility payments to see them here.",
            fontSize = 12.sp,
            color = AppColors.TextSecondary
        )
        if (showAddButton) {
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAddRecord,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AzureDark,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 18.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add First Record", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
        color = AppColors.SurfaceWhite,
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
                            text = "Bill month: ${billMonthLabel(bill)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.AzureDark
                        )
                        Text(
                            text = "Paid on ${dateFormat.format(Date(bill.timestamp))}",
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
                        text = "₹${String.format(Locale.ENGLISH, "%.2f", bill.amountPaid)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.EmeraldSuccess
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", fontSize = 9.sp, color = AppColors.TextSecondary)
                    if (bill.remainingDue > 0) {
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%.2f", bill.remainingDue)} Due",
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
    maintenanceTotal: Double,
    duesTotal: Double,
    advanceTotal: Double,
    rentCount: Int = 0,
    electricityCount: Int = 0,
    maintenanceCount: Int = 0,
    duesCount: Int = 0,
    advanceCount: Int = 0,
    growthText: String? = null,
    growthUp: Boolean = true,
    monthlyTotals: List<Double> = List(12) { 0.0 },
    forCurrentYearOnly: Boolean
) {
    var activeCategory by remember { mutableStateOf<String?>(null) }
    var showPropertyBreakdown by remember { mutableStateOf(false) }
    val allProperties by vm.properties.collectAsState()
    val allRooms by vm.rooms.collectAsState()
    val allBills by vm.bills.collectAsState()
    val blue = Color(0xFF1E6FD9)
    val navy = Color(0xFF17408F)
    val growthColor = if (growthUp) AppColors.EmeraldSuccess else AppColors.CrimsonAlert

    // Lock the card's text to normal size so it looks the same on every phone,
    // even when the phone's font size is set larger.
    val currentDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(currentDensity.density, fontScale = 1f)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppColors.SurfaceWhite,
            border = BorderStroke(0.5.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 11.dp, bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showPropertyBreakdown = true }
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = if (forCurrentYearOnly) "Total Collections ($year)" else "Total Collections (Lifetime)",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%,.2f", totalCollections)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            color = AppColors.TextPrimary
                        )
                        if (growthText != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(growthColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (growthUp) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                                        contentDescription = null,
                                        tint = growthColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(growthText, fontSize = 12.sp, color = growthColor)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.AzureContainer.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = AppColors.AzureDark,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                MonthlyBarChart(values = monthlyTotals)
                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RevenueStatBox(
                        icon = Icons.Rounded.Home,
                        label = "Rent Income",
                        amount = rentTotal,
                        subText = "$rentCount payments",
                        color = AppColors.EmeraldSuccess,
                        labelColor = AppColors.EmeraldSuccess,
                        labelSize = 12.sp,
                        amountSize = 16.sp,
                        subSize = 11.sp,
                        iconSize = 28.dp,
                        hPad = 10.dp,
                        iconGap = 8.dp,
                        modifier = Modifier.weight(1f),
                        onClick = { activeCategory = "rent" }
                    )
                    RevenueStatBox(
                        icon = Icons.Rounded.Bolt,
                        label = "Electricity",
                        amount = electricityTotal,
                        subText = "$electricityCount payments",
                        color = AppColors.AmberWarning,
                        labelColor = AppColors.AmberWarning,
                        labelSize = 12.sp,
                        amountSize = 16.sp,
                        subSize = 11.sp,
                        iconSize = 28.dp,
                        hPad = 10.dp,
                        iconGap = 8.dp,
                        modifier = Modifier.weight(1f),
                        onClick = { activeCategory = "electricity" }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RevenueStatBox(
                        icon = Icons.Rounded.Handyman,
                        label = "Maintenance",
                        amount = maintenanceTotal,
                        subText = "$maintenanceCount expenses",
                        color = blue,
                        labelColor = navy,
                        labelSize = 10.sp,
                        amountSize = 13.sp,
                        subSize = 10.sp,
                        iconSize = 20.dp,
                        hPad = 6.dp,
                        iconGap = 4.dp,
                        modifier = Modifier.weight(1f),
                        onClick = { activeCategory = "maintenance" }
                    )
                    RevenueStatBox(
                        icon = Icons.Rounded.WarningAmber,
                        label = "Dues",
                        amount = duesTotal,
                        subText = "$duesCount pending",
                        color = AppColors.CrimsonAlert,
                        labelColor = AppColors.CrimsonAlert,
                        labelSize = 10.sp,
                        amountSize = 13.sp,
                        subSize = 10.sp,
                        iconSize = 20.dp,
                        hPad = 6.dp,
                        iconGap = 4.dp,
                        modifier = Modifier.weight(1f),
                        onClick = { activeCategory = "dues" }
                    )
                    RevenueStatBox(
                        icon = Icons.Rounded.Savings,
                        label = "Advance",
                        amount = advanceTotal,
                        subText = "$advanceCount records",
                        color = AppColors.EmeraldSuccess,
                        labelColor = AppColors.EmeraldSuccess,
                        labelSize = 10.sp,
                        amountSize = 13.sp,
                        subSize = 10.sp,
                        iconSize = 20.dp,
                        hPad = 6.dp,
                        iconGap = 4.dp,
                        modifier = Modifier.weight(1f),
                        onClick = { activeCategory = "advance" }
                    )
                }
            }
        }
    }

    if (showPropertyBreakdown) {
        val cal = Calendar.getInstance()
        fun inScope(b: Bill): Boolean {
            if (!forCurrentYearOnly) return true
            cal.timeInMillis = b.timestamp
            return cal.get(Calendar.YEAR) == year
        }
        val rows = allProperties.map { p ->
            val propRooms = allRooms.filter { it.propertyId == p.id }
            val roomRows = propRooms.map { r ->
                val rb = allBills.filter { it.roomId == r.id && inScope(it) }
                PropertyRoomRow("Room ${r.roomNumber}", rb.sumOf { it.amountPaid }, rb.size)
            }
            PropertyBreakdownRow(
                name = p.name,
                total = roomRows.sumOf { it.total },
                payments = roomRows.sumOf { it.payments },
                rooms = roomRows
            )
        }.toMutableList()
        // Bills whose room no longer exists (e.g. a removed property/room) so the total always adds up
        val knownRoomIds = allRooms.map { it.id }.toSet()
        val orphanBills = allBills.filter { it.roomId !in knownRoomIds && inScope(it) }
        if (orphanBills.isNotEmpty()) {
            rows.add(
                PropertyBreakdownRow(
                    name = "Removed properties / rooms",
                    total = orphanBills.sumOf { it.amountPaid },
                    payments = orphanBills.size,
                    rooms = emptyList()
                )
            )
        }
        PropertyBreakdownDialog(
            title = if (forCurrentYearOnly) "Collections by property ($year)" else "Collections by property (Lifetime)",
            rows = rows,
            grandTotal = totalCollections,
            onDismiss = { showPropertyBreakdown = false }
        )
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

private data class PropertyRoomRow(val label: String, val total: Double, val payments: Int)

private data class PropertyBreakdownRow(
    val name: String,
    val total: Double,
    val payments: Int,
    val rooms: List<PropertyRoomRow>
)

@Composable
private fun PropertyBreakdownDialog(
    title: String,
    rows: List<PropertyBreakdownRow>,
    grandTotal: Double,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(setOf<String>()) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.SurfaceWhite,
        title = {
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Text(
                    "Total ₹${String.format(Locale.ENGLISH, "%,.2f", grandTotal)}",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rows.isEmpty()) {
                    Text("No properties yet.", fontSize = 13.sp, color = AppColors.TextSecondary)
                }
                rows.forEach { row ->
                    val isOpen = expanded.contains(row.name)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.ScaffoldBackground,
                        border = BorderStroke(1.dp, AppColors.BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                expanded = if (isOpen) expanded - row.name else expanded + row.name
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
    
       
