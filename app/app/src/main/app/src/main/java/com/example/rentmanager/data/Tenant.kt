package com.example.rentmanager

data class Tenant(
    val id: String = "",
    val roomId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val entryDate: Long = System.currentTimeMillis(),
    val exitDate: Long? = null,
    val securityDeposit: Double = 0.0,
    val isCurrent: Boolean = true
) {
    // Seamless alias so code using .phone or .phoneNumber works without compilation errors
    val phone: String get() = phoneNumber
}
