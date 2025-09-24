package com.example.blitz.platform

import android.content.Context
import android.content.Intent
import android.net.Uri

class AndroidNavigationManager(private val context: Context) : NavigationManager {
    
    override fun navigateToLocation(latitude: Double, longitude: Double, destinationName: String) {
        try {
            // Try to open Google Maps with navigation
            val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback to web Google Maps if Google Maps app is not installed
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&destination_place_id=$destinationName")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            // Final fallback to generic maps intent
            val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($destinationName)")
            val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
            context.startActivity(geoIntent)
        }
    }
}
