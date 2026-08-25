package com.example.rentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.rentmanager.ui.MainScreen
import com.example.rentmanager.viewmodel.RentViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: RentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = ViewModelProvider(this)[RentViewModel::class.java]
        
        setContent {
            MainScreen(viewModel = viewModel)
        }
    }
}

