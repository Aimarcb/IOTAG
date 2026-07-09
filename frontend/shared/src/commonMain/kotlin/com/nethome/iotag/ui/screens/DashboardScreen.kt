package com.nethome.iotag.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nethome.iotag.ui.components.NetHomeCard
import com.nethome.iotag.ui.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val viewModel = remember { DashboardViewModel() }
    val temperatura by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.update()
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row() {
            NetHomeCard(
                title = "CONSUMO",
                value = "1.2 kW",
                icon = Icons.Default.Bolt,
                subtitle = "↘ -5% hoy",
                accentColor = Color(0xFF34D399),
                valueColor = Color(0xFF34D399),
                modifier = Modifier.weight(1f)
            )

            NetHomeCard(
                title = "CLIMA",
                value = "22 °C",
                icon = Icons.Default.Thermostat,
                subtitle = "Interior",
                accentColor = Color(0xFFFBBF24),
                modifier = Modifier.weight(1f),
                trailingContent = {
                    CircularProgressIndicator(
                        progress = { 0.7f },
                        color = Color(0xFFFBBF24),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(targetState = temperatura, label = "temp_anim") { temp ->
            Text(
                text = "$temp °C",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.update() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Actualizar Datos")
        }
    }
}