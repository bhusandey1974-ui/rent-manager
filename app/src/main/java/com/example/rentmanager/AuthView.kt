package com.example.rentmanager.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.rentmanager.AppColors
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

private enum class AuthTab { EMAIL, PHONE }

@Composable
fun AuthView(
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val context = LocalContext.current

    var authTab by remember { mutableStateOf(AuthTab.EMAIL) }

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var otpSent by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("527554410861-81m4ukk2eg1dqu3ug08kdbsfsa99ct0e.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            isLoading = true
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    isLoading = false
                    onAuthSuccess()
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Google sign-in failed."
                }
        } catch (e: ApiException) {
            errorMessage = "Google sign-in failed (code ${e.statusCode})."
        }
    }

    fun sendOtp() {
        val activity = context as? Activity ?: return
        val digits = phoneNumber.filter { it.isDigit() }
        if (digits.length != 10) {
            errorMessage = "Enter a valid 10-digit mobile number."
            return
        }
        isLoading = true
        errorMessage = null

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$digits")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    isLoading = true
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            isLoading = false
                            onAuthSuccess()
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Phone sign-in failed."
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Phone verification failed."
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    isLoading = false
                    verificationId = id
                    resendToken = token
                    otpSent = true
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp() {
        val id = verificationId
        if (id == null) {
            errorMessage = "Please request an OTP first."
            return
        }
        if (otpCode.trim().length < 4) {
            errorMessage = "Enter the OTP you received."
            return
        }
        isLoading = true
        errorMessage = null
        val credential = PhoneAuthProvider.getCredential(id, otpCode.trim())
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                isLoading = false
                onAuthSuccess()
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Invalid OTP. Please try again."
            }
    }

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
                        text = if (authTab == AuthTab.EMAIL && isSignUp) "Create Account" else "Welcome Back",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Text(
                        text = "Sign in to manage rooms, bills & tenants",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.ScaffoldBackground)
                            .padding(4.dp)
                    ) {
                        listOf(AuthTab.EMAIL to "Email", AuthTab.PHONE to "Phone").forEach { (tab, label) ->
                            val selected = authTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) AppColors.AzurePrimary else Color.Transparent)
                                    .clickable { authTab = tab; errorMessage = null }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) Color.White else AppColors.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (authTab == AuthTab.EMAIL) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
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

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
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

                        AnimatedVisibility(visible = isSignUp) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it; errorMessage = null },
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
                    } else {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                if (it.length <= 10) phoneNumber = it.filter { c -> c.isDigit() }
                                errorMessage = null
                            },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("10-digit number") },
                            singleLine = true,
                            enabled = !otpSent,
                            leadingIcon = {
                                Icon(Icons.Rounded.Phone, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.AzurePrimary,
                                unfocusedBorderColor = AppColors.BorderSubtle,
                                focusedLabelColor = AppColors.AzurePrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AnimatedVisibility(visible = otpSent) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { if (it.length <= 6) otpCode = it.filter { c -> c.isDigit() }; errorMessage = null },
                                    label = { Text("Enter OTP") },
                                    placeholder = { Text("6-digit code") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.AzurePrimary,
                                        unfocusedBorderColor = AppColors.BorderSubtle,
                                        focusedLabelColor = AppColors.AzurePrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(onClick = { sendOtp() }) {
                                    Text("Resend OTP", fontSize = 12.sp, color = AppColors.AzurePrimary)
                                }
                            }
                        }
                    }

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

                    Button(
                        onClick = {
                            when (authTab) {
                                AuthTab.EMAIL -> {
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
                                            .addOnSuccessListener { isLoading = false; onAuthSuccess() }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = e.localizedMessage ?: "Failed to create account."
                                            }
                                    } else {
                                        auth.signInWithEmailAndPassword(cleanEmail, cleanPass)
                                            .addOnSuccessListener { isLoading = false; onAuthSuccess() }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = e.localizedMessage ?: "Invalid email or password."
                                            }
                                    }
                                }
                                AuthTab.PHONE -> {
                                    if (!otpSent) sendOtp() else verifyOtp()
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
                            val label = when (authTab) {
                                AuthTab.EMAIL -> if (isSignUp) "Sign Up" else "Sign In"
                                AuthTab.PHONE -> if (otpSent) "Verify OTP" else "Send OTP"
                            }
                            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (authTab == AuthTab.EMAIL) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { isSignUp = !isSignUp; errorMessage = null }
                        ) {
                            Text(
                                text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                                fontSize = 12.sp,
                                color = AppColors.AzurePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = AppColors.BorderSubtle)

                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            googleLauncher.launch(googleSignInClient.signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(AppColors.ScaffoldBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.AzurePrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Continue with Google", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
