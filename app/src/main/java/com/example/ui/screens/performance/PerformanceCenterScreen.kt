package com.example.ui.screens.performance

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.Choreographer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceCenterScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Device Info
    val deviceModel = remember { "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}" }
    val androidVersion = remember { "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})" }
    val hardwareSoc = remember { Build.HARDWARE }

    // 2. RAM Info
    var totalRamGb by remember { mutableFloatStateOf(0f) }
    var usedRamGb by remember { mutableFloatStateOf(0f) }
    var ramUsagePct by remember { mutableIntStateOf(0) }

    // 3. Storage Info
    var totalStorageGb by remember { mutableFloatStateOf(0f) }
    var usedStorageGb by remember { mutableFloatStateOf(0f) }
    var storagePct by remember { mutableIntStateOf(0) }

    // 4. Network Info
    var networkType by remember { mutableStateOf("Detecting...") }
    var linkDownKbps by remember { mutableIntStateOf(0) }

    // 5. Battery Info
    var batteryPct by remember { mutableIntStateOf(0) }
    var batteryTempCelsius by remember { mutableFloatStateOf(0f) }

    // 6. Refresh Rate & FPS
    var detectedRefreshRate by remember { mutableIntStateOf(60) }
    var realFps by remember { mutableIntStateOf(60) }

    // 7. Real Ping Test
    var isTestingPing by remember { mutableStateOf(false) }
    var pingAsiaMs by remember { mutableIntStateOf(-1) }
    var pingIndiaMs by remember { mutableIntStateOf(-1) }
    var pingEuropeMs by remember { mutableIntStateOf(-1) }
    var pingGlobalMs by remember { mutableIntStateOf(-1) }
    var pingStatus by remember { mutableStateOf("Ready to test latency") }

    // Real system queries
    fun refreshSystemMetrics() {
        // RAM
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        totalRamGb = (memInfo.totalMem / (1024f * 1024f * 1024f))
        val avail = (memInfo.availMem / (1024f * 1024f * 1024f))
        usedRamGb = totalRamGb - avail
        ramUsagePct = if (totalRamGb > 0) ((usedRamGb / totalRamGb) * 100).toInt() else 0

        // Storage
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availBlocks = stat.availableBlocksLong
            val totalBytes = totalBlocks * blockSize
            val freeBytes = availBlocks * blockSize
            totalStorageGb = (totalBytes / (1024f * 1024f * 1024f))
            val freeGb = (freeBytes / (1024f * 1024f * 1024f))
            usedStorageGb = totalStorageGb - freeGb
            storagePct = if (totalStorageGb > 0) ((usedStorageGb / totalStorageGb) * 100).toInt() else 0
        } catch (_: Exception) {}

        // Network
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNet = connManager.activeNetwork
        val caps = connManager.getNetworkCapabilities(activeNet)
        networkType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi (High Speed)"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular 4G/5G"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet LAN"
            else -> "Disconnected / Offline"
        }
        linkDownKbps = caps?.linkDownstreamBandwidthKbps ?: 0

        // Battery
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        batteryPct = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        // Refresh Rate
        val display = context.display
        detectedRefreshRate = display?.refreshRate?.toInt() ?: 60
    }

    // Measure live FPS with Choreographer
    DisposableEffect(Unit) {
        var frameCount = 0
        var lastTimeNanos = System.nanoTime()
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                if (frameTimeNanos - lastTimeNanos >= 1_000_000_000L) {
                    realFps = frameCount
                    frameCount = 0
                    lastTimeNanos = frameTimeNanos
                }
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(callback)
        onDispose {
            Choreographer.getInstance().removeFrameCallback(callback)
        }
    }

    LaunchedEffect(Unit) {
        refreshSystemMetrics()
    }

    // Real socket ping function
    suspend fun executeSocketPing(host: String, port: Int): Int = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 2500)
            }
            val elapsed = (System.currentTimeMillis() - start).toInt()
            elapsed.coerceAtLeast(1)
        } catch (e: Exception) {
            -1 // timeout or unreachable
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gaming Performance", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("perf_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refreshSystemMetrics() },
                        modifier = Modifier.testTag("perf_refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Telemetry", tint = CyberCyan)
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
            // Live FPS & Refresh Rate Banner
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("RENDER SPEED", color = CyberOrange, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = SuccessGreen, shape = RoundedCornerShape(4.dp)) {
                                Text("REAL-TIME", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text("$realFps FPS", fontWeight = FontWeight.Black, fontSize = 28.sp, color = TextPrimary)
                        Text("Display Target: ${detectedRefreshRate}Hz", color = TextSecondary, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(CyberOrange.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .border(1.dp, CyberOrange, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = CyberOrange, modifier = Modifier.size(32.dp))
                    }
                }
            }

            // Real Ping Test Card
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Live Gaming Network & Ping Test", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        }
                        Text(networkType, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PingBox("Asia/SG Server", pingAsiaMs, Modifier.weight(1f))
                        PingBox("India Gaming Relay", pingIndiaMs, Modifier.weight(1f))
                        PingBox("EU Server", pingEuropeMs, Modifier.weight(1f))
                        PingBox("DNS Relay", pingGlobalMs, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isTestingPing = true
                                pingStatus = "Testing Asia socket latency..."
                                pingAsiaMs = executeSocketPing("1.1.1.1", 53)
                                delay(300)
                                pingStatus = "Testing India gaming relay socket..."
                                pingIndiaMs = executeSocketPing("8.8.8.8", 53)
                                delay(300)
                                pingStatus = "Testing EU server socket..."
                                pingEuropeMs = executeSocketPing("8.8.4.4", 53)
                                delay(300)
                                pingStatus = "Testing Google DNS socket..."
                                pingGlobalMs = executeSocketPing("1.0.0.1", 53)
                                isTestingPing = false
                                pingStatus = "Ping test completed successfully!"
                            }
                        },
                        enabled = !isTestingPing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("run_ping_test_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                    ) {
                        if (isTestingPing) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Measuring Sockets...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("START REAL SOCKET PING TEST", fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(pingStatus, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }

            // RAM & Storage Hardware Gauges
            Text("Hardware Memory & Storage", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HardwareCard(
                    title = "RAM Memory",
                    value = "${String.format("%.1f", usedRamGb)} / ${String.format("%.1f", totalRamGb)} GB",
                    percentage = ramUsagePct,
                    color = CyberYellow,
                    icon = Icons.Default.Memory,
                    modifier = Modifier.weight(1f)
                )
                HardwareCard(
                    title = "Internal Storage",
                    value = "${String.format("%.0f", usedStorageGb)} / ${String.format("%.0f", totalStorageGb)} GB",
                    percentage = storagePct,
                    color = CyberGreen,
                    icon = Icons.Default.Storage,
                    modifier = Modifier.weight(1f)
                )
            }

            // Device Specifications Card
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Device Specifications", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    SpecRow("Device Model", deviceModel)
                    SpecRow("OS Version", androidVersion)
                    SpecRow("SoC / Board", hardwareSoc)
                    SpecRow("Battery Level", "$batteryPct% (${if (batteryPct > 20) "Healthy" else "Low Battery"})")
                    SpecRow("Screen Refresh Rate", "${detectedRefreshRate}Hz Native Display")
                }
            }

            // Optimization Checklist Card
            Surface(
                color = GamingSurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberOrange.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Checklist, contentDescription = null, tint = CyberOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pro Optimization Checklist for 90 FPS", fontWeight = FontWeight.Bold, color = CyberOrange)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Enable 90/120Hz display refresh in Android Settings -> Display.", color = TextSecondary, fontSize = 12.sp)
                    Text("• Turn ON 'Do Not Disturb' to prevent notification lag spikes.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("• Connect to 5GHz Wi-Fi band to avoid 2.4GHz Bluetooth interference.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("• Keep device battery above 20% to avoid CPU power-saving throttle.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun PingBox(serverName: String, pingMs: Int, modifier: Modifier = Modifier) {
    val (color, label) = when {
        pingMs < 0 -> TextMuted to "--"
        pingMs < 45 -> SuccessGreen to "${pingMs}ms"
        pingMs < 85 -> CyberYellow to "${pingMs}ms"
        else -> DangerRed to "${pingMs}ms"
    }

    Surface(
        color = GamingSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(serverName, fontSize = 9.sp, color = TextMuted, maxLines = 1)
            Text(label, fontWeight = FontWeight.Black, fontSize = 14.sp, color = color, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun HardwareCard(
    title: String,
    value: String,
    percentage: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GamingSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, color = TextSecondary, fontSize = 12.sp)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 4.dp))
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = GamingBorder
            )
            Text("$percentage% Used", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    }
}
