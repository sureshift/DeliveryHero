package com.deliveryninja.data.db

import androidx.room.*
import com.deliveryninja.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlatformDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(platform: Platform): Long

    @Update
    suspend fun update(platform: Platform)

    @Delete
    suspend fun delete(platform: Platform)

    @Query("SELECT * FROM platforms WHERE isActive = 1 ORDER BY name ASC")
    fun getActivePlatforms(): Flow<List<Platform>>

    @Query("SELECT * FROM platforms ORDER BY name ASC")
    fun getAllPlatforms(): Flow<List<Platform>>

    @Query("SELECT COUNT(*) FROM platforms")
    suspend fun count(): Int
}

@Dao
interface RestaurantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(restaurant: Restaurant): Long

    @Update
    suspend fun update(restaurant: Restaurant)

    @Delete
    suspend fun delete(restaurant: Restaurant)

    @Query("SELECT * FROM restaurants ORDER BY name ASC")
    fun getAllRestaurants(): Flow<List<Restaurant>>

    @Query("SELECT * FROM restaurants WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchRestaurants(query: String): Flow<List<Restaurant>>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getById(id: Long): Restaurant?
}

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE address LIKE '%' || :query || '%' OR area LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): Customer?
}

@Dao
interface DeliveryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(delivery: Delivery): Long

    @Update
    suspend fun update(delivery: Delivery)

    @Delete
    suspend fun delete(delivery: Delivery)

    @Query("SELECT * FROM deliveries ORDER BY createdAt DESC")
    fun getAllDeliveries(): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getDeliveriesByDateRange(startTime: Long, endTime: Long): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE platformName = :platform ORDER BY createdAt DESC")
    fun getDeliveriesByPlatform(platform: String): Flow<List<Delivery>>

    @Query("SELECT SUM(earnings + tipAmount) FROM deliveries WHERE createdAt >= :startTime AND createdAt <= :endTime AND status = 'COMPLETED'")
    fun getTotalEarnings(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM deliveries WHERE createdAt >= :startTime AND createdAt <= :endTime AND status = 'COMPLETED'")
    fun getTotalDeliveries(startTime: Long, endTime: Long): Flow<Int?>

    @Query("SELECT platformName, SUM(earnings + tipAmount) as total FROM deliveries WHERE createdAt >= :startTime AND createdAt <= :endTime AND status = 'COMPLETED' GROUP BY platformName")
    fun getEarningsByPlatform(startTime: Long, endTime: Long): Flow<List<PlatformEarnings>>

    @Query("SELECT * FROM deliveries WHERE pickupLat != 0.0 OR dropLat != 0.0 ORDER BY createdAt DESC LIMIT 500")
    fun getDeliveriesWithLocation(): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries ORDER BY createdAt DESC LIMIT 10")
    fun getRecentDeliveries(): Flow<List<Delivery>>

    @Query("SELECT restaurantName, COUNT(*) as count, SUM(earnings+tipAmount) as total FROM deliveries WHERE restaurantName != '' GROUP BY restaurantName ORDER BY count DESC LIMIT 10")
    fun getTopRestaurants(): Flow<List<RestaurantStats>>
}

data class PlatformEarnings(val platformName: String, val total: Double)
data class RestaurantStats(val restaurantName: String, val count: Int, val total: Double)

@Dao
interface ShiftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shift: Shift): Long

    @Update
    suspend fun update(shift: Shift)

    @Query("SELECT * FROM shifts ORDER BY startTime DESC")
    fun getAllShifts(): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE endTime = 0 LIMIT 1")
    fun getActiveShift(): Flow<Shift?>
}

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveGoals(): Flow<List<Goal>>
}

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startTime AND date <= :endTime")
    fun getTotalExpenses(startTime: Long, endTime: Long): Flow<Double?>
}

@Database(
    entities = [Platform::class, Restaurant::class, Customer::class, Delivery::class, Shift::class, Goal::class, Expense::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun platformDao(): PlatformDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun customerDao(): CustomerDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun shiftDao(): ShiftDao
    abstract fun goalDao(): GoalDao
    abstract fun expenseDao(): ExpenseDao
}
