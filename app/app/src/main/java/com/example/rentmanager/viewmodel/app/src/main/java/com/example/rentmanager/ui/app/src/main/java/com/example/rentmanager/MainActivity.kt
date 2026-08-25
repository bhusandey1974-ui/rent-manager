package com.example.rentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.example.rentmanager.ui.MainScreen
import com.example.rentmanager.viewmodel.RentViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: RentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
