package com.example.ui.screens.training

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LessonItem(
    val id: String,
    val title: String,
    val category: String,
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val technique: String,
    val steps: List<String>,
    val proTip: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingCenterScreen(
    appRepository: AppRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val progressList by appRepository.getTrainingProgress().collectAsStateWithLifecycle(initialValue = emptyList())

    val lessons = remember {
        listOf(
            LessonItem(
                id = "drag_headshot_straight",
                title = "Straight Drag Headshot",
                category = "Headshot Practice",
                level = "Beginner",
                technique = "Pull fire button straight up when enemy is running directly towards or away from you.",
                steps = listOf(
                    "Place white crosshair slightly above chest level.",
                    "Tap fire button and swiftly drag vertically upward without pausing.",
                    "Switch immediately to melee weapon or Gloo Wall after firing."
                ),
                proTip = "Keep General Sensitivity above 95 for effortless vertical drag on mobile screens."
            ),
            LessonItem(
                id = "drag_headshot_jdrag",
                title = "J-Drag (Lateral Headshot)",
                category = "Drag Headshot",
                level = "Intermediate",
                technique = "Curve fire button in a 'J' motion corresponding to enemy horizontal running angle.",
                steps = listOf(
                    "Observe enemy sprint vector (left or right).",
                    "Drag fire button downward slightly, then whip upward towards enemy head trajectory.",
                    "Synchronize with right thumb flick for guaranteed red numbers."
                ),
                proTip = "Use Red Dot Sensitivity at 92-96 for precise lateral tracking."
            ),
            LessonItem(
                id = "gloo_wall_situp",
                title = "Fast Sit-Up Gloo Wall",
                category = "Gloo Wall Training",
                level = "Intermediate",
                technique = "Deploy defensive wall while crouching instantly to absorb shotgun bursts.",
                steps = listOf(
                    "Fire weapon (fire button dragged up).",
                    "Tap Gloo Wall icon while pulling screen down to your feet.",
                    "Tap Crouch button and Left Fire button simultaneously within 0.25 seconds."
                ),
                proTip = "Place Gloo Wall button on the left side with size 75%+ and opacity 90%."
            ),
            LessonItem(
                id = "movement_zigzag",
                title = "Agile Zigzag Sprint",
                category = "Movement Training",
                level = "Beginner",
                technique = "Disrupt enemy sniper and AR tracking through rapid diagonal camera snaps.",
                steps = listOf(
                    "Sprint forward with weapon holstered.",
                    "Swipe camera 45° left, tap jump, then swipe camera 45° right.",
                    "Combine with slide cancel before entering doorways."
                ),
                proTip = "Never sprint in a straight line for more than 1.5 seconds in open terrain."
            ),
            LessonItem(
                id = "crosshair_preaim",
                title = "Corner Pre-Aiming & Neck Level",
                category = "Crosshair Placement",
                level = "Advanced",
                technique = "Keep crosshair positioned where the enemy head will appear before peeking cover.",
                steps = listOf(
                    "Align crosshair horizontally with head height using environmental reference objects.",
                    "Slice the corner step by step (pieing the angle).",
                    "Fire only after confirming line of sight without jerking crosshair."
                ),
                proTip = "Good pre-aiming cuts required reaction time in half."
            ),
            LessonItem(
                id = "shotgun_flick",
                title = "M1887 2-Shot Flick Combo",
                category = "Close Range Combat",
                level = "Advanced",
                technique = "High-damage jumping shotgun flick followed by immediate weapon swap.",
                steps = listOf(
                    "Jump toward cover while aiming crosshair at enemy feet.",
                    "Whip fire button up sharply at the apex of your jump.",
                    "Immediately switch to Gloo Wall before landing on the ground."
                ),
                proTip = "Practice the rhythm: Jump -> Fire -> Wall within 0.3 seconds."
            )
        )
    }

    var selectedLesson by remember { mutableStateOf<LessonItem?>(null) }

    // Interactive Drill State
    var isDrillActive by remember { mutableStateOf(false) }
    var drillTimeSeconds by remember { mutableIntStateOf(15) }
    var drillScore by remember { mutableIntStateOf(0) }
    var drillTaps by remember { mutableIntStateOf(0) }
    var drillFeedback by remember { mutableStateOf("Drag fire button up when target flashes RED!") }
    var targetActive by remember { mutableStateOf(false) }

    // Drill Loop
    LaunchedEffect(isDrillActive) {
        if (isDrillActive) {
            drillScore = 0
            drillTaps = 0
            drillTimeSeconds = 15
            while (drillTimeSeconds > 0 && isDrillActive) {
                targetActive = true
                delay(1200)
                targetActive = false
                delay(600)
                drillTimeSeconds--
            }
            isDrillActive = false
            drillFeedback = "Drill Finished! Score: $drillScore (${if (drillScore >= 6) "Headshot Master" else "Keep Practicing"})"

            selectedLesson?.let { lesson ->
                appRepository.updateTrainingProgress(
                    lessonId = lesson.id,
                    category = lesson.category,
                    completed = drillScore >= 5,
                    score = drillScore * 10
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Training Center", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("training_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Interactive Drill Arena Card
            item {
                Surface(
                    color = GamingSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (targetActive) DangerRed else CyberCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DRAG HEADSHOT DRILL ARENA", color = CyberCyan, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Surface(
                                color = if (isDrillActive) DangerRed else TextMuted,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isDrillActive) "TIME: ${drillTimeSeconds}s" else "READY",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Target Box with Drag detection
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(if (targetActive) DangerRed.copy(alpha = 0.25f) else GamingSurfaceElevated)
                                .border(2.dp, if (targetActive) DangerRed else GamingBorder, CircleShape)
                                .pointerInput(isDrillActive, targetActive) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (isDrillActive && targetActive && dragAmount.y < -15f) {
                                            drillScore++
                                            drillTaps++
                                            targetActive = false
                                            drillFeedback = "🔥 HEADSHOT! Clean drag! (+1)"
                                            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                vib?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                            }
                                        }
                                    }
                                }
                                .clickable {
                                    if (isDrillActive && targetActive) {
                                        drillScore++
                                        drillTaps++
                                        targetActive = false
                                        drillFeedback = "🎯 Direct Hit! (+1)"
                                    }
                                }
                                .testTag("drill_target_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (targetActive) Icons.Default.GpsFixed else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (targetActive) DangerRed else TextMuted,
                                    modifier = Modifier.size(54.dp)
                                )
                                Text(
                                    text = if (targetActive) "DRAG UP!" else "AIM",
                                    fontWeight = FontWeight.Black,
                                    color = if (targetActive) DangerRed else TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(drillFeedback, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Score: $drillScore", color = CyberYellow, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { isDrillActive = !isDrillActive },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("drill_toggle_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDrillActive) DangerRed else CyberOrange)
                        ) {
                            Icon(if (isDrillActive) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isDrillActive) "STOP DRILL" else "START 15S HEADSHOT DRILL", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Lessons Header
            item {
                Text("Tactical Lesson Syllabus", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
            }

            // Lessons List
            items(lessons) { lesson ->
                val progress = progressList.find { it.lessonId == lesson.id }
                val isCompleted = progress?.isCompleted == true

                Surface(
                    color = GamingSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isCompleted) SuccessGreen.copy(alpha = 0.5f) else GamingBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedLesson = if (selectedLesson?.id == lesson.id) null else lesson
                        }
                        .testTag("lesson_card_${lesson.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(lesson.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = when (lesson.level) {
                                            "Beginner" -> CyberGreen.copy(alpha = 0.2f)
                                            "Intermediate" -> CyberYellow.copy(alpha = 0.2f)
                                            else -> CyberOrange.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = lesson.level,
                                            color = when (lesson.level) {
                                                "Beginner" -> CyberGreen
                                                "Intermediate" -> CyberYellow
                                                else -> CyberOrange
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(lesson.category, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }

                            if (isCompleted) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Mastered", tint = SuccessGreen)
                            } else {
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                            }
                        }

                        // Expanded Lesson Details
                        AnimatedVisibility(visible = selectedLesson?.id == lesson.id) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(color = GamingBorder)
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Core Technique", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 13.sp)
                                Text(lesson.technique, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Execution Steps", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                lesson.steps.forEachIndexed { i, step ->
                                    Text("${i + 1}. $step", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = CyberYellow.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(lesson.proTip, color = CyberYellow, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            appRepository.updateTrainingProgress(
                                                lessonId = lesson.id,
                                                category = lesson.category,
                                                completed = true,
                                                score = 100
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isCompleted) SuccessGreen else CyberOrange),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isCompleted) "MASTERED" else "MARK AS MASTERED", fontWeight = FontWeight.Bold)
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
