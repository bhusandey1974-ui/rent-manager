package com.example.rentmanager.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.rentmanager.PinStore
import com.example.rentmanager.AppPrefs
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import com.example.rentmanager.RentViewModel
import com.google.firebase.auth.FirebaseAuth

private data class ProfileRow(
    val emoji: String,
    val fg: Color,
    val bg: Color,
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
    val onClick: () -> Unit
)

/**
 * Profile tab. Pass [vm] to show the property / tenant counts (optional, so existing
 * calls like ProfileView(onOpenSettings = ...) still compile).
 * Rows without a screen yet show a "Coming soon" message.
 */
@Composable
fun ProfileView(
    onOpenSettings: () -> Unit,
    vm: RentViewModel? = null,
    onOpenProperties: () -> Unit = {},
    onOpenRevenue: () -> Unit = {},
    onOpenAlerts: () -> Unit = {}
) {
    val context = LocalContext.current
    val soon = { name: String -> Toast.makeText(context, "$name is coming soon", Toast.LENGTH_SHORT).show() }
    var dialog by remember { mutableStateOf<String?>(null) }
    var pinEnabled by remember { mutableStateOf(PinStore.isEnabled(context)) }

    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName?.takeIf { it.isNotBlank() } ?: if (user != null) "Signed in" else "Guest"
    val email = user?.email ?: "Data is stored on this device only"
    val signedIn = user != null

    val propertyCount = vm?.properties?.collectAsState()?.value?.size
    val tenantCount = vm?.tenants?.collectAsState()?.value?.size

    val blue = Color(0xFF1565C0); val blueBg = Color(0xFFE3EEFC)
    val green = Color(0xFF1B8A5A); val greenBg = Color(0xFFE0F4EA)
    val orange = Color(0xFFE67E00); val orangeBg = Color(0xFFFFF0DC)
    val purple = Color(0xFF7B2FBF); val purpleBg = Color(0xFFF0E8FB)
    val red = Color(0xFFD32F2F); val redBg = Color(0xFFFDE7E9)

    when (dialog) {
        "properties" -> if (vm != null) PropertiesDialog(vm, onPicked = { dialog = null; onOpenProperties() }, onDismiss = { dialog = null })
        "tenants" -> if (vm != null) TenantsDialog(vm, onDismiss = { dialog = null })
        "payments" -> if (vm != null) PaymentMethodsDialog(vm, onDismiss = { dialog = null })
        "documents" -> DocumentsDialog(onDismiss = { dialog = null })
        "appearance" -> AppearanceDialog(onDismiss = { dialog = null })
        "about" -> AboutDialog(onDismiss = { dialog = null })
        "security" -> SecurityDialog(onChanged = { pinEnabled = PinStore.isEnabled(context) }, onDismiss = { dialog = null })
    }

    Scaffold(containerColor = AppColors.ScaffoldBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Text("Your account and settings", fontSize = 11.sp, color = AppColors.TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))

            // Account card + backup banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onOpenSettings() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(AppColors.AzureContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Person, null, tint = AppColors.AzurePrimary, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Text(email, fontSize = 12.sp, color = AppColors.TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (signedIn) "☁️  Cloud Sync" else "📱  Local Storage",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = green,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(greenBg).padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = AppColors.TextMuted)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.AzureContainer).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ℹ️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (signedIn) "Your data is synced to the cloud." else "Create a backup to keep your data safe.",
                            fontSize = 12.sp,
                            color = AppColors.AzurePrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onOpenSettings,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.AzurePrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) { Text("☁ Backup Now", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
                    }
                }
            }

            ProfileSection(
                "Property Management",
                listOf(
                    ProfileRow("🏢", blue, blueBg, "My Properties", "Manage your buildings and rooms",
                        propertyCount?.let { "$it ${if (it == 1) "property" else "properties"}" }) { dialog = "properties" },
                    ProfileRow("👥", green, greenBg, "Tenants", "View and manage tenants",
                        tenantCount?.let { "$it ${if (it == 1) "tenant" else "tenants"}" }) { dialog = "tenants" },
                    ProfileRow("📄", orange, orangeBg, "Documents", "Rent agreements, IDs and more") { dialog = "documents" },
                    ProfileRow("💳", purple, purpleBg, "Payment Methods", "Manage how you record payments") { dialog = "payments" }
                )
            )
            ProfileSection(
                "App & Preferences",
                listOf(
                    ProfileRow("⚙️", blue, blueBg, "Settings", "App preferences and general settings") { onOpenSettings() },
                    ProfileRow("🔔", red, redBg, "Notifications", "Rent due, bill reminders and alerts") { onOpenAlerts() },
                    ProfileRow("🛡️", green, greenBg, "Security & PIN", "Protect your app with a 4-digit PIN", if (pinEnabled) "On" else "Off") { dialog = "security" },
                    ProfileRow("🎨", purple, purpleBg, "Appearance", "Text size and display options") { dialog = "appearance" },
                    ProfileRow("₹", orange, orangeBg, "Currency & Financial Settings", "Set currency, tax and financial preferences") { soon("Currency settings") }
                )
            )
            ProfileSection(
                "Data & Backup",
                listOf(
                    ProfileRow("☁️", blue, blueBg, "Backup & Restore", "Backup your data to cloud or device") { onOpenSettings() },
                    ProfileRow("⬇️", green, greenBg, "Export Data", "Save all bills as a CSV file (opens in Excel)") { if (vm != null) exportBillsCsv(context, vm) else soon("Export Data") },
                    ProfileRow("🕘", red, redBg, "Restore Data", "Restore from a previous backup") { onOpenSettings() }
                )
            )
            ProfileSection(
                "Reports",
                listOf(ProfileRow("📊", purple, purpleBg, "Financial Reports", "Monthly, annual and custom reports") { onOpenRevenue() })
            )
            ProfileSection(
                "About",
                listOf(ProfileRow("ℹ️", blue, blueBg, "About Rent Manager", "App version, privacy policy and support") { dialog = "about" })
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProfileSection(title: String, rows: List<ProfileRow>) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            rows.forEachIndexed { index, r ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { r.onClick() }.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(r.bg),
                        contentAlignment = Alignment.Center
                    ) { Text(r.emoji, fontSize = 18.sp, color = r.fg, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                        Text(r.subtitle, fontSize = 11.sp, color = AppColors.TextSecondary)
                    }
                    if (r.trailing != null) {
                        Text(r.trailing, fontSize = 11.sp, color = AppColors.TextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(start = 64.dp, end = 12.dp), color = AppColors.TextMuted.copy(alpha = 0.2f))
                }
            }
        }
    }
}

