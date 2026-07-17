package com.nethome.iotag.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nethome.iotag.data.HistorialResponse
import com.nethome.iotag.data.repository.SensorRepository
import com.nethome.iotag.ui.components.CanvasBarChart
import com.nethome.iotag.ui.components.SelectorMesAno
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun EnergyScreen(repository: SensorRepository) {
    val fechaHoy = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    var periodoSeleccionado by remember { mutableStateOf("semana") }

    var mesActual by remember { mutableStateOf(fechaHoy.monthNumber) }
    var anioActual by remember { mutableStateOf(fechaHoy.year) }

    var minMes by remember { mutableStateOf(fechaHoy.monthNumber) }
    var minAnio by remember { mutableStateOf(fechaHoy.year) }

    var historial by remember { mutableStateOf<HistorialResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val info = repository.getFechaInicio()
        minMes = info.mes
        minAnio = info.ano
    }

    LaunchedEffect(periodoSeleccionado, mesActual, anioActual) {
        isLoading = true
        if (periodoSeleccionado == "mes") {
            historial = repository.getHistorial(periodoSeleccionado, mesActual, anioActual)
        } else {
            historial = repository.getHistorial(periodoSeleccionado)
        }
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Consumo Eléctrico", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FiltroButton("Día", periodoSeleccionado == "dia") { periodoSeleccionado = "dia" }
            FiltroButton("Semana", periodoSeleccionado == "semana") { periodoSeleccionado = "semana" }
            FiltroButton("Mes", periodoSeleccionado == "mes") { periodoSeleccionado = "mes" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (periodoSeleccionado == "mes") {
            SelectorMesAno(
                mes = mesActual,
                anio = anioActual,
                minMes = minMes,
                minAno = minAnio,  
                onFechaCambiada = { nuevoMes, nuevoAnio ->
                    mesActual = nuevoMes
                    anioActual = nuevoAnio
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (historial != null) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Gastado: ${historial!!.resumen.total_dinero_periodo} €")
                    Text("Total Energía: ${historial!!.resumen.total_kwh_periodo} kWh")
                    Text("Punto de Pico: ${historial!!.resumen.hora_pico}", fontWeight = FontWeight.Bold)
                }
            }

            CanvasBarChart(
                datos = historial!!.datos_grafica,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text("Error al traer los históricos de la base de datos.", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun FiltroButton(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(texto)
    }
}