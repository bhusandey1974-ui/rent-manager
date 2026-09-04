package com.example.rentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.ui.components.SettingsDialog
import com.example.rentmanager.ui.screens.AuthView
import com.example.rentmanager.ui.screens.PropertiesView
import com.example.rentmanager.ui.screens.RevenueView
import com.example.rentmanager.ui.theme.RentManagerTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val viewModel: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentManagerTheme {
                MainAppRoot(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppRoot(viewModel: RentViewModel) {
    val auth = remember { FirebaseAuth.getInstance() }
    
    // Check if user is logged into Firebase or opted for offline mode
    var isAuthenticated by remember { mutableStateOf(auth.currentUser != null) }
    var currentTabIndex by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (!isAuthenticated) {
        AuthView(
            onAuthSuccess = { isAuthenticated = true },
            onContinueAsGuest = { isAuthenticated = true }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (currentTabIndex == 0) "Rent Manager" else "Financial Ledger",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = AppColors.TextPrimary
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings & Account",
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppColors.SurfaceWhite
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = AppColors.SurfaceWhite,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentTabIndex == 0,
                        onClick = { currentTabIndex = 0 },
                        icon = {
                            Icon(Icons.Rounded.Apartment, contentDescription = "Properties")
                        },
                        label = { Text("Properties", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.AzurePrimary,
                            selectedTextColor = AppColors.AzurePrimary,
                            indicatorColor = AppColors.AzureContainer
                        )
                    )

                    NavigationBarItem(
                        selected = currentTabIndex == 1,
                        onClick = { currentTabIndex = 1 },
                        icon = {
                            Icon(Icons.Rounded.Insights, contentDescription = "Revenue")
                        },
                        label = { Text("Revenue", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.AzurePrimary,
                            selectedTextColor = AppColors.AzurePrimary,
                            indicatorColor = AppColors.AzureContainer
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTabIndex) {
                    0 -> PropertiesView(
                        vm = viewModel,
                        onNavigateToRevenue = { currentTabIndex = 1 }
                    )
                    1 -> RevenueView(vm = viewModel)
                }
            }
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
package com.example.rentmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.rentmanager.AppColors
import com.example.rentmanager.RentViewModel

@Composable
fun SettingsDialog(
    vm: RentViewModel,
    onDismiss: () -> Unit,
    onSignOutSuccess: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }
    val userEmail = vm.getCurrentUserEmail()
    val isCloud = vm.isCloudConnected()

    if (showConfirmDelete) {
        DeleteConfirmationDialog(
            title = "Erase All App Data?",
            message = "This permanently deletes all rooms, tenants, billing history, and balances from both your device and cloud storage. This cannot be undone.",
            onDismiss = { showConfirmDelete = false },
            onConfirm = {
                showConfirmDelete = false
                vm.clearAllData {
                    onDismiss()
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AppColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account & Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = AppColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Account Info Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.AzureContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AppColors.AzurePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isCloud) (userEmail ?: "Logged In") else "Local Offline Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = if (isCloud) "Cloud Sync Enabled" else "Records stored on this device only",
                                fontSize = 11.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sign Out Option
                OutlinedButton(
                    onClick = {
                        vm.signOut {
                            onDismiss()
                            onSignOutSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AppColors.BorderSubtle)
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp), tint = AppColors.TextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCloud) "Sign Out" else "Exit to Login Screen",
                        color = AppColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = AppColors.BorderSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                // Delete All Data Option
                Button(
                    onClick = { showConfirmDelete = true },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.CrimsonAlert.copy(alpha = 0.1f),
                        contentColor = AppColors.CrimsonAlert
                    ),
                    border = BorderStroke(1.dp, AppColors.CrimsonAlert.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete All Property Data", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
