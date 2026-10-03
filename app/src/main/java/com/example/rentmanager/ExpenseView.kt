package com.example.rentmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import com.example.rentmanager.Expense
import com.example.rentmanager.RentViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class CategoryStyle(val emoji: String, val fg: Color, val bg: Color)

private val categoryStyles = linkedMapOf(
    "Maintenance" to CategoryStyle("🔧", Color(0xFF1565C0), Color(0xFFE3EEFC)),
    "Electricity" to CategoryStyle("⚡", Color(0xFFE67E00), Color(0xFFFFF0DC)),
    "Cleaning" to CategoryStyle("🧹", Color(0xFF1B8A5A), Color(0xFFE0F4EA)),
    "Property Tax" to CategoryStyle("🧾", Color(0xFFD32F2F), Color(0xFFFDE7E9)),
    "Other" to CategoryStyle("📦", Color(0xFF7B2FBF), Color(0xFFF0E8FB))
)

private fun styleFor(category: String) =
    categoryStyles[category] ?: categoryStyles.getValue("Other")

private fun inr(v: Double) = "₹" + String.format(Locale("en", "IN"), "%,.0f", v)

private fun monthKey(millis: Long): Int {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH)
}

private fun monthLabel(key: Int): String {
    val c = Calendar.getInstance().apply { set(key / 12, key % 12, 1) }
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(c.time)
}

