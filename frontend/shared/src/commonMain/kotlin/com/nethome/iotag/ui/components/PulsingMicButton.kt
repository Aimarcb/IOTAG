package com.nethome.iotag.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PulsingMicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF34D399),
    isListening: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(pulseScale)
                    .background(accentColor.copy(alpha = pulseAlpha), CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.85f))
                    ),
                    CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Hablar con el asistente",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun VoiceAssistantCard(
    suggestions: List<String>,
    onMicClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isListening: Boolean = true,
    accentColor: Color = Color(0xFF34D399)
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PulsingMicButton(
                onClick = onMicClick,
                accentColor = accentColor,
                isListening = isListening
            )

            Text(
                text = "¿Cómo puedo ayudarte hoy?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "ASISTENTE DE VOZ ACTIVO",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp
                ),
                color = accentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            suggestions.forEach { suggestion ->
                SuggestionChip(
                    text = "\"$suggestion\"",
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}