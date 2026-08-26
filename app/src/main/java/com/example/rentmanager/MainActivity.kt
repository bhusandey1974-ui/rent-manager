package com.example.rentmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BrandBackground) {
                    AppNavigationContainer(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigationContainer(viewModel: RentViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Apartment, contentDescription = "Rooms") },
                    label = { Text("Properties") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandPrimary,
                        selectedTextColor = BrandPrimary,
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.QueryStats, contentDescription = "Analytics") },
                    label = { Text("Revenue") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandPrimary,
                        selectedTextColor = BrandPrimary,
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedTab == 0) {
                PropertiesScreen(viewModel = viewModel)
            } else {
                RevenueAnalyticsScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen(viewModel: RentViewModel) {
    val tenants by viewModel.allTenants.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTenantForBill by remember { mutableStateOf<Tenant?>(null) }
    var selectedTenantForLedger by remember { mutableStateOf<Tenant?>(null) }
    var selectedTenantForCheckout by remember { mutableStateOf<Tenant?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(BrandPrimary, BrandAccent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Rent Manager", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BrandDarkNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Room")
            }
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Properties & Tenants (${tenants.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkNavy
                )
            }

            if (tenants.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Domain, contentDescription = null, tint = BrandSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No rooms configured yet", fontWeight = FontWeight.Bold, color = BrandDarkNavy)
                            Text("Tap + to add a tenant with entry date & meter reading.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(tenants, key = { it.id }) { tenant ->
                    EnhancedTenantCard(
                        tenant = tenant,
                        onAddBillClick = { selectedTenantForBill = tenant },
                        onViewHistoryClick = { selectedTenantForLedger = tenant },
                        onCheckoutClick = { selectedTenantForCheckout = tenant },
                        onDeleteClick = { viewModel.deleteTenant(tenant) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(70.dp)) }
        }
    }

    if (showAddDialog) {
        AddTenantDialog(
            existingRooms = tenants.map { it.roomNumber.trim().lowercase() }.toSet(),
            onDismiss = { showAddDialog = false },
            onSave = { name, room, phone, rent, rate, reading, entryDate ->
                viewModel.addTenant(name, room, phone, rent, rate, reading, entryDate)
                showAddDialog = false
            }
        )
    }

    selectedTenantForCheckout?.let { tenant ->
        CheckoutTenantDialog(
            tenant = tenant,
            onDismiss = { selectedTenantForCheckout = null },
            onConfirm = { exitDate, finalReading ->
                viewModel.checkoutTenant(tenant, exitDate, finalReading)
                selectedTenantForCheckout = null
            }
        )
    }

    selectedTenantForBill?.let { tenant ->
        AddMonthlyBillDialog(
            tenant = tenant,
            onDismiss = { selectedTenantForBill = null },
            onSave = { currReading, baseRent, amountPaid, paymentDate, mode, monthYear ->
                viewModel.addMonthlyBill(tenant, currReading, baseRent, amountPaid, paymentDate, mode, monthYear)
                selectedTenantForBill = null
            }
        )
    }

    selectedTenantForLedger?.let { tenant ->
        TenantHistoryBottomSheet(
            tenant = tenant,
            viewModel = viewModel,
            onDismiss = { selectedTenantForLedger = null }
        )
    }
}

@Composable
fun EnhancedTenantCard(
    tenant: Tenant,
    onAddBillClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (tenant.isOccupied) Color(0xFFE3F2FD) else Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tenant.roomNumber,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (tenant.isOccupied) BrandPrimary else Color(0xFFC62828),
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tenant.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandDarkNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (tenant.isOccupied) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (tenant.isOccupied) "Active" else "Vacated",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tenant.isOccupied) SuccessGreen else WarningRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (tenant.isOccupied) "📞 ${tenant.phone}" else "Left on: ${tenant.exitDate ?: "N/A"}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = BrandBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Entered: ${tenant.entryDate}", fontSize = 11.sp, color = Color.DarkGray)
                        Text("Initial Meter: ${formatUnits(tenant.initialMeterReading)}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Rent: ${formatCurrency(tenant.defaultBaseRent)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                        Text("Latest Meter: ${formatUnits(tenant.lastMeterReading)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewHistoryClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPrimary)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ledger")
                }

                if (tenant.isOccupied) {
                    OutlinedButton(
                        onClick = onCheckoutClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningRed)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exit")
                    }

                    Button(
                        onClick = onAddBillClick,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Bill")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Room ${tenant.roomNumber}?") },
            text = { Text("This will permanently delete this tenant and all associated bills.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningRed)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun RevenueAnalyticsScreen(viewModel: RentViewModel) {
    val allBills by viewModel.allBills.collectAsState()
    val tenants by viewModel.allTenants.collectAsState()

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var selectedYear by remember { mutableIntStateOf(currentYear) }

    val lifetimeRent = allBills.sumOf { it.baseRent }
    val lifetimeElec = allBills.sumOf { it.electricityAmount }
    val lifetimeCollected = allBills.sumOf { it.amountPaid }
    val lifetimeDue = allBills.sumOf { it.dueAmount }

    val yearBills = allBills.filter { it.billingYear == selectedYear }
    val yearRent = yearBills.sumOf { it.baseRent }
    val yearElec = yearBills.sumOf { it.electricityAmount }
    val yearTotal = yearBills.sumOf { it.totalBillAmount }
    val yearCollected = yearBills.sumOf { it.amountPaid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Revenue & Analytics", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = BrandDarkNavy)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(BrandPrimary, BrandSecondary, BrandAccent)))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("LIFETIME COLLECTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                        Text(formatCurrency(lifetimeCollected), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Rent Earnings", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(formatCurrency(lifetimeRent), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Elec. Earnings", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(formatCurrency(lifetimeElec), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Total Due", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(formatCurrency(lifetimeDue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Yearly Breakdown ($selectedYear)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandDarkNavy)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(currentYear - 1, currentYear, currentYear + 1).forEach { yr ->
                                FilterChip(
                                    selected = selectedYear == yr,
                                    onClick = { selectedYear = yr },
                                    label = { Text(yr.toString(), fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AnalyticsMiniBox(label = "Rent Billed", value = formatCurrency(yearRent), color = BrandDarkNavy)
                        AnalyticsMiniBox(label = "Electricity", value = formatCurrency(yearElec), color = BrandSecondary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AnalyticsMiniBox(label = "Total Invoiced", value = formatCurrency(yearTotal), color = BrandPrimary)
                        AnalyticsMiniBox(label = "Collected", value = formatCurrency(yearCollected), color = SuccessGreen)
                    }
                }
            }
        }

        item {
            Text("Monthly Ledger Entries ($selectedYear)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandDarkNavy)
        }

        if (yearBills.isEmpty()) {
            item {
                Text("No bills generated for $selectedYear.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            items(yearBills, key = { it.id }) { bill ->
                val tenant = tenants.find { it.id == bill.tenantId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bill.monthYear, fontWeight = FontWeight.Bold, color = BrandDarkNavy)
                            Text("Room ${tenant?.roomNumber ?: "-"} • ${tenant?.name ?: "Unknown"}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("+ ${formatCurrency(bill.amountPaid)}", fontWeight = FontWeight.ExtraBold, color = SuccessGreen, fontSize = 14.sp)
                            Text("Rent: ${formatCurrency(bill.baseRent)} | Elec: ${formatCurrency(bill.electricityAmount)}", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(50.dp)) }
    }
}

@Composable
fun AnalyticsMiniBox(label: String, value: String, color: Color) {
    Surface(
        color = BrandBackground,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AddTenantDialog(
    existingRooms: Set<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var baseRent by remember { mutableStateOf("") }
    var ratePerUnit by remember { mutableStateOf("10.0") }
    var initialReading by remember { mutableStateOf("0.0") }
    val today = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    var entryDate by remember { mutableStateOf(today) }

    val isDuplicate = existingRooms.contains(roomNumber.trim().lowercase())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Tenant & Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room Number") },
                    isError = isDuplicate
                )
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tenant Name") })
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = baseRent,
                    onValueChange = { baseRent = it },
                    label = { Text("Monthly Rent (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = ratePerUnit,
                    onValueChange = { ratePerUnit = it },
                    label = { Text("Rate / Unit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = initialReading,
                    onValueChange = { initialReading = it },
                    label = { Text("Initial Meter Reading (Check-in)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = entryDate, onValueChange = { entryDate = it }, label = { Text("Entry Date") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        roomNumber.trim(),
                        phone,
                        baseRent.toDoubleOrNull() ?: 0.0,
                        ratePerUnit.toDoubleOrNull() ?: 0.0,
                        initialReading.toDoubleOrNull() ?: 0.0,
                        entryDate
                    )
                },
                enabled = roomNumber.isNotBlank() && name.isNotBlank() && !isDuplicate
            ) { Text("Save Tenant") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CheckoutTenantDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    val today = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    var exitDate by remember { mutableStateOf(today) }
    var finalReading by remember { mutableStateOf(tenant.lastMeterReading.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tenant Checkout (Room ${tenant.roomNumber})", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mark ${tenant.name} as vacated. History and revenue will be preserved.")
                OutlinedTextField(value = exitDate, onValueChange = { exitDate = it }, label = { Text("Exit / Move-Out Date") })
                OutlinedTextField(
                    value = finalReading,
                    onValueChange = { finalReading = it },
                    label = { Text("Final Meter Reading") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(exitDate, finalReading.toDoubleOrNull() ?: tenant.lastMeterReading) },
                colors = ButtonDefaults.buttonColors(containerColor = WarningRed)
            ) { Text("Confirm Checkout") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddMonthlyBillDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double, String, String, String) -> Unit
) {
    var currReadingStr by remember { mutableStateOf("") }
    var baseRentStr by remember { mutableStateOf(tenant.defaultBaseRent.toString()) }
    var amountPaidStr by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("UPI") }

    val todayFormatted = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val defaultMonthYear = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()) }
    var paymentDate by remember { mutableStateOf(todayFormatted) }
    var monthYear by remember { mutableStateOf(defaultMonthYear) }

    val currReading = currReadingStr.toDoubleOrNull() ?: tenant.lastMeterReading
    val baseRent = baseRentStr.toDoubleOrNull() ?: 0.0
    val unitsConsumed = (currReading - tenant.lastMeterReading).coerceAtLeast(0.0)
    val elecAmount = unitsConsumed * tenant.electricityRatePerUnit
    val totalBill = baseRent + elecAmount
    val amountPaid = amountPaidStr.toDoubleOrNull() ?: totalBill
    val due = totalBill - amountPaid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Bill: Room ${tenant.roomNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Previous Meter: ${formatUnits(tenant.lastMeterReading)}", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = currReadingStr,
                    onValueChange = { currReadingStr = it },
                    label = { Text("Current Meter Reading") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = baseRentStr,
                    onValueChange = { baseRentStr = it },
                    label = { Text("Rent Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("Elec: ${formatUnits(unitsConsumed)} units × ₹${tenant.electricityRatePerUnit} = ${formatCurrency(elecAmount)}")
                Text("Total Payable: ${formatCurrency(totalBill)}", fontWeight = FontWeight.Bold, color = BrandPrimary)
                OutlinedTextField(
                    value = amountPaidStr,
                    onValueChange = { amountPaidStr = it },
                    label = { Text("Amount Paid (₹)") },
                    placeholder = { Text(formatCurrency(totalBill)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = paymentDate, onValueChange = { paymentDate = it }, label = { Text("Payment Date") })
                OutlinedTextField(value = monthYear, onValueChange = { monthYear = it }, label = { Text("Billing Month") })
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("UPI", "Cash", "Bank").forEach { mode ->
                        FilterChip(selected = paymentMode == mode, onClick = { paymentMode = mode }, label = { Text(mode) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(currReading, baseRent, amountPaid, paymentDate, paymentMode, monthYear) },
                enabled = currReadingStr.isNotBlank() && paymentDate.isNotBlank()
            ) { Text("Save Bill") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantHistoryBottomSheet(
    tenant: Tenant,
    viewModel: RentViewModel,
    onDismiss: () -> Unit
) {
    val bills by viewModel.getBillsForTenant(tenant.id).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val tenantTotalPaid = bills.sumOf { it.amountPaid }
    val tenantTotalDue = bills.sumOf { it.dueAmount }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = BrandBackground) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Room ${tenant.roomNumber} - ${tenant.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandDarkNavy)
            Text("Total Paid: ${formatCurrency(tenantTotalPaid)} | Due: ${formatCurrency(tenantTotalDue)}", fontSize = 13.sp, color = BrandSecondary)
            Spacer(modifier = Modifier.height(12.dp))

            if (bills.isEmpty()) {
                Text("No past billing records.", color = Color.Gray, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bills, key = { it.id }) { bill ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(bill.monthYear, fontWeight = FontWeight.Bold)
                                Text("Rent: ${formatCurrency(bill.baseRent)} | Elec: ${formatCurrency(bill.electricityAmount)} (${formatUnits(bill.unitsConsumed)} units)")
                                Text("Paid: ${formatCurrency(bill.amountPaid)} (${bill.paymentMode}) on ${bill.paymentDate}")
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = { shareReceiptOnWhatsApp(context, tenant, bill) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share WhatsApp Receipt")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun shareReceiptOnWhatsApp(context: Context, tenant: Tenant, bill: RentBill) {
    val sb = StringBuilder()
    sb.append("🏠 *RENT & ELECTRICITY RECEIPT*\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("👤 *Tenant:* ").append(tenant.name).append(" (Room ").append(tenant.roomNumber).append(")\n")
    sb.append("📅 *Billing Period:* ").append(bill.monthYear).append("\n")
    sb.append("🗓️ *Date of Payment:* ").append(bill.paymentDate).append("\n\n")
    sb.append("⚡ *Electricity Details:*\n")
    sb.append("• Previous Reading: ").append(formatUnits(bill.prevMeterReading)).append("\n")
    sb.append("• Current Reading: ").append(formatUnits(bill.currMeterReading)).append("\n")
    sb.append("• Units Consumed: ").append(formatUnits(bill.unitsConsumed)).append("\n")
    sb.append("• Rate / Unit: ").append(formatCurrency(bill.electricityRate)).append("\n")
    sb.append("• Total Electricity: ").append(formatCurrency(bill.electricityAmount)).append("\n\n")
    sb.append("🏢 *Base Rent:* ").append(formatCurrency(bill.baseRent)).append("\n")
    sb.append("🧾 *Total Amount:* ").append(formatCurrency(bill.totalBillAmount)).append("\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("✅ *Amount Paid:* ").append(formatCurrency(bill.amountPaid)).append(" (").append(bill.paymentMode).append(")\n")
    if (bill.dueAmount > 0) {
        sb.append("⚠️ *Pending Due:* ").append(formatCurrency(bill.dueAmount)).append("\n")
    } else {
        sb.append("✨ *Status:* Fully Paid\n")
    }
    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("Thank you!")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share Receipt via"))
}
