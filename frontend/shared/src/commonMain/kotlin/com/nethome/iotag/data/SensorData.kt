package com.nethome.iotag.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TemperaturaData(
    val temperatura: Float,
    val tendencia: String,
    val unidad: String
)

@Serializable
data class ElectricidadData(
    val potencia_kw: Float,
    val tendencia: String,
    val unidad: String
)