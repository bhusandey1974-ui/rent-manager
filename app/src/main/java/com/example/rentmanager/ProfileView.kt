package com.example.rentmanager.ui.screens

import android.widget.Toast
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
fun ProfileView(onOpenSettings: () -> Unit, vm: RentViewModel? = null) {
    val context = LocalContext.current
    val soon = { name: String -> Toast.makeText(context, "$name is coming soon", Toast.LENGTH_SHORT).show() }

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
                        propertyCount?.let { "$it ${if (it == 1) "property" else "properties"}" }) { soon("My Properties") },
                    ProfileRow("👥", green, greenBg, "Tenants", "View and manage tenants",
                        tenantCount?.let { "$it ${if (it == 1) "tenant" else "tenants"}" }) { soon("Tenants") },
                    ProfileRow("📄", orange, orangeBg, "Documents", "Rent agreements, IDs and more") { soon("Documents") },
                    ProfileRow("💳", purple, purpleBg, "Payment Methods", "Manage how you record payments") { soon("Payment Methods") }
                )
            )
            ProfileSection(
                "App & Preferences",
                listOf(
                    ProfileRow("⚙️", blue, blueBg, "Settings", "App preferences and general settings") { onOpenSettings() },
                    ProfileRow("🔔", red, redBg, "Notifications", "Rent due, bill reminders and alerts") { soon("Notifications") },
                    ProfileRow("🛡️", green, greenBg, "Security & PIN", "Protect your app with a PIN or biometrics") { soon("Security & PIN") },
                    ProfileRow("🎨", purple, purpleBg, "Appearance", "Theme, language and display options") { soon("Appearance") },
                    ProfileRow("₹", orange, orangeBg, "Currency & Financial Settings", "Set currency, tax and financial preferences") { soon("Currency settings") }
                )
            )
            ProfileSection(
                "Data & Backup",
                listOf(
                    ProfileRow("☁️", blue, blueBg, "Backup & Restore", "Backup your data to cloud or device") { onOpenSettings() },
                    ProfileRow("⬇️", green, greenBg, "Export Data", "Export to Excel, PDF or CSV") { soon("Export Data") },
                    ProfileRow("🕘", red, redBg, "Restore Data", "Restore from a previous backup") { onOpenSettings() }
                )
            )
            ProfileSection(
                "Reports",
                listOf(ProfileRow("📊", purple, purpleBg, "Financial Reports", "Monthly, annual and custom reports") { soon("Financial Reports") })
            )
            ProfileSection(
                "About",
                listOf(ProfileRow("ℹ️", blue, blueBg, "About Rent Manager", "App version, privacy policy and support") { soon("About") })
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
