package com.example.blitz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blitz.data.ChargingStation
import com.example.blitz.repository.ChargingStationRepository
import com.example.blitz.ui.components.ChargingStationItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingStationsScreen() {
    var stations by remember { mutableStateOf<List<ChargingStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOnlyAvailable by remember { mutableStateOf(false) }
    var selectedStation by remember { mutableStateOf<ChargingStation?>(null) }
    
    val repository = remember { ChargingStationRepository() }
    val scope = rememberCoroutineScope()
    
    fun loadStations() {
        scope.launch {
            isLoading = true
            errorMessage = null
            
            val result = if (showOnlyAvailable) {
                repository.getAvailableStations()
            } else {
                repository.getChargingStations()
            }
            
            result.fold(
                onSuccess = { loadedStations ->
                    stations = loadedStations
                    isLoading = false
                },
                onFailure = { error ->
                    stations = emptyList()
                    isLoading = false
                    errorMessage = error.message ?: "Unknown error occurred"
                }
            )
        }
    }
    
    LaunchedEffect(showOnlyAvailable) {
        loadStations()
    }
    
    // Show detail screen if a station is selected
    selectedStation?.let { station ->
        ChargingStationDetailScreen(
            station = station,
            onBackClick = { selectedStation = null }
        )
        return
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Charging Stations",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                // Filter toggle button
                IconButton(
                    onClick = { 
                        showOnlyAvailable = !showOnlyAvailable
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Toggle filter",
                        tint = if (showOnlyAvailable) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Refresh button
                IconButton(
                    onClick = { loadStations() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh stations"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Filter indicator
        if (showOnlyAvailable) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Showing only available stations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                isLoading -> {
                    LoadingState()
                }
                
                errorMessage != null -> {
                    ErrorState(
                        message = errorMessage!!,
                        onRetry = { loadStations() }
                    )
                }
                
                stations.isEmpty() -> {
                    EmptyState(
                        showOnlyAvailable = showOnlyAvailable,
                        onShowAll = { showOnlyAvailable = false }
                    )
                }
                
                else -> {
                    StationsList(
                        stations = stations,
                        onStationClick = { station -> selectedStation = station }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading charging stations...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun EmptyState(
    showOnlyAvailable: Boolean,
    onShowAll: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = if (showOnlyAvailable) {
                    "No available stations found"
                } else {
                    "No charging stations found"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (showOnlyAvailable) {
                    "Try showing all stations or refresh the list"
                } else {
                    "Please try refreshing the list"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (showOnlyAvailable) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onShowAll) {
                    Text("Show All Stations")
                }
            }
        }
    }
}

@Composable
private fun StationsList(
    stations: List<ChargingStation>,
    onStationClick: (ChargingStation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(stations) { station ->
            ChargingStationItem(
                station = station,
                onClick = { onStationClick(station) }
            )
        }
    }
}
