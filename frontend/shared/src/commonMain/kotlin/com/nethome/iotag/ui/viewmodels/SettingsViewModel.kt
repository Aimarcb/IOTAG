package com.nethome.iotag.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nethome.iotag.data.repository.SensorRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SensorRepository): ViewModel() {
    var precioInput by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var mensajeResultado by mutableStateOf<String?>(null)

    fun guardarPrecio() {
        val precio = precioInput.toDoubleOrNull()

        if (precio != null && precio > 0) {
            viewModelScope.launch {
                isLoading = true
                mensajeResultado = null

                val exito = repository.actualizarPrecioLuz(precio)

                isLoading = false
                if (exito) {
                    mensajeResultado = "Precio actualizado correctamente"
                } else {
                    mensajeResultado = "Error al conectar con el servidor"
                }
            }
        } else {
            mensajeResultado = "Por favor, introduce un número válido mayor que 0."
        }
    }

}