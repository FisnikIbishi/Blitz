package com.example.blitz.platform

import platform.UIKit.UIApplication
import platform.Foundation.NSURL

class IOSNavigationManager : NavigationManager {
    
    override fun navigateToLocation(latitude: Double, longitude: Double, destinationName: String) {
        try {
            // Create Apple Maps URL for navigation  
            val appleMapsUrl = "http://maps.apple.com/?daddr=$latitude,$longitude&dirflg=d"
            val nsUrl = NSURL.URLWithString(appleMapsUrl)
            
            if (nsUrl != null) {
                val application = UIApplication.sharedApplication
                if (application.canOpenURL(nsUrl)) {
                    application.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
                    println("iOS Navigation: Successfully opened Apple Maps for $destinationName")
                } else {
                    println("iOS Navigation: Cannot open Apple Maps URL")
                }
            } else {
                println("iOS Navigation: Invalid URL created")
            }
        } catch (e: Exception) {
            println("iOS Navigation: Error opening maps: ${e.message}")
            println("iOS Navigation: Fallback - would navigate to $destinationName at ($latitude, $longitude)")
        }
    }
}
