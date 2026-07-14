package com.nethome.iotag.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nethome.iotag.data.repository.SensorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val temperatura: Float = 0.0f,
    val tendenciaTemperatura: String = "...",
    val unidadTemperatura: String = "°C",

    val consumoKw: Float = 0.0f,
    val tendenciaConsumo: String = "...",
    val unidadConsumo: String = "kW",

    val isLoading: Boolean = true,
    val error: String? = null,

    val mensajeIA: String = "..."
)

class DashboardViewModel(private val repo: SensorRepository = SensorRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun update() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dataTemp = repo.getTemperature()
                val dataElec = repo.getElectricity()

                _uiState.update { currentState ->
                    currentState.copy(
                        temperatura = dataTemp.temperatura,
                        tendenciaTemperatura = dataTemp.tendencia,
                        unidadTemperatura = dataTemp.unidad,

                        consumoKw = dataElec.potencia_kw,
                        tendenciaConsumo = dataElec.tendencia,
                        unidadConsumo = dataElec.unidad,

                        isLoading = false
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = "Error al conectar con el servidor") }
            }
        }
    }

    fun procesarComandoVoz(texto: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(mensajeIA = "Pensando...", isLoading = true) }

            try {
                val respuesta = repo.enviarOrdenIA(texto)
                _uiState.update { it.copy(mensajeIA = respuesta, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeIA = "Error de conexión", isLoading = false) }
            }
        }
    }
}