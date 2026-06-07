package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageEntity
import com.example.data.repository.ResponseParser
import com.example.ui.theme.*
import com.example.ui.components.ThreeDotThinkingIndicator
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.window.Dialog
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun AnalysisScreen(
    activeMessages: List<MessageEntity>,
    selectedMode: String,
    onBackToHome: () -> Unit,
    onSubmitQuery: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    // Find the latest model/AI message that contains diagnostic results
    val latestAiMessage = remember(activeMessages) {
        activeMessages.lastOrNull { it.role == "model" && !it.text.startsWith("Error:") }
    }

    val latestUserMessage = remember(activeMessages) {
        activeMessages.lastOrNull { it.role == "user" }
    }

    val parsedResponse = remember(latestAiMessage?.text) {
        if (latestAiMessage != null) {
            ResponseParser.parse(latestAiMessage.text)
        } else {
            null
        }
    }

    // Keep track of which cards are expanded
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    // Standard 10 Reality Layers definition with colors and specific default definitions
    val standardLayers = remember {
        listOf(
            LayerDefinition(1, "Observable Reality", Layer1, "Surface events, objective metrics, and immediate sensory data."),
            LayerDefinition(2, "Behavioral Reality", Layer2, "Consistent patterns of action, communication styles, and habit loops."),
            LayerDefinition(3, "Psychological Reality", Layer3, "Individual beliefs, defense mechanisms, cognitive biases, and internal narratives."),
            LayerDefinition(4, "Emotional Reality", Layer4, "Underlying feelings, affective drives, insecurities, and sub-conscious state dynamics."),
            LayerDefinition(5, "Social Reality", Layer5, "Interpersonal communication channels, cultural rules, dynamic peer pressure mechanics."),
            LayerDefinition(6, "Strategic Reality", Layer6, "Conscious intent, tactical utility, negotiation dynamics, and resource allocations."),
            LayerDefinition(7, "Systemic Reality", Layer7, "Feedback loops, environmental limits, structural layouts, and flow dynamics."),
            LayerDefinition(8, "Pattern Reality", Layer8, "Historical frequencies, cyclic re-occurrences, and probabilistic repetitions."),
            LayerDefinition(9, "Root Cause Reality", Layer9, "Competing incentive models, design flaws, or ontological assumptions generating the situation."),
            LayerDefinition(10, "Absolute Reality", Layer10, "Core existential truths, natural laws, and non-dual realities behind all constructs.")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepMidnight)
    ) {
        // Redesigned Topbar Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface1)
                .border(1.dp, BorderSubtle)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onBackToHome() }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SESSIONS",
                    fontSize = 9.sp,
                    fontFamily = DMMonoFontFamily,
                    letterSpacing = 1.sp,
                    color = TextSecondaryColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "REALITY DETECTOR",
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                fontFamily = DMMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = ElectricViolet
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = TextMutedColor,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { /* Share placeholder */ }
                )
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = TextMutedColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Dashboard header
            val queryText = latestUserMessage?.text ?: "All Layers Active Simulator"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (latestAiMessage != null) SuccessColor else TextMutedColor,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (latestAiMessage != null) "ACTIVE CONVERSATION PROFILE" else "REALITY MATRIX MODE",
                    fontSize = 8.sp,
                    letterSpacing = 1.5.sp,
                    fontFamily = DMMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (latestAiMessage != null) SuccessColor else TextMutedColor
                )
            }

            Text(
                text = "\"$queryText\"",
                fontFamily = DMSerifDisplayFontFamily,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                color = TextPrimaryColor,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = if (latestAiMessage != null) {
                    "$selectedMode · Diagnostic Study · ${parsedResponse?.depthLayers?.size ?: 0} Active Lenses"
                } else {
                    "Simulated Offline Framework · 10 Active Diagnostic Lenses"
                },
                fontSize = 8.5.sp,
                fontFamily = DMMonoFontFamily,
                color = TextMutedColor,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Reality Layer Card Grid Stack
            Text(
                text = "REALITY INTELLIGENCE DASHBOARD",
                fontSize = 8.5.sp,
                letterSpacing = 1.3.sp,
                fontFamily = DMMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                standardLayers.forEach { layerDef ->
                    val parsedMatch = parsedResponse?.depthLayers?.find { it.layerNumber == layerDef.num }
                    val isLiveActive = parsedMatch != null
                    val isExpanded = expandedStates[layerDef.num] == true

                    // Setup custom Card background & glow styling matching bash.html guidelines
                    val cardBorder = if (isLiveActive) BorderActive else BorderSubtle

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Surface2
                        ),
                        border = BorderStroke(
                            width = if (isExpanded) 1.5.dp else 1.dp,
                            color = if (isExpanded) ElectricViolet else cardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedStates[layerDef.num] = !isExpanded
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            // Card Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = String.format("%02d", layerDef.num),
                                        fontSize = 10.sp,
                                        fontFamily = DMMonoFontFamily,
                                        color = if (isLiveActive) TextPrimaryColor else TextMutedColor,
                                        modifier = Modifier.width(22.dp),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(
                                                color = layerDef.accentColor,
                                                shape = CircleShape
                                            )
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = layerDef.name,
                                        fontSize = 11.5.sp,
                                        color = TextPrimaryColor,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 5 Strength Pips indicator
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val strength = if (isLiveActive) {
                                            when (layerDef.num) {
                                                9 -> 5
                                                6, 7, 10 -> 4
                                                else -> 3
                                            }
                                        } else {
                                            // Non-active display default pips
                                            when (layerDef.num) {
                                                1 -> 4
                                                2 -> 3
                                                else -> 2
                                            }
                                        }

                                        for (pipIdx in 1..5) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(
                                                        color = if (pipIdx <= strength) layerDef.accentColor else Surface4,
                                                        shape = RoundedCornerShape(1.5.dp)
                                                    )
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = TextMutedColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Expanded Content
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(
                                    color = BorderSubtle,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = "FRAMEWORK DEFINITION",
                                    fontSize = 7.5.sp,
                                    letterSpacing = 1.sp,
                                    fontFamily = DMMonoFontFamily,
                                    color = TextMutedColor,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Text(
                                    text = layerDef.description,
                                    fontSize = 10.5.sp,
                                    fontFamily = InstrumentSansFontFamily,
                                    color = TextSecondaryColor,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                if (isLiveActive && parsedMatch != null) {
                                    Text(
                                        text = "ACTIVE SESSION INTELLIGENCE",
                                        fontSize = 7.5.sp,
                                        letterSpacing = 1.sp,
                                        fontFamily = DMMonoFontFamily,
                                        color = layerDef.accentColor,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    Text(
                                        text = parsedMatch.description,
                                        fontSize = 11.5.sp,
                                        fontFamily = InstrumentSansFontFamily,
                                        color = TextPrimaryColor,
                                        lineHeight = 16.sp
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Surface3, shape = RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderSubtle, shape = RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "No study is currently processing this layer. Launch an analysis diagnostic inside Chat sheet to evaluate active real-world metrics for this level.",
                                            fontSize = 9.5.sp,
                                            fontFamily = InstrumentSansFontFamily,
                                            color = TextMutedColor,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FUTURE PROBABILITY CANVS & MODEL (D3-style)
            var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
            val scenarioCurvePoints = remember {
                listOf(
                    PlotPointData(0, 0.12f, 0.70f, "T+1 Month", "October 2026", "Initial Loop Friction", 58, "System response begins rejecting current paradigm. Feedback loop resistance bubbles across user networks."),
                    PlotPointData(1, 0.38f, 0.35f, "T+3 Months", "December 2026", "Maximum Tension Structural Peak", 82, "Underlying psychological biases and competing incentives trigger structural choke points."),
                    PlotPointData(2, 0.65f, 0.55f, "T+6 Months", "March 2027", "Intervention Horizon Shift", 45, "Structural policy adjustment alters resource distribution, resetting system boundaries."),
                    PlotPointData(3, 0.88f, 0.20f, "T+12 Months", "September 2027", "Steady State Equilibrium", 92, "Absolute reality constraints assert control. System establishes sustainable, highly balanced alignment loops.")
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Surface2),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface3)
                            .border(BorderStroke(1.dp, BorderSubtle))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INTERACTIVE FORECAST CANVUSES (TAP POINTS)",
                            fontSize = 8.sp,
                            letterSpacing = 1.1.sp,
                            fontFamily = DMMonoFontFamily,
                            color = TextMutedColor,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(5.dp).background(SuccessColor, CircleShape))
                            Text(
                                text = "D3 INTERACTION CORE",
                                fontSize = 7.5.sp,
                                fontFamily = DMMonoFontFamily,
                                color = SuccessColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // The Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Surface2)
                            .padding(8.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(scenarioCurvePoints) {
                                    detectTapGestures { offset ->
                                        val w = size.width
                                        val h = size.height
                                        var closestIdx: Int? = null
                                        var minDist = Float.MAX_VALUE
                                        scenarioCurvePoints.forEach { pt ->
                                            val px = pt.xRatio * w
                                            val py = pt.yRatio * h
                                            val dist = Math.hypot((offset.x - px).toDouble(), (offset.y - py).toDouble()).toFloat()
                                            if (dist < 36.dp.toPx() && dist < minDist) {
                                                closestIdx = pt.index
                                                minDist = dist
                                            }
                                        }
                                        selectedPointIndex = if (closestIdx == selectedPointIndex) null else closestIdx
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height

                            // Draw baseline grid lines
                            val lines = 4
                            for (i in 1..lines) {
                                val ratio = i.toFloat() / (lines + 1)
                                drawLine(
                                    color = BorderSubtle.copy(alpha = 0.2f),
                                    start = Offset(0f, ratio * h),
                                    end = Offset(w, ratio * h),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Draw line curve
                            val path = Path().apply {
                                scenarioCurvePoints.forEachIndexed { idx, pt ->
                                    val px = pt.xRatio * w
                                    val py = pt.yRatio * h
                                    if (idx == 0) {
                                        moveTo(px, py)
                                    } else {
                                        val prev = scenarioCurvePoints[idx - 1]
                                        val prevX = prev.xRatio * w
                                        val prevY = prev.yRatio * h
                                        cubicTo(
                                            (prevX + px) / 2f, prevY,
                                            (prevX + px) / 2f, py,
                                            px, py
                                        )
                                    }
                                }
                            }

                            drawPath(
                                path = path,
                                color = ElectricViolet.copy(alpha = 0.8f),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw Event/Date Plot Points
                            scenarioCurvePoints.forEach { pt ->
                                val px = pt.xRatio * w
                                val py = pt.yRatio * h
                                val isSelected = selectedPointIndex == pt.index

                                // Shimmering outer aura
                                drawCircle(
                                    color = if (isSelected) PremiumCyan.copy(alpha = 0.4f) else ElectricViolet.copy(alpha = 0.15f),
                                    radius = if (isSelected) 11.dp.toPx() else 6.dp.toPx(),
                                    center = Offset(px, py)
                                )

                                // Inner core
                                drawCircle(
                                    color = if (isSelected) PremiumCyan else ElectricViolet,
                                    radius = 3.5.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }

                    // Specific Tooltip Overlay details
                    selectedPointIndex?.let { idx ->
                        val pt = scenarioCurvePoints[idx]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .background(Surface3, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, PremiumCyan.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${pt.label} Forecast · ${pt.date}",
                                        fontSize = 8.sp,
                                        fontFamily = DMMonoFontFamily,
                                        color = PremiumCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${pt.prob}% PROB",
                                        fontSize = 8.sp,
                                        fontFamily = DMMonoFontFamily,
                                        color = TextMutedColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = pt.title,
                                    fontSize = 11.sp,
                                    fontFamily = InstrumentSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryColor
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = pt.desc,
                                    fontSize = 9.sp,
                                    fontFamily = InstrumentSansFontFamily,
                                    color = TextSecondaryColor,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }

                    if (selectedPointIndex == null) {
                        Text(
                            text = "▲ Tap points above to inspect date markers and systemic implications",
                            fontSize = 8.5.sp,
                            fontStyle = FontStyle.Italic,
                            color = TextMutedColor,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // QUICK ACTIONS TEMPLATE BAR
            Text(
                text = "SIMULATOR QUICK ACTIONS",
                fontSize = 8.sp,
                letterSpacing = 1.3.sp,
                fontFamily = DMMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val actionTemplates = listOf(
                    Triple("Analyze Relationship", "🤝", "Deconstruct hidden psychological biases and interpersonal feedback cycles..."),
                    Triple("Evaluate Risk", "⚠️", "Identify escalation thresholds, stress-bearing parameters, and systemic risks..."),
                    Triple("Map Incentives", "🪙", "Evaluate competing payoff structures and strategic user alignment levers...")
                )

                actionTemplates.forEach { action ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Surface2, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, BorderSubtle, shape = RoundedCornerShape(8.dp))
                            .clickable {
                                onSubmitQuery(action.third)
                                onBackToHome()
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = action.second, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = action.first,
                                fontSize = 8.sp,
                                fontFamily = DMMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 10.sp
                            )
                        }
                    }
                }
            }

            // SIMULATOR VOICE CONSOLE TOOlBAR
            var isMicActive by remember { mutableStateOf(false) }
            var isVoiceAnalyzing by remember { mutableStateOf(false) }
            var transcribedTextMemo by remember { mutableStateOf("") }

            Text(
                text = "SIMULATOR INTELLIGENCE TOOLBAR",
                fontSize = 8.sp,
                letterSpacing = 1.3.sp,
                fontFamily = DMMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Surface2)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (isMicActive) ErrorColor.copy(alpha = 0.2f) else Surface3,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                if (isMicActive) {
                                    isMicActive = false
                                    isVoiceAnalyzing = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        isVoiceAnalyzing = false
                                        transcribedTextMemo = "Evaluate the cognitive friction and organizational communication limits of modern distributed systems."
                                    }
                                } else {
                                    isMicActive = true
                                    transcribedTextMemo = ""
                                }
                            },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = if (isMicActive) "🛑" else "🎙️", fontSize = 12.sp)
                            }

                            Column {
                                Text(
                                    text = if (isMicActive) "Voice Recording Enabled" else "Brainstorm Buffer Dictation",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMicActive) ErrorColor else TextPrimaryColor,
                                    fontFamily = InstrumentSansFontFamily
                                )
                                if (isVoiceAnalyzing) {
                                    ThreeDotThinkingIndicator(
                                        modifier = Modifier.padding(top = 2.dp),
                                        text = "Voice analyzing..."
                                    )
                                } else {
                                    Text(
                                        text = if (isMicActive) "Pulsing audio stream active. Tap STOP to transcribe." 
                                               else if (transcribedTextMemo.isNotEmpty()) "Transcription ready for parsing"
                                               else "Tap MIC to capture live vocal thought matrices directly",
                                        fontSize = 8.5.sp,
                                        color = TextMutedColor,
                                        fontFamily = InstrumentSansFontFamily
                                    )
                                }
                            }
                        }

                        if (transcribedTextMemo.isNotEmpty()) {
                            Button(
                                onClick = {
                                    onSubmitQuery(transcribedTextMemo)
                                    transcribedTextMemo = ""
                                    onBackToHome()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("DIAGNOSE", fontSize = 8.sp, color = Color.White, fontFamily = DMMonoFontFamily)
                            }
                        }
                    }

                    if (transcribedTextMemo.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface3, shape = RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "\"$transcribedTextMemo\"",
                                fontSize = 9.sp,
                                fontFamily = InstrumentSansFontFamily,
                                fontStyle = FontStyle.Italic,
                                color = TextSecondaryColor
                            )
                        }
                    }
                }
            }

            // GOOGLE SEARCH GROUNDING SIGNALS
            var searchFieldName by remember { mutableStateOf("") }
            var isSearchingWeb by remember { mutableStateOf(false) }
            var webSignalList by remember {
                mutableStateOf(
                    listOf(
                        WebSignalCardData("Regulatory Volatility Ledger Guidelines", "Fintech Intelligence Bureau", 89, "SEC revised centralized liquidity reserve standards, creating sudden feedback loops on stable networks.")
                    )
                )
            }

            Text(
                text = "REAL-TIME WEB SIGNAL GROUNDING",
                fontSize = 8.sp,
                letterSpacing = 1.3.sp,
                fontFamily = DMMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchFieldName,
                    onValueChange = { searchFieldName = it },
                    placeholder = { Text("Search Web to ground active forecast metrics...", fontSize = 11.sp, color = TextMutedColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryColor,
                        unfocusedTextColor = TextPrimaryColor,
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = Surface2,
                        unfocusedContainerColor = Surface2
                    ),
                    trailingIcon = {
                        if (isSearchingWeb) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.6.dp, color = PremiumCyan)
                        } else {
                            Text(
                                text = "🔍",
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    if (searchFieldName.isNotBlank()) {
                                        isSearchingWeb = true
                                        val filter = searchFieldName
                                        searchFieldName = ""
                                        coroutineScope.launch {
                                            delay(1200)
                                            val newSignal = WebSignalCardData(
                                                title = "Signal Grounding Index: $filter",
                                                source = "Intelligence Service APIs & News",
                                                relevance = 92,
                                                summary = "Retrieved fresh context indicating increased index volatility and shifting strategic bounds related to user topic constraints."
                                            )
                                            webSignalList = listOf(newSignal) + webSignalList
                                            isSearchingWeb = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                )

                webSignalList.forEach { signal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = Surface3),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "EXTERNAL GROUNDING SIGNAL",
                                    fontSize = 7.5.sp,
                                    fontFamily = DMMonoFontFamily,
                                    color = WarningColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${signal.relevance}% RELEVANCE",
                                    fontSize = 7.5.sp,
                                    fontFamily = DMMonoFontFamily,
                                    color = TextMutedColor
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = signal.title,
                                fontSize = 11.sp,
                                fontFamily = InstrumentSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryColor
                            )
                            Text(
                                text = "Source: ${signal.source}",
                                fontSize = 8.5.sp,
                                fontFamily = DMMonoFontFamily,
                                color = TextMutedColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = signal.summary,
                                fontSize = 9.sp,
                                fontFamily = InstrumentSansFontFamily,
                                color = TextSecondaryColor,
                                lineHeight = 12.2.sp
                            )
                        }
                    }
                }
            }

            // PRINTABLE SESSION PDF REPORT GENERATOR
            var showReportDrawerModal by remember { mutableStateOf(false) }

            Button(
                onClick = { showReportDrawerModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                border = BorderStroke(1.dp, ElectricViolet),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
            ) {
                Text("📄 GENERATE PDF INTELLIGENCE REPORT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = DMMonoFontFamily)
            }

            if (showReportDrawerModal) {
                Dialog(onDismissRequest = { showReportDrawerModal = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "DEPTHLENS GLOBAL SYSTEM REPORT",
                                fontSize = 12.sp,
                                fontFamily = DMMonoFontFamily,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = "CONFIDENTIAL DIAGNOSTIC ANALYTICS BRIEF",
                                fontSize = 8.sp,
                                fontFamily = DMMonoFontFamily,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = Color.Black, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "TARGET EXAMINED: \"$queryText\"",
                                fontSize = 11.sp,
                                fontFamily = InstrumentSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Exploration Mode Scope: $selectedMode",
                                fontSize = 8.5.sp,
                                fontFamily = DMMonoFontFamily,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "A. PROCESSED REALITY COGNITIVE LENSES",
                                fontSize = 9.5.sp,
                                fontFamily = DMMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            standardLayers.forEach { layer ->
                                val processedLensText = parsedResponse?.depthLayers?.firstOrNull { it.layerNumber == layer.num }?.description
                                                     ?: "Diagnostic metric offline. Standard reality alignment active."
                                Text(
                                    text = "Layer ${layer.num}: ${layer.name}",
                                    fontSize = 9.sp,
                                    fontFamily = InstrumentSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = processedLensText,
                                    fontSize = 8.5.sp,
                                    fontFamily = InstrumentSansFontFamily,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = Color.Black, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "B. PROBABILITY TIMELINE EVENT FORECASTS",
                                fontSize = 9.5.sp,
                                fontFamily = DMMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            scenarioCurvePoints.forEach { pt ->
                                Text(
                                    text = "${pt.label} (${pt.prob}% Probable) - ${pt.title}",
                                    fontSize = 9.sp,
                                    fontFamily = InstrumentSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = pt.desc,
                                    fontSize = 8.5.sp,
                                    fontFamily = InstrumentSansFontFamily,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showReportDrawerModal = false }) {
                                    Text("CLOSE DOCUMENT", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { showReportDrawerModal = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("PRINT OUT / SHARE PDF", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Scenarios structures helper classes
data class LayerDefinition(val num: Int, val name: String, val accentColor: Color, val description: String)
data class PlotPointData(
    val index: Int,
    val xRatio: Float,
    val yRatio: Float,
    val label: String,
    val date: String,
    val title: String,
    val prob: Int,
    val desc: String
)
data class WebSignalCardData(
    val title: String,
    val source: String,
    val relevance: Int,
    val summary: String
)

