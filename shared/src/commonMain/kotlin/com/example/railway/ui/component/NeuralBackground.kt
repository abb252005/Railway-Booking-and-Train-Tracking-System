package com.example.railway.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun NeuralBackground(
    modifier: Modifier = Modifier,
    neuronCount: Int = 360,
    connectionDistance: Float = 450f,
    color: Color = Color.LightGray.copy(alpha = 0.25f)
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val neurons = remember {
        List(neuronCount) {
            MutableNeuron(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.0012f,
                vy = (Random.nextFloat() - 0.5f) * 0.0012f,
                activityOffset = Random.nextFloat()
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Update and draw
        neurons.forEach { neuron ->
            neuron.x += neuron.vx
            neuron.y += neuron.vy

            // Wrap around
            if (neuron.x < 0) neuron.x = 1f
            if (neuron.x > 1) neuron.x = 0f
            if (neuron.y < 0) neuron.y = 1f
            if (neuron.y > 1) neuron.y = 0f

            val pos = Offset(neuron.x * width, neuron.y * height)
            drawCircle(color = color, radius = 2.dp.toPx(), center = pos)
        }

        // Connections
        for (i in 0 until neuronCount) {
            val n1 = neurons[i]
            val p1 = Offset(n1.x * width, n1.y * height)
            
            for (j in i + 1 until neuronCount) {
                val n2 = neurons[j]
                val p2 = Offset(n2.x * width, n2.y * height)
                
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val distSq = dx * dx + dy * dy
                val maxDistSq = connectionDistance * connectionDistance
                
                if (distSq < maxDistSq) {
                    val dist = sqrt(distSq)
                    val proximityAlpha = 1f - (dist / connectionDistance)
                    
                    // Dynamic appearance/disappearance
                    // Combine distance with a time-based factor unique to this pair
                    val pairHash = (i * 31 + j).toFloat()
                    val activity = (pulse + pairHash + n1.activityOffset + n2.activityOffset) % 1f
                    val activityAlpha = if (activity < 0.5f) activity * 2f else 1f - (activity - 0.5f) * 2f
                    
                    val finalAlpha = proximityAlpha * activityAlpha * color.alpha
                    
                    drawLine(
                        color = color.copy(alpha = finalAlpha),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
    }
}

private class MutableNeuron(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val activityOffset: Float
)
