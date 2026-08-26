package com.example.rentmanager

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roomNumber: String,
    val phone: String,
    val defaultBaseRent: Double,
    val electricityRatePerUnit: Double,
    val initialMeterReading: Double = 0.0,
    val lastMeterReading: Double = 0.0,
    val isOccupied: Boolean = true,
    val entryDate: String = "",
    val exitDate: String? = null
)

@Entity(
    tableName = "rent_bills",
    foreignKeys = [
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tenantId")]
)
data class RentBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tenantId: Long,
    val monthYear: String,
    val baseRent: Double,
    val prevMeterReading: Double,
    val currMeterReading: Double,
    val unitsConsumed: Double,
    val electricityRate: Double,
    val electricityAmount: Double,
    val totalBillAmount: Double,
    val amountPaid: Double,
    val dueAmount: Double,
    val paymentDate: String,
    val paymentMode: String,
    val billingYear: Int = 2026,
    val billingMonthIndex: Int = 8
)

@Dao
interface AppDao {
    @Query("SELECT * FROM tenants ORDER BY roomNumber ASC")
    fun getAllTenants(): Flow<List<Tenant>>

    @Insert
    suspend fun insertTenant(tenant: Tenant): Long

    @Delete
    suspend fun deleteTenant(tenant: Tenant)

    @Update
    suspend fun updateTenant(tenant: Tenant)

    @Query("SELECT * FROM rent_bills WHERE tenantId = :tenantId ORDER BY id DESC")
    fun getBillsForTenant(tenantId: Long): Flow<List<RentBill>>

    @Query("SELECT * FROM rent_bills ORDER BY id DESC")
    fun getAllBills(): Flow<List<RentBill>>

    @Insert
    suspend fun insertBill(bill: RentBill)
}

@Database(entities = [Tenant::class, RentBill::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rent_manager_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

