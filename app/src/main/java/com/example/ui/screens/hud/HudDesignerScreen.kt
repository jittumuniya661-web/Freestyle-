package com.example.ui.screens.hud

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.HudLayoutEntity
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class HudItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    var xFraction: Float,
    var yFraction: Float,
    var sizeDp: Int = 60,
    var opacityPct: Int = 85,
    val color: Color = CyberOrange
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HudDesignerScreen(
    appRepository: AppRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeFingerSetup by remember { mutableStateOf("3 Finger") }

    // Initial default items for 3-finger setup
    fun getPresetItems(setup: String): List<HudItem> {
        return when (setup) {
            "2 Finger" -> listOf(
                HudItem("fire_r", "Fire (Right)", Icons.Default.Adjust, 0.82f, 0.70f, 65, 90, CyberOrange),
                HudItem("gloo", "Gloo Wall", Icons.Default.Shield, 0.22f, 0.65f, 70, 95, CyberCyan),
                HudItem("jump", "Jump", Icons.Default.ArrowUpward, 0.88f, 0.48f, 56, 85, CyberYellow),
                HudItem("crouch", "Crouch", Icons.Default.ArrowDownward, 0.78f, 0.84f, 56, 85, CyberGreen),
                HudItem("scope", "Scope", Icons.Default.CenterFocusStrong, 0.74f, 0.48f, 58, 85, CyberPurple),
                HudItem("reload", "Reload", Icons.Default.Refresh, 0.65f, 0.30f, 50, 75, TextSecondary),
                HudItem("joystick", "Movement", Icons.Default.ControlCamera, 0.18f, 0.72f, 75, 65, TextPrimary)
            )
            "3 Finger" -> listOf(
                HudItem("fire_r", "Fire (Right)", Icons.Default.Adjust, 0.84f, 0.72f, 62, 90, CyberOrange),
                HudItem("fire_l", "Fire (Left)", Icons.Default.Adjust, 0.18f, 0.22f, 60, 90, CyberOrange),
                HudItem("gloo", "Gloo Wall", Icons.Default.Shield, 0.24f, 0.66f, 75, 95, CyberCyan),
                HudItem("jump", "Jump", Icons.Default.ArrowUpward, 0.88f, 0.48f, 58, 85, CyberYellow),
                HudItem("crouch", "Crouch", Icons.Default.ArrowDownward, 0.78f, 0.84f, 56, 85, CyberGreen),
                HudItem("scope", "Scope", Icons.Default.CenterFocusStrong, 0.75f, 0.45f, 60, 85, CyberPurple),
                HudItem("reload", "Reload", Icons.Default.Refresh, 0.68f, 0.25f, 50, 80, TextSecondary),
                HudItem("joystick", "Movement", Icons.Default.ControlCamera, 0.18f, 0.72f, 75, 65, TextPrimary)
            )
            else -> listOf( // 4 Finger
                HudItem("fire_l", "Fire (Left)", Icons.Default.Adjust, 0.16f, 0.18f, 65, 95, CyberOrange),
                HudItem("scope", "Scope (Right Index)", Icons.Default.CenterFocusStrong, 0.84f, 0.18f, 65, 95, CyberPurple),
                HudItem("fire_r", "Fire (Thumb)", Icons.Default.Adjust, 0.82f, 0.72f, 58, 85, CyberOrange),
                HudItem("gloo", "Gloo Wall", Icons.Default.Shield, 0.25f, 0.60f, 75, 95, CyberCyan),
                HudItem("jump", "Jump", Icons.Default.ArrowUpward, 0.90f, 0.48f, 58, 85, CyberYellow),
                HudItem("crouch", "Crouch", Icons.Default.ArrowDownward, 0.76f, 0.84f, 58, 85, CyberGreen),
                HudItem("reload", "Reload", Icons.Default.Refresh, 0.68f, 0.32f, 52, 80, TextSecondary),
                HudItem("joystick", "Movement", Icons.Default.ControlCamera, 0.18f, 0.72f, 75, 65, TextPrimary)
            )
        }
    }

    var hudItems by remember { mutableStateOf(getPresetItems("3 Finger")) }
    var selectedItemId by remember { mutableStateOf<String?>("fire_r") }

    val selectedItem = hudItems.find { it.id == selectedItemId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom HUD Designer", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("hud_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val array = JSONArray()
                            hudItems.forEach { item ->
                                val obj = JSONObject()
                                obj.put("id", item.id)
                                obj.put("name", item.name)
                                obj.put("x", item.xFraction.toDouble())
                                obj.put("y", item.yFraction.toDouble())
                                obj.put("size", item.sizeDp)
                                obj.put("opacity", item.opacityPct)
                                array.put(obj)
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("FF HUD Layout", array.toString()))
                            Toast.makeText(context, "HUD JSON Copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("hud_export_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export Code", tint = CyberCyan)
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                val array = JSONArray()
                                hudItems.forEach { item ->
                                    val obj = JSONObject()
                                    obj.put("id", item.id)
                                    obj.put("x", item.xFraction.toDouble())
                                    obj.put("y", item.yFraction.toDouble())
                                    obj.put("size", item.sizeDp)
                                    obj.put("opacity", item.opacityPct)
                                    array.put(obj)
                                }
                                val entity = HudLayoutEntity(
                                    userId = appRepository.currentUserId,
                                    layoutName = "$activeFingerSetup Pro Layout",
                                    fingerSetup = activeFingerSetup,
                                    configJson = array.toString(),
                                    isDefault = true
                                )
                                appRepository.saveHudLayout(entity)
                                Toast.makeText(context, "Layout Saved to Database!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("hud_save_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Layout", tint = CyberOrange)
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
            // Preset Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("2 Finger", "3 Finger", "4 Finger").forEach { setup ->
                    val isSel = activeFingerSetup == setup
                    Surface(
                        color = if (isSel) CyberOrange else GamingSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyberOrange else GamingBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                activeFingerSetup = setup
                                hudItems = getPresetItems(setup)
                                selectedItemId = hudItems.firstOrNull()?.id
                            }
                            .testTag("hud_preset_$setup")
                    ) {
                        Text(
                            text = setup,
                            color = if (isSel) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Interactive Gaming Screen Canvas Box
            var canvasWidthPx by remember { mutableFloatStateOf(1f) }
            var canvasHeightPx by remember { mutableFloatStateOf(1f) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
                    .background(Color.Black, RoundedCornerShape(14.dp))
                    .border(2.dp, GamingBorder, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .onGloballyPositioned { coords ->
                        canvasWidthPx = coords.size.width.toFloat()
                        canvasHeightPx = coords.size.height.toFloat()
                    }
            ) {
                // Tactical Screen Guidelines
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                )

                // Render Draggable Buttons
                hudItems.forEach { item ->
                    val isSelected = item.id == selectedItemId
                    val pxX = item.xFraction * canvasWidthPx - (item.sizeDp / 2f * 2.5f)
                    val pxY = item.yFraction * canvasHeightPx - (item.sizeDp / 2f * 2.5f)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(pxX.roundToInt(), pxY.roundToInt()) }
                            .size(item.sizeDp.dp)
                            .alpha(item.opacityPct / 100f)
                            .background(
                                color = item.color.copy(alpha = 0.25f),
                                shape = CircleShape
                            )
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) CyberCyan else item.color.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .pointerInput(item.id) {
                                detectDragGestures(
                                    onDragStart = { selectedItemId = item.id }
                                ) { change, dragAmount ->
                                    change.consume()
                                    val newX = (item.xFraction + (dragAmount.x / canvasWidthPx)).coerceIn(0.05f, 0.95f)
                                    val newY = (item.yFraction + (dragAmount.y / canvasHeightPx)).coerceIn(0.05f, 0.95f)
                                    hudItems = hudItems.map {
                                        if (it.id == item.id) it.copy(xFraction = newX, yFraction = newY) else it
                                    }
                                }
                            }
                            .clickable { selectedItemId = item.id }
                            .testTag("hud_btn_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name,
                                tint = if (isSelected) CyberCyan else item.color,
                                modifier = Modifier.size((item.sizeDp * 0.42f).dp)
                            )
                            Text(
                                text = item.name.take(6),
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Bottom Inspector / Controls Sheet
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedItem != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(selectedItem.icon, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Adjust: ${selectedItem.name}",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "X: ${(selectedItem.xFraction * 100).toInt()}%  Y: ${(selectedItem.yFraction * 100).toInt()}%",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Size Slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Size: ${selectedItem.sizeDp}dp", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                            Slider(
                                value = selectedItem.sizeDp.toFloat(),
                                onValueChange = { newSize ->
                                    hudItems = hudItems.map {
                                        if (it.id == selectedItem.id) it.copy(sizeDp = newSize.toInt()) else it
                                    }
                                },
                                valueRange = 35f..95f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = CyberOrange, activeTrackColor = CyberOrange)
                            )
                        }

                        // Opacity Slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Opacity: ${selectedItem.opacityPct}%", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                            Slider(
                                value = selectedItem.opacityPct.toFloat(),
                                onValueChange = { newOp ->
                                    hudItems = hudItems.map {
                                        if (it.id == selectedItem.id) it.copy(opacityPct = newOp.toInt()) else it
                                    }
                                },
                                valueRange = 20f..100f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                            )
                        }
                    } else {
                        Text("Tap any button on the screen above to adjust its position, size, and transparency.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
