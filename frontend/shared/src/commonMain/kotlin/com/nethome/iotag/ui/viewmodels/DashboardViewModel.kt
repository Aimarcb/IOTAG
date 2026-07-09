package com.nethome.iotag.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nethome.iotag.data.repository.SensorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val repo: SensorRepository = SensorRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(0.0f)
    val uiState: StateFlow<Float> = _uiState

    fun update() {
        viewModelScope.launch {
            try {
                val data = repo.getTemperature()
                _uiState.value = data.temperatura
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}