package com.example.blitz.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.math.abs

class ChargingStationCoordinateTest {

    @Test
    fun testAllStationsMapToRealCities() {
        // Test first 20 posts to verify they map to our 20 cities correctly
        val expectedCities = listOf(
            Pair(40.7128, -74.0060), // New York City - Post ID 1
            Pair(34.0522, -118.2437), // Los Angeles - Post ID 2  
            Pair(41.8781, -87.6298), // Chicago - Post ID 3
            Pair(29.7604, -95.3698), // Houston - Post ID 4
            Pair(33.4484, -112.0740), // Phoenix - Post ID 5
        )

        for (i in 1..5) {
            val post = Post(i, 1, "Test Station $i", "Body")
            val station = post.toChargingStation()
            val expectedCoords = expectedCities[i - 1]
            
            // Should be within 0.1 degrees of expected city center (≈11km)
            assertTrue(
                abs(station.latitude - expectedCoords.first) < 0.1,
                "Station $i latitude ${station.latitude} should be near ${expectedCoords.first}"
            )
            assertTrue(
                abs(station.longitude - expectedCoords.second) < 0.1,
                "Station $i longitude ${station.longitude} should be near ${expectedCoords.second}"
            )
        }
    }

    @Test
    fun testCyclicCityMapping() {
        // Test that IDs cycle through cities (20 cities total)
        val post1 = Post(1, 1, "Test", "Body")    // Should map to city 0 (NYC)
        val post21 = Post(21, 1, "Test", "Body")  // Should also map to city 0 (21 % 20 = 1, but array is 0-indexed)
        val post41 = Post(41, 1, "Test", "Body")  // Should also map to city 0
        
        val station1 = post1.toChargingStation()
        val station21 = post21.toChargingStation()
        val station41 = post41.toChargingStation()
        
        // All should be in same city area (within 0.2 degrees to account for offset variation)
        assertTrue(abs(station1.latitude - station21.latitude) < 0.2)
        assertTrue(abs(station1.longitude - station21.longitude) < 0.2)
        assertTrue(abs(station1.latitude - station41.latitude) < 0.2)
        assertTrue(abs(station1.longitude - station41.longitude) < 0.2)
    }

    @Test
    fun testCoordinateVariation() {
        // Test that stations in same city have slight variation
        val posts = listOf(
            Post(1, 1, "Station A", "Body"),
            Post(21, 1, "Station B", "Body"),  // Same city as post 1
            Post(41, 1, "Station C", "Body")   // Same city as post 1
        )
        
        val stations = posts.map { it.toChargingStation() }
        
        // Stations should have some variation (but not too much)
        for (i in 0 until stations.size - 1) {
            for (j in i + 1 until stations.size) {
                val station1 = stations[i]
                val station2 = stations[j]
                
                // Should have some difference (offset variation)
                val latDiff = abs(station1.latitude - station2.latitude)
                val lngDiff = abs(station1.longitude - station2.longitude)
                
                // At least one coordinate should be different (due to offset)
                assertTrue(latDiff > 0.0001 || lngDiff > 0.0001, "Stations should have coordinate variation")
                
                // But not too different (same city)
                assertTrue(latDiff < 0.2, "Latitude difference should be < 0.2 degrees")
                assertTrue(lngDiff < 0.2, "Longitude difference should be < 0.2 degrees")
            }
        }
    }

    @Test
    fun testNoBoundaryViolations() {
        // Test that no coordinates fall outside reasonable US bounds
        val posts = (1..100).map { Post(it, 1, "Station $it", "Body") }
        
        posts.forEach { post ->
            val station = post.toChargingStation()
            
            // US latitude bounds (with margin for Alaska/Hawaii edge cases)
            assertTrue(
                station.latitude > 20.0 && station.latitude < 55.0,
                "Station ${station.id} latitude ${station.latitude} is outside reasonable US bounds"
            )
            
            // US longitude bounds (with margin)  
            assertTrue(
                station.longitude > -130.0 && station.longitude < -60.0,
                "Station ${station.id} longitude ${station.longitude} is outside reasonable US bounds"
            )
        }
    }

    @Test
    fun testDeterministicBehavior() {
        // Same input should always produce same output
        val post = Post(42, 3, "Test Station", "Test Body")
        
        val station1 = post.toChargingStation()
        val station2 = post.toChargingStation()
        val station3 = post.toChargingStation()
        
        assertEquals(station1.latitude, station2.latitude, "Latitude should be deterministic")
        assertEquals(station1.longitude, station2.longitude, "Longitude should be deterministic")
        assertEquals(station1.latitude, station3.latitude, "Latitude should be deterministic")
        assertEquals(station1.longitude, station3.longitude, "Longitude should be deterministic")
    }

    @Test
    fun testCityDistribution() {
        // Test that we get good distribution across all cities
        val posts = (1..100).map { Post(it, 1, "Station $it", "Body") }
        val stations = posts.map { it.toChargingStation() }
        
        // Group by approximate city (round to 1 decimal to group nearby coordinates)
        val cityGroups = stations.groupBy { 
            Pair(
                (it.latitude * 10).toInt(), 
                (it.longitude * 10).toInt()
            )
        }
        
        // Should have multiple city groups (at least 15 out of 20 possible cities)
        assertTrue(cityGroups.size >= 15, "Should have good distribution across cities")
        
        // Each city should have roughly equal number of stations (100 stations / 20 cities = 5 each)
        val stationsPerCity = cityGroups.values.map { it.size }
        val avgStationsPerCity = stationsPerCity.average()
        
        // Average should be around 5 (100/20)
        assertTrue(avgStationsPerCity > 3.0 && avgStationsPerCity < 7.0, "Should have roughly equal distribution")
    }

    @Test
    fun testOffsetCalculation() {
        // Test that offset calculation works as expected
        val post = Post(1, 1, "Test", "Body")
        val station = post.toChargingStation()
        
        // Calculate expected offset for post ID 1
        val expectedLatOffset = ((1 * 7) % 100 - 50) * 0.001 // = (7 - 50) * 0.001 = -0.043
        val expectedLngOffset = ((1 * 11) % 100 - 50) * 0.001 // = (11 - 50) * 0.001 = -0.039
        
        // NYC coordinates: 40.7128, -74.0060
        val expectedLat = 40.7128 + expectedLatOffset
        val expectedLng = -74.0060 + expectedLngOffset
        
        assertEquals(expectedLat, station.latitude, 0.0001, "Latitude offset calculation")
        assertEquals(expectedLng, station.longitude, 0.0001, "Longitude offset calculation")
    }
}