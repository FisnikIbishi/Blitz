package com.example.blitz.network

import com.example.blitz.data.ChargingStation
import com.example.blitz.data.ChargingStationsResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ChargingStationApiClient {
    
    private val mockClient = HttpClient(MockEngine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        engine {
            addHandler { request ->
                when (request.url.encodedPath) {
                    "/api/charging-stations" -> {
                        val mockStations = getMockChargingStations()
                        val responseContent = Json.encodeToString(
                            ChargingStationsResponse.serializer(),
                            ChargingStationsResponse(mockStations)
                        )
                        respond(
                            content = responseContent,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    else -> {
                        error("Unhandled ${request.url.encodedPath}")
                    }
                }
            }
        }
    }
    
    suspend fun getChargingStations(): List<ChargingStation> {
        val response: ChargingStationsResponse = mockClient.get("/api/charging-stations").body()
        return response.stations
    }
    
    private fun getMockChargingStations(): List<ChargingStation> {
        return listOf(
            ChargingStation(
                id = "1",
                name = "Tesla Supercharger - City Center",
                address = "123 Main Street, Downtown",
                latitude = 40.7128,
                longitude = -74.0060,
                numberOfPorts = 8,
                availablePorts = 3,
                pricePerKwh = 0.28,
                operator = "Tesla",
                chargingSpeed = "Ultra-rapid"
            ),
            ChargingStation(
                id = "2",
                name = "ChargePoint - Shopping Mall",
                address = "456 Commerce Way, West Side",
                latitude = 40.7589,
                longitude = -73.9851,
                numberOfPorts = 6,
                availablePorts = 6,
                pricePerKwh = 0.35,
                operator = "ChargePoint",
                chargingSpeed = "Fast"
            ),
            ChargingStation(
                id = "3",
                name = "Electrify America - Highway Rest Stop",
                address = "789 Highway 101, North Exit",
                latitude = 40.7831,
                longitude = -73.9712,
                numberOfPorts = 12,
                availablePorts = 2,
                pricePerKwh = 0.31,
                operator = "Electrify America",
                chargingSpeed = "Rapid"
            ),
            ChargingStation(
                id = "4",
                name = "EVgo - Business District",
                address = "321 Corporate Plaza, Financial District",
                latitude = 40.7484,
                longitude = -73.9857,
                numberOfPorts = 4,
                availablePorts = 1,
                pricePerKwh = 0.33,
                operator = "EVgo",
                chargingSpeed = "Fast"
            ),
            ChargingStation(
                id = "5",
                name = "Blink Charging - University Campus",
                address = "555 College Avenue, Campus North",
                latitude = 40.7282,
                longitude = -73.9942,
                numberOfPorts = 10,
                availablePorts = 7,
                pricePerKwh = 0.25,
                operator = "Blink",
                chargingSpeed = "Fast"
            ),
            ChargingStation(
                id = "6",
                name = "Tesla Supercharger - Airport",
                address = "Airport Terminal 1, Parking Level 3",
                latitude = 40.6892,
                longitude = -74.1745,
                numberOfPorts = 16,
                availablePorts = 9,
                pricePerKwh = 0.30,
                operator = "Tesla",
                chargingSpeed = "Ultra-rapid"
            ),
            ChargingStation(
                id = "7",
                name = "Shell Recharge - Gas Station",
                address = "888 Route 9, Suburban Area",
                latitude = 40.7614,
                longitude = -73.9776,
                numberOfPorts = 2,
                availablePorts = 0,
                pricePerKwh = 0.36,
                operator = "Shell",
                chargingSpeed = "Rapid",
                isOperational = false
            ),
            ChargingStation(
                id = "8",
                name = "ChargePoint - Hotel District",
                address = "999 Hotel Row, Tourism Quarter",
                latitude = 40.7505,
                longitude = -73.9934,
                numberOfPorts = 8,
                availablePorts = 5,
                pricePerKwh = 0.29,
                operator = "ChargePoint",
                chargingSpeed = "Fast"
            )
        )
    }
}
