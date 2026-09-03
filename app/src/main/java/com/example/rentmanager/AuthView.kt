package com.example.rentmanager

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

enum class AuthMode {
    EMAIL, PHONE
}

@Composable
fun AuthView(
    onLoginSuccess: (userId: String) -> Unit,
    onSkipOffline: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = remember { FirebaseAuth.getInstance() }

    var selectedMode by remember { mutableStateOf(AuthMode.EMAIL) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Email state
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Phone state
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var isOtpSent by remember { mutableStateOf(false) }

    // Google Sign-In Setup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    isLoading = true
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { authRes ->
                            isLoading = false
                            onLoginSuccess(authRes.user?.uid.orEmpty())
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Google sign-in failed"
                        }
                } else {
                    errorMessage = "Missing Google ID token"
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Google sign-in failed"
            }
        }
    }

    // Phone verification logic
    fun sendOtp() {
        val cleanNumber = phoneNumber.trim().replace(" ", "").replace("-", "")
        val formattedNumber = when {
            cleanNumber.startsWith("+") -> cleanNumber
            cleanNumber.length == 10 -> "+91$cleanNumber"
            else -> "+$cleanNumber"
        }

        if (cleanNumber.length < 10) {
            errorMessage = "Please enter a valid 10-digit mobile number"
            return
        }
        if (activity == null) {
            errorMessage = "Cannot launch phone verification"
            return
        }

        isLoading = true
        errorMessage = null

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { res ->
                            isLoading = false
                            onLoginSuccess(res.user?.uid.orEmpty())
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Verification failed"
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Phone verification failed"
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    isLoading = false
                    verificationId = id
                    resendToken = token
                    isOtpSent = true
                }
            })

        resendToken?.let { optionsBuilder.setForceResendingToken(it) }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun verifyOtp() {
        val id = verificationId
        val code = otpCode.trim()

        if (id.isNullOrBlank() || code.length != 6) {
            errorMessage = "Enter the 6-digit OTP"
            return
        }

        isLoading = true
        errorMessage = null

        val credential = PhoneAuthProvider.getCredential(id, code)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { res ->
                isLoading = false
                onLoginSuccess(res.user?.uid.orEmpty())
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Incorrect OTP code"
            }
    }

    // Email/Password logic
    fun handleEmailAuth() {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            errorMessage = "Please fill in all fields"
            return
        }
        if (cleanPass.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }
        if (isRegisterMode && cleanPass != confirmPassword.trim()) {
            errorMessage = "Passwords do not match"
            return
        }

        isLoading = true
        errorMessage = null

        if (isRegisterMode) {
            auth.createUserWithEmailAndPassword(cleanEmail, cleanPass)
                .addOnSuccessListener { res ->
                    isLoading = false
                    onLoginSuccess(res.user?.uid.orEmpty())
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Registration failed"
                }
        } else {
            auth.signInWithEmailAndPassword(cleanEmail, cleanPass)
                .addOnSuccessListener { res ->
                    isLoading = false
                    onLoginSuccess(res.user?.uid.orEmpty())
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Sign-in failed"
                }
        }
    }
        Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
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
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rent Manager Sync",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sign in to keep your records safely backed up",
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Switch Tabs: Email vs Mobile OTP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedMode == AuthMode.EMAIL) Color.White else Color.Transparent)
                        .clickable { selectedMode = AuthMode.EMAIL; errorMessage = null }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Email",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = if (selectedMode == AuthMode.EMAIL) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (selectedMode == AuthMode.EMAIL) Color(0xFF2563EB) else Color(0xFF64748B)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedMode == AuthMode.PHONE) Color.White else Color.Transparent)
                        .clickable { selectedMode = AuthMode.PHONE; errorMessage = null }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mobile OTP",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = if (selectedMode == AuthMode.PHONE) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (selectedMode == AuthMode.PHONE) Color(0xFF2563EB) else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form 1: Email & Password
            if (selectedMode == AuthMode.EMAIL) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email address", fontFamily = FontFamily.SansSerif) },
                    leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null, tint = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password", fontFamily = FontFamily.SansSerif) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )

                AnimatedVisibility(visible = isRegisterMode) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = null },
                            label = { Text("Confirm password", fontFamily = FontFamily.SansSerif) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { handleEmailAuth() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (isRegisterMode) "Create Account" else "Sign In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isRegisterMode) "Already have an account? " else "Don't have an account? ",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = if (isRegisterMode) "Sign In" else "Register",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable {
                            isRegisterMode = !isRegisterMode
                            errorMessage = null
                        }
                    )
                }
            }

            // Form 2: Mobile OTP
            if (selectedMode == AuthMode.PHONE) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it; errorMessage = null },
                    label = { Text("Mobile number", fontFamily = FontFamily.SansSerif) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isOtpSent && !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp)
                )

                AnimatedVisibility(visible = isOtpSent) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it; errorMessage = null },
                            label = { Text("6-Digit OTP", fontFamily = FontFamily.SansSerif) },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF64748B)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { if (isOtpSent) verifyOtp() else sendOtp() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (isOtpSent) "Verify OTP & Continue" else "Send OTP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                if (isOtpSent) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Edit phone number",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable {
                            isOtpSent = false
                            otpCode = ""
                            errorMessage = null
                        }
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text(
                    text = "  OR  ",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form 3: Google Sign-In
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google",
                    tint = Color(0xFFEA4335),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue with Google",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Continue Offline (Local Storage)",
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSkipOffline() }
                    .padding(6.dp)
            )
        }
    }
}
