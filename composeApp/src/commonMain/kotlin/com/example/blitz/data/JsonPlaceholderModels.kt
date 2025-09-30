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
    // Real city coordinates for major US cities (lat, lng)
    val realCityCoordinates = listOf(
        Pair(40.7128, -74.0060), // New York City
        Pair(34.0522, -118.2437), // Los Angeles
        Pair(41.8781, -87.6298), // Chicago
        Pair(29.7604, -95.3698), // Houston
        Pair(33.4484, -112.0740), // Phoenix
        Pair(39.9526, -75.1652), // Philadelphia
        Pair(29.4241, -98.4936), // San Antonio
        Pair(32.7767, -96.7970), // Dallas
        Pair(37.3382, -121.8863), // San Jose
        Pair(30.2672, -97.7431), // Austin
        Pair(32.7157, -117.1611), // San Diego
        Pair(37.7749, -122.4194), // San Francisco
        Pair(39.7392, -104.9903), // Denver
        Pair(35.2271, -80.8431), // Charlotte
        Pair(47.6062, -122.3321), // Seattle
        Pair(25.7617, -80.1918), // Miami
        Pair(38.9072, -77.0369), // Washington DC
        Pair(42.3601, -71.0589), // Boston
        Pair(36.1627, -86.7816), // Nashville
        Pair(45.5152, -122.6784), // Portland
    )
    
    // Use post ID to deterministically select coordinates
    val coordinates = realCityCoordinates[id % realCityCoordinates.size]
    
    // Add small random offset for variety within the city (±0.05 degrees ≈ 5.5km)
    val latOffset = ((id * 7) % 100 - 50) * 0.001 // -0.05 to +0.05
    val lngOffset = ((id * 11) % 100 - 50) * 0.001 // -0.05 to +0.05
    
    val finalLatitude = coordinates.first + latOffset
    val finalLongitude = coordinates.second + lngOffset
    
    val operators = listOf("Tesla", "ChargePoint", "Electrify America", "EVgo", "Blink", "Shell")
    val speeds = listOf("Fast", "Rapid", "Ultra-rapid")
    val prices = listOf(0.25, 0.28, 0.30, 0.31, 0.33, 0.35, 0.36)
    
    return ChargingStation(
        id = id.toString(),
        name = title.take(50), // Use post title as station name
        address = user?.address?.let { "${it.street}, ${it.city}" } ?: "Address not available",
        latitude = finalLatitude,
        longitude = finalLongitude,
        numberOfPorts = (4..16).random(),
        availablePorts = (0..8).random(),
        pricePerKwh = prices.random(),
        operator = user?.company?.name ?: operators.random(),
        chargingSpeed = speeds.random(),
        isOperational = (id % 7) != 0 // Make some stations offline
    )
}