package com.deliveryninja.data.repository

import com.deliveryninja.data.db.*
import com.deliveryninja.data.models.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class DeliveryRepository(
    val platformDao: PlatformDao,
    val restaurantDao: RestaurantDao,
    val customerDao: CustomerDao,
    val deliveryDao: DeliveryDao,
    val shiftDao: ShiftDao,
    val goalDao: GoalDao,
    val expenseDao: ExpenseDao
) {
    // Platforms
    fun getActivePlatforms(): Flow<List<Platform>> = platformDao.getActivePlatforms()
    fun getAllPlatforms(): Flow<List<Platform>> = platformDao.getAllPlatforms()
    suspend fun addPlatform(platform: Platform): Long = platformDao.insert(platform)
    suspend fun updatePlatform(platform: Platform) = platformDao.update(platform)
    suspend fun deletePlatform(platform: Platform) = platformDao.delete(platform)
    suspend fun platformCount(): Int = platformDao.count()

    // Restaurants
    fun getAllRestaurants(): Flow<List<Restaurant>> = restaurantDao.getAllRestaurants()
    fun searchRestaurants(query: String): Flow<List<Restaurant>> = restaurantDao.searchRestaurants(query)
    suspend fun addRestaurant(restaurant: Restaurant): Long = restaurantDao.insert(restaurant)
    suspend fun updateRestaurant(restaurant: Restaurant) = restaurantDao.update(restaurant)
    suspend fun deleteRestaurant(restaurant: Restaurant) = restaurantDao.delete(restaurant)
    suspend fun getRestaurantById(id: Long): Restaurant? = restaurantDao.getById(id)

    // Customers
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)
    suspend fun addCustomer(customer: Customer): Long = customerDao.insert(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.update(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)

    // Deliveries
    fun getAllDeliveries(): Flow<List<Delivery>> = deliveryDao.getAllDeliveries()
    fun getRecentDeliveries(): Flow<List<Delivery>> = deliveryDao.getRecentDeliveries()
    fun getDeliveriesWithLocation(): Flow<List<Delivery>> = deliveryDao.getDeliveriesWithLocation()
    fun getTopRestaurants(): Flow<List<RestaurantStats>> = deliveryDao.getTopRestaurants()
    suspend fun addDelivery(delivery: Delivery): Long = deliveryDao.insert(delivery)
    suspend fun updateDelivery(delivery: Delivery) = deliveryDao.update(delivery)
    suspend fun deleteDelivery(delivery: Delivery) = deliveryDao.delete(delivery)

    fun getTodayEarnings(): Flow<Double?> { val (s,e) = todayRange(); return deliveryDao.getTotalEarnings(s,e) }
    fun getWeekEarnings(): Flow<Double?> { val (s,e) = weekRange(); return deliveryDao.getTotalEarnings(s,e) }
    fun getMonthEarnings(): Flow<Double?> { val (s,e) = monthRange(); return deliveryDao.getTotalEarnings(s,e) }
    fun getTodayCount(): Flow<Int?> { val (s,e) = todayRange(); return deliveryDao.getTotalDeliveries(s,e) }
    fun getPlatformEarningsMonth(): Flow<List<PlatformEarnings>> { val (s,e) = monthRange(); return deliveryDao.getEarningsByPlatform(s,e) }

    // Shifts
    fun getActiveShift(): Flow<Shift?> = shiftDao.getActiveShift()
    suspend fun startShift(battery: Int): Long = shiftDao.insert(Shift(startTime = System.currentTimeMillis(), batteryStart = battery))
    suspend fun endShift(shift: Shift, battery: Int, fuel: Double) = shiftDao.update(shift.copy(endTime = System.currentTimeMillis(), batteryEnd = battery, fuelCost = fuel))

    // Goals
    fun getActiveGoals(): Flow<List<Goal>> = goalDao.getActiveGoals()
    suspend fun addGoal(goal: Goal): Long = goalDao.insert(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.update(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.delete(goal)

    // Expenses
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    suspend fun addExpense(expense: Expense): Long = expenseDao.insert(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)
    fun getTodayExpenses(): Flow<Double?> { val (s,e) = todayRange(); return expenseDao.getTotalExpenses(s,e) }

    // Date helpers
    fun todayRange(): Pair<Long,Long> {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0)
        val s = c.timeInMillis
        c.set(Calendar.HOUR_OF_DAY,23); c.set(Calendar.MINUTE,59); c.set(Calendar.SECOND,59)
        return Pair(s, c.timeInMillis)
    }
    fun weekRange(): Pair<Long,Long> {
        val c = Calendar.getInstance(); c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
        c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0)
        return Pair(c.timeInMillis, System.currentTimeMillis())
    }
    fun monthRange(): Pair<Long,Long> {
        val c = Calendar.getInstance(); c.set(Calendar.DAY_OF_MONTH,1)
        c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0)
        return Pair(c.timeInMillis, System.currentTimeMillis())
    }
}
