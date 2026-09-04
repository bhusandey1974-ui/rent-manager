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
