package com.example.rentmanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmanager.data.AppDatabase
import com.example.rentmanager.data.RentBill
import com.example.rentmanager.data.Tenant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val allTenants: StateFlow<List<Tenant>> = dao.getAllTenants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getBillsForTenant(tenantId: Long): Flow<List<RentBill>> {
        return dao.getBillsForTenant(tenantId)
    }

    fun addTenant(
        name: String,
        roomNumber: String,
        phone: String,
        baseRent: Double,
        ratePerUnit: Double,
        initialReading: Double
    ) {
        viewModelScope.launch {
            val tenant = Tenant(
                name = name,
                roomNumber = roomNumber,
                phone = phone,
                defaultBaseRent = baseRent,
                electricityRatePerUnit = ratePerUnit,
                lastMeterReading = initialReading
            )
            dao.insertTenant(tenant)
        }
    }

    fun deleteTenant(tenant: Tenant) {
        viewModelScope.launch {
            dao.deleteTenant(tenant)
        }
    }

    fun addMonthlyBill(
        tenant: Tenant,
        currReading: Double,
        baseRent: Double,
        amountPaid: Double,
        paymentMode: String
    ) {
        viewModelScope.launch {
            val prevReading = tenant.lastMeterReading
            val units = (currReading - prevReading).coerceAtLeast(0.0)
            val elecAmount = units * tenant.electricityRatePerUnit
            val total = baseRent + elecAmount
            val due = total - amountPaid
            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

            val bill = RentBill(
                tenantId = tenant.id,
                monthYear = monthYear,
                baseRent = baseRent,
                prevMeterReading = prevReading,
                currMeterReading = currReading,
                unitsConsumed = units,
                electricityRate = tenant.electricityRatePerUnit,
                electricityAmount = elecAmount,
                totalBillAmount = total,
                amountPaid = amountPaid,
                dueAmount = due,
                paymentDate = currentDate,
                paymentMode = paymentMode
            )
            dao.insertBill(bill)
            dao.updateTenant(tenant.copy(lastMeterReading = currReading))
        }
    }
}
