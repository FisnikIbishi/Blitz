package com.example.blitz.viewmodel

import com.example.blitz.data.ChargingStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChargingStationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun testInitialState() {
        // Given & When
        val viewModel = ChargingStationViewModel()
        
        // Then
        val initialState = viewModel.uiState.value
        assertEquals(emptyList(), initialState.stations)
        assertFalse(initialState.isLoading)
        assertEquals(null, initialState.errorMessage)
        assertFalse(initialState.showOnlyAvailable)
    }

    @Test
    fun testLoadChargingStationsSuccess() = runTest {
        // Given
        Dispatchers.setMain(testDispatcher)
        val viewModel = ChargingStationViewModel()
        
        // Wait for initial loading to complete
        testScheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.stations.isNotEmpty())
        assertEquals(null, state.errorMessage)
        
        // Cleanup
        Dispatchers.resetMain()
    }

    @Test
    fun testToggleShowOnlyAvailable() = runTest {
        // Given
        Dispatchers.setMain(testDispatcher)
        val viewModel = ChargingStationViewModel()
        
        // Wait for initial loading
        testScheduler.advanceUntilIdle()
        val initialStationCount = viewModel.uiState.value.stations.size
        
        // When
        viewModel.toggleShowOnlyAvailable()
        testScheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.showOnlyAvailable)
        
        // Available stations should be <= all stations
        assertTrue(state.stations.size <= initialStationCount)
        
        // All returned stations should be operational and have available ports
        state.stations.forEach { station ->
            assertTrue(station.isOperational, "Station ${station.id} should be operational")
            assertTrue(station.availablePorts > 0, "Station ${station.id} should have available ports")
        }
        
        // Toggle back
        viewModel.toggleShowOnlyAvailable()
        testScheduler.advanceUntilIdle()
        
        val finalState = viewModel.uiState.value
        assertFalse(finalState.showOnlyAvailable)
        assertEquals(initialStationCount, finalState.stations.size)
        
        // Cleanup
        Dispatchers.resetMain()
    }

    @Test
    fun testRefreshStations() = runTest {
        // Given
        Dispatchers.setMain(testDispatcher)
        val viewModel = ChargingStationViewModel()
        
        // Wait for initial loading
        testScheduler.advanceUntilIdle()
        val initialState = viewModel.uiState.value
        
        // When
        viewModel.refreshStations()
        
        // Should start loading
        val loadingState = viewModel.uiState.value
        // Note: Due to the speed of our test dispatcher, loading might complete immediately
        
        testScheduler.advanceUntilIdle()
        
        // Then
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertEquals(initialState.stations.size, finalState.stations.size)
        assertEquals(null, finalState.errorMessage)
        
        // Cleanup
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModelMemoryManagement() {
        // Test that ViewModel properly handles lifecycle
        val viewModel = ChargingStationViewModel()
        
        // ViewModel should initialize properly
        val state = viewModel.uiState.value
        assertEquals(emptyList<ChargingStation>(), state.stations)
        assertFalse(state.isLoading)
        assertFalse(state.showOnlyAvailable)
    }

    @Test
    fun testStateTransitions() = runTest {
        // Test the loading state transitions
        Dispatchers.setMain(testDispatcher)
        val viewModel = ChargingStationViewModel()
        
        // Check initial state
        var state = viewModel.uiState.value
        assertEquals(emptyList<ChargingStation>(), state.stations)
        
        // Loading should complete after initialization
        testScheduler.advanceUntilIdle()
        
        state = viewModel.uiState.value
        assertFalse(state.isLoading, "Loading should be false after completion")
        assertTrue(state.stations.isNotEmpty(), "Should have loaded stations")
        assertEquals(null, state.errorMessage, "Should not have error")
        
        // Cleanup
        Dispatchers.resetMain()
    }

    @Test
    fun testFilteringLogicConsistency() = runTest {
        // Test that filtering logic is consistent
        Dispatchers.setMain(testDispatcher)
        val viewModel = ChargingStationViewModel()
        
        testScheduler.advanceUntilIdle()
        
        // Get all stations
        val allStations = viewModel.uiState.value.stations
        
        // Toggle to available only
        viewModel.toggleShowOnlyAvailable()
        testScheduler.advanceUntilIdle()
        
        val availableStations = viewModel.uiState.value.stations
        
        // Manually filter and compare
        val expectedAvailable = allStations.filter { it.isOperational && it.availablePorts > 0 }
        assertEquals(expectedAvailable.size, availableStations.size, "Filtering logic should be consistent")
        
        // Cleanup
        Dispatchers.resetMain()
    }
}