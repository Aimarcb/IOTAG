package com.nethome.iotag.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nethome.iotag.data.GraficaData
import kotlin.math.max

@OptIn(ExperimentalTextApi::class)
@Composable
fun CanvasBarChart(
    datos: List<GraficaData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (datos.isEmpty()) return

    val maxKwh = max(1f, datos.maxOf { it.kwh })
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 11.sp, color = textColor)
    val valueStyle = TextStyle(fontSize = 10.sp, color = Color.Gray)

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val maxWidthDp = maxWidth

        val minWidthPerBarDp = 40.dp
        val sideMarginDp = 16.dp

        val minRequiredWidthDp = (minWidthPerBarDp * datos.size) + (sideMarginDp * 2)

        val necesitaScroll = minRequiredWidthDp > maxWidthDp
        val canvasWidthDp = if (necesitaScroll) minRequiredWidthDp else maxWidthDp

        val scrollModifier = if (necesitaScroll) Modifier.horizontalScroll(rememberScrollState()) else Modifier

        Canvas(
            modifier = Modifier
                .then(scrollModifier)
                .width(canvasWidthDp)
                .height(250.dp)
                .padding(vertical = 16.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val bottomLabelSpace = 30.dp.toPx()
            val topValueSpace = 20.dp.toPx()
            val maxBarHeight = canvasHeight - bottomLabelSpace - topValueSpace

            val sideMarginPx = sideMarginDp.toPx()
            val availableWidth = canvasWidth - (sideMarginPx * 2)
            val spacePerBar = availableWidth / datos.size

            val barWidth = spacePerBar * 0.6f

            datos.forEachIndexed { index, dato ->
                val barHeight = (dato.kwh / maxKwh) * maxBarHeight

                // Calculamos el inicio de la celda sumando el margen lateral izquierdo
                val slotStartX = sideMarginPx + (index * spacePerBar)
                // Centramos la barra dentro de su celda
                val startX = slotStartX + (spacePerBar - barWidth) / 2f
                val startY = canvasHeight - bottomLabelSpace - barHeight

                // A. DIBUJAR LA BARRA
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x = startX, y = startY),
                    size = Size(width = barWidth, height = barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // B. DIBUJAR EL VALOR (kWh)
                val redondeado = kotlin.math.round(dato.kwh * 10) / 10.0
                val kwhText = if (redondeado > 0) redondeado.toString() else ""

                if (kwhText.isNotEmpty()) {
                    val kwhMeasure = textMeasurer.measure(
                        text = kwhText,
                        style = valueStyle,
                        maxLines = 1,
                        softWrap = false // Evita que los números se partan si son largos
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = kwhText,
                        style = valueStyle,
                        topLeft = Offset(
                            x = startX + (barWidth - kwhMeasure.size.width) / 2f,
                            y = startY - kwhMeasure.size.height - 4.dp.toPx()
                        )
                    )
                }

                // C. DIBUJAR LA FECHA / HORA
                val labelTexto = dato.etiqueta
                // BLINDAJE: maxLines = 1 y softWrap = false prohíben a Compose romper el texto en 2 líneas
                val labelMeasure = textMeasurer.measure(
                    text = labelTexto,
                    style = textStyle,
                    maxLines = 1,
                    softWrap = false
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = labelTexto,
                    style = textStyle,
                    topLeft = Offset(
                        x = startX + (barWidth - labelMeasure.size.width) / 2f,
                        y = canvasHeight - bottomLabelSpace + 8.dp.toPx()
                    )
                )
            }
        }
    }
}