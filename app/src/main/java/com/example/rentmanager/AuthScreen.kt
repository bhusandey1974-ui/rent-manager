package com.example.rentmanager

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(
    authRepo: AuthRepository,
    webClientId: String = "",
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    var phoneNo by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        authRepo.firebaseAuthWithGoogle(
            data = result.data,
            onSuccess = { user ->
                isLoading = false
                onLoginSuccess(user.uid)
            },
            onError = { err ->
                isLoading = false
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(listOf(SkyBlueGradientStart, SkyBlueGradientEnd)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Cloud Backup & Sync",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Text(
                text = "Sign in to keep all properties, rooms & payment records safe across reinstalls.",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val client = authRepo.getGoogleSignInClient(context as Activity, webClientId)
                            googleSignInLauncher.launch(client.signInIntent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google (Gmail)",
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), thickness = 0.6.dp, color = Color(0xFFE2E8F0))
                        Text("  OR PHONE OTP  ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Divider(modifier = Modifier.weight(1f), thickness = 0.6.dp, color = Color(0xFFE2E8F0))
                    }

                    if (!isOtpSent) {
                        OutlinedTextField(
                            value = phoneNo,
                            onValueChange = { input ->
                                if (input.length <= 10 && input.all { it.isDigit() }) {
                                    phoneNo = input
                                }
                            },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("10-digit number") },
                            prefix = { Text("+91 ", fontWeight = FontWeight.Bold, color = TextDark) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandBlue) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (phoneNo.length == 10) {
                                    isLoading = true
                                    authRepo.sendOtp(
                                        phoneNumber = phoneNo,
                                        activity = context as Activity,
                                        onCodeSent = {
                                            isLoading = false
                                            isOtpSent = true
                                            Toast.makeText(context, "OTP Sent!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            enabled = !isLoading && phoneNo.length == 10,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Send Phone OTP", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                    otpCode = input
                                }
                            },
                            label = { Text("Enter 6-Digit OTP") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = BrandBlue) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (otpCode.length == 6) {
                                    isLoading = true
                                    authRepo.verifyOtp(
                                        otp = otpCode,
                                        onSuccess = {
                                            isLoading = false
                                            authRepo.getUserId()?.let { onLoginSuccess(it) }
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            enabled = !isLoading && otpCode.length == 6,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Verify & Restore Data", fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(
                            onClick = { isOtpSent = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Change Mobile Number", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
