package com.example.blitz

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.blitz.ui.screens.ChargingStationsScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        ChargingStationsScreen()
    }
}