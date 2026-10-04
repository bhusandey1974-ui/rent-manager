package com.example.rentmanager.ui.screens

import android.app.Activity
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
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
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
    val tenantCount = vm?.tenants?.collectAsState()?.value?.count { it.isCurrent }

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
        "account" -> AccountDialog(
            name = name,
            email = email,
            signedIn = signedIn,
            vm = vm,
            onExport = { dialog = "export" },
            onDismiss = { dialog = null }
        )
        "export" -> if (vm != null) ExportDialog(
            onExport = { thisYear, share -> exportBillsXlsx(context, vm, thisYear, share) },
            onDismiss = { dialog = null }
        )
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
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { dialog = "account" },
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
                            onClick = {
                                if (signedIn && vm != null) {
                                    vm.refreshFromCloud()
                                    Toast.makeText(context, "Syncing with the cloud…", Toast.LENGTH_SHORT).show()
                                } else {
                                    dialog = "account"
                                }
                            },
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
                    ProfileRow("☁️", blue, blueBg, "Backup & Restore", "Sync to the cloud or export a copy") { dialog = "account" },
                    ProfileRow("⬇️", green, greenBg, "Export Data", "Save your bills as an Excel file") { dialog = "export" },
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
private fun PropertiesDialog(vm: RentViewModel, onPicked: () -> Unit, onDismiss: () -> Unit) {
    val properties by vm.properties.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val selectedId by vm.selectedPropertyId.collectAsState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Properties", fontWeight = FontWeight.Bold) },
        text = {
            if (properties.isEmpty()) {
                Text("No properties yet.")
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    properties.forEach { prop ->
                        val propRooms = rooms.filter { it.propertyId == prop.id }
                        val occupied = propRooms.count { it.isOccupied }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    vm.setSelectedProperty(prop.id)
                                    onPicked()
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prop.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(
                                    "${propRooms.size} rooms · $occupied occupied",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                            if (prop.id == selectedId) {
                                Text("Selected", fontSize = 11.sp, color = AppColors.AzurePrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
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
