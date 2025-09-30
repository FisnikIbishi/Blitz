package com.example.blitz.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class JsonPlaceholderModelsTest {

    @Test
    fun testPostToChargingStationConversion() {
        // Given
        val testPost = Post(
            id = 1,
            userId = 1,
            title = "This is a test charging station name that is very long and should be truncated",
            body = "Test body content"
        )
        
        val testUser = User(
            id = 1,
            name = "John Doe",
            username = "johndoe",
            email = "john@example.com",
            phone = "123-456-7890",
            website = "example.com",
            address = Address(
                street = "123 Main St",
                suite = "Apt 1",
                city = "Test City",
                zipcode = "12345",
                geo = Geo(lat = "40.7128", lng = "-74.0060")
            ),
            company = Company(
                name = "Test Company",
                catchPhrase = "Test catch phrase",
                bs = "test business"
            )
        )

        // When
        val chargingStation = testPost.toChargingStation(testUser)

        // Then
        assertEquals("1", chargingStation.id)
        assertEquals("This is a test charging station name that is very", chargingStation.name) // Truncated to 50 chars
        assertEquals("123 Main St, Test City", chargingStation.address)
        assertEquals("Test Company", chargingStation.operator)
        assertTrue(chargingStation.numberOfPorts in 4..16)
        assertTrue(chargingStation.availablePorts in 0..8)
        assertTrue(chargingStation.pricePerKwh in listOf(0.25, 0.28, 0.30, 0.31, 0.33, 0.35, 0.36))
        assertTrue(chargingStation.chargingSpeed in listOf("Fast", "Rapid", "Ultra-rapid"))
    }

    @Test
    fun testPostToChargingStationWithoutUser() {
        // Given
        val testPost = Post(
            id = 5,
            userId = 2,
            title = "Short title",
            body = "Test body"
        )

        // When
        val chargingStation = testPost.toChargingStation(null)

        // Then
        assertEquals("5", chargingStation.id)
        assertEquals("Short title", chargingStation.name)
        assertEquals("Address not available", chargingStation.address)
        assertTrue(chargingStation.operator in listOf("Tesla", "ChargePoint", "Electrify America", "EVgo", "Blink", "Shell"))
    }

    @Test
    fun testCoordinatesAreInRealCities() {
        // Test that coordinates are mapped to real US cities
        val posts = listOf(
            Post(1, 1, "Test 1", "Body 1"), // Should map to NYC
            Post(2, 1, "Test 2", "Body 2"), // Should map to LA
            Post(21, 1, "Test 21", "Body 21") // Should also map to NYC (21 % 20 = 1)
        )

        posts.forEach { post ->
            val station = post.toChargingStation()
            
            // Verify coordinates are within reasonable US bounds
            assertTrue(station.latitude > 25.0 && station.latitude < 50.0, "Latitude should be within US bounds")
            assertTrue(station.longitude > -125.0 && station.longitude < -65.0, "Longitude should be within US bounds")
        }
    }

    @Test
    fun testSamePostIdProducesSameBaseCity() {
        // Same post ID should always map to same base city (with slight variation)
        val post = Post(1, 1, "Test", "Body")
        
        val station1 = post.toChargingStation()
        val station2 = post.toChargingStation()
        
        // Should be in same general area (within 0.1 degrees ≈ 11km)
        assertTrue(kotlin.math.abs(station1.latitude - station2.latitude) < 0.1)
        assertTrue(kotlin.math.abs(station1.longitude - station2.longitude) < 0.1)
    }

    @Test
    fun testOperationalStatusLogic() {
        // Test that stations with ID % 7 == 0 are non-operational
        val operationalPost = Post(1, 1, "Test", "Body") // 1 % 7 != 0
        val nonOperationalPost = Post(7, 1, "Test", "Body") // 7 % 7 == 0
        
        val operationalStation = operationalPost.toChargingStation()
        val nonOperationalStation = nonOperationalPost.toChargingStation()
        
        assertTrue(operationalStation.isOperational)
        assertFalse(nonOperationalStation.isOperational)
    }

    @Test
    fun testTitleTruncation() {
        val longTitlePost = Post(
            1, 1, 
            "This is an extremely long title that should definitely be truncated to exactly fifty characters or less",
            "Body"
        )
        
        val station = longTitlePost.toChargingStation()
        
        assertTrue(station.name.length <= 50)
        assertEquals("This is an extremely long title that should defi", station.name)
    }
}