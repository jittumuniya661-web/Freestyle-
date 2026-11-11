package com.example.ui.screens.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiApiService
import com.example.data.auth.SessionManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatbotScreen(
    apiService: GeminiApiService,
    sessionManager: SessionManager,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var activeLanguage by remember { mutableStateOf(sessionManager.getLanguage()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val initialGreeting = if (activeLanguage == "hi") {
        "नमस्ते! मैं हूँ आपका FF Genius AI कोच 🔥। ड्रैग हेडशॉट, सेंसिटिविटी, कस्टम HUD या ग्लू वॉल ट्रिक्स के बारे में कुछ भी पूछें!"
    } else {
        "Hey Survivor! I'm your FF Genius AI Coach 🔥. Ask me anything about drag headshots, device sensitivity, custom HUDs, or tournament strategies!"
    }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    sender = "ai",
                    text = initialGreeting
                )
            )
        )
    }

    val suggestionChips = listOf(
        "ड्रेग हेडशॉट की बेस्ट ट्रिक क्या है?",
        "Best sensitivity for 3-finger?",
        "How to do 360 Gloo Wall in 0.2s?",
        "MP40 vs M1887 for rushers?",
        "Redmi/Realme best settings?"
    )

    fun sendMessage(promptText: String) {
        val clean = promptText.trim()
        if (clean.isBlank() || isLoading) return

        messages = messages + ChatMessage(sender = "user", text = clean)
        inputText = ""
        isLoading = true

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)

            val systemInstruction = """
                You are FF Genius AI, an elite Free Fire esports champion and tactical gaming coach.
                You natively speak and understand both English and Hindi (हिंदी / Hinglish).
                Answer questions with expert accuracy on:
                - Drag Headshots (Straight drag, J-drag, U-drag)
                - Sensitivity calibrations for specific Android phones
                - Custom 2, 3, 4 finger HUD setups
                - Instant sit-up and 360 Gloo Wall mechanics
                - Weapon comparisons and clutch positioning
                Respond in the language the user speaks. Be encouraging, concise, and structured.
            """.trimIndent()

            val res = apiService.generateContent(
                prompt = clean,
                systemInstruction = systemInstruction
            )
            isLoading = false

            val replyText = res.getOrElse { err ->
                if (!apiService.isConfigured()) {
                    "⚠️ Gemini API key is not configured yet. Please open Settings -> API Configuration to paste your key."
                } else {
                    "Network error: ${err.localizedMessage}. Please verify your internet connection."
                }
            }

            messages = messages + ChatMessage(sender = "ai", text = replyText)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CyberOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("AI Gaming Chatbot", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("Bilingual Coach (हिंदी / English)", fontSize = 10.sp, color = CyberCyan)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            activeLanguage = if (activeLanguage == "en") "hi" else "en"
                            sessionManager.setLanguage(activeLanguage)
                        },
                        modifier = Modifier.testTag("chat_lang_toggle")
                    ) {
                        Surface(
                            color = CyberOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (activeLanguage == "hi") "हिंदी" else "English",
                                color = CyberOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GamingBackground)
            )
        },
        containerColor = GamingBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Suggestion Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestionChips) { chip ->
                    Surface(
                        color = GamingSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                        modifier = Modifier.clickable { sendMessage(chip) }
                    ) {
                        Text(
                            text = chip,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (isUser) CyberOrange else GamingSurface,
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isUser) 14.dp else 2.dp,
                                bottomEnd = if (isUser) 2.dp else 14.dp
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isUser) CyberOrange else GamingBorder
                            ),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (isUser) Color.White else TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Coach is thinking...", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = GamingSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask Coach (e.g. Best sensitivity?)...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = GamingBorder
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isLoading) CyberOrange else GamingBorder)
                            .testTag("chat_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}
