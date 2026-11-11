package com.example.ui.screens.skin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiApiService
import com.example.data.local.entities.SkinDesignEntity
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinDesignerScreen(
    appRepository: AppRepository,
    apiService: GeminiApiService,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var itemType by remember { mutableStateOf("Gun Skin") } // or "Character Outfit"
    var selectedItem by remember { mutableStateOf("MP40") }
    var selectedTheme by remember { mutableStateOf("Cyberpunk") }
    var primaryColor by remember { mutableStateOf(CyberCyan) }
    var secondaryColor by remember { mutableStateOf(CyberPurple) }
    var selectedPattern by remember { mutableStateOf("Hex Grid") }

    var designTitle by remember { mutableStateOf("MP40 - Cyber Venom") }
    var isAiGenerating by remember { mutableStateOf(false) }
    var aiConceptLore by remember { mutableStateOf("Infused with radioactive cyber neon plasma. Fires high-density electromagnetic bolts with custom neon reload animation and digital kill broadcast banner.") }
    var aiStatsConcept by remember { mutableStateOf("Attributes: ++ Rate of Fire | + Armor Penetration | - Magazine Size") }

    val gunOptions = listOf("MP40", "AK47", "M1887", "AWM", "SCAR", "Groza")
    val outfitOptions = listOf("Cyber Samurai", "Flame Demon", "Arctic Phantom", "Neon Shinobi")
    val themeOptions = listOf("Cyberpunk", "Fire Dragon", "Glacial Ice", "Gold Royalty", "Neon Venom")
    val patternOptions = listOf("Hex Grid", "Dragon Scales", "Lava Veins", "Lightning", "Carbon Fiber")

    val colorOptions = listOf(
        CyberCyan to "Cyan",
        CyberOrange to "Orange",
        CyberGreen to "Neon Green",
        CyberPurple to "Violet",
        CyberYellow to "Gold",
        DangerRed to "Crimson"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Skin Studio", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("skin_back_button")) {
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
            // Visual Skin Preview Card
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Custom Canvas Weapon/Outfit Illustration
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Ambient glowing gradient background
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.25f), secondaryColor.copy(alpha = 0.08f), Color.Transparent),
                                center = Offset(w / 2, h / 2),
                                radius = w * 0.6f
                            )
                        )

                        // Draw stylized weapon or outfit silhouette
                        if (itemType == "Gun Skin") {
                            // Weapon barrel & body path
                            val gunPath = Path().apply {
                                moveTo(w * 0.15f, h * 0.45f) // muzzle
                                lineTo(w * 0.65f, h * 0.45f) // receiver
                                lineTo(w * 0.85f, h * 0.50f) // stock
                                lineTo(w * 0.82f, h * 0.65f) // stock base
                                lineTo(w * 0.70f, h * 0.58f) // grip top
                                lineTo(w * 0.62f, h * 0.75f) // grip bottom
                                lineTo(w * 0.55f, h * 0.72f) // trigger guard
                                lineTo(w * 0.48f, h * 0.55f) // magazine well
                                lineTo(w * 0.42f, h * 0.78f) // magazine bottom
                                lineTo(w * 0.35f, h * 0.74f)
                                lineTo(w * 0.40f, h * 0.55f)
                                lineTo(w * 0.15f, h * 0.52f)
                                close()
                            }
                            drawPath(
                                path = gunPath,
                                brush = Brush.linearGradient(listOf(primaryColor, secondaryColor))
                            )
                            // Neon glowing edge outline
                            drawPath(
                                path = gunPath,
                                color = Color.White.copy(alpha = 0.85f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                            )
                        } else {
                            // Stylized armor silhouette
                            drawRoundRect(
                                brush = Brush.linearGradient(listOf(primaryColor, secondaryColor)),
                                topLeft = Offset(w * 0.32f, h * 0.25f),
                                size = Size(w * 0.36f, h * 0.55f),
                                cornerRadius = CornerRadius(24f, 24f)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 22f,
                                center = Offset(w * 0.5f, h * 0.18f)
                            )
                        }
                    }

                    // Watermark & Title Overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "CONCEPT: $designTitle",
                                color = primaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = aiStatsConcept,
                                color = CyberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Category Toggle (Gun Skin vs Character Outfit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("Gun Skin", "Character Outfit").forEach { cat ->
                    val isSel = itemType == cat
                    Button(
                        onClick = {
                            itemType = cat
                            selectedItem = if (cat == "Gun Skin") gunOptions.first() else outfitOptions.first()
                            designTitle = "$selectedItem - $selectedTheme"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("skin_cat_$cat"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) CyberOrange else GamingSurface,
                            contentColor = if (isSel) Color.White else TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyberOrange else GamingBorder)
                    ) {
                        Text(cat, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Select Target Item Chips
            Text("Target Model", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val currentItems = if (itemType == "Gun Skin") gunOptions else outfitOptions
                currentItems.take(4).forEach { item ->
                    val sel = selectedItem == item
                    Surface(
                        color = if (sel) CyberCyan else GamingSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedItem = item
                                designTitle = "$selectedItem - $selectedTheme"
                            }
                            .testTag("skin_item_$item")
                    ) {
                        Text(
                            text = item,
                            color = if (sel) Color.Black else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Theme Selection
            Text("Visual Theme & Aura", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                themeOptions.take(4).forEach { theme ->
                    val sel = selectedTheme == theme
                    Surface(
                        color = if (sel) CyberOrange else GamingSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTheme = theme
                                designTitle = "$selectedItem - $selectedTheme"
                            }
                            .testTag("skin_theme_$theme")
                    ) {
                        Text(
                            text = theme,
                            color = if (sel) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Color Palette Picker
            Text("Primary Neon Accent Color", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                colorOptions.forEach { (col, _) ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                width = if (primaryColor == col) 3.dp else 1.dp,
                                color = if (primaryColor == col) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { primaryColor = col }
                    )
                }
            }

            // Pattern Chips
            Text("Texture & Pattern", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                patternOptions.take(4).forEach { pat ->
                    val sel = selectedPattern == pat
                    Surface(
                        color = if (sel) CyberPurple else GamingSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPattern = pat }
                    ) {
                        Text(
                            text = pat,
                            color = if (sel) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Generate AI Concept with Gemini API Button
            Button(
                onClick = {
                    scope.launch {
                        isAiGenerating = true
                        val prompt = """
                            You are an elite weapon skin designer for Free Fire.
                            Generate a high-tier legendary concept for:
                            Item: $selectedItem ($itemType)
                            Theme: $selectedTheme
                            Colors: Neon Primary with $selectedPattern pattern.
                            
                            Return:
                            1. Skin Name
                            2. 2-sentence visual animation & particle aura lore
                            3. In-game stat boosts concept (+ / - attributes)
                        """.trimIndent()
                        val res = apiService.generateContent(prompt)
                        isAiGenerating = false
                        res.fold(
                            onSuccess = { text ->
                                aiConceptLore = text
                                designTitle = "$selectedItem: ${selectedTheme.uppercase()}"
                            },
                            onFailure = { err ->
                                Toast.makeText(context, "AI note: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("skin_generate_ai_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
            ) {
                if (isAiGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating Legendary Concept with Gemini AI...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GENERATE AI CONCEPT & LORE", fontWeight = FontWeight.Bold)
                }
            }

            // AI Generated Lore Card
            Surface(
                color = GamingSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Concept Lore & Particle FX", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(aiConceptLore, color = TextPrimary, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

            // Save Design Button
            Button(
                onClick = {
                    scope.launch {
                        val entity = SkinDesignEntity(
                            userId = appRepository.currentUserId,
                            title = designTitle,
                            itemType = itemType,
                            weaponOrCharacter = selectedItem,
                            theme = selectedTheme,
                            primaryColorHex = "#" + Integer.toHexString(primaryColor.hashCode()),
                            secondaryColorHex = "#" + Integer.toHexString(secondaryColor.hashCode()),
                            pattern = selectedPattern,
                            conceptLore = aiConceptLore,
                            statsConcept = aiStatsConcept
                        )
                        appRepository.saveSkinDesign(entity)
                        Toast.makeText(context, "Skin Concept Saved to Studio!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("skin_save_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberOrange)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("SAVE DESIGN TO GALLERY", fontWeight = FontWeight.Bold)
            }

            // Legal Fan Art Disclaimer
            Surface(
                color = GamingSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "DISCLAIMER: All skin creations in this studio are visual fan-made concepts. They cannot be transferred, claimed, or equipped inside the official Free Fire game client.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
