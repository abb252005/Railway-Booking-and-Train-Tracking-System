package com.example.railway.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AiSupportOrb(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isThinking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    
    val floatDuration = if (isThinking) 900 else 1800
    val pulseDuration = if (isThinking) 1200 else 2400
    val photonDuration = if (isThinking) 600 else 1200

    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(floatDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val photonTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(photonDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "photonTime"
    )

    val photonCount = if (isThinking) 24 else 12
    val photons = remember(photonCount) { 
        List(photonCount) { 
            val angle = Random.nextFloat() * 360f
            val angleRad = (angle * (kotlin.math.PI / 180f)).toFloat()
            PhotonState(
                cosA = cos(angleRad),
                sinA = sin(angleRad),
                delay = Random.nextFloat()
            ) 
        } 
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer { 
                translationY = floatAnim.dp.toPx() 
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    rotationZ = rotation
                }
                .drawWithCache {
                    val orbRadius = 32.dp.toPx()
                    val center = Offset(size.width / 2, size.height / 2)

                    val bodyBrush = Brush.radialGradient(
                        0.0f to primaryColor.copy(alpha = 0.9f),
                        0.5f to primaryColor.copy(alpha = 0.6f),
                        0.8f to primaryColor.copy(alpha = 0.3f),
                        1.0f to Color.Transparent
                    )
                    
                    val coreBrush = Brush.radialGradient(
                        0.0f to Color.White.copy(alpha = 0.4f),
                        0.4f to primaryColor.copy(alpha = 0.8f),
                        1.0f to Color.Transparent
                    )
                    
                    val specularBrush = Brush.radialGradient(
                        0.0f to Color.White.copy(alpha = 0.7f),
                        1.0f to Color.Transparent
                    )
                    
                    val rimBrush = Brush.radialGradient(
                        0.0f to Color.White.copy(alpha = 0.25f),
                        1.0f to Color.Transparent
                    )
                    
                    val edgeBrush = Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.5f),
                        1.0f to Color.Black.copy(alpha = 0.1f)
                    )

                    onDrawWithContent {
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.15f),
                            radius = orbRadius,
                            center = center
                        )
                        drawCircle(
                            brush = bodyBrush,
                            radius = orbRadius,
                            center = center
                        )
                        drawCircle(
                            brush = coreBrush,
                            radius = orbRadius * 0.7f,
                            center = center
                        )
                        drawCircle(
                            brush = specularBrush,
                            center = Offset(center.x - orbRadius * 0.35f, center.y - orbRadius * 0.35f),
                            radius = orbRadius * 0.45f
                        )
                        drawCircle(
                            brush = rimBrush,
                            center = Offset(center.x + orbRadius * 0.2f, center.y + orbRadius * 0.4f),
                            radius = orbRadius * 0.5f
                        )
                        drawCircle(
                            brush = edgeBrush,
                            radius = orbRadius,
                            style = Stroke(width = 1.dp.toPx())
                        )

                        val maxEmissionRadius = 25.dp.toPx()
                        photons.forEach { photon ->
                            val progress = (photonTime + photon.delay) % 1f
                            val currentRadius = orbRadius + (progress * maxEmissionRadius)
                            val alpha = 1f - progress
                            
                            val x = center.x + photon.cosA * currentRadius
                            val y = center.y + photon.sinA * currentRadius
                            
                            drawCircle(
                                color = primaryColor.copy(alpha = alpha * 0.6f),
                                radius = 1.5.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) { }
        
        Text(
            "RAIL-E",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private data class PhotonState(
    val cosA: Float,
    val sinA: Float,
    val delay: Float
)
