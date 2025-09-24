package com.example.blitz.repository

import com.example.blitz.data.ChargingStation
import com.example.blitz.network.ChargingStationApiClient

class ChargingStationRepository {
    private val apiClient = ChargingStationApiClient()
    
    suspend fun getChargingStations(): Result<List<ChargingStation>> {
        return try {
            val stations = apiClient.getChargingStations()
            Result.success(stations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAvailableStations(): Result<List<ChargingStation>> {
        return try {
            val stations = apiClient.getChargingStations()
            val availableStations = stations.filter { it.isOperational && it.availablePorts > 0 }
            Result.success(availableStations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
