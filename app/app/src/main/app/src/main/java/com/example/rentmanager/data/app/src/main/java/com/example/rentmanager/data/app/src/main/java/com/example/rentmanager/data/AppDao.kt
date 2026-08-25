package com.example.rentmanager.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM tenants ORDER BY roomNumber ASC")
    fun getAllTenants(): Flow<List<Tenant>>

    @Query("SELECT * FROM tenants WHERE id = :id")
    suspend fun getTenantById(id: Long): Tenant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: Tenant): Long

    @Update
    suspend fun updateTenant(tenant: Tenant)

    @Delete
    suspend fun deleteTenant(tenant: Tenant)

    @Query("SELECT * FROM rent_bills WHERE tenantId = :tenantId ORDER BY id DESC")
    fun getBillsForTenant(tenantId: Long): Flow<List<RentBill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: RentBill)
}
