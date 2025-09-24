package com.example.blitz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blitz.data.ChargingStation
import com.example.blitz.repository.ChargingStationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChargingStationUiState(
    val stations: List<ChargingStation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showOnlyAvailable: Boolean = false
)

class ChargingStationViewModel : ViewModel() {
    private val repository = ChargingStationRepository()
    
    private val _uiState = MutableStateFlow(ChargingStationUiState())
    val uiState: StateFlow<ChargingStationUiState> = _uiState.asStateFlow()
    
    init {
        loadChargingStations()
    }
    
    fun loadChargingStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = if (_uiState.value.showOnlyAvailable) {
                repository.getAvailableStations()
            } else {
                repository.getChargingStations()
            }
            
            result.fold(
                onSuccess = { stations ->
                    _uiState.value = _uiState.value.copy(
                        stations = stations,
                        isLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        stations = emptyList(),
                        isLoading = false,
                        errorMessage = error.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }
    
    fun toggleShowOnlyAvailable() {
        _uiState.value = _uiState.value.copy(
            showOnlyAvailable = !_uiState.value.showOnlyAvailable
        )
        loadChargingStations()
    }
    
    fun refreshStations() {
        loadChargingStations()
    }
}
