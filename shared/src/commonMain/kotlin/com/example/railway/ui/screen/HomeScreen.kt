package com.example.railway.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.ui.component.GlassPanel
import com.example.railway.ui.theme.LocalRailwayStrings

@Composable
fun HomeScreen(
    onNavigateToDiscovery: () -> Unit,
    onNavigateToLiveTracking: () -> Unit,
    isDark: Boolean = true,
    showThemeToggle: Boolean = true,
    onToggleTheme: () -> Unit = {},
    currentLanguage: String = "English",
    onLanguageChange: (String) -> Unit = {},
) {
    val strings = LocalRailwayStrings.current
    val colorScheme = MaterialTheme.colorScheme
    
    // -- Graphics Animations --
    val infiniteTransition = rememberInfiniteTransition(label = "HomeAnimations")
    
    // 1. Pulsing LIVE badge animation
    val livePulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LivePulse"
    )

    // 2. Slow background shift for mesh effect
    val bgShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BgShift"
    )

    // 3. Entrance animation state
    var visible by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "EntranceAlpha"
    )
    val contentOffsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 40.dp,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "EntranceOffset"
    )

    LaunchedEffect(Unit) { visible = true }
    
    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        
        // Dynamic "Mesh" Background Layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = 0.15f * bgShift),
                        Color.Transparent,
                        colorScheme.secondary.copy(alpha = 0.12f * (1f - bgShift)),
                        Color.Transparent
                    ),
                    center = Offset(
                        x = centerX + (centerX * 0.5f * kotlin.math.sin(bgShift * 2 * kotlin.math.PI.toFloat())),
                        y = centerY + (centerY * 0.5f * kotlin.math.cos(bgShift * 2 * kotlin.math.PI.toFloat()))
                    ),
                    radius = if (canvasWidth < canvasHeight) canvasHeight else canvasWidth
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 64.dp)
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentOffsetY.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Styled macOS Icon Header with Glass Effect
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorScheme.onBackground.copy(alpha = 0.15f),
                                colorScheme.onBackground.copy(alpha = 0.03f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Train,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Header Typography with Modern Spacing
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-3).sp
                ),
                color = colorScheme.onBackground
            )
            
            Text(
                text = strings.nextGenSystems.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Enhanced Status Board with Animated Pulse
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(48.dp)
                    .background(colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.Red.copy(alpha = livePulse),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.graphicsLayer {
                            val scale = 0.95f + (livePulse * 0.05f)
                            scaleX = scale
                            scaleY = scale
                        }
                    ) {
                        Text(
                            "LIVE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "${strings.nationalStatusBoard}${strings.colonSeparator} ${strings.nationalStatusTicker}",
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            // Navigation Cards with Enhanced Visuals
            Row(
                modifier = Modifier.fillMaxWidth(0.95f),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                VibrantHomeCard(
                    title = strings.discovery,
                    subtitle = strings.findBestPath,
                    icon = Icons.Rounded.Explore,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToDiscovery
                )
                
                VibrantHomeCard(
                    title = strings.liveTracking,
                    subtitle = strings.realTimeMonitoring,
                    icon = Icons.Rounded.Train,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLiveTracking
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Refined Theme/Language controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showThemeToggle) {
                    TextButton(onClick = onToggleTheme) {
                        Icon(
                            if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            contentDescription = null,
                            tint = colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isDark) strings.darkMap else strings.lightMap,
                            color = colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                var showLanguageMenu by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { showLanguageMenu = true }) {
                        Icon(Icons.Rounded.Translate, contentDescription = null, tint = colorScheme.onBackground.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currentLanguage.uppercase(), color = colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                        DropdownMenuItem(text = { Text(strings.langEnglish) }, onClick = { onLanguageChange("English"); showLanguageMenu = false })
                        DropdownMenuItem(text = { Text(strings.langHawaii) }, onClick = { onLanguageChange("Hawaii"); showLanguageMenu = false })
                        DropdownMenuItem(text = { Text(strings.langSpanish) }, onClick = { onLanguageChange("Spanish"); showLanguageMenu = false })
                        DropdownMenuItem(text = { Text(strings.langFrench) }, onClick = { onLanguageChange("French"); showLanguageMenu = false })
                        DropdownMenuItem(text = { Text(strings.langGerman) }, onClick = { onLanguageChange("German"); showLanguageMenu = false })
                        DropdownMenuItem(text = { Text(strings.langItalian) }, onClick = { onLanguageChange("Italian"); showLanguageMenu = false })
                    }
                }
            }
        }
    }
}

@Composable
fun VibrantHomeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    GlassPanel(
        onClick = onClick,
        modifier = modifier.height(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon with Soft Gradient Glow
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(colorScheme.primary.copy(0.2f), colorScheme.primary.copy(0.05f))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    null, 
                    modifier = Modifier.size(32.dp), 
                    tint = colorScheme.primary
                )
            }
            
            Column {
                Text(
                    title, 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onSurface
                )
                Text(
                    subtitle, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
