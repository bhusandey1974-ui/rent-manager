package com.example.rentmanager

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest

/** Stores a hashed 4-digit PIN on this device. */
object PinStore {
    private const val PREFS = "rm_security"
    private const val KEY = "pin_hash"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun hash(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(("rent-manager:$pin").toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun isEnabled(c: Context): Boolean = prefs(c).contains(KEY)
    fun set(c: Context, pin: String) { prefs(c).edit().putString(KEY, hash(pin)).apply() }
    fun verify(c: Context, pin: String): Boolean = prefs(c).getString(KEY, null) == hash(pin)
    fun clear(c: Context) { prefs(c).edit().remove(KEY).apply() }
}

/** Small display preferences shared across the app. */
object AppPrefs {
    private const val PREFS = "rm_prefs"

    var textScale by mutableFloatStateOf(1f)
        private set

    fun load(c: Context) {
        textScale = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat("text_scale", 1f)
    }

    fun setTextScale(c: Context, v: Float) {
        textScale = v
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat("text_scale", v).apply()
    }
}

/** Full-screen PIN lock drawn on top of the app. */
@Composable
fun PinLockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var showForgot by remember { mutableStateOf(false) }
    val signedIn = FirebaseAuth.getInstance().currentUser != null

    BackHandler(enabled = true) { }

    fun press(d: String) {
        if (entered.length >= 4) return
        error = false
        entered += d
        if (entered.length == 4) {
            if (PinStore.verify(context, entered)) {
                entered = ""
                onUnlocked()
            } else {
                error = true
                entered = ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScaffoldBackground)
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Enter PIN", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (error) "Wrong PIN, try again" else "Unlock Rent Manager",
                fontSize = 13.sp,
                color = if (error) AppColors.CrimsonAlert else AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (i < entered.length) AppColors.AzurePrimary else AppColors.TextMuted.copy(alpha = 0.35f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "<")
            )
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(if (key.isEmpty()) Color.Transparent else AppColors.SurfaceWhite)
                                .clickable(enabled = key.isNotEmpty()) {
                                    if (key == "<") {
                                        if (entered.isNotEmpty()) entered = entered.dropLast(1)
                                    } else {
                                        press(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (key == "<") "⌫" else key,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = { showForgot = true }) {
                Text("Forgot PIN?", color = AppColors.AzurePrimary)
            }
        }
    }

    if (showForgot) {
        AlertDialog(
            onDismissRequest = { showForgot = false },
            title = { Text("Forgot PIN?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (signedIn)
                        "You can sign out and remove the PIN. Your data stays safe in the cloud and comes back when you sign in again."
                    else
                        "You are using local storage, so the PIN cannot be reset without erasing this device's data. Clear the app's data in Android settings if you cannot remember it."
                )
            },
            confirmButton = {
                if (signedIn) {
                    TextButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        PinStore.clear(context)
                        showForgot = false
                        (context as? Activity)?.recreate()
                    }) { Text("Sign out & remove PIN") }
                } else {
                    TextButton(onClick = { showForgot = false }) { Text("OK") }
                }
            },
            dismissButton = {
                if (signedIn) TextButton(onClick = { showForgot = false }) { Text("Cancel") }
            }
        )
    }
}
