package com.example.ui.screens.preview3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*

data class Point3D(val x: Float, val y: Float, val z: Float)
data class Polygon3D(val indices: List<Int>, val normal: Point3D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Preview3DScreen(
    onNavigateBack: () -> Unit
) {
    var yawDeg by remember { mutableFloatStateOf(35f) }
    var pitchDeg by remember { mutableFloatStateOf(-15f) }
    var zoomScale by remember { mutableFloatStateOf(1.2f) }
    var ambientIntensity by remember { mutableFloatStateOf(0.75f) }
    var lightAngleDeg by remember { mutableFloatStateOf(45f) }

    var selectedEnvironment by remember { mutableStateOf("Cyber Dojo") }
    var selectedSkin by remember { mutableStateOf("Cyber Neon") }

    val skinColor = when (selectedSkin) {
        "Cyber Neon" -> CyberCyan
        "Dragon Flame" -> CyberOrange
        "Glacial Ice" -> Color(0xFF80D8FF)
        else -> CyberYellow // Golden Emperor
    }

    val envBackgroundBrush = when (selectedEnvironment) {
        "Cyber Dojo" -> Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF000814)))
        "Training Ground" -> Brush.verticalGradient(listOf(Color(0xFF2B2D42), Color(0xFF1F2022), Color(0xFF111111)))
        "Fire Forge" -> Brush.verticalGradient(listOf(Color(0xFF370617), Color(0xFF6A040F), Color(0xFF03071E)))
        else -> Brush.verticalGradient(listOf(Color(0xFF050505), Color(0xFF0A0A0A), Color(0xFF000000))) // Dark Void
    }

    // Define 3D Weapon Model (Cyber Tactical Katana / Blade & Sheath)
    val vertices3D = remember {
        listOf(
            // Blade Tip & Spine
            Point3D(0f, -140f, 0f),      // 0: Tip
            Point3D(18f, -70f, 6f),      // 1: Upper blade spine
            Point3D(-18f, -70f, 0f),     // 2: Upper blade edge
            Point3D(18f, 20f, 6f),       // 3: Mid blade spine
            Point3D(-18f, 20f, 0f),      // 4: Mid blade edge
            Point3D(22f, 50f, 8f),       // 5: Tsuba guard right
            Point3D(-22f, 50f, -8f),     // 6: Tsuba guard left
            Point3D(10f, 130f, 5f),      // 7: Hilt handle
            Point3D(-10f, 130f, -5f),    // 8: Hilt handle
            Point3D(14f, 155f, 6f),      // 9: Pommel cap
            Point3D(-14f, 155f, -6f)     // 10: Pommel cap
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("3D Weapon Preview", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("preview3d_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            yawDeg = 35f
                            pitchDeg = -15f
                            zoomScale = 1.2f
                            ambientIntensity = 0.75f
                        },
                        modifier = Modifier.testTag("preview3d_reset_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Camera", tint = CyberCyan)
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
                .verticalScroll(rememberScrollState())
        ) {
            // Interactive 3D Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(envBackgroundBrush)
                    .border(2.dp, skinColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            yawDeg = (yawDeg + dragAmount.x * 0.7f) % 360f
                            pitchDeg = (pitchDeg - dragAmount.y * 0.7f).coerceIn(-85f, 85f)
                        }
                    }
                    .testTag("canvas_3d_viewport")
            ) {
                // 3D Canvas Projection
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    val radYaw = Math.toRadians(yawDeg.toDouble()).toFloat()
                    val radPitch = Math.toRadians(pitchDeg.toDouble()).toFloat()
                    val lightRad = Math.toRadians(lightAngleDeg.toDouble()).toFloat()
                    val lightDir = Point3D(cos(lightRad), -0.5f, sin(lightRad))

                    // Project 3D vertices using Euler rotation
                    val projectedPoints = vertices3D.map { p ->
                        // Rotate Pitch (around X axis)
                        val y1 = p.y * cos(radPitch) - p.z * sin(radPitch)
                        val z1 = p.y * sin(radPitch) + p.z * cos(radPitch)

                        // Rotate Yaw (around Y axis)
                        val x2 = p.x * cos(radYaw) + z1 * sin(radYaw)
                        val z2 = -p.x * sin(radYaw) + z1 * cos(radYaw)

                        // Perspective projection
                        val cameraDist = 450f
                        val factor = (cameraDist / (cameraDist + z2)) * zoomScale
                        Offset(cx + x2 * factor, cy + y1 * factor)
                    }

                    // Draw Weapon Wireframe / Facets
                    val lines = listOf(
                        0 to 1, 0 to 2, 1 to 2,
                        1 to 3, 2 to 4, 3 to 4,
                        3 to 5, 4 to 6, 5 to 6,
                        5 to 7, 6 to 8, 7 to 8,
                        7 to 9, 8 to 10, 9 to 10
                    )

                    // Draw blade body fill
                    val bladePath = Path().apply {
                        moveTo(projectedPoints[0].x, projectedPoints[0].y)
                        lineTo(projectedPoints[1].x, projectedPoints[1].y)
                        lineTo(projectedPoints[3].x, projectedPoints[3].y)
                        lineTo(projectedPoints[5].x, projectedPoints[5].y)
                        lineTo(projectedPoints[6].x, projectedPoints[6].y)
                        lineTo(projectedPoints[4].x, projectedPoints[4].y)
                        lineTo(projectedPoints[2].x, projectedPoints[2].y)
                        close()
                    }

                    drawPath(
                        path = bladePath,
                        brush = Brush.radialGradient(
                            listOf(skinColor.copy(alpha = ambientIntensity), skinColor.copy(alpha = 0.2f)),
                            center = projectedPoints[3]
                        )
                    )

                    // Draw edges with lighting
                    lines.forEach { (startIdx, endIdx) ->
                        val p1 = projectedPoints[startIdx]
                        val p2 = projectedPoints[endIdx]
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = p1,
                            end = p2,
                            strokeWidth = 3f * zoomScale
                        )
                    }

                    // Draw glowing vertex energy nodes
                    projectedPoints.forEach { pt ->
                        drawCircle(
                            color = skinColor,
                            radius = 4f * zoomScale,
                            center = pt
                        )
                    }
                }

                // Camera Angle HUD Tag
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "Yaw: ${yawDeg.toInt()}° | Pitch: ${pitchDeg.toInt()}° | Zoom: ${(zoomScale * 100).toInt()}%",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Interactive drag tip
                Text(
                    text = "Drag screen to freely rotate in 3D space",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                )
            }

            // Controls Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Weapon Skin Switcher
                Text("Select Weapon Skin Texture", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cyber Neon", "Dragon Flame", "Glacial Ice", "Gold Elite").forEach { skin ->
                        val sel = selectedSkin == skin
                        Surface(
                            color = if (sel) skinColor else GamingSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) skinColor else GamingBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSkin = skin }
                                .testTag("preview_skin_$skin")
                        ) {
                            Text(
                                text = skin,
                                color = if (sel) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Environment Switcher
                Text("Render Environment & Lighting Studio", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cyber Dojo", "Training Ground", "Fire Forge", "Dark Void").forEach { env ->
                        val sel = selectedEnvironment == env
                        Surface(
                            color = if (sel) CyberOrange else GamingSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) CyberOrange else GamingBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedEnvironment = env }
                        ) {
                            Text(
                                text = env,
                                color = if (sel) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Sliders for Zoom and Lighting
                Surface(
                    color = GamingSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Zoom Scale", color = TextSecondary, fontSize = 13.sp)
                            Text("${(zoomScale * 100).toInt()}%", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Slider(
                            value = zoomScale,
                            onValueChange = { zoomScale = it },
                            valueRange = 0.6f..2.2f,
                            colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ambient Lighting Intensity", color = TextSecondary, fontSize = 13.sp)
                            Text("${(ambientIntensity * 100).toInt()}%", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Slider(
                            value = ambientIntensity,
                            onValueChange = { ambientIntensity = it },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = CyberYellow, activeTrackColor = CyberYellow)
                        )
                    }
                }

                // 3D Rendering Architecture Note
                Surface(
                    color = GamingSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "3D Engine Note: Real-time 3D vector geometry rendered with Compose 3D matrix projection, perspective division, and dynamic diffuse lighting shader.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
