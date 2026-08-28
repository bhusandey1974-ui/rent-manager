package com.example.rentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: RentViewModel = viewModel()
                val currentUser by vm.authRepo.currentUser.collectAsState()

                if (currentUser == null) {
                    AuthScreen(
                        authRepo = vm.authRepo,
                        onLoginSuccess = { uid -> vm.listenToUserData(uid) }
                    )
                } else {
                    RentManagerMainApp(vm)
                }
            }
        }
    }
}
