package com.nethome.iotag.utils

import androidx.compose.runtime.Composable

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    function(callback) {
        var SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRecognition) {
            console.warn("Tu navegador web no soporta reconocimiento de voz.");
            return;
        }
        var recognition = new SpeechRecognition();
        recognition.lang = 'es-ES';
        recognition.onresult = function(event) {
            var texto = event.results[0][0].transcript;
            callback(texto);
        };
        recognition.start();
    }
""")
private external fun startWebSpeech(callback: (String) -> Unit)

// 2. Cumplimos la promesa de commonMain
@Composable
actual fun rememberSpeechRecognizer(onResult: (String) -> Unit): () -> Unit {
    return {
        // Al pulsar el botón en la web, llamamos a la función JS
        startWebSpeech { texto ->
            onResult(texto)
        }
    }
}