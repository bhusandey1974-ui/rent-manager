package com.example.rentmanager

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Icon Design System Palette
val BrandPrimary = Color(0xFF0D47A1)
val BrandSecondary = Color(0xFF1976D2)
val BrandAccent = Color(0xFF00B0FF)
val BrandDarkNavy = Color(0xFF0A192F)
val BrandBackground = Color(0xFFF4F7FB)
val SuccessGreen = Color(0xFF00C853)
val WarningRed = Color(0xFFFF3D00)

fun formatCurrency(amount: Double): String = "₹" + String.format(Locale.US, "%.2f", amount)
fun formatUnits(amount: Double): String = String.format(Locale.US, "%.1f", amount)

class RentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val allTenants: StateFlow<List<Tenant>> = dao.getAllTenants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBills: StateFlow<List<RentBill>> = dao.getAllBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getBillsForTenant(tenantId: Long): Flow<List<RentBill>> = dao.getBillsForTenant(tenantId)

    fun addTenant(
        name: String,
        room: String,
        phone: String,
        rent: Double,
        rate: Double,
        reading: Double,
        entryDate: String
    ) {
        viewModelScope.launch {
            dao.insertTenant(
                Tenant(
                    id = 0,
                    name = name,
                    roomNumber = room,
                    phone = phone,
                    defaultBaseRent = rent,
                    electricityRatePerUnit = rate,
                    initialMeterReading = reading,
                    lastMeterReading = reading,
                    isOccupied = true,
                    entryDate = entryDate,
                    exitDate = null
                )
            )
        }
    }

    fun checkoutTenant(tenant: Tenant, exitDate: String, finalReading: Double) {
        viewModelScope.launch {
            dao.updateTenant(
                tenant.copy(
                    isOccupied = false,
                    exitDate = exitDate,
                    lastMeterReading = finalReading
                )
            )
        }
    }

    fun deleteTenant(tenant: Tenant) {
        viewModelScope.launch { dao.deleteTenant(tenant) }
    }

    fun addMonthlyBill(
        tenant: Tenant,
        currReading: Double,
        baseRent: Double,
        amountPaid: Double,
        paymentDate: String,
        mode: String,
        monthYearInput: String
    ) {
        viewModelScope.launch {
            val prevReading = tenant.lastMeterReading
            val units = (currReading - prevReading).coerceAtLeast(0.0)
            val elecAmount = units * tenant.electricityRatePerUnit
            val total = baseRent + elecAmount
            val due = total - amountPaid

            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1

            val bill = RentBill(
                id = 0,
                tenantId = tenant.id,
                monthYear = monthYearInput,
                baseRent = baseRent,
                prevMeterReading = prevReading,
                currMeterReading = currReading,
                unitsConsumed = units,
                electricityRate = tenant.electricityRatePerUnit,
                electricityAmount = elecAmount,
                totalBillAmount = total,
                amountPaid = amountPaid,
                dueAmount = due,
                paymentDate = paymentDate,
                paymentMode = mode,
                billingYear = year,
                billingMonthIndex = month
            )
            dao.insertBill(bill)
            dao.updateTenant(tenant.copy(lastMeterReading = currReading))
        }
    }
}

