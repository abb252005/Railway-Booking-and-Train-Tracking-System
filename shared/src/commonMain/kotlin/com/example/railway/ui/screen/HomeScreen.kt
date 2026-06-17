package com.example.railway.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Light
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.ui.component.GlassPanel

@Composable
fun HomeScreen(
    onNavigateToDiscovery: () -> Unit,
    onNavigateToLiveTracking: () -> Unit,
    isDark: Boolean = true,
    showThemeToggle: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // macOS Style Header
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Train,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Theme Toggle
        if (showThemeToggle) {
            TextButton(onClick = onToggleTheme) {
                Icon(
                    if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isDark) "DARK MAP" else "LIGHT MAP",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Text(
            text = "RailTrack Pro",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-2).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Next-Gen Train Systems",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            fontWeight = FontWeight.Light
        )
        
        Spacer(modifier = Modifier.height(80.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            VibrantHomeCard(
                title = "Discovery",
                subtitle = "Explore routes and stations",
                icon = Icons.Rounded.Explore,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToDiscovery
            )
            
            VibrantHomeCard(
                title = "Live Tracking",
                subtitle = "Real-time fleet monitoring",
                icon = Icons.Rounded.Train,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLiveTracking
            )
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
    GlassPanel(
        onClick = onClick,
        modifier = modifier.height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
