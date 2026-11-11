package com.example.ui.screens.reports

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.GameplayReportEntity
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    appRepository: AppRepository,
    onNavigateBack: () -> Unit,
    onNavigateToAnalyzer: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val reports by appRepository.getReports().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedReport by remember { mutableStateOf<GameplayReportEntity?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Analysis Reports", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("reports_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GamingBackground)
            )
        },
        containerColor = GamingBackground
    ) { padding ->
        if (reports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("No Gameplay Reports Yet", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text(
                        "Upload or record a gameplay clip in Gameplay Analyzer to receive detailed tactical reports.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateToAnalyzer,
                        modifier = Modifier.testTag("reports_start_analyzer_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ANALYZE GAMEPLAY NOW", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Saved Match Breakdowns (${reports.size})", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                }

                items(reports) { report ->
                    val isExpanded = selectedReport?.id == report.id
                    Surface(
                        color = GamingSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedReport = if (isExpanded) null else report
                            }
                            .testTag("report_card_${report.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(report.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                    Text(dateFormat.format(Date(report.createdAt)), color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                        .border(1.dp, CyberCyan, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${report.overallScore}", color = CyberCyan, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MiniScore("Aim", report.aimScore, CyberOrange)
                                MiniScore("Movement", report.movementScore, CyberYellow)
                                MiniScore("Gloo Wall", report.glooWallScore, CyberCyan)
                                MiniScore("Positioning", report.positioningScore, CyberGreen)
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    HorizontalDivider(color = GamingBorder)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Tactical Summary", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(report.summary, color = TextPrimary, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 2.dp))

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Identified Flaws", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(report.mistakes, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Key Recommendations", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(report.recommendations, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                scope.launch { appRepository.deleteReport(report) }
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Delete Report", color = DangerRed)
                                        }
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
}

@Composable
fun MiniScore(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$score%", color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}
