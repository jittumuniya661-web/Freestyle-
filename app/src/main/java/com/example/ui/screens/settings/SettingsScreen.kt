package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.GeminiApiService
import com.example.data.auth.SessionManager
import com.example.data.repository.AppRepository
import com.example.data.repository.AuthRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authRepository: AuthRepository,
    appRepository: AppRepository,
    sessionManager: SessionManager,
    apiService: GeminiApiService,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profile by appRepository.getProfileFlow().collectAsStateWithLifecycle(initialValue = null)

    var apiKeyInput by remember { mutableStateOf(sessionManager.getCustomApiKey()) }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTestingKey by remember { mutableStateOf(false) }

    var backupJson by remember { mutableStateOf<String?>(null) }

    var selectedLang by remember { mutableStateOf(sessionManager.getLanguage()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings & API", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Account Info Card
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CyberOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active Player Account", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Username: ${sessionManager.getUsername()}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("Email: ${sessionManager.getEmail()}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                    Text("Free Fire Rank: ${profile?.ffRank ?: "Heroic"}", color = CyberYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                    Text("Playstyle: ${profile?.playStyle ?: "Rusher"}", color = CyberCyan, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }

            // API Configuration Card (Section 14)
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (apiService.isConfigured()) SuccessGreen else CyberOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini AI API Configuration", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        }
                        Surface(
                            color = if (apiService.isConfigured()) SuccessGreen.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (apiService.isConfigured()) "ACTIVE" else "NOT SET",
                                color = if (apiService.isConfigured()) SuccessGreen else DangerRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your Google Gemini API key to activate AI Vision live coach, gameplay analysis, and Hindi/English chatbot. You can get a free key from Google AI Studio.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        trailingIcon = {
                            IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                Icon(
                                    imageVector = if (apiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_api_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = GamingBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                sessionManager.saveCustomApiKey(apiKeyInput)
                                Toast.makeText(context, "API Key Saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("save_api_key_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange)
                        ) {
                            Text("SAVE KEY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                sessionManager.saveCustomApiKey(apiKeyInput)
                                scope.launch {
                                    isTestingKey = true
                                    testStatus = "Validating key with Gemini API..."
                                    val res = apiService.generateContent("Reply with 'OK'")
                                    isTestingKey = false
                                    res.fold(
                                        onSuccess = {
                                            testStatus = "✅ Connection successful! Gemini API is active."
                                        },
                                        onFailure = { err ->
                                            testStatus = "❌ Error: ${err.localizedMessage}"
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("test_api_key_button"),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
                        ) {
                            if (isTestingKey) {
                                CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(16.dp))
                            } else {
                                Text("TEST CONNECTION", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    AnimatedVisibility(visible = testStatus != null) {
                        Text(
                            text = testStatus.orEmpty(),
                            color = if (testStatus?.startsWith("✅") == true) SuccessGreen else DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Database Management & Backup Card (Section 13)
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Database & Data Backups", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your profiles, sensitivities, HUD layouts, and reports are persisted securely in local Room database. Export a JSON backup anytime.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val json = appRepository.exportBackupJson()
                                backupJson = json
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("FF Genius Backup", json))
                                Toast.makeText(context, "Database Backup Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("export_backup_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberYellow, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXPORT DATABASE BACKUP (JSON)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    AnimatedVisibility(visible = backupJson != null) {
                        Surface(
                            color = GamingSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = backupJson.orEmpty(),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            // Language Selector
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Preferred AI Coach Language", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("en" to "English", "hi" to "हिंदी (Hindi)").forEach { (code, label) ->
                            val sel = selectedLang == code
                            Surface(
                                color = if (sel) CyberOrange else GamingSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedLang = code
                                        sessionManager.setLanguage(code)
                                    }
                            ) {
                                Text(
                                    text = label,
                                    color = if (sel) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Sign Out Button
            Button(
                onClick = {
                    authRepository.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("settings_logout_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("SIGN OUT OF FF GENIUS", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
