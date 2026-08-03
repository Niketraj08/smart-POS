package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.domain.model.MenuItemModel
import com.example.domain.util.GeminiVoiceOrderService
import com.example.domain.util.ParsedVoiceItem
import com.example.ui.PosViewModel
import kotlinx.coroutines.launch

@Composable
fun VoiceOrderAssistantDialog(
    viewModel: PosViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val menuItems by viewModel.menuItems.collectAsState(initial = emptyList())

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Microphone permission is required for Voice Ordering", Toast.LENGTH_SHORT).show()
        }
    }

    var isListening by remember { mutableStateOf(false) }
    var isAnalyzingWithGemini by remember { mutableStateOf(false) }
    var liveTranscript by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Tap the mic button and speak order items...") }
    var rmsVolume by remember { mutableFloatStateOf(0f) }

    val recognizedItems = remember { mutableStateListOf<ParsedVoiceItem>() }

    // Speech Recognizer setup
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun processSpeechInput(text: String) {
        if (text.isBlank()) return
        liveTranscript = text
        isAnalyzingWithGemini = true
        statusMessage = "Gemini AI is matching items with menu..."

        scope.launch {
            val parsed = GeminiVoiceOrderService.parseVoiceOrder(text, menuItems)
            recognizedItems.clear()
            recognizedItems.addAll(parsed)
            isAnalyzingWithGemini = false

            if (parsed.isEmpty()) {
                statusMessage = "No matching menu items found for \"$text\". Try again or speak clearly."
            } else {
                statusMessage = "Found ${parsed.size} menu item(s)! Review below and add to cart."
            }
        }
    }

    fun startListening() {
        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech Recognition is not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Order food items (e.g. 2 Paneer Tikka, 1 Naan)")
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    statusMessage = "Listening... Speak order command now!"
                }

                override fun onBeginningOfSpeech() {
                    statusMessage = "Hearing speech..."
                }

                override fun onRmsChanged(rmsdB: Float) {
                    rmsVolume = (rmsdB / 10f).coerceIn(0f, 1f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                    statusMessage = "Processing spoken audio..."
                }

                override fun onError(error: Int) {
                    isListening = false
                    val errMsg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try speaking again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out. Tap mic to retry."
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                        else -> "Speech recognition error code: $error"
                    }
                    statusMessage = errMsg
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""
                    if (spokenText.isNotBlank()) {
                        processSpeechInput(spokenText)
                    } else {
                        statusMessage = "Could not detect clear words. Try again."
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partialText = matches?.firstOrNull() ?: ""
                    if (partialText.isNotBlank()) {
                        liveTranscript = partialText
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            statusMessage = "Failed to start microphone: ${e.localizedMessage}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isListening = false
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Mic Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF8C1D11))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("AI Voice Order Assistant", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Powered by Gemini 3.5 Flash", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_voice_dialog_button")) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mic Status Card & Pulse Button
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isListening) Color(0xFF8C1D11).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isListening) Color(0xFF8C1D11) else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (isListening) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(Color(0xFF8C1D11).copy(alpha = 0.25f))
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (isListening) Color(0xFF8C1D11) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clickable {
                                        if (isListening) stopListening() else startListening()
                                    }
                                    .testTag("voice_mic_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                        contentDescription = "Microphone",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isListening) "Tap to Stop Listening" else "Tap Mic to Start Voice Ordering",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isListening) Color(0xFF8C1D11) else MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = statusMessage,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Spoken Transcript Input Field
                OutlinedTextField(
                    value = liveTranscript,
                    onValueChange = { liveTranscript = it },
                    label = { Text("Spoken Voice Transcript") },
                    placeholder = { Text("e.g. \"Add 2 Butter Chicken, 1 Garlic Naan, 2 Cold Coffee\"") },
                    trailingIcon = {
                        if (isAnalyzingWithGemini) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(
                                onClick = { processSpeechInput(liveTranscript) },
                                enabled = liveTranscript.isNotBlank(),
                                modifier = Modifier.testTag("send_voice_text_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Parse with Gemini", tint = Color(0xFF8C1D11))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_transcript_input"),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini Extracted Items Review List
                if (isAnalyzingWithGemini) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF8C1D11).copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF8C1D11), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Gemini AI is parsing speech...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Matching item names, quantities, and preparation notes", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                } else if (recognizedItems.isNotEmpty()) {
                    Text(
                        text = "Matched Order Items (${recognizedItems.size}):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(recognizedItems) { index, item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.menuItem.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("₹${item.menuItem.price * item.quantity} (₹${item.menuItem.price} each)", fontSize = 11.sp, color = Color(0xFF8C1D11))
                                        if (item.notes.isNotBlank()) {
                                            Text("Note: ${item.notes}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (item.quantity > 1) {
                                                    recognizedItems[index] = item.copy(quantity = item.quantity - 1)
                                                } else {
                                                    recognizedItems.removeAt(index)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = "${item.quantity}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                recognizedItems[index] = item.copy(quantity = item.quantity + 1)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = { recognizedItems.removeAt(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (recognizedItems.isNotEmpty()) {
                Button(
                    onClick = {
                        for (item in recognizedItems) {
                            repeat(item.quantity) {
                                viewModel.addToCart(item.menuItem)
                            }
                        }
                        Toast.makeText(context, "Added ${recognizedItems.sumOf { it.quantity }} items to cart!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C1D11)),
                    modifier = Modifier.testTag("confirm_add_voice_items_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add All to Order Cart (${recognizedItems.sumOf { it.quantity }})", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
