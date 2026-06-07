package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DMMonoFontFamily
import com.example.ui.theme.ElectricViolet

@Composable
fun ThreeDotThinkingIndicator(
    modifier: Modifier = Modifier,
    text: String = "Analyzing..."
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Thinking dots row
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "ThinkingIndicator")

            val offsets = (0..2).map { index ->
                infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -5f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1200
                            0f at (index * 150) with FastOutSlowInEasing
                            -5f at (index * 150 + 200) with FastOutSlowInEasing
                            0f at (index * 150 + 400) with FastOutSlowInEasing
                            0f at 1200 with FastOutSlowInEasing
                        },
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "offset_$index"
                )
            }

            val alphas = (0..2).map { index ->
                infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1200
                            0.3f at (index * 150) with FastOutSlowInEasing
                            1.0f at (index * 150 + 200) with FastOutSlowInEasing
                            0.3f at (index * 150 + 400) with FastOutSlowInEasing
                            0.3f at 1200 with FastOutSlowInEasing
                        },
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "alpha_$index"
                )
            }

            for (i in 0..2) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .graphicsLayer {
                            translationY = offsets[i].value
                            alpha = alphas[i].value
                        }
                        .drawBehind {
                            // Subtle soft radial glow matching other neon tokens
                            drawCircle(
                                color = ElectricViolet.copy(alpha = 0.35f * alphas[i].value),
                                radius = size.width * 1.5f
                            )
                        }
                        .background(ElectricViolet, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            fontSize = 11.sp,
            fontFamily = DMMonoFontFamily,
            color = ElectricViolet,
            letterSpacing = 1.sp
        )
    }
}
