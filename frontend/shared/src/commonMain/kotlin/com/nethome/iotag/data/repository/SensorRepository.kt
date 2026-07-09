package com.nethome.iotag.data.repository

import com.nethome.iotag.data.SensorData
import com.nethome.iotag.data.network.KtorClient
import com.nethome.iotag.data.network.NetworkConfig
import io.ktor.client.call.body
import io.ktor.client.request.get

class SensorRepository {
    suspend fun getTemperature(): SensorData {
        return KtorClient.client
            .get("${NetworkConfig.BASE_URL}/api/temperatura/actual").body()
    }
}