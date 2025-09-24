package com.example.blitz.data

import kotlinx.serialization.Serializable

@Serializable
data class ChargingStation(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val numberOfPorts: Int,
    val availablePorts: Int,
    val pricePerKwh: Double,
    val operator: String,
    val chargingSpeed: String, // "Fast", "Rapid", "Ultra-rapid"
    val isOperational: Boolean = true
)

@Serializable
data class ChargingStationsResponse(
    val stations: List<ChargingStation>
)
