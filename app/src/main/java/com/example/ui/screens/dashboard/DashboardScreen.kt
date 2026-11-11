package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.GeminiApiService
import com.example.data.auth.SessionManager
import com.example.data.repository.AppRepository
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

data class DashboardItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val screen: Screen,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    appRepository: AppRepository,
    sessionManager: SessionManager,
    apiService: GeminiApiService,
    onNavigate: (Screen) -> Unit
) {
    val profile by appRepository.getProfileFlow().collectAsStateWithLifecycle(initialValue = null)
    val isApiActive = apiService.isConfigured()

    val dashboardItems = listOf(
        DashboardItem(
            title = "AI Live Coach",
            subtitle = "Real-time screen vision & Gloo Wall assistant",
            icon = Icons.Default.Videocam,
            accentColor = CyberOrange,
            screen = Screen.LiveCoach,
            tag = "dash_live_coach"
        ),
        DashboardItem(
            title = "Gameplay Analyzer",
            subtitle = "Video & screenshot combat breakdown",
            icon = Icons.Default.Analytics,
            accentColor = CyberCyan,
            screen = Screen.GameplayAnalyzer,
            tag = "dash_gameplay_analyzer"
        ),
        DashboardItem(
            title = "Sensitivity Generator",
            subtitle = "Hardware-calibrated 2/3/4 finger profiles",
            icon = Icons.Default.Tune,
            accentColor = CyberYellow,
            screen = Screen.SensitivityGenerator,
            tag = "dash_sensitivity_generator"
        ),
        DashboardItem(
            title = "Custom HUD Designer",
            subtitle = "Drag & drop interactive button editor",
            icon = Icons.Default.TouchApp,
            accentColor = CyberGreen,
            screen = Screen.HudDesigner,
            tag = "dash_hud_designer"
        ),
        DashboardItem(
            title = "AI Skin Designer",
            subtitle = "Cyberpunk, Fire & Ice weapon concepts",
            icon = Icons.Default.Palette,
            accentColor = CyberPurple,
            screen = Screen.SkinDesigner,
            tag = "dash_skin_designer"
        ),
        DashboardItem(
            title = "3D Preview System",
            subtitle = "Rotate, zoom & light 3D gear assets",
            icon = Icons.Default.ViewInAr,
            accentColor = CyberCyan,
            screen = Screen.Preview3D,
            tag = "dash_3d_preview"
        ),
        DashboardItem(
            title = "Gaming Performance",
            subtitle = "RAM, Storage, Refresh Rate & Ping Test",
            icon = Icons.Default.Speed,
            accentColor = CyberOrange,
            screen = Screen.PerformanceCenter,
            tag = "dash_performance_center"
        ),
        DashboardItem(
            title = "AI Training Center",
            subtitle = "Drag headshot & Gloo Wall drill lessons",
            icon = Icons.Default.FitnessCenter,
            accentColor = CyberYellow,
            screen = Screen.TrainingCenter,
            tag = "dash_training_center"
        ),
        DashboardItem(
            title = "AI Gaming Chatbot",
            subtitle = "Hindi & English Free Fire strategist",
            icon = Icons.Default.SmartToy,
            accentColor = CyberGreen,
            screen = Screen.AiChatbot,
            tag = "dash_ai_chatbot"
        ),
        DashboardItem(
            title = "My Reports",
            subtitle = "Saved analysis history & progression",
            icon = Icons.Default.FolderShared,
            accentColor = CyberPurple,
            screen = Screen.MyReports,
            tag = "dash_my_reports"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CyberOrange, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("FF GENIUS AI", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                            Text("PRO COMPANION", fontSize = 10.sp, color = CyberOrange, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate(Screen.Settings) },
                        modifier = Modifier.testTag("dashboard_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GamingBackground
                )
            )
        },
        containerColor = GamingBackground
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Player Profile Banner
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    color = GamingSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(CyberOrange, CyberOrangeDark)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile?.avatarEmoji ?: "🔥",
                                    fontSize = 26.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile?.displayName ?: sessionManager.getUsername(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Surface(
                                        color = CyberOrange.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = profile?.ffRank ?: "Heroic",
                                            color = CyberOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        color = CyberCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = profile?.playStyle ?: "Rusher",
                                            color = CyberCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // AI Status Indicator
                            Surface(
                                color = if (isApiActive) SuccessGreen.copy(alpha = 0.15f) else CyberYellow.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isApiActive) SuccessGreen.copy(alpha = 0.4f) else CyberYellow.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isApiActive) SuccessGreen else CyberYellow)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isApiActive) "AI Ready" else "Setup Key",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isApiActive) SuccessGreen else CyberYellow
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = GamingBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatBox(label = "Headshot Rate", value = "${profile?.headshotRate ?: 64.5f}%", color = CyberOrange)
                            StatBox(label = "K/D Ratio", value = "${profile?.kdRatio ?: 3.8f}", color = CyberCyan)
                            StatBox(label = "Fav Weapon", value = profile?.favoriteWeapon ?: "MP40", color = CyberYellow)
                        }
                    }
                }
            }

            // Hero Live Coach Feature Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    CyberOrange.copy(alpha = 0.25f),
                                    GamingSurface
                                )
                            )
                        )
                        .clickable { onNavigate(Screen.LiveCoach) }
                        .testTag("hero_launch_coach_card")
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI LIVE COACH", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = DangerRed,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            Text("Screen capture analysis, crosshair & instant Gloo Wall guidance", fontSize = 12.sp, color = TextSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyberOrange)
                    }
                }
            }

            // Dashboard Grid Menu Items
            items(dashboardItems) { item ->
                Surface(
                    color = GamingSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigate(item.screen) }
                        .testTag(item.tag)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .heightIn(min = 125.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(item.accentColor.copy(alpha = 0.15f))
                                .border(1.dp, item.accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = item.title, tint = item.accentColor, modifier = Modifier.size(22.dp))
                        }

                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = item.subtitle,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 2,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}
