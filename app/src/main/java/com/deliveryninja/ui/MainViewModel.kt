package com.deliveryninja.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliveryninja.data.models.*
import com.deliveryninja.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(val repository: DeliveryRepository) : ViewModel() {

    // Platforms
    val platforms = repository.getActivePlatforms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Restaurants & Customers
    val allRestaurants = repository.getAllRestaurants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCustomers = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val topRestaurants = repository.getTopRestaurants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Earnings
    val todayEarnings = repository.getTodayEarnings()
        .map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val weekEarnings = repository.getWeekEarnings()
        .map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val monthEarnings = repository.getMonthEarnings()
        .map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val todayCount = repository.getTodayCount()
        .map { it ?: 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val todayExpenses = repository.getTodayExpenses()
        .map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val platformEarnings = repository.getPlatformEarningsMonth()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentDeliveries = repository.getRecentDeliveries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDeliveries = repository.getAllDeliveries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val deliveriesWithLocation = repository.getDeliveriesWithLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeGoals = repository.getActiveGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeShift = repository.getActiveShift()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allExpenses = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val todayNetEarnings = combine(todayEarnings, todayExpenses) { e, x -> e - x }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Platform actions
    fun addPlatform(name: String, colorHex: String, logoUri: String = "") = viewModelScope.launch {
        repository.addPlatform(Platform(name = name, colorHex = colorHex, logoUri = logoUri))
    }
    fun deletePlatform(platform: Platform) = viewModelScope.launch {
        repository.deletePlatform(platform)
    }
    fun updatePlatform(platform: Platform) = viewModelScope.launch {
        repository.updatePlatform(platform)
    }

    // Restaurant actions
    fun addRestaurant(name: String, address: String, lat: Double, lon: Double, notes: String) = viewModelScope.launch {
        repository.addRestaurant(Restaurant(name = name, address = address, lat = lat, lon = lon, notes = notes))
    }
    fun deleteRestaurant(r: Restaurant) = viewModelScope.launch { repository.deleteRestaurant(r) }

    // Customer actions
    fun addCustomer(name: String, address: String, area: String, lat: Double, lon: Double, notes: String) = viewModelScope.launch {
        repository.addCustomer(Customer(name = name, address = address, area = area, lat = lat, lon = lon, notes = notes))
    }
    fun deleteCustomer(c: Customer) = viewModelScope.launch { repository.deleteCustomer(c) }

    // Delivery actions
    fun addDelivery(
        platformName: String, earnings: Double, tip: Double = 0.0, distanceKm: Double = 0.0,
        restaurantName: String = "", pickupAddress: String = "", pickupLat: Double = 0.0, pickupLon: Double = 0.0,
        customerName: String = "", dropAddress: String = "", dropLat: Double = 0.0, dropLon: Double = 0.0,
        notes: String = ""
    ) = viewModelScope.launch {
        val delivery = Delivery(
            platformName = platformName, earnings = earnings, tipAmount = tip, distanceKm = distanceKm,
            restaurantName = restaurantName, pickupAddress = pickupAddress, pickupLat = pickupLat, pickupLon = pickupLon,
            customerName = customerName, dropAddress = dropAddress, dropLat = dropLat, dropLon = dropLon, notes = notes
        )
        repository.addDelivery(delivery)
        updateGoalsProgress(earnings + tip)
    }
    fun deleteDelivery(d: Delivery) = viewModelScope.launch { repository.deleteDelivery(d) }

    // Goals
    fun addGoal(title: String, target: Double, period: String) = viewModelScope.launch {
        repository.addGoal(Goal(title = title, targetAmount = target, periodType = period))
    }
    fun deleteGoal(g: Goal) = viewModelScope.launch { repository.deleteGoal(g) }

    private fun updateGoalsProgress(amount: Double) = viewModelScope.launch {
        activeGoals.value.forEach { repository.updateGoal(it.copy(currentAmount = it.currentAmount + amount)) }
    }

    // Expenses
    fun addExpense(type: String, amount: Double, desc: String) = viewModelScope.launch {
        repository.addExpense(Expense(type = type, amount = amount, description = desc))
    }
    fun deleteExpense(e: Expense) = viewModelScope.launch { repository.deleteExpense(e) }

    // Shift
    fun startShift(battery: Int) = viewModelScope.launch { repository.startShift(battery) }
    fun endShift(shift: Shift, battery: Int, fuel: Double) = viewModelScope.launch { repository.endShift(shift, battery, fuel) }
}

class MainViewModelFactory(private val repository: DeliveryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(repository) as T
    }
}
