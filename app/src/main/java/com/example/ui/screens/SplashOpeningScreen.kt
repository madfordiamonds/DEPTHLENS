package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashOpeningScreen(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Staggered Fadealphas
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val barAlpha = remember { Animatable(0f) }
    val barFill = remember { Animatable(0f) }

    // Core drawing logic progress objects from current code
    val introProgress = remember { Animatable(0f) }      // For overall scale and logo base
    val eyeOpenProgress = remember { Animatable(0f) }    // For drawing eye path height
    val irisRotation = remember { Animatable(-90f) }     // For diagonal line alignment
    val splashAlpha = remember { Animatable(1f) }        // For entire screen exit transition

    LaunchedEffect(Unit) {
        // Step 1: Staggered alphas and logo introduction
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = EaseInOutQuad)
            )
        }
        launch {
            introProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f) // Custom elastic EaseOutBack
                )
            )
        }

        delay(150)
        // Smooth eye opening animation
        launch {
            eyeOpenProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = FastOutSlowInEasing
                )
            )
        }

        delay(150)
        // Title text fade-in
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = EaseOutQuad)
            )
        }

        delay(150)
        // Tagline text fade-in
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = EaseOutQuad)
            )
        }
        // Iris rotation lock-in
        launch {
            irisRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 850,
                    easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
                )
            )
        }

        delay(150)
        // Bottom loading bar shows and starts loading to 100%
        launch {
            barAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, easing = EaseOutQuad)
            )
        }
        launch {
            barFill.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1100, easing = EaseInOutQuad)
            )
        }

        // Total delay before exiting aligns with full loading progress
        delay(1150)

        // Step 4: Fade out splash stage completely
        splashAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400)
        )
        onAnimationComplete()
    }

    // Floating animation driver for ambient blur orbs
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_orbs")
    
    val orb1Offset by infiniteTransition.animateFloat(
        initialValue = (-8f),
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_left_float"
    )
    val orb2Offset by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = (-10f),
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_right_float"
    )

    // Pulsing halo rings metrics
    val haloGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )
    val haloGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060609)) // solid Background (Void)
            .alpha(splashAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Blur Orb 1: Top-Left (Violet, size 200dp, Color(0x407E65FF))
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-30).dp + orb1Offset.dp, y = (-20).dp + orb1Offset.dp)
                .size(200.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x407E65FF), Color.Transparent),
                    )
                )
        )

        // Ambient Blur Orb 2: Bottom-Right (Cyan, size 150dp, Color(0x2E00D4FF))
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp + orb2Offset.dp, y = 40.dp + orb2Offset.dp)
                .size(150.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x2E00D4FF), Color.Transparent),
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Eye SVG with custom glowing violet halo ring background
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.alpha(logoAlpha.value)
            ) {
                // Pulsing Halo Ring (Violet Glow)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(haloGlowScale * introProgress.value)
                        .alpha(haloGlowAlpha)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x3B7E65FF), Color.Transparent),
                            )
                        )
                )

                // Outer decorative halo line
                Canvas(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(haloGlowScale * introProgress.value)
                        .alpha(haloGlowAlpha * 0.4f)
                ) {
                    drawCircle(
                        color = Color(0x667E65FF),
                        radius = size.width / 2.1f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Core Eye Drawing matching standard drawing mechanics
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(introProgress.value)
                ) {
                    val w = size.width
                    val h = size.height
                    val centerOffset = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f)

                    // 1. Path of Outer Eye outline
                    val eyePath = Path().apply {
                        val startX = w * 0.15f
                        val endX = w * 0.85f
                        val centerY = h * 0.5f

                        val peakYOffset = h * 0.22f * eyeOpenProgress.value

                        moveTo(startX, centerY)
                        cubicTo(
                            w * 0.35f, centerY - peakYOffset,
                            w * 0.65f, centerY - peakYOffset,
                            endX, centerY
                        )
                        cubicTo(
                            w * 0.65f, centerY + peakYOffset,
                            w * 0.35f, centerY + peakYOffset,
                            startX, centerY
                        )
                        close()
                    }

                    drawPath(
                        path = eyePath,
                        color = ElectricViolet,
                        style = Stroke(
                            width = 3.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 2. Center Iris and interior structures (drawn when eye opened)
                    if (eyeOpenProgress.value > 0.05f) {
                        val elementAlpha = ((eyeOpenProgress.value - 0.05f) / 0.95f).coerceIn(0f, 1f)
                        val irisRadius = w * 0.16f * eyeOpenProgress.value

                        // Draw outer circular iris stroke - Premium Cyan (#00D4FF)
                        drawCircle(
                            color = PremiumCyan.copy(alpha = elementAlpha),
                            radius = irisRadius,
                            center = centerOffset,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Math for 45-degree diagonal line rotation
                        val currentRotationAngle = 45f + irisRotation.value
                        val angleRad = Math.toRadians(currentRotationAngle.toDouble())
                        val cosValue = Math.cos(angleRad).toFloat()
                        val sinValue = Math.sin(angleRad).toFloat()

                        // Diagonal line coordinates centered inside iris - Violet (#7E65FF)
                        val lineHalfLength = irisRadius * 0.9f
                        val startLineOfs = androidx.compose.ui.geometry.Offset(
                            centerOffset.x - lineHalfLength * cosValue,
                            centerOffset.y + lineHalfLength * sinValue
                        )
                        val endLineOfs = androidx.compose.ui.geometry.Offset(
                            centerOffset.x + lineHalfLength * cosValue,
                            centerOffset.y - lineHalfLength * sinValue
                        )

                        drawLine(
                            color = ElectricViolet.copy(alpha = elementAlpha),
                            start = startLineOfs,
                            end = endLineOfs,
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // 3. Symmetric node rings centered on either half-line sides
                        val nodeDistance = irisRadius * 0.44f
                        val node1Center = androidx.compose.ui.geometry.Offset(
                            centerOffset.x - nodeDistance * cosValue,
                            centerOffset.y + nodeDistance * sinValue
                        )
                        val node2Center = androidx.compose.ui.geometry.Offset(
                            centerOffset.x + nodeDistance * cosValue,
                            centerOffset.y - nodeDistance * sinValue
                        )

                        val nodeRadius = w * 0.045f * eyeOpenProgress.value

                        // Left-bottom ring node - Cyan outline, Void background fill
                        drawCircle(
                            color = PremiumCyan.copy(alpha = elementAlpha),
                            radius = nodeRadius,
                            center = node1Center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF060609).copy(alpha = elementAlpha),
                            radius = (nodeRadius - 1.2.dp.toPx()).coerceAtLeast(0.1f),
                            center = node1Center,
                            style = Fill
                        )

                        // Right-top ring node
                        drawCircle(
                            color = PremiumCyan.copy(alpha = elementAlpha),
                            radius = nodeRadius,
                            center = node2Center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF060609).copy(alpha = elementAlpha),
                            radius = (nodeRadius - 1.2.dp.toPx()).coerceAtLeast(0.1f),
                            center = node2Center,
                            style = Fill
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title: DEPTHLENS
            Text(
                text = "DEPTHLENS",
                color = Color(0xFFF0EEFF),
                fontSize = 22.sp,
                style = TextStyle.Default, // Prevent system overrides
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.22.sp * 22, // letter spacing 0.22em equivalent
                fontFamily = DMMonoFontFamily,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline: REALITY INTELLIGENCE SYSTEM
            Text(
                text = "REALITY INTELLIGENCE SYSTEM",
                color = Color(0xFF00D4FF),
                fontSize = 10.sp,
                style = TextStyle.Default,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.25.sp * 10, // letter spacing 0.25em equivalent
                fontFamily = DMMonoFontFamily,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Loading bar at bottom
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
                    .alpha(barAlpha.value)
                    .background(Color(0xFF22223A), shape = RoundedCornerShape(1.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(barFill.value)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF7E65FF), Color(0xFF00D4FF))
                            ),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}
