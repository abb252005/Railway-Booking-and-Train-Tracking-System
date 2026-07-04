package com.example.railway.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.railway.ui.theme.HackerBackground
import com.example.railway.ui.theme.HackerGreenDark
import com.example.railway.ui.theme.RailwayBackgroundDark

@Composable
fun VibrantGradientBackground(
    isAdmin: Boolean = false,
    isStatic: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundColor = if (isStatic) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.background
    val secondaryColor = if (isStatic) Color(0xFFD6E9FF) else MaterialTheme.colorScheme.secondaryContainer
    
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_bg")
    
    val color1 by infiniteTransition.animateColor(
        initialValue = if (isAdmin) HackerBackground else backgroundColor,
        targetValue = if (isAdmin) HackerGreenDark else secondaryColor.copy(alpha = 0.3f),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c1"
    )
    
    val color2 by infiniteTransition.animateColor(
        initialValue = if (isAdmin) HackerGreenDark else secondaryColor.copy(alpha = 0.2f),
        targetValue = if (isAdmin) HackerBackground else backgroundColor,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(color1, color2)
                )
            )
    ) {
        // Subtle overlay to ensure the background isn't too distracting
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (isAdmin) 0.05f else if (isStatic) 0f else 0.02f))
        )
        content()
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    opacity: Float? = null,
    isStatic: Boolean = false,
    onClick: (() -> Unit)? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    content: @Composable () -> Unit
) {
    val baseBackgroundColor = if (isStatic) Color.White else MaterialTheme.colorScheme.surface
    val backgroundColor = if (opacity != null) baseBackgroundColor.copy(alpha = opacity) else baseBackgroundColor
    val onSurface = if (isStatic) Color.Black else MaterialTheme.colorScheme.onSurface
    
    // Check if it's the hacker theme by comparing onSurface color
    val isHacker = !isStatic && onSurface == Color(0xFF00FF41) // HackerGreen

    val borderColor = when {
        isHacker -> onSurface.copy(alpha = 0.3f)
        isStatic -> Color.Black.copy(alpha = 0.1f)
        backgroundColor.alpha > 0.8f -> onSurface.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.2f)
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .clip(RoundedCornerShape(cornerRadius))
                .let { if (border != null) it.border(border, RoundedCornerShape(cornerRadius)) else it.border(1.dp, borderColor, RoundedCornerShape(cornerRadius)) },
            color = backgroundColor,
            shape = RoundedCornerShape(cornerRadius),
            content = content
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .let { if (border != null) it.border(border, RoundedCornerShape(cornerRadius)) else it.border(1.dp, borderColor, RoundedCornerShape(cornerRadius)) }
        ) {
            content()
        }
    }
}
