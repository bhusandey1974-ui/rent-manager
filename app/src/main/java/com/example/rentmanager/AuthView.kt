package com.example.rentmanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthView(
    onLoginSuccess: (email: String) -> Unit,
    onSkipOffline: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAppBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(UIBluePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isRegisterMode) "Create Account" else "Welcome Back",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CleanFont,
                color = UIDarkText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isRegisterMode) 
                    "Set up cloud backup to keep your records safe" 
                else 
                    "Sign in to access your synced properties",
                fontSize = 13.sp,
                fontFamily = CleanFont,
                color = UIMutedText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email address", fontFamily = CleanFont) },
                leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null, tint = UIMutedText) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UIBluePrimary,
                    unfocusedBorderColor = UICardBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password", fontFamily = CleanFont) },
                leadingIcon = { Icon(Icons.Default.LockOutline, contentDescription = null, tint = UIMutedText) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = UIMutedText
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UIBluePrimary,
                    unfocusedBorderColor = UICardBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            AnimatedVisibility(visible = isRegisterMode) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = { Text("Confirm password", fontFamily = CleanFont) },
                        leadingIcon = { Icon(Icons.Default.LockOutline, contentDescription = null, tint = UIMutedText) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UIBluePrimary,
                            unfocusedBorderColor = UICardBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = UIRedDanger,
                    fontSize = 12.sp,
                    fontFamily = CleanFont,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all required fields"
                    } else if (isRegisterMode && password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                    } else {
                        onLoginSuccess(email)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIBluePrimary)
            ) {
                Text(
                    text = if (isRegisterMode) "Create Account" else "Sign In",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CleanFont
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isRegisterMode) "Already have an account? " else "Don't have an account? ",
                    fontSize = 13.sp,
                    fontFamily = CleanFont,
                    color = UIMutedText
                )
                Text(
                    text = if (isRegisterMode) "Sign In" else "Register",
                    fontSize = 13.sp,
                    fontFamily = CleanFont,
                    fontWeight = FontWeight.Bold,
                    color = UIBluePrimary,
                    modifier = Modifier.clickable {
                        isRegisterMode = !isRegisterMode
                        errorMessage = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Continue Offline (Local Storage)",
                fontSize = 12.sp,
                fontFamily = CleanFont,
                fontWeight = FontWeight.Medium,
                color = UIMutedText,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSkipOffline() }
                    .padding(8.dp)
            )
        }
    }
}

