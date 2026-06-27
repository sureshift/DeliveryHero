package com.deliveryninja.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "platforms")
data class Platform(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#FC8019",
    val logoUri: String = "",   // file:// URI of user-picked image, or "" for initial letter
    val isActive: Boolean = true
)

val DEFAULT_PLATFORMS = listOf(
    Platform(name = "Swiggy",     colorHex = "#FC8019"),
    Platform(name = "Shadowfax",  colorHex = "#2563EB"),
    Platform(name = "Rapido",     colorHex = "#FFD700"),
    Platform(name = "Loadshare",  colorHex = "#16A34A"),
    Platform(name = "MagicFleet", colorHex = "#9333EA"),
    Platform(name = "Ola",        colorHex = "#222222"),
    Platform(name = "Shiprocket", colorHex = "#EF4444"),
    Platform(name = "Other",      colorHex = "#6B7280")
)

@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val platformId: Long = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val address: String,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val area: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class DeliveryStatus { COMPLETED, CANCELLED, PENDING }

@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platformName: String,
    val orderId: String = "",
    val earnings: Double,
    val tipAmount: Double = 0.0,
    val distanceKm: Double = 0.0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val restaurantId: Long = 0,
    val restaurantName: String = "",
    val pickupAddress: String = "",
    val pickupLat: Double = 0.0,
    val pickupLon: Double = 0.0,
    val customerId: Long = 0,
    val customerName: String = "",
    val dropAddress: String = "",
    val dropLat: Double = 0.0,
    val dropLon: Double = 0.0,
    val status: String = DeliveryStatus.COMPLETED.name,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long = 0,
    val totalEarnings: Double = 0.0,
    val totalDeliveries: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val batteryStart: Int = 0,
    val batteryEnd: Int = 0,
    val fuelCost: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val periodType: String = "DAILY",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val amount: Double,
    val description: String = "",
    val date: Long = System.currentTimeMillis()
)
