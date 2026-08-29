package com.example.rentmanager

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private var verificationId: String? = null

    fun getUserId(): String? = auth.currentUser?.uid

    // --- GOOGLE SIGN-IN ---
    fun getGoogleSignInClient(activity: Activity, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId.ifBlank { "default" })
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun firebaseAuthWithGoogle(
        data: Intent?,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken ?: return onError("Missing Google ID token")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        _currentUser.value = auth.currentUser
                        auth.currentUser?.let(onSuccess)
                    } else {
                        onError(authTask.exception?.localizedMessage ?: "Google Sign-In failed")
                    }
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Google Sign-In error")
        }
    }

    // --- PHONE OTP SIGN-IN ---
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val digitsOnly = phoneNumber.filter { it.isDigit() }
            val cleanNumber = if (digitsOnly.length == 10) {
                "+91$digitsOnly"
            } else if (digitsOnly.length > 10 && digitsOnly.startsWith("91")) {
                "+$digitsOnly"
            } else {
                "+$digitsOnly"
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(cleanNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(credential, {}, onError)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        onError(e.localizedMessage ?: "OTP Verification Failed")
                    }

                    override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        verificationId = vId
                        onCodeSent()
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Failed to initiate phone verification")
        }
    }

    fun verifyOtp(otp: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val vId = verificationId ?: return onError("Please request OTP first")
        val credential = PhoneAuthProvider.getCredential(vId, otp)
        signInWithPhoneAuthCredential(credential, onSuccess, onError)
    }

    private fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    onSuccess()
                } else {
                    onError(task.exception?.localizedMessage ?: "Invalid Code")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }
}