// ============================================================================
// Dialogs and helpers for the Profile rows
// ============================================================================

private fun money(v: Double): String = "₹" + String.format(Locale.ENGLISH, "%,.2f", v)

@Composable
private fun TenantsDialog(vm: RentViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val tenants by vm.tenants.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val bills by vm.bills.collectAsState()
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH) }
    val sorted = tenants.sortedWith(
        compareByDescending<com.example.rentmanager.Tenant> { it.isCurrent }
            .thenByDescending { it.moveInDate }
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tenants", fontWeight = FontWeight.Bold) },
        text = {
            if (sorted.isEmpty()) {
                Text("No tenants yet. Add one from a vacant room.")
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    sorted.forEach { t ->
                        val room = rooms.find { it.id == t.roomId }
                        val due = bills.filter { it.tenantId == t.id }.sumOf { it.remainingDue }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = t.phoneNumber.isNotBlank()) {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + t.phoneNumber)))
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                Text(
                                    if (t.isCurrent) "Current" else "Past",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (t.isCurrent) AppColors.EmeraldSuccess else AppColors.TextMuted
                                )
                            }
                            Text("Room ${room?.roomNumber ?: "-"} · ${t.phoneNumber}", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Text("Moved in: ${dateFmt.format(Date(t.moveInDate))}", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Text(
                                if (t.moveOutDate != null) "Moved out: ${dateFmt.format(Date(t.moveOutDate))}" else "Still staying",
                                fontSize = 12.sp, color = AppColors.TextSecondary
                            )
                            if (t.permanentAddress.isNotBlank()) {
                                Text("Address: ${t.permanentAddress}", fontSize = 12.sp, color = AppColors.TextSecondary)
                            }
                            if (t.aadhaarNumber.isNotBlank()) {
                                Text("Aadhaar: ${t.aadhaarNumber}", fontSize = 12.sp, color = AppColors.TextSecondary)
                            }
                            if (t.securityDeposit > 0.0) {
                                val refund = when (t.depositRefunded) {
                                    true -> " (refunded)"
                                    false -> " (not refunded)"
                                    null -> ""
                                }
                                Text("Deposit: ${money(t.securityDeposit)}$refund", fontSize = 12.sp, color = AppColors.TextSecondary)
                            }
                            if (due > 0.0) {
                                Text(money(due) + " due", fontSize = 12.sp, color = AppColors.CrimsonAlert, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        HorizontalDivider(color = AppColors.TextMuted.copy(alpha = 0.2f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap a tenant to call.", fontSize = 11.sp, color = AppColors.TextMuted)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun TenantsDialog(vm: RentViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val tenants by vm.tenants.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val current = tenants.filter { it.isCurrent }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tenants", fontWeight = FontWeight.Bold) },
        text = {
            if (current.isEmpty()) {
                Text("No tenants yet. Add one from a vacant room.")
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    current.forEach { t ->
                        val room = rooms.find { it.currentTenantId == t.id }
                        val due = if (room != null) vm.getPendingDueForCurrentTenant(room.id) else 0.0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = t.phoneNumber.isNotBlank()) {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + t.phoneNumber)))
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(
                                    "Room ${room?.roomNumber ?: "-"} · ${t.phoneNumber}",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                            if (due > 0.0) {
                                Text(money(due) + " due", fontSize = 12.sp, color = AppColors.CrimsonAlert, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap a tenant to call.", fontSize = 11.sp, color = AppColors.TextMuted)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun PaymentMethodsDialog(vm: RentViewModel, onDismiss: () -> Unit) {
    val bills by vm.bills.collectAsState()
    val groups = bills
        .groupBy { it.paymentMode.toString() }
        .map { (mode, list) -> Triple(mode, list.size, list.sumOf { it.amountPaid }) }
        .sortedByDescending { it.third }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment Methods", fontWeight = FontWeight.Bold) },
        text = {
            if (groups.isEmpty()) {
                Text("No payments recorded yet.")
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("How payments were recorded so far:", fontSize = 12.sp, color = AppColors.TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    groups.forEach { (mode, count, total) ->
                        Row(
                            modifier = Modifier
                              .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mode, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("$count payments", fontSize = 12.sp, color = AppColors.TextSecondary)
                            }
                            Text(money(total), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

private data class DocItem(val name: String, val uri: String)

private object DocStore {
    private const val PREFS = "rm_docs"
    private const val KEY = "items"

    fun load(c: Context): List<DocItem> {
        val raw = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                DocItem(o.getString("name"), o.getString("uri"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(c: Context, items: List<DocItem>) {
        val arr = JSONArray()
        items.forEach { arr.put(JSONObject().put("name", it.name).put("uri", it.uri)) }
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }
}

@Composable
private fun DocumentsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var items by remember { mutableStateOf(DocStore.load(context)) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { }
            var name = "Document"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) name = cursor.getString(idx) ?: name
            }
            items = items + DocItem(name, uri.toString())
            DocStore.save(context, items)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Documents", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (items.isEmpty()) {
                    Text("Add rent agreements, ID proofs or any file. They stay on this device.", fontSize = 13.sp)
                }
                items.forEach { d ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    val u = Uri.parse(d.uri)
                                    val type = context.contentResolver.getType(u) ?: "*/*"
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW)
                                            .setDataAndType(u, type)
                                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open this file", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(d.name, modifier = Modifier.weight(1f), fontSize = 14.sp, maxLines = 2)
                        TextButton(onClick = {
                            items = items.filter { it.uri != d.uri }
                            DocStore.save(context, items)
                        }) { Text("Remove", color = AppColors.CrimsonAlert, fontSize = 12.sp) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { picker.launch(arrayOf("*/*")) }) { Text("Add document") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun AppearanceDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val options = listOf("Small" to 0.9f, "Default" to 1.0f, "Large" to 1.15f, "Extra large" to 1.3f)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Text size", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { (label, scale) ->
                    val selected = kotlin.math.abs(AppPrefs.textScale - scale) < 0.01f
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { AppPrefs.setTextScale(context, scale) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        if (selected) Text("✓", color = AppColors.AzurePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val version = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "" } catch (e: Exception) { "" }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rent Manager", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Version $version", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Manage properties, tenants, rent and bills in one place.", fontSize = 13.sp, color = AppColors.TextSecondary)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

private enum class PinStep { CURRENT, NEW, CONFIRM }

@Composable
private fun SecurityDialog(onChanged: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(PinStore.isEnabled(context)) }
    var flow by remember { mutableStateOf<String?>(null) }

    if (flow == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Security & PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        if (enabled) "PIN lock is ON. The app asks for your PIN when it opens or after it has been in the background for 30 seconds."
                        else "Protect your tenants' data with a 4-digit PIN.",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (enabled) {
                        TextButton(onClick = { flow = "change" }) { Text("Change PIN") }
                        TextButton(onClick = { flow = "off" }) { Text("Turn off PIN", color = AppColors.CrimsonAlert) }
                    } else {
                        TextButton(onClick = { flow = "set" }) { Text("Set PIN") }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
        )
    } else {
        val mode = flow ?: "set"
        val steps = when (mode) {
            "change" -> listOf(PinStep.CURRENT, PinStep.NEW, PinStep.CONFIRM)
            "off" -> listOf(PinStep.CURRENT)
            else -> listOf(PinStep.NEW, PinStep.CONFIRM)
        }
        var stepIndex by remember { mutableStateOf(0) }
        var input by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        val step = steps[stepIndex]

        fun submit() {
            if (input.length != 4) {
                error = "PIN must be 4 digits"
                return
            }
            when (step) {
                PinStep.CURRENT -> {
                    if (!PinStore.verify(context, input)) {
                        error = "Wrong PIN"
                    } else if (mode == "off") {
                        PinStore.clear(context)
                        enabled = false
                        Toast.makeText(context, "PIN turned off", Toast.LENGTH_SHORT).show()
                        onChanged()
                        flow = null
                    } else {
                        stepIndex += 1
                        input = ""
                    }
                }
                PinStep.NEW -> {
                    newPin = input
                    stepIndex += 1
                    input = ""
                }
                PinStep.CONFIRM -> {
                    if (input == newPin) {
                        PinStore.set(context, newPin)
                        enabled = true
                        Toast.makeText(context, "PIN saved", Toast.LENGTH_SHORT).show()
                        onChanged()
                        flow = null
                    } else {
                        error = "PINs do not match"
                        input = ""
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { flow = null },
            title = {
                Text(
                    when (step) {
                        PinStep.CURRENT -> "Enter current PIN"
                        PinStep.NEW -> "Enter new 4-digit PIN"
                        PinStep.CONFIRM -> "Confirm new PIN"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { v -> if (v.length <= 4 && v.all { it.isDigit() }) { input = v; error = null } },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = error != null
                    )
                    if (error != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(error ?: "", color = AppColors.CrimsonAlert, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { submit() }) { Text(if (stepIndex == steps.lastIndex) "Done" else "Next") }
            },
            dismissButton = { TextButton(onClick = { flow = null }) { Text("Cancel") } }
        )
    }
}

/** Saves all billing records as a CSV file in Downloads (or shares it as text on older phones). */
private fun exportBillsCsv(context: Context, vm: RentViewModel) {
    val bills = vm.bills.value
    if (bills.isEmpty()) {
        Toast.makeText(context, "No billing records to export", Toast.LENGTH_SHORT).show()
        return
    }
    fun esc(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
    val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val sb = StringBuilder()
    sb.append("Date,Room,Tenant,Rent,Electricity,Maintenance,Total Payable,Paid,Remaining Due,Payment Mode\n")
    bills.sortedBy { it.timestamp }.forEach { b ->
        val room = vm.getRoomForBill(b)
        val tenant = vm.getTenantForBill(b)
        sb.append(dateFmt.format(Date(b.timestamp))).append(',')
            .append(esc(room?.roomNumber?.toString() ?: "")).append(',')
            .append(esc(tenant?.name ?: "")).append(',')
            .append(String.format(Locale.ENGLISH, "%.2f", b.baseRent)).append(',')
            .append(String.format(Locale.ENGLISH, "%.2f", b.electricityAmount)).append(',')
            .append(String.format(Locale.ENGLISH, "%.2f", b.maintenanceAmount)).append(',')
            .append(String.format(Locale.ENGLISH, "%.2f", b.totalPayable)).append(',')
            .append(String.format(Locale.ENGLISH, "%.2f", b.amountPaid)).append(',')
            .append(String.format(Locale.ENGLISH, "%.2f", b.remainingDue)).append(',')
            .append(esc(b.paymentMode.toString())).append('\n')
    }
    val csv = sb.toString()
    val fileName = "rent_manager_bills_" + SimpleDateFormat("yyyyMMdd_HHmm", Locale.ENGLISH).format(Date()) + ".csv"

    if (Build.VERSION.SDK_INT >= 29) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                return
            }
        } catch (e: Exception) { /* fall through to sharing */ }
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, fileName)
        putExtra(Intent.EXTRA_TEXT, csv)
    }
    context.startActivity(Intent.createChooser(send, "Export bills"))
}
