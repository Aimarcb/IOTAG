package com.nethome.iotag.data.repository

import com.nethome.iotag.data.ElectricidadData
import com.nethome.iotag.data.FechaInicio
import com.nethome.iotag.data.HistorialResponse
import com.nethome.iotag.data.TemperaturaData
import com.nethome.iotag.data.network.KtorClient
import com.nethome.iotag.data.network.NetworkConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class IaRequest(val mensaje: String)

@Serializable
data class IaResponse(val respuesta: String)

class SensorRepository {
    suspend fun getTemperature(): TemperaturaData {
        return KtorClient.client
            .get("${NetworkConfig.BASE_URL}/api/temperatura/actual").body()
    }

    suspend fun getElectricity(): ElectricidadData {
        return KtorClient.client
            .get("${NetworkConfig.BASE_URL}/api/electricidad/actual").body()
    }

    suspend fun enviarOrdenIA(texto: String): String {
        return try {
            // Hacer un POST al endpoint
            val response = KtorClient.client.post("${NetworkConfig.BASE_URL}/api/ia/procesar_orden") {
                contentType(ContentType.Application.Json)
                setBody(IaRequest(mensaje = texto))
            }

            // Leer respuesta de FastApi
            val iaResponse = response.body<IaResponse>()
            iaResponse.respuesta
        } catch (e: Exception)  {
            e.printStackTrace()
            "Error al comunicar con la ia"
        }
    }

    suspend fun actualizarPrecioLuz(nuevoPrecio: Double): Boolean {
        return try {
            val response = KtorClient.client
                .post("${NetworkConfig.BASE_URL}/api/config/precio") {
                    parameter("nuevo_precio", nuevoPrecio)
                    contentType(ContentType.Application.Json)
                }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getHistorial(periodo: String, mes: Int? = null, ano: Int? = null): HistorialResponse? {
        return try {
            KtorClient.client.get("${NetworkConfig.BASE_URL}/api/electricidad/historial") {
                parameter("periodo", periodo)
                if (mes != null) parameter("mes", mes)
                if (ano != null) parameter("ano", ano)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getFechaInicio(): FechaInicio {
        return KtorClient.client
                .get("${NetworkConfig.BASE_URL}/api/electricidad/fecha-inicio").body()
    }
}