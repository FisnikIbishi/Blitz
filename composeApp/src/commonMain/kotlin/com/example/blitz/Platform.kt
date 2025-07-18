package com.example.blitz

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform