package com.nethome.iotag.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorData(
    @SerialName("valor")
    val temperatura: Float
)
