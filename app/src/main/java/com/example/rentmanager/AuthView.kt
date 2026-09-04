package com.example.rentmanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthView(
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.ScaffoldBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                border = BorderStroke(1.dp, AppColors.BorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Header
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AppColors.AzurePrimary, AppColors.AzureDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Apartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isSignUp) "Create Account" else "Welcome Back",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Text(
                        text = if (isSignUp) "Sign up to sync property records across devices" else "Sign in to manage rooms, bills & tenants",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("landlord@example.com") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Rounded.Email, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = AppColors.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AzurePrimary,
                            unfocusedBorderColor = AppColors.BorderSubtle,
                            focusedLabelColor = AppColors.AzurePrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                                        // Confirm Password (Sign-up only)
                    AnimatedVisibility(visible = isSignUp) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Confirm Password") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.AzurePrimary,
                                    unfocusedBorderColor = AppColors.BorderSubtle,
                                    focusedLabelColor = AppColors.AzurePrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Error Message
                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let { msg ->
                            Text(
                                text = msg,
                                color = AppColors.CrimsonAlert,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Button (Sign In / Sign Up)
                    Button(
                        onClick = {
                            val cleanEmail = email.trim()
                            val cleanPass = password.trim()

                            if (cleanEmail.isBlank() || cleanPass.isBlank()) {
                                errorMessage = "Email and password cannot be empty."
                                return@Button
                            }

                            if (isSignUp && cleanPass != confirmPassword.trim()) {
                                errorMessage = "Passwords do not match."
                                return@Button
                            }

                            if (cleanPass.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null

                            if (isSignUp) {
                                auth.createUserWithEmailAndPassword(cleanEmail, cleanPass)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onAuthSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Failed to create account."
                                    }
                            } else {
                                auth.signInWithEmailAndPassword(cleanEmail, cleanPass)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onAuthSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Invalid email or password."
                                    }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (isSignUp) "Sign Up" else "Sign In",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toggle Sign In vs Sign Up
                    TextButton(
                        onClick = {
                            isSignUp = !isSignUp
                            errorMessage = null
                        }
                    ) {
                        Text(
                            text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                            fontSize = 12.sp,
                            color = AppColors.AzurePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = AppColors.BorderSubtle)

                    // Skip / Continue with Local Storage Only
                    OutlinedButton(
                        onClick = onContinueAsGuest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle)
                    ) {
                        Text("Continue in Offline / Local Mode", color = AppColors.TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
