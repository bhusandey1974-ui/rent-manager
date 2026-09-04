package com.example.rentmanager

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

@Composable
fun AuthView(
    onLoginSuccess: (uid: String) -> Unit,
    onSkipOffline: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val auth = remember { FirebaseAuth.getInstance() }

    var isPhoneMode by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }

    // Email state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Phone state
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isOtpSent by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Tile Emblem
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 3.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = "Rent Manager Emblem",
                        tint = Color(0xFF1E40AF),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Rent Manager Cloud",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0F172A),
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Secure portfolio sync across properties & devices",
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Auth Card Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Segmented Method Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (!isPhoneMode) Color.White else Color.Transparent)
                                .clickable {
                                    isPhoneMode = false
                                    errorMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Email Account",
                                fontSize = 13.sp,
                                fontWeight = if (!isPhoneMode) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif,
                                color = if (!isPhoneMode) Color(0xFF1E40AF) else Color(0xFF64748B)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isPhoneMode) Color.White else Color.Transparent)
                                .clickable {
                                    isPhoneMode = true
                                    errorMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Mobile OTP",
                                fontSize = 13.sp,
                                fontWeight = if (isPhoneMode) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif,
                                color = if (isPhoneMode) Color(0xFF1E40AF) else Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dynamic Auth Forms
                    if (!isPhoneMode) {
                        // Email / Password Form
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            label = { Text("Email Address", fontFamily = FontFamily.SansSerif, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.MailOutline, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password", fontFamily = FontFamily.SansSerif, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.LockOutline, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Phone OTP Flow
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                errorMessage = null
                            },
                            label = { Text("Phone (+91...)", fontFamily = FontFamily.SansSerif, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                            },
                            singleLine = true,
                            enabled = !isOtpSent,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isOtpSent) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = {
                                    otpCode = it
                                    errorMessage = null
                                },
                                label = { Text("6-Digit Verification Code", fontFamily = FontFamily.SansSerif, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                                        // Error Feedback Banner
                    AnimatedVisibility(visible = errorMessage != null) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            errorMessage = null
                            if (!isPhoneMode) {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter both email and password."
                                    return@Button
                                }
                                isLoading = true
                                if (isRegisterMode) {
                                    auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                                        .addOnSuccessListener { result ->
                                            isLoading = false
                                            result.user?.uid?.let { onLoginSuccess(it) }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = e.localizedMessage ?: "Registration failed."
                                        }
                                } else {
                                    auth.signInWithEmailAndPassword(email.trim(), password.trim())
                                        .addOnSuccessListener { result ->
                                            isLoading = false
                                            result.user?.uid?.let { onLoginSuccess(it) }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = e.localizedMessage ?: "Invalid credentials."
                                        }
                                }
                            } else {
                                if (!isOtpSent) {
                                    val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber.trim() else "+91${phoneNumber.trim()}"
                                    if (phoneNumber.length < 10) {
                                        errorMessage = "Please enter a valid 10-digit mobile number."
                                        return@Button
                                    }
                                    isLoading = true
                                    val options = PhoneAuthOptions.newBuilder(auth)
                                        .setPhoneNumber(formattedPhone)
                                        .setTimeout(60L, TimeUnit.SECONDS)
                                        .setActivity(context as Activity)
                                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                isLoading = false
                                                auth.signInWithCredential(credential).addOnSuccessListener { res ->
                                                    res.user?.uid?.let { onLoginSuccess(it) }
                                                }
                                            }

                                            override fun onVerificationFailed(e: FirebaseException) {
                                                isLoading = false
                                                errorMessage = e.localizedMessage ?: "Verification failed."
                                            }

                                            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                                isLoading = false
                                                verificationId = id
                                                isOtpSent = true
                                                Toast.makeText(context, "OTP sent successfully.", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                        .build()
                                    PhoneAuthProvider.verifyPhoneNumber(options)
                                } else {
                                    val id = verificationId
                                    if (id == null || otpCode.length < 6) {
                                        errorMessage = "Please enter the complete 6-digit OTP."
                                        return@Button
                                    }
                                    isLoading = true
                                    val credential = PhoneAuthProvider.getCredential(id, otpCode.trim())
                                    auth.signInWithCredential(credential)
                                        .addOnSuccessListener { result ->
                                            isLoading = false
                                            result.user?.uid?.let { onLoginSuccess(it) }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = e.localizedMessage ?: "Invalid OTP code."
                                        }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (isPhoneMode) {
                                    if (isOtpSent) "Verify OTP & Continue" else "Request Verification OTP"
                                } else {
                                    if (isRegisterMode) "Create Account" else "Sign In"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }

                    // Email Mode Mode-Switch (Sign In vs Register)
                    if (!isPhoneMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isRegisterMode) "Already have an account? " else "Need a new account? ",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = if (isRegisterMode) "Sign In" else "Register Portfolio",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF),
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.clickable {
                                    isRegisterMode = !isRegisterMode
                                    errorMessage = null
                                }
                            )
                        }
                    } else if (isOtpSent) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Change Mobile Number",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    isOtpSent = false
                                    otpCode = ""
                                    errorMessage = null
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Offline Gateway Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .clickable { onSkipOffline() },
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderShared,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue in Local Storage Mode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}
