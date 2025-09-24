import Foundation
import UIKit

@objc public class NavigationHelper: NSObject {
    
    @objc public static func openMaps(latitude: Double, longitude: Double, destinationName: String) {
        // Try Apple Maps first
        let appleMapsURLString = "http://maps.apple.com/?daddr=\(latitude),\(longitude)&dirflg=d"
        
        if let appleMapsURL = URL(string: appleMapsURLString), UIApplication.shared.canOpenURL(appleMapsURL) {
            UIApplication.shared.open(appleMapsURL, options: [:], completionHandler: nil)
        } else {
            // Fallback to Google Maps if Apple Maps is not available
            let googleMapsURLString = "comgooglemaps://?daddr=\(latitude),\(longitude)&directionsmode=driving"
            
            if let googleMapsURL = URL(string: googleMapsURLString), UIApplication.shared.canOpenURL(googleMapsURL) {
                UIApplication.shared.open(googleMapsURL, options: [:], completionHandler: nil)
            } else {
                // Final fallback to web Google Maps
                let webMapsURLString = "https://www.google.com/maps/dir/?api=1&destination=\(latitude),\(longitude)"
                if let webMapsURL = URL(string: webMapsURLString) {
                    UIApplication.shared.open(webMapsURL, options: [:], completionHandler: nil)
                }
            }
        }
    }
}