/** Expenses tab: withdrawals card, monthly category totals, recent expenses, add button. */
@Composable
fun ExpensesView(vm: RentViewModel) {
    // Net cash is a lifetime figure, so this uses lifetime collections.
    val summary = vm.getRevenueSummary(forCurrentYearOnly = false)
    val allExpenses by vm.expenses.collectAsState()

    val nowKey = monthKey(System.currentTimeMillis())
    var selectedKey by remember { mutableStateOf(nowKey) }
    var monthMenu by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var showAll by remember { mutableStateOf(false) }
    var showCategories by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Expense?>(null) }

    val monthExpenses = allExpenses.filter { monthKey(it.timestamp) == selectedKey }
    val prevTotal = allExpenses.filter { monthKey(it.timestamp) == selectedKey - 1 }.sumOf { it.amount }
    val total = monthExpenses.sumOf { it.amount }
    val byCategory = categoryStyles.keys.associateWith { cat ->
        monthExpenses.filter { it.category == cat }.sumOf { it.amount }
    }
    val sortedMonth = monthExpenses.sortedByDescending { it.timestamp }
    val recent = if (showAll) sortedMonth else sortedMonth.take(4)
    val dateFmt = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    Scaffold(containerColor = AppColors.ScaffoldBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title + month picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Expenses", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("Track your expenses and withdrawals", fontSize = 11.sp, color = AppColors.TextSecondary)
                }
                Box {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AppColors.SurfaceWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { monthMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(monthLabel(selectedKey), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                            Icon(Icons.Rounded.KeyboardArrowDown, null, tint = AppColors.TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    DropdownMenu(expanded = monthMenu, onDismissRequest = { monthMenu = false }) {
                        (0..11).forEach { back ->
                            val k = nowKey - back
                            DropdownMenuItem(
                                text = { Text(monthLabel(k)) },
                                onClick = { selectedKey = k; monthMenu = false }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Existing withdrawals / net cash card (Withdraw + History buttons live here)
            ExpenseSummaryCard(
                vm = vm,
                collectedAfterExpenses = summary.totalCollected - allExpenses.sumOf { it.amount },
                monthTotal = total,
                prevTotal = prevTotal,
                prevMonthLabel = monthLabel(selectedKey - 1)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Total expenses for the selected month
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Expenses", fontSize = 12.sp, color = AppColors.TextSecondary)
                        Text(inr(total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    }
                    if (prevTotal > 0) {
                        val pct = ((total - prevTotal) / prevTotal * 100).toInt()
                        val up = pct >= 0
                        val c = if (up) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = (if (up) "↑ " else "↓ ") + kotlin.math.abs(pct) + "%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = c,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(c.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                            Text("vs " + monthLabel(selectedKey - 1), fontSize = 10.sp, color = AppColors.TextSecondary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Categories (2-column grid)
            SectionHeader("Expense Categories") { showCategories = true }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    byCategory.entries.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEach { (cat, amt) ->
                                CategoryTile(cat, amt, Modifier.weight(1f))
                            }
                            if (rowItems.size == 1) {
                                ViewAllTile(Modifier.weight(1f)) { showCategories = true }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Recent expenses
            SectionHeader("Recent Expenses", if (showAll) "Show Less" else "View All") { showAll = !showAll }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (recent.isEmpty()) {
                    Text(
                        "No expenses in " + monthLabel(selectedKey),
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.padding(20.dp)
                    )
                } else {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        recent.forEach { e ->
                            val st = styleFor(e.category)
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { selected = e }.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(st.bg),
                                    contentAlignment = Alignment.Center
                                ) { Text(st.emoji, fontSize = 18.sp) }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(e.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                    Text(e.location + " · " + e.category, fontSize = 11.sp, color = AppColors.TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(inr(e.amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                    Text(dateFmt.format(Date(e.timestamp)), fontSize = 10.sp, color = AppColors.TextSecondary)
                                }
                                Icon(Icons.Rounded.ChevronRight, null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAdd = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AzurePrimary)
            ) {
                Icon(Icons.Rounded.Add, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Expense", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    selected?.let { e ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(e.title, fontWeight = FontWeight.Bold) },
            text = {
                Text(inr(e.amount) + "\n" + e.location + " · " + e.category + "\n" + dateFmt.format(Date(e.timestamp)))
            },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Close") } },
            dismissButton = {
                TextButton(onClick = { vm.deleteExpense(e.id); selected = null }) {
                    Text("Delete", color = Color(0xFFD32F2F))
                }
            }
        )
    }

    if (showCategories) {
        AlertDialog(
            onDismissRequest = { showCategories = false },
            title = { Text("Categories · " + monthLabel(selectedKey), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    byCategory.forEach { (cat, amt) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(styleFor(cat).emoji + "  " + cat, fontSize = 14.sp)
                            Text(inr(amt), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCategories = false }) { Text("Close") } }
        )
    }

    if (showAdd) {
        AddExpenseDialog(
            onDismiss = { showAdd = false },
            onSave = { title, cat, prop, amt ->
                vm.addExpense(title, amt, cat, prop, System.currentTimeMillis())
                showAdd = false
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String = "View All", onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onAction() }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.AzurePrimary)
            Icon(Icons.Rounded.ChevronRight, null, tint = AppColors.AzurePrimary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ViewAllTile(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(AppColors.AzureContainer).clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) { Text("•••", fontSize = 11.sp, color = AppColors.AzurePrimary, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.width(8.dp))
        Text("View All Categories", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppColors.AzurePrimary, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = AppColors.AzurePrimary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun CategoryTile(category: String, amount: Double, modifier: Modifier = Modifier) {
    val st = styleFor(category)
    Row(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(st.bg).padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) { Text(st.emoji, fontSize = 15.sp) }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = st.fg)
            Text(inr(amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = st.fg, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, property: String, amount: Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var property by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Maintenance") }
    val amt = amount.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount (₹)") }, singleLine = true)
                OutlinedTextField(property, { property = it }, label = { Text("Property / Room") }, singleLine = true)
                categoryStyles.keys.chunked(3).forEach { rowCats ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowCats.forEach { c ->
                            val st = styleFor(c)
                            val sel = c == category
                            Text(
                                text = c,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (sel) Color.White else st.fg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (sel) st.fg else st.bg)
                                    .clickable { category = c }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && amt != null && amt > 0,
                onClick = { onSave(title.trim(), category, property.trim().ifBlank { "General" }, amt ?: 0.0) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
// ===== PASTE EVERYTHING BELOW AT THE VERY BOTTOM OF ExpenseView.kt =====

/** One card like the reference: Net Cash | Total Expenses (+ % badge) | Withdrawn, then the two buttons. */
@Composable
private fun ExpenseSummaryCard(
    vm: RentViewModel,
    collectedAfterExpenses: Double,
    monthTotal: Double,
    prevTotal: Double,
    prevMonthLabel: String
) {
    val withdrawals by vm.withdrawals.collectAsState()
    val withdrawn = withdrawals.sumOf { it.amount }
    val netCash = collectedAfterExpenses - withdrawn
    var showWithdraw by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.SurfaceWhite,
        border = BorderStroke(1.dp, AppColors.BorderSubtle),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                // Net cash
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Net Cash Available", fontSize = 10.5.sp, color = AppColors.TextSecondary, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        inr(netCash),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(52.dp)
                        .background(AppColors.BorderSubtle)
                )
                // Total expenses + badge
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Total Expenses", fontSize = 10.5.sp, color = AppColors.TextSecondary, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        inr(monthTotal),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 1
                    )
                    if (prevTotal > 0) {
                        val pct = ((monthTotal - prevTotal) / prevTotal * 100).toInt()
                        val up = pct >= 0
                        val c = if (up) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        val shortPrev = prevMonthLabel.substringBefore(' ').take(3) +
                            " " + prevMonthLabel.substringAfterLast(' ')
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = (if (up) "↑ " else "↓ ") + kotlin.math.abs(pct) + "%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = c,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(c.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("vs $shortPrev", fontSize = 10.sp, color = AppColors.TextSecondary, maxLines = 1)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(52.dp)
                        .background(AppColors.BorderSubtle)
                )
                // Withdrawn
                Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.End) {
                    Text("Withdrawn", fontSize = 10.5.sp, color = AppColors.TextSecondary, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        inr(withdrawn),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showWithdraw = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.AzurePrimary)
                ) {
                    Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Withdraw", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                OutlinedButton(
                    onClick = { showHistory = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, AppColors.BorderStrong)
                ) {
                    Text(
                        "History (${withdrawals.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.AzurePrimary
                    )
                }
            }
        }
    }

    if (showWithdraw) {
        ExpenseWithdrawDialog(
            maxAmount = netCash.coerceAtLeast(0.0),
            onDismiss = { showWithdraw = false },
            onSave = { amount, recipient, purpose ->
                vm.addWithdrawal(amount, recipient, purpose)
                showWithdraw = false
            }
        )
    }
    if (showHistory) {
        ExpenseHistoryDialog(vm = vm, onDismiss = { showHistory = false })
    }
}

@Composable
private fun ExpenseWithdrawDialog(
    maxAmount: Double,
    onDismiss: () -> Unit,
    onSave: (amount: Double, recipient: String, purpose: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    val amt = amount.toDoubleOrNull()
    val tooMuch = amt != null && amt > maxAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Available: " + inr(maxAmount), fontSize = 12.sp, color = AppColors.TextSecondary)
                OutlinedTextField(
                    amount, { amount = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    isError = tooMuch
                )
                if (tooMuch) {
                    Text("More than the available cash", fontSize = 11.sp, color = Color(0xFFD32F2F))
                }
                OutlinedTextField(recipient, { recipient = it }, label = { Text("Withdrawn by") }, singleLine = true)
                OutlinedTextField(purpose, { purpose = it }, label = { Text("Purpose") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = amt != null && amt > 0 && !tooMuch,
                onClick = {
                    onSave(
                        amt ?: 0.0,
                        recipient.trim().ifBlank { "Owner" },
                        purpose.trim().ifBlank { "General" }
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ExpenseHistoryDialog(vm: RentViewModel, onDismiss: () -> Unit) {
    val list by vm.withdrawals.collectAsState()
    val fmt = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdrawal History", fontWeight = FontWeight.Bold) },
        text = {
            if (list.isEmpty()) {
                Text("No withdrawals yet.", fontSize = 13.sp, color = AppColors.TextSecondary)
            } else {
                Column(
                    modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    list.sortedByDescending { it.timestamp }.forEach { w ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inr(w.amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                Text(w.recipient + " · " + w.purpose, fontSize = 11.sp, color = AppColors.TextSecondary)
                                Text(fmt.format(Date(w.timestamp)), fontSize = 10.sp, color = AppColors.TextMuted)
                            }
                            TextButton(onClick = { vm.deleteWithdrawal(w.id) }) {
                                Text("Delete", color = Color(0xFFD32F2F))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

                
