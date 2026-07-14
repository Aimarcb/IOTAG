package com.nethome.iotag.utils

import androidx.compose.runtime.Composable

@Composable
actual fun rememberSpeechRecognizer(onResult: (String) -> Unit): () -> Unit {
    return {
        // TODO: Implementar SFSpeechRecognizer
        println("🎤 Botón de micro pulsado en iOS. (Requiere configurar NSSpeechRecognitionUsageDescription en Info.plist)")
    }
}