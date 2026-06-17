package com.example.railway.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.ui.theme.RailwayTheme

@Composable
fun HomeScreen(
    onNavigateToDiscovery: () -> Unit,
    onNavigateToLiveTracking: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vibrant_bg")
    val animatedColor1 by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        targetValue = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_color_1"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(animatedColor1, MaterialTheme.colorScheme.surface)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Train,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "RailTrack Pro",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Your journey, reimagined.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(56.dp))
            
            // Primary Action Card
            VibrantHomeCard(
                title = "Discover Routes",
                subtitle = "Find the best path for your next adventure",
                icon = Icons.Rounded.Explore,
                color = MaterialTheme.colorScheme.primary,
                onClick = onNavigateToDiscovery
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Secondary Action Card
            VibrantHomeCard(
                title = "Live Tracking",
                subtitle = "Real-time updates on active trains",
                icon = Icons.Rounded.Train,
                color = MaterialTheme.colorScheme.secondary,
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
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = color,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    RailwayTheme {
        HomeScreen({}, {})
    }
}
