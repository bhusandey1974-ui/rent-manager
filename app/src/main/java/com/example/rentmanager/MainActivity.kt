package com.example.rentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import java.util.Locale
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.rentmanager.ui.components.BannerAdView
import com.example.rentmanager.ui.components.SettingsDialog
import com.example.rentmanager.ui.screens.AuthView
import com.example.rentmanager.ui.screens.ExpensesView
import com.example.rentmanager.ui.screens.ProfileView
import com.example.rentmanager.ui.screens.PropertiesView
import com.example.rentmanager.ui.screens.RevenueView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth

// ---------------------------------------------------------------------------
// App-wide font (Inter). Requires these files in app/src/main/res/font/:
//   inter_regular.ttf, inter_medium.ttf, inter_semibold.ttf, inter_bold.ttf
// ---------------------------------------------------------------------------
val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private fun interTypography(): Typography {
    val b = Typography()
    return Typography(
        displayLarge = b.displayLarge.copy(fontFamily = InterFamily),
        displayMedium = b.displayMedium.copy(fontFamily = InterFamily),
        displaySmall = b.displaySmall.copy(fontFamily = InterFamily),
        headlineLarge = b.headlineLarge.copy(fontFamily = InterFamily),
        headlineMedium = b.headlineMedium.copy(fontFamily = InterFamily),
        headlineSmall = b.headlineSmall.copy(fontFamily = InterFamily),
        titleLarge = b.titleLarge.copy(fontFamily = InterFamily),
        titleMedium = b.titleMedium.copy(fontFamily = InterFamily),
        titleSmall = b.titleSmall.copy(fontFamily = InterFamily),
        bodyLarge = b.bodyLarge.copy(fontFamily = InterFamily),
        bodyMedium = b.bodyMedium.copy(fontFamily = InterFamily),
        bodySmall = b.bodySmall.copy(fontFamily = InterFamily),
        labelLarge = b.labelLarge.copy(fontFamily = InterFamily),
        labelMedium = b.labelMedium.copy(fontFamily = InterFamily),
        labelSmall = b.labelSmall.copy(fontFamily = InterFamily)
    )
}

/** Wraps content so every Text (including ones with no explicit style) uses Inter,
 *  while keeping whatever colors/shapes RentManagerTheme already set. */
@Composable
private fun WithInterFont(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        shapes = MaterialTheme.shapes,
        typography = interTypography()
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.merge(TextStyle(fontFamily = InterFamily))
        ) {
            content()
        }
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.rgb(42, 107, 85) // matches AppColors.HeaderTop
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        MobileAds.initialize(this) {}
        setContent {
            RentManagerTheme {
                WithInterFont {
                    androidx.compose.material3.Surface(
                        color = AppColors.ScaffoldBackground,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        MainAppRoot(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppRoot(viewModel: RentViewModel) {
    val auth = remember { FirebaseAuth.getInstance() }
    val haptic = LocalHapticFeedback.current

    var isAuthenticated by remember { mutableStateOf(auth.currentUser != null) }
    var currentTabIndex by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAlertsDialog by remember { mutableStateOf(false) }
    var showAddPropertyDialog by remember { mutableStateOf(false) }

    val rooms by viewModel.rooms.collectAsState()
    val tenants by viewModel.tenants.collectAsState()
    val dueRooms = rooms.filter { it.isOccupied && viewModel.getPendingDueForCurrentTenant(it.id) > 0.0 }

    if (!isAuthenticated) {
        AuthView(
            onAuthSuccess = {
                isAuthenticated = true
                viewModel.refreshFromCloud()
            },
            onContinueAsGuest = { isAuthenticated = true }
        )
    } else {
        Scaffold(
            topBar = {
                com.example.rentmanager.ui.components.WaveHeader(
                    title = "Rent Manager",
                    subtitle = if (currentTabIndex == 0) "Manage Smarter. Earn Better." else "Manage Properties, Grow Smarter.",
                    onSettingsClick = { showSettingsDialog = true },
                    hasAlerts = dueRooms.isNotEmpty(),
                    onBellClick = { showAlertsDialog = true },
                    onAddPropertyClick = if (currentTabIndex == 0) ({ showAddPropertyDialog = true }) else null
                )
            },
            bottomBar = {
                Column {
                BannerAdView(
                    adUnitId = "ca-app-pub-4334614941668154/7590527977",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Surface(
                    color = AppColors.SurfaceWhite,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 12.dp
                ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    val tabs: List<Pair<String, ImageVector>> = listOf(
                        "Properties" to Icons.Rounded.Apartment,
                        "Revenue" to Icons.Rounded.MonetizationOn,
                        "Expenses" to Icons.Rounded.ReceiptLong,
                        "Profile" to Icons.Rounded.Person
                    )
                    tabs.forEachIndexed { index, (name, icon) ->
                        NavigationBarItem(
                            selected = currentTabIndex == index,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentTabIndex = index
                            },
                            icon = { Icon(icon, contentDescription = name) },
                            label = { Text(name, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.AzurePrimary,
                                selectedTextColor = AppColors.AzurePrimary,
                                indicatorColor = AppColors.AzureContainer
                            )
                        )
                    }
                }
                }
                }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = currentTabIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) togetherWith
                            fadeOut(animationSpec = androidx.compose.animation.core.tween(180))
                    },
                    label = "tab_content"
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> PropertiesView(
                            vm = viewModel,
                            onNavigateToRevenue = { currentTabIndex = 1 },
                            addPropertyRequested = showAddPropertyDialog,
                            onAddPropertyHandled = { showAddPropertyDialog = false }
                        )
                        1 -> RevenueView(vm = viewModel, onAddRecord = { currentTabIndex = 0 })
                        2 -> ExpensesView(vm = viewModel)
                        3 -> ProfileView(onOpenSettings = { showSettingsDialog = true })
                    }
                }
            }
        }

        if (showAlertsDialog) {
            AlertDialog(
                onDismissRequest = { showAlertsDialog = false },
                title = { Text("Alerts", fontWeight = FontWeight.Bold) },
                text = {
                    if (dueRooms.isEmpty()) {
                        Text("No pending dues. You're all caught up.")
                    } else {
                        Column {
                            dueRooms.forEach { room ->
                                val tenantName = tenants.find { it.id == room.currentTenantId }?.name ?: "Tenant"
                                val due = viewModel.getPendingDueForCurrentTenant(room.id)
                                Text(
                                    text = "Room ${room.roomNumber} · $tenantName · ₹${String.format(Locale.ENGLISH, "%,.0f", due)} due",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAlertsDialog = false }) { Text("Close") }
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                vm = viewModel,
                onDismiss = { showSettingsDialog = false },
                onSignOutSuccess = {
                    showSettingsDialog = false
                    isAuthenticated = false
                }
            )
        }
    }
}
