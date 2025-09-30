package com.example.blitz.network

import com.example.blitz.data.ChargingStation
import com.example.blitz.data.Post
import com.example.blitz.data.User
import com.example.blitz.data.toChargingStation
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ChargingStationApiClient {
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    private val baseUrl = "https://jsonplaceholder.typicode.com"
    
    suspend fun getChargingStations(): List<ChargingStation> {
        try {
            // Get posts (we'll use these as charging stations)
            val posts: List<Post> = httpClient.get("$baseUrl/posts").body()
            
            // Get users for additional info
            val users: List<User> = httpClient.get("$baseUrl/users").body()
            val userMap = users.associateBy { it.id }
            
            // Convert posts to charging stations
            return posts.map { post ->
                val user = userMap[post.userId]
                post.toChargingStation(user)
            }
        } catch (e: Exception) {
            throw Exception("Failed to load charging stations: ${e.message}", e)
        }
    }
    
//    suspend fun getChargingStationById(id: String): ChargingStation? {
//        return try {
//            val postId = id.toIntOrNull() ?: return null
//
//            // Get specific post
//            val post: Post = httpClient.get("$baseUrl/posts/$postId").body()
//
//            // Get user info for this post
//            val user: User = httpClient.get("$baseUrl/users/${post.userId}").body()
//
//            post.toChargingStation(user)
//        } catch (e: Exception) {
//            null
//        }
//    }
    
    fun close() {
        httpClient.close()
    }
}
