package com.example.ui.screens.sensitivity

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.SensitivityProfileEntity
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensitivityGeneratorScreen(
    appRepository: AppRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current

    // Auto-detect real device hardware
    val detectedModel = remember { "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}" }
    val detectedRamGb = remember {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalGb = (memInfo.totalMem / (1024 * 1024 * 1024.0)).toInt()
        if (totalGb > 0) totalGb else 6
    }
    val detectedRefreshRate = remember {
        val display = context.display
        display?.refreshRate?.toInt() ?: 60
    }
    val screenRes = remember {
        "${configuration.screenWidthDp}x${configuration.screenHeightDp} dp"
    }

    var selectedFinger by remember { mutableStateOf("3 Finger") }
    var selectedPlaystyle by remember { mutableStateOf("Rusher (Drag Headshot)") }

    var general by remember { mutableIntStateOf(98) }
    var redDot by remember { mutableIntStateOf(94) }
    var scope2x by remember { mutableIntStateOf(90) }
    var scope4x by remember { mutableIntStateOf(86) }
    var sniperScope by remember { mutableIntStateOf(68) }
    var freeLook by remember { mutableIntStateOf(75) }
    var profileName by remember { mutableStateOf("My Custom Sensitivity") }

    var showCompareSheet by remember { mutableStateOf(false) }
    val savedProfiles by appRepository.getSensitivityProfiles().collectAsStateWithLifecycle(initialValue = emptyList())

    // Generation formula
    fun generateRecommendations() {
        val isHighRefresh = detectedRefreshRate >= 90
        val isHighRam = detectedRamGb >= 8

        when (selectedFinger) {
            "2 Finger" -> {
                general = if (isHighRefresh) 96 else 100
                redDot = if (isHighRam) 94 else 98
                scope2x = 92
                scope4x = 88
                sniperScope = 62
                freeLook = 80
            }
            "3 Finger" -> {
                general = if (isHighRefresh) 98 else 100
                redDot = 96
                scope2x = 94
                scope4x = 90
                sniperScope = 66
                freeLook = 82
            }
            else -> { // 4 Finger
                general = if (isHighRefresh) 94 else 98
                redDot = 92
                scope2x = 88
                scope4x = 85
                sniperScope = 72
                freeLook = 85
            }
        }
        if (selectedPlaystyle.contains("Sniper")) {
            sniperScope += 12
            general -= 4
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensitivity Generator", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("sens_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCompareSheet = !showCompareSheet },
                        modifier = Modifier.testTag("sens_compare_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = "Compare", tint = CyberCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GamingBackground)
            )
        },
        containerColor = GamingBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hardware Detection Card
            item {
                Surface(
                    color = GamingSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hardware Telemetry Auto-Detected", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TelemetryChip("Model", detectedModel)
                            TelemetryChip("RAM", "${detectedRamGb}GB")
                            TelemetryChip("Display", "${detectedRefreshRate}Hz")
                            TelemetryChip("Resolution", screenRes)
                        }
                    }
                }
            }

            // Finger & Playstyle Selection
            item {
                Surface(
                    color = GamingSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Hand Setup & Finger Layout", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("2 Finger", "3 Finger", "4 Finger").forEach { setup ->
                                val selected = selectedFinger == setup
                                Surface(
                                    color = if (selected) CyberOrange else GamingSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) CyberOrange else GamingBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedFinger = setup; generateRecommendations() }
                                        .testTag("finger_opt_$setup")
                                ) {
                                    Text(
                                        text = setup,
                                        color = if (selected) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { generateRecommendations() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("sens_auto_generate_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CALIBRATE OPTIMAL SENSITIVITY", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Interactive Sliders
            item {
                Surface(
                    color = GamingSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Fine-Tune Sensitivity Sliders", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        SensSlider("General (Drag Headshot)", general, CyberOrange) { general = it }
                        SensSlider("Red Dot", redDot, CyberCyan) { redDot = it }
                        SensSlider("2X Scope", scope2x, CyberYellow) { scope2x = it }
                        SensSlider("4X Scope", scope4x, CyberGreen) { scope4x = it }
                        SensSlider("Sniper Scope", sniperScope, CyberPurple) { sniperScope = it }
                        SensSlider("Free Look", freeLook, TextSecondary) { freeLook = it }
                    }
                }
            }

            // Save & Export Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val code = "FFGENIUS-$detectedRamGb-$detectedRefreshRate-G$general-RD$redDot-2X$scope2x-4X$scope4x-SN$sniperScope"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("FF Sensitivity Code", code))
                            Toast.makeText(context, "Sensitivity Code Copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("sens_copy_code_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyberCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COPY CODE", color = CyberCyan, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val newProfile = SensitivityProfileEntity(
                                    userId = appRepository.currentUserId,
                                    profileName = "$selectedFinger Pro ($detectedModel)",
                                    phoneModel = detectedModel,
                                    ramGb = detectedRamGb,
                                    refreshRateHz = detectedRefreshRate,
                                    fingerSetup = selectedFinger,
                                    general = general,
                                    redDot = redDot,
                                    scope2x = scope2x,
                                    scope4x = scope4x,
                                    sniperScope = sniperScope,
                                    freeLook = freeLook,
                                    isFavorite = true
                                )
                                appRepository.saveSensitivityProfile(newProfile)
                                Toast.makeText(context, "Profile Saved to Database!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("sens_save_profile_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberOrange)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE PROFILE", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Saved Profiles Header
            if (savedProfiles.isNotEmpty()) {
                item {
                    Text("Saved Profiles in Database", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                }
                items(savedProfiles) { p ->
                    Surface(
                        color = GamingSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(p.profileName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text("General: ${p.general} | RedDot: ${p.redDot} | 2X: ${p.scope2x} | 4X: ${p.scope4x}", color = TextSecondary, fontSize = 11.sp)
                            }
                            Row {
                                TextButton(
                                    onClick = {
                                        general = p.general
                                        redDot = p.redDot
                                        scope2x = p.scope2x
                                        scope4x = p.scope4x
                                        sniperScope = p.sniperScope
                                        freeLook = p.freeLook
                                        selectedFinger = p.fingerSetup
                                        Toast.makeText(context, "Applied ${p.profileName}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Apply", color = CyberCyan, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { scope.launch { appRepository.deleteSensitivityProfile(p) } }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun SensSlider(
    name: String,
    value: Int,
    color: Color,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, color = TextSecondary, fontSize = 13.sp)
            Text("$value", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = GamingBorder
            )
        )
    }
}

@Composable
fun TelemetryChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}
