package com.nethome.iotag.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberSpeechRecognizer(onResult: (String) -> Unit): () -> Unit {
    val context = LocalContext.current

    // 1. El lanzador que recibe la voz de Google
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val voiceData = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = voiceData?.get(0) ?: ""
            if (recognizedText.isNotEmpty()) {
                onResult(recognizedText) // Se lo devolvemos a commonMain
            }
        }
    }

    // 2. El lanzador de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con NetHome...")
            }
            speechLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Se necesita permiso de micrófono", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Devolvemos una función que commonMain puede ejecutar al hacer clic
    return {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con NetHome...")
            }
            speechLauncher.launch(intent)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}