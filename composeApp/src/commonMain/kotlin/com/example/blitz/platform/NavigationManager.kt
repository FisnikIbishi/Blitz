package com.example.blitz.platform

interface NavigationManager {
    fun navigateToLocation(latitude: Double, longitude: Double, destinationName: String)
}
