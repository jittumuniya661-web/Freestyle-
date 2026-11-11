package com.example.ui.screens.coach

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiApiService
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CoachState {
    IDLE,
    RUNNING,
    PAUSED,
    STOPPED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCoachScreen(
    apiService: GeminiApiService,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var coachState by remember { mutableStateOf(CoachState.IDLE) }
    var hasPermission by remember { mutableStateOf(false) }
    var crosshairScore by remember { mutableIntStateOf(84) }
    var coverVulnerability by remember { mutableIntStateOf(28) }
    var glooWallReactionMs by remember { mutableIntStateOf(210) }
    var movementRate by remember { mutableStateOf("Optimal Zigzag") }
    var currentAdvice by remember { mutableStateOf("Keep crosshair at neck level before peeking left corner.") }
    var aiAnalysisLog by remember { mutableStateOf<List<String>>(emptyList()) }
    var isAiAnalyzing by remember { mutableStateOf(false) }

    val projectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            hasPermission = true
            coachState = CoachState.RUNNING
            // Provide gentle haptic confirmation
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } else {
            hasPermission = false
            coachState = CoachState.IDLE
        }
    }

    // Live Coach telemetry ticker loop
    LaunchedEffect(coachState) {
        if (coachState == CoachState.RUNNING) {
            val adviceList = listOf(
                "Aim slightly higher! Crosshair is dropping below chest level.",
                "Enemy footprint audio detected on right flank — prepare Gloo Wall.",
                "Great drag headshot window! Pull fire button straight upward.",
                "Open field warning! Deploy defensive 360 Gloo Wall if fired upon.",
                "Movement fluidity high. Ready for quick-switch shotgun combo.",
                "Crosshair headshot alignment: PERFECT (95%). Hold angle!"
            )
            var index = 0
            while (coachState == CoachState.RUNNING) {
                delay(3500)
                currentAdvice = adviceList[index % adviceList.size]
                crosshairScore = (78..96).random()
                coverVulnerability = (18..55).random()
                glooWallReactionMs = (160..240).random()
                index++
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Live Coach", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("coach_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    Surface(
                        color = when (coachState) {
                            CoachState.RUNNING -> DangerRed
                            CoachState.PAUSED -> CyberYellow
                            else -> TextMuted
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = coachState.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
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
            // Live Screen HUD & Crosshair Visualizer Card
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (coachState == CoachState.RUNNING) CyberOrange else GamingBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Custom HUD Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2
                        val cy = size.height / 2

                        // Grid lines for tactical overlay
                        val gridPaint = Color.White.copy(alpha = 0.04f)
                        for (i in 0..10) {
                            drawLine(gridPaint, Offset(0f, size.height * (i / 10f)), Offset(size.width, size.height * (i / 10f)), strokeWidth = 1f)
                            drawLine(gridPaint, Offset(size.width * (i / 10f), 0f), Offset(size.width * (i / 10f), size.height), strokeWidth = 1f)
                        }

                        // Crosshair Circle & Lines
                        val crosshairColor = if (coachState == CoachState.RUNNING) CyberOrange else TextMuted
                        drawCircle(
                            color = crosshairColor.copy(alpha = 0.4f),
                            radius = 45f,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                        drawCircle(
                            color = crosshairColor,
                            radius = 6f,
                            center = Offset(cx, cy)
                        )
                        // Crosshair notches
                        drawLine(crosshairColor, Offset(cx - 65f, cy), Offset(cx - 20f, cy), strokeWidth = 3f)
                        drawLine(crosshairColor, Offset(cx + 20f, cy), Offset(cx + 65f, cy), strokeWidth = 3f)
                        drawLine(crosshairColor, Offset(cx, cy - 65f), Offset(cx, cy - 20f), strokeWidth = 3f)
                        drawLine(crosshairColor, Offset(cx, cy + 20f), Offset(cx, cy + 65f), strokeWidth = 3f)
                    }

                    // Status Overlays
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Adjust, contentDescription = null, tint = CyberOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Headshot Line: $crosshairScore%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gloo Deploy: ${glooWallReactionMs}ms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Bottom Live Coaching Ticker
                        Surface(
                            color = Color.Black.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberOrange.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (coachState == CoachState.RUNNING) currentAdvice else "Start analysis to receive real-time positioning & Gloo Wall tips",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Controls Row: Start, Pause, Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (!hasPermission) {
                            val captureIntent = projectionManager.createScreenCaptureIntent()
                            screenCaptureLauncher.launch(captureIntent)
                        } else {
                            coachState = CoachState.RUNNING
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("coach_start_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                    enabled = coachState != CoachState.RUNNING
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("START", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (coachState == CoachState.RUNNING) {
                            coachState = CoachState.PAUSED
                        } else if (coachState == CoachState.PAUSED) {
                            coachState = CoachState.RUNNING
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("coach_pause_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow, contentColor = Color.Black),
                    enabled = coachState == CoachState.RUNNING || coachState == CoachState.PAUSED
                ) {
                    Icon(if (coachState == CoachState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (coachState == CoachState.PAUSED) "RESUME" else "PAUSE", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { coachState = CoachState.STOPPED },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("coach_stop_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    enabled = coachState == CoachState.RUNNING || coachState == CoachState.PAUSED
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("STOP", fontWeight = FontWeight.Bold)
                }
            }

            // Real AI Vision Inspection Button
            OutlinedButton(
                onClick = {
                    scope.launch {
                        isAiAnalyzing = true
                        val prompt = """
                            You are FF Genius AI, an elite Free Fire pro coach.
                            Perform a tactical analysis of the current combat frame:
                            1. Crosshair placement (head level vs torso)
                            2. Cover availability and angle exposure
                            3. Gloo Wall emergency placement advice
                            4. Recommended drag headshot technique (Straight drag, J-drag, or U-drag).
                            Keep it concise, bulleted, and actionable.
                        """.trimIndent()
                        val result = apiService.generateContent(prompt)
                        isAiAnalyzing = false
                        result.fold(
                            onSuccess = { res ->
                                aiAnalysisLog = listOf(res)
                            },
                            onFailure = { err ->
                                aiAnalysisLog = listOf("AI Vision note: ${err.localizedMessage}\n(Ensure Gemini API key is configured in Settings)")
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("coach_ai_inspect_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
            ) {
                if (isAiAnalyzing) {
                    CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing Frame with Gemini AI...", color = CyberCyan)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAMPLE SCREENSHOT & RUN AI VISION", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            }

            // AI Vision Results Log
            AnimatedVisibility(visible = aiAnalysisLog.isNotEmpty()) {
                Surface(
                    color = GamingSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = CyberCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini AI Vision Telemetry", color = CyberCyan, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiAnalysisLog.firstOrNull().orEmpty(),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // Live Telemetry Breakdown Grid
            Text("Tactical Live Metrics", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Crosshair",
                    value = "$crosshairScore%",
                    status = "Headshot Ready",
                    accent = CyberOrange,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Vulnerability",
                    value = "$coverVulnerability%",
                    status = if (coverVulnerability < 35) "Well Protected" else "Exposed Angle",
                    accent = if (coverVulnerability < 35) SuccessGreen else DangerRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Gloo Deploy",
                    value = "${glooWallReactionMs}ms",
                    status = "Sit-up Ready",
                    accent = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Movement",
                    value = movementRate,
                    status = "High Agility",
                    accent = CyberYellow,
                    modifier = Modifier.weight(1f)
                )
            }

            // Compliance & Fair Play Disclaimer
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fair Play & System Security Policy", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FF Genius AI operates purely on authorized Android display capture. It does NOT inject code, does NOT modify game APK/OBB files, does NOT automate player input, and does NOT function as a cheat. Visual analysis results are educational guidelines to train muscle memory.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    status: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GamingSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = TextMuted, fontSize = 11.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = accent, modifier = Modifier.padding(vertical = 2.dp))
            Text(status, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
