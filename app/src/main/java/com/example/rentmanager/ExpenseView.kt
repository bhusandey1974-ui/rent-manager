package com.example.rentmanager.ui.screens

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
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import com.example.rentmanager.RentViewModel
import com.google.firebase.auth.FirebaseAuth

/** Expenses tab: holds the withdrawals / net cash card that used to sit on the Revenue screen. */
@Composable
fun ExpensesView(vm: RentViewModel) {
    // Net cash is a lifetime figure, so this uses lifetime collections.
    val summary = vm.getRevenueSummary(forCurrentYearOnly = false)

    Scaffold(containerColor = AppColors.ScaffoldBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Expenses",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = "Withdrawals and cash balance",
                fontSize = 11.sp,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))

            WithdrawalsCard(
                vm = vm,
                totalCollected = summary.totalCollected,
                forCurrentYearOnly = false
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/** Profile tab: who is signed in, plus a shortcut to the existing Settings dialog. */
@Composable
fun ProfileView(onOpenSettings: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName?.takeIf { it.isNotBlank() } ?: if (user != null) "Signed in" else "Guest"
    val email = user?.email ?: "Data is stored on this device only"

    Scaffold(containerColor = AppColors.ScaffoldBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = "Your account and settings",
                fontSize = 11.sp,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AppColors.AzureContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = AppColors.AzurePrimary, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Text(email, fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenSettings() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = AppColors.AzurePrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Settings, backup & sign out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AppColors.TextMuted)
                }
            }
        }
    }
}
