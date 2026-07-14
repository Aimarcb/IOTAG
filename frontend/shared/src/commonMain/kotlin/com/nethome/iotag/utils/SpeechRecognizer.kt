package com.nethome.iotag.utils

import androidx.compose.runtime.Composable

@Composable
expect fun rememberSpeechRecognizer(onResult: (String) -> Unit): () -> Unit