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

@Serializable
data class HistorialResponse(
    val periodo: String,
    val resumen: ResumenData,
    val datos_grafica: List<GraficaData>
)

@Serializable
data class ResumenData(
    val total_kwh_periodo: Float,
    val total_dinero_periodo: Float,
    val hora_pico: String
)

@Serializable
data class GraficaData(
    val etiqueta: String,
    val kwh: Float,
    val coste: Float
)

@Serializable
data class FechaInicio(
    @SerialName("mes_inicio") val mes: Int,
    @SerialName("ano_inicio") val ano: Int
)