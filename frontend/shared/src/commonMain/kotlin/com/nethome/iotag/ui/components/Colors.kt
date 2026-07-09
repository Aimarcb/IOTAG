package com.nethome.iotag.ui.components

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val EcoHomeDarkColors = darkColorScheme(
    primary = Color(0xFF2ECC71),      // Verde esmeralda (botones, iconos activos)
    onPrimary = Color(0xFF0B1326),    // Texto oscuro sobre los botones verdes (Alta legibilidad)
    secondary = Color(0xFF27AE60),    // Verde secundario (acentos, gráficas)

    tertiary = Color(0xFFE67E22),     // Naranja vibrante para el Termostato / Clima
    onTertiary = Color(0xFFFFFFFF),   // Texto blanco sobre fondo naranja

    background = Color(0xFF0B1326),   // Fondo azul profundo (estilo noche/premium)
    surface = Color(0xFF131B2E),      // Fondo de tarjetas y paneles (un tono más claro para que destaquen)
    onBackground = Color(0xFFF2F3FC), // Texto principal claro sobre el fondo azul
    onSurface = Color(0xFFF2F3FC)     // Texto principal claro sobre las tarjetas
)