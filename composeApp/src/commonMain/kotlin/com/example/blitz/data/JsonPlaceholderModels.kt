package com.example.blitz.data

import kotlinx.serialization.Serializable

// JSONPlaceholder Post model - we'll adapt this for charging stations
@Serializable
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

// JSONPlaceholder User model - for additional station info
@Serializable
data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    val address: Address,
    val company: Company
)

@Serializable
data class Address(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    val geo: Geo
)

@Serializable
data class Geo(
    val lat: String,
    val lng: String
)

@Serializable
data class Company(
    val name: String,
    val catchPhrase: String,
    val bs: String
)

// Adapter function to convert Post + User data to ChargingStation
fun Post.toChargingStation(user: User? = null): ChargingStation {
    // Generate realistic charging station data based on post/user info
    val baseLatitude = user?.address?.geo?.lat?.toDoubleOrNull() ?: (40.7128 + (id % 10) * 0.01)
    val baseLongitude = user?.address?.geo?.lng?.toDoubleOrNull() ?: (-74.0060 + (id % 10) * 0.01)
    
    val operators = listOf("Tesla", "ChargePoint", "Electrify America", "EVgo", "Blink", "Shell")
    val speeds = listOf("Fast", "Rapid", "Ultra-rapid")
    val prices = listOf(0.25, 0.28, 0.30, 0.31, 0.33, 0.35, 0.36)
    
    return ChargingStation(
        id = id.toString(),
        name = title.take(50), // Use post title as station name
        address = user?.address?.let { "${it.street}, ${it.city}" } ?: "Address not available",
        latitude = baseLatitude,
        longitude = baseLongitude,
        numberOfPorts = (4..16).random(),
        availablePorts = (0..8).random(),
        pricePerKwh = prices.random(),
        operator = user?.company?.name ?: operators.random(),
        chargingSpeed = speeds.random(),
        isOperational = (id % 7) != 0 // Make some stations offline
    )
}