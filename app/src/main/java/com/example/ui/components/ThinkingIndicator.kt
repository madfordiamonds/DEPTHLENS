package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DMMonoFontFamily
import com.example.ui.theme.ElectricViolet
import kotlin.math.abs

@Composable
fun ThreeDotThinkingIndicator(
    modifier: Modifier = Modifier,
    text: String = "ANALYZING DEEPER..."
) {
    Row(
        modifier = modifier.padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "ThinkingWave")
        val pulseIndex by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseIndex"
        )

        // Monospace glowing text
        Text(
            text = text.uppercase(),
            fontSize = 11.sp,
            fontFamily = DMMonoFontFamily,
            color = ElectricViolet,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        // Row of 3 sequential pulsing dots (● ○ ○ -> ○ ● ○ -> ○ ○ ●)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..2) {
                // Piecewise linear interpolation to create perfect sequential pulse
                val dotAlpha = remember(pulseIndex) {
                    val rawAlpha = when {
                        i == 0 -> {
                            if (pulseIndex < 1f) 1.0f - pulseIndex 
                            else if (pulseIndex > 2f) pulseIndex - 2f 
                            else 0.15f
                        }
                        i == 1 -> {
                            if (pulseIndex in 0f..2f) 1.0f - abs(pulseIndex - 1f) 
                            else 0.15f
                        }
                        i == 2 -> {
                            if (pulseIndex > 1f) 1.0f - abs(pulseIndex - 2f) 
                            else 0.15f
                        }
                        else -> 0.15f
                    }
                    rawAlpha.coerceIn(0.15f, 1.0f)
                }

                val scale = 0.8f + (dotAlpha * 0.4f)
                val hoverOffsetY = -((dotAlpha - 0.15f) / 0.85f * 5f) // translate up by 5dp when fully active

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationY = hoverOffsetY
                            alpha = dotAlpha
                        }
                        .drawBehind {
                            // Radial glow effect matching cyber OS style
                            drawCircle(
                                color = ElectricViolet.copy(alpha = 0.45f * dotAlpha),
                                radius = size.width * 1.8f
                            )
                        }
                        .background(ElectricViolet, CircleShape)
                )
            }
        }
    }
}
