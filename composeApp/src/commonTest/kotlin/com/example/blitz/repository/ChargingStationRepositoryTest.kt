package com.example.blitz.repository

import com.example.blitz.data.ChargingStation
import com.example.blitz.network.ChargingStationApiClient
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChargingStationRepositoryTest {

    // Mock API client for testing
    class MockChargingStationApiClient : ChargingStationApiClient() {
        var shouldThrowException = false
        var mockStations = listOf<ChargingStation>()

        override suspend fun getChargingStations(): List<ChargingStation> {
            if (shouldThrowException) {
                throw Exception("Network error")
            }
            return mockStations
        }

        override suspend fun getChargingStationById(id: String): ChargingStation? {
            if (shouldThrowException) {
                throw Exception("Network error")
            }
            return mockStations.find { it.id == id }
        }
    }

    private fun createMockRepository(): Pair<ChargingStationRepository, MockChargingStationApiClient> {
        val mockApiClient = MockChargingStationApiClient()
        // Note: We'd need to modify the repository to accept an API client for proper testing
        // For now, we'll test the actual repository behavior
        val repository = ChargingStationRepository()
        return Pair(repository, mockApiClient)
    }

    @Test
    fun testGetChargingStationsSuccess() = runTest {
        // Given
        val repository = ChargingStationRepository()

        // When
        val result = repository.getChargingStations()

        // Then
        assertTrue(result.isSuccess, "Should successfully fetch charging stations")
        val stations = result.getOrNull()
        assertNotNull(stations, "Stations list should not be null")
        assertTrue(stations.isNotEmpty(), "Should return some charging stations")
        
        // Verify each station has required fields
        stations.forEach { station ->
            assertNotNull(station.id, "Station ID should not be null")
            assertNotNull(station.name, "Station name should not be null")
            assertNotNull(station.address, "Station address should not be null")
            assertNotNull(station.operator, "Station operator should not be null")
            assertTrue(station.numberOfPorts > 0, "Station should have ports")
            assertTrue(station.availablePorts >= 0, "Available ports should be non-negative")
            assertTrue(station.pricePerKwh > 0, "Price should be positive")
        }
    }

    @Test
    fun testGetAvailableStationsFiltering() = runTest {
        // Given
        val repository = ChargingStationRepository()

        // When
        val allStationsResult = repository.getChargingStations()
        val availableStationsResult = repository.getAvailableStations()

        // Then
        assertTrue(allStationsResult.isSuccess)
        assertTrue(availableStationsResult.isSuccess)
        
        val allStations = allStationsResult.getOrNull()!!
        val availableStations = availableStationsResult.getOrNull()!!
        
        // Available stations should be a subset of all stations
        assertTrue(availableStations.size <= allStations.size, "Available stations should be <= all stations")
        
        // All available stations should be operational and have available ports
        availableStations.forEach { station ->
            assertTrue(station.isOperational, "Available station should be operational")
            assertTrue(station.availablePorts > 0, "Available station should have available ports")
        }
        
        // Count stations that should be filtered out
        val nonOperationalStations = allStations.count { !it.isOperational }
        val fullStations = allStations.count { it.isOperational && it.availablePorts == 0 }
        
        assertEquals(
            allStations.size - nonOperationalStations - fullStations,
            availableStations.size,
            "Available stations count should match filter logic"
        )
    }

    @Test
    fun testRepositoryReturnsExpectedStationCount() = runTest {
        // JSONPlaceholder has 100 posts, so we should get 100 charging stations
        val repository = ChargingStationRepository()
        
        val result = repository.getChargingStations()
        
        assertTrue(result.isSuccess)
        val stations = result.getOrNull()!!
        assertEquals(100, stations.size, "Should return 100 charging stations from 100 JSONPlaceholder posts")
    }

    @Test
    fun testStationDataConsistency() = runTest {
        // Test that station data is consistent across multiple calls
        val repository = ChargingStationRepository()
        
        val result1 = repository.getChargingStations()
        val result2 = repository.getChargingStations()
        
        assertTrue(result1.isSuccess && result2.isSuccess)
        
        val stations1 = result1.getOrNull()!!
        val stations2 = result2.getOrNull()!!
        
        assertEquals(stations1.size, stations2.size, "Station count should be consistent")
        
        // Since we use deterministic generation based on post ID, 
        // same posts should generate same stations
        stations1.forEachIndexed { index, station1 ->
            val station2 = stations2[index]
            assertEquals(station1.id, station2.id, "Station IDs should be consistent")
            assertEquals(station1.name, station2.name, "Station names should be consistent")
            assertEquals(station1.operator, station2.operator, "Station operators should be consistent")
            // Note: Random fields like availablePorts might vary, which is expected
        }
    }

    @Test
    fun testStationCoordinateValidity() = runTest {
        // Test that all stations have valid US coordinates
        val repository = ChargingStationRepository()
        
        val result = repository.getChargingStations()
        assertTrue(result.isSuccess)
        
        val stations = result.getOrNull()!!
        
        stations.forEach { station ->
            // Verify coordinates are within US bounds
            assertTrue(
                station.latitude > 25.0 && station.latitude < 50.0,
                "Station ${station.id} latitude ${station.latitude} should be within US bounds"
            )
            assertTrue(
                station.longitude > -125.0 && station.longitude < -65.0,
                "Station ${station.id} longitude ${station.longitude} should be within US bounds"
            )
        }
    }

    @Test
    fun testOperatorDistribution() = runTest {
        // Test that we get a variety of operators
        val repository = ChargingStationRepository()
        
        val result = repository.getChargingStations()
        assertTrue(result.isSuccess)
        
        val stations = result.getOrNull()!!
        val operators = stations.map { it.operator }.toSet()
        
        // Should have multiple different operators
        assertTrue(operators.size > 1, "Should have multiple different operators")
        
        // Should include some expected operators
        val expectedOperators = setOf("Tesla", "ChargePoint", "Electrify America", "EVgo", "Blink", "Shell")
        val hasExpectedOperators = operators.any { it in expectedOperators }
        assertTrue(hasExpectedOperators, "Should have some expected operators from our list")
    }
}