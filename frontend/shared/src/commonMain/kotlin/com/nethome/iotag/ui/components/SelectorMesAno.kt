package com.nethome.iotag.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SelectorMesAno(
    mes: Int,
    anio: Int,
    minMes: Int,
    minAno: Int,
    onFechaCambiada: (Int, Int) -> Unit
) {
    val nombresMeses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    val momentoActual = kotlin.time.Clock.System.now()
    val fechaLocal = momentoActual.toLocalDateTime(TimeZone.currentSystemDefault())

    val mesReal = fechaLocal.monthNumber
    val anioReal = fechaLocal.year

    val esMesActual = (mes == mesReal && anio == anioReal)
    val esMesMinimo = (anio == minAno && mes <= minMes) || (anio < minAno)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            if (mes == 1) onFechaCambiada(12, anio - 1)
            else onFechaCambiada(mes - 1, anio)
        },
            enabled = !esMesMinimo
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Anterior",
                tint = if (esMesMinimo) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                else LocalContentColor.current)
        }

        Text(
            text = "${nombresMeses[mes - 1]} $anio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        IconButton(
            onClick = {
                if (mes == 12) onFechaCambiada(1, anio + 1)
                else onFechaCambiada(mes + 1, anio)
            },
            enabled = !esMesActual
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Siguiente",
                tint = if (esMesActual) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else LocalContentColor.current
            )
        }
    }
}