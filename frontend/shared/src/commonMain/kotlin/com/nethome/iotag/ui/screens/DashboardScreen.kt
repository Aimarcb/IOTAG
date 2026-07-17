package com.nethome.iotag.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nethome.iotag.ui.components.NetHomeCard
import com.nethome.iotag.ui.components.NetHomeStatusCard
import com.nethome.iotag.ui.components.VoiceAssistantCard
import com.nethome.iotag.ui.viewmodels.DashboardViewModel
import com.nethome.iotag.utils.rememberSpeechRecognizer

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val viewModel = remember { DashboardViewModel() }
    val data by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.update()
    }

    val startSpeechRecognition = rememberSpeechRecognizer { textoReconocido ->
        viewModel.procesarComandoVoz(textoReconocido)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Panel Principal",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row( modifier = Modifier.fillMaxWidth()
                    .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NetHomeCard(
                        title = "CONSUMO",
                        value = "${data.consumoKw} ${data.unidadConsumo}",
                        icon = Icons.Default.Bolt,
                        subtitle = data.tendenciaConsumo,
                        accentColor = Color(0xFF34D399),
                        valueColor = Color(0xFF34D399),
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                    )

                    NetHomeCard(
                        title = "CLIMA",
                        value = "${data.temperatura} ${data.unidadTemperatura}",
                        icon = Icons.Default.Thermostat,
                        subtitle = data.tendenciaTemperatura,
                        accentColor = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight(),
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
                NetHomeStatusCard(
                    label = "ESTADO DEL SISTEMA",
                    title = "Desarmado",
                    icon = Icons.Default.LockOpen,
                    statusText = "Desarmado",
                    statusColor = Color(0xFF9CA3AF)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        VoiceAssistantCard(
            suggestions = listOf(
                "Enciende el salón",
                "¿Qué temperatura hace?",
                "Cierra todo"
            ),
            onMicClick = {
                startSpeechRecognition()
            },
            onSuggestionClick = { suggestion ->
                viewModel.procesarComandoVoz(suggestion)
            },
            modifier = Modifier.padding(top = 12.dp)
        )

        if (data.mensajeIA.isNotEmpty()) {
            Text(
                text = data.mensajeIA,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}