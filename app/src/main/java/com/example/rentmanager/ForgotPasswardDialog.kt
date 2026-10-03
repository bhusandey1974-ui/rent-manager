package com.example.rentmanager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import com.google.firebase.auth.FirebaseAuth

/** Asks for an email and sends Firebase's password-reset link to it. */
@Composable
fun ForgotPasswordDialog(initialEmail: String = "", onDismiss: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf(initialEmail.trim()) }
    var sending by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!sending) onDismiss() },
        title = { Text("Reset password", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (sent) {
                    Text(
                        "If an account exists for $email, a reset link is on its way. Check your inbox and the spam folder.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Signed up with Google? There is no password to reset. Use the Continue with Google button.",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                } else {
                    Text("Enter your account email and we will send you a link to set a new password.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = { Text("Email Address") },
                        singleLine = true,
                        isError = error != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(error ?: "", color = AppColors.CrimsonAlert, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (sent) {
                TextButton(onClick = onDismiss) { Text("Done") }
            } else {
                TextButton(
                    enabled = !sending,
                    onClick = {
                        val e = email.trim()
                        if (e.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                            error = "Enter a valid email address"
                        } else {
                            sending = true
                            FirebaseAuth.getInstance().sendPasswordResetEmail(e)
                                .addOnCompleteListener { task ->
                                    sending = false
                                    if (task.isSuccessful) {
                                        sent = true
                                    } else {
                                        error = "Could not send the link. Check your internet and try again."
                                        Toast.makeText(context, "Reset failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                ) { Text(if (sending) "Sending..." else "Send reset link") }
            }
        },
        dismissButton = {
            if (!sent) TextButton(enabled = !sending, onClick = onDismiss) { Text("Cancel") }
        }
    )
}
