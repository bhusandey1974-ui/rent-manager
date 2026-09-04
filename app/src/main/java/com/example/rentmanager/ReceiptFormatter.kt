package com.example.rentmanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptFormatter {

    /**
     * Builds the exact formatted WhatsApp receipt string.
     */
    fun formatReceipt(
        tenantName: String,
        roomNumber: String,
        billingPeriod: String,
        paymentDateMillis: Long = System.currentTimeMillis(),
        previousReading: Double,
        currentReading: Double,
        unitsConsumed: Double,
        ratePerUnit: Double,
        totalElectricity: Double,
        baseRent: Double,
        totalAmount: Double,
        amountPaid: Double,
        paymentMode: String = "Cash",
        remainingDue: Double
    ): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val formattedDate = dateFormat.format(Date(paymentDateMillis))

        val statusLine = when {
            remainingDue > 0.0 -> "⚠️ *Pending Due:* ₹${String.format(Locale.ENGLISH, "%.2f", remainingDue)}"
            remainingDue < 0.0 -> "🎁 *Advance Credit:* ₹${String.format(Locale.ENGLISH, "%.2f", -remainingDue)}"
            else -> "✅ *Status:* Fully Cleared (No Dues)"
        }

        return buildString {
            append("🏠 *RENT & ELECTRICITY RECEIPT*\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("👤 *Tenant:* $tenantName  (Room $roomNumber)\n")
            append("📅 *Billing Period:* $billingPeriod\n")
            append("🗓️ *Date of Payment:* $formattedDate\n\n")
            append("⚡ *Electricity Details:*\n")
            append("• Previous Reading: ${String.format(Locale.ENGLISH, "%.1f", previousReading)}\n")
            append("• Current Reading: ${String.format(Locale.ENGLISH, "%.1f", currentReading)}\n")
            append("• Units Consumed: ${String.format(Locale.ENGLISH, "%.1f", unitsConsumed)}\n")
            append("• Rate / Unit: ₹${String.format(Locale.ENGLISH, "%.2f", ratePerUnit)}\n")
            append("• Total Electricity: ₹${String.format(Locale.ENGLISH, "%.2f", totalElectricity)}\n\n")
            append("🏢 *Base Rent:* ₹${String.format(Locale.ENGLISH, "%.2f", baseRent)}\n")
            append("🧾 *Total Amount:* ₹${String.format(Locale.ENGLISH, "%.2f", totalAmount)}\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("✅ *Amount Paid:* ₹${String.format(Locale.ENGLISH, "%.2f", amountPaid)} ($paymentMode)\n")
            append("$statusLine\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("Thank you!")
        }
    }

    /**
     * Opens WhatsApp directly with the pre-filled receipt message.
     */
    fun sendViaWhatsApp(
        context: Context,
        phoneNumber: String,
        message: String
    ) {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "").let { phone ->
            when {
                phone.startsWith("+") -> phone.removePrefix("+")
                phone.length == 10 -> "91$phone" // Default to India country code if 10 digits
                else -> phone
            }
        }

        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = if (cleanPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed or available.", Toast.LENGTH_SHORT).show()
        }
    }
}

