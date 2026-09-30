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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
                        .padding(top = 14.dp),
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
                    Text(
                        text = "Billing History & Receipts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
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
                                        
