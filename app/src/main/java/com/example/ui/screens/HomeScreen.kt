package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.content.Intent
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    sessions: List<SessionEntity>,
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    onSessionSelected: (String) -> Unit,
    onSubmitQuery: (String) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onAddAttachment: (String) -> Unit = {},
    archivedInsights: List<com.example.data.model.ArchivedInsightEntity> = emptyList(),
    onDeleteArchivedInsight: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var rawText by remember { mutableStateOf("") }
    var showAttachBottomSheet by remember { mutableStateOf(false) }
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            in 17..20 -> "Good evening,"
            else -> "Good night,"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepMidnight) // Void/Dark base background Color
    ) {
        // Main scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp)) // Safe draw offset

            // Landing Top Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = InstrumentSansFontFamily,
                    fontSize = 15.sp,
                    color = TextSecondaryColor
                )

                // Premium Gradient Avatar "A"
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ElectricViolet, PremiumCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Landing Hero Headline
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "What do you want\nto understand?",
                    fontFamily = DMSerifDisplayFontFamily,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 26.sp,
                    lineHeight = 31.sp,
                    color = TextPrimaryColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "Choose a mode or start typing — DepthLens will reveal what lies beneath.",
                    fontFamily = InstrumentSansFontFamily,
                    fontSize = 11.sp,
                    color = TextMutedColor,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Mode Selection Grid (2x2 Layout expanded to 8 elements matching bash.html guidelines)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val modesList = listOf(
                    Triple("Root Cause", "🔍", "Trace back to the origin of any situation"),
                    Triple("Psychology", "🧠", "Unpack hidden motives & patterns"),
                    Triple("Systems", "🌐", "Feedback loops & incentives"),
                    Triple("Probability", "📈", "Map timeline outcomes forward"),
                    Triple("Business", "💼", "Corporate models, strategies & goals"),
                    Triple("Relationships", "⚓", "Interpersonal dynamics & attachments"),
                    Triple("Spiritual", "✨", "Higher principles, purpose & growth"),
                    Triple("Decision Making", "🎯", "Heuristics, risks & choices")
                )

                for (i in modesList.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (j in 0..1) {
                            if (i + j < modesList.size) {
                                val (mode, emoji, desc) = modesList[i + j]
                                val isActive = mode == selectedMode

                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) Surface2 else Surface2
                                    ),
                                    border = BorderStroke(
                                        width = if (isActive) 1.5.dp else 1.dp,
                                        color = if (isActive) ElectricViolet else BorderSubtle
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onModeSelected(mode)
                                            onNavigateToChat()
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Text(
                                            text = emoji,
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(
                                            text = mode,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryColor,
                                            fontFamily = InstrumentSansFontFamily,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 8.5.sp,
                                            fontFamily = InstrumentSansFontFamily,
                                            color = TextMutedColor,
                                            lineHeight = 11.sp
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Analyses Section
            Text(
                text = "RECENT ANALYSES",
                fontSize = 8.sp,
                letterSpacing = 1.2.sp,
                fontFamily = DMMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (sessions.isEmpty()) {
                Surface(
                    color = Surface2,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No recent analyses found.",
                        fontSize = 10.sp,
                        fontFamily = InstrumentSansFontFamily,
                        color = TextMutedColor,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sessions.take(3).forEach { session ->
                        // Dynamically determine mode emoji
                        val emoji = when {
                            session.title.contains("psychology", true) -> "🧠"
                            session.title.contains("systems", true) -> "🌐"
                            session.title.contains("probabilit", true) -> "📈"
                            session.title.contains("business", true) -> "💼"
                            session.title.contains("relation", true) -> "⚓"
                            session.title.contains("spiritual", true) -> "✨"
                            session.title.contains("decision", true) -> "🎯"
                            else -> "🔍"
                        }

                        // Determine label
                        val modeLabel = when {
                            session.title.contains("psychology", true) -> "Psychology"
                            session.title.contains("systems", true) -> "Systems"
                            session.title.contains("probabilit", true) -> "Probability"
                            session.title.contains("business", true) -> "Business"
                            session.title.contains("relation", true) -> "Relationships"
                            session.title.contains("spiritual", true) -> "Spiritual"
                            session.title.contains("decision", true) -> "Decision"
                            else -> "Root Cause"
                        }

                        // Compact session row
                        Row(
                            modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Surface2, shape = RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderSubtle, shape = RoundedCornerShape(8.dp))
                                            .clickable {
                                                onSessionSelected(session.id)
                                                onNavigateToAnalysis()
                                            }
                                            .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge icon
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        ElectricViolet.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(7.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Conversation details
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = session.title.ifEmpty { "Untitled Diagnostic Study" },
                                    fontSize = 10.5.sp,
                                    color = TextPrimaryColor,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$modeLabel · Live profile · 10 Layers",
                                    fontSize = 8.sp,
                                    fontFamily = DMMonoFontFamily,
                                    color = TextMutedColor
                                )
                            }

                            Text(
                                text = "›",
                                fontSize = 14.sp,
                                color = TextMutedColor,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Archived Explorations Section
            Text(
                text = "ARCHIVED EXPLORATIONS",
                fontSize = 8.sp,
                letterSpacing = 1.2.sp,
                fontFamily = DMMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (archivedInsights.isEmpty()) {
                Surface(
                    color = Surface2,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No archived deep-dive findings saved yet.",
                        fontSize = 10.sp,
                        fontFamily = InstrumentSansFontFamily,
                        color = TextMutedColor,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    archivedInsights.forEach { insight ->
                        var showDetailDialog by remember { mutableStateOf(false) }

                        Surface(
                            color = Surface2,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDetailDialog = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "🔓",
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = insight.query,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryColor,
                                            fontFamily = InstrumentSansFontFamily,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val formattedDate = remember(insight.timestamp) {
                                            java.text.DateFormat.getDateTimeInstance(
                                                java.text.DateFormat.SHORT, 
                                                java.text.DateFormat.SHORT
                                            ).format(java.util.Date(insight.timestamp))
                                        }
                                        Text(
                                            text = "Deep Scan · $formattedDate",
                                            fontSize = 8.sp,
                                            fontFamily = DMMonoFontFamily,
                                            color = TextMutedColor
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onDeleteArchivedInsight(insight.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Archived Insight",
                                            tint = Color(0xFFF44336).copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "›",
                                        fontSize = 14.sp,
                                        color = TextMutedColor
                                    )
                                }
                            }
                        }

                        if (showDetailDialog) {
                            val parsedDetails = remember(insight.jsonContent) {
                                parseArchivedJson(insight.jsonContent)
                            }
                            
                            Dialog(
                                onDismissRequest = { showDetailDialog = false }
                            ) {
                                Surface(
                                    color = Surface1,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ARCHIVED EXPLORATION",
                                                fontSize = 8.sp,
                                                letterSpacing = 1.sp,
                                                fontFamily = DMMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricViolet
                                            )
                                            IconButton(
                                                onClick = { showDetailDialog = false },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close",
                                                    tint = TextMutedColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = insight.query,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryColor,
                                            fontFamily = InstrumentSansFontFamily
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        if (parsedDetails.introduction.isNotEmpty()) {
                                            Text(
                                                text = parsedDetails.introduction,
                                                fontSize = 11.sp,
                                                color = TextSecondaryColor,
                                                lineHeight = 15.sp,
                                                fontFamily = InstrumentSansFontFamily,
                                                modifier = Modifier.padding(bottom = 12.dp)
                                            )
                                        }

                                        Text(
                                            text = "DEEP SCAN LAYERS",
                                            fontSize = 7.5.sp,
                                            letterSpacing = 1.sp,
                                            fontFamily = DMMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMutedColor,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            parsedDetails.depthLayers.forEach { layer ->
                                                val layerColor = when (layer.layerNumber) {
                                                    1 -> Layer1
                                                    2 -> Layer2
                                                    3 -> Layer3
                                                    4 -> Layer4
                                                    5 -> Layer5
                                                    6 -> Layer6
                                                    7 -> Layer7
                                                    8 -> Layer8
                                                    9 -> Layer9
                                                    else -> Layer10
                                                }

                                                var isLayerExpanded by remember { mutableStateOf(false) }

                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Surface3, shape = RoundedCornerShape(6.dp))
                                                        .border(
                                                            1.dp,
                                                            if (isLayerExpanded) layerColor.copy(alpha = 0.6f) else BorderSubtle,
                                                            shape = RoundedCornerShape(6.dp)
                                                        )
                                                        .clickable { isLayerExpanded = !isLayerExpanded }
                                                        .padding(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(modifier = Modifier.size(5.dp).background(layerColor, CircleShape))
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "L${layer.layerNumber} · ${layer.layerName}",
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextPrimaryColor,
                                                                fontFamily = DMMonoFontFamily
                                                            )
                                                        }
                                                        Icon(
                                                            imageVector = if (isLayerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                            contentDescription = null,
                                                            tint = TextMutedColor,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }

                                                    if (isLayerExpanded) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = layer.description,
                                                            fontSize = 11.sp,
                                                            color = TextSecondaryColor,
                                                            lineHeight = 15.sp,
                                                            fontFamily = InstrumentSansFontFamily
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Home level Compose Box at bottom of screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface1)
                .border(BorderStroke(1.dp, BorderSubtle))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2, shape = RoundedCornerShape(26.dp))
                    .border(1.dp, BorderSubtle, shape = RoundedCornerShape(26.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // (A) Modern + attachment button on the left
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ElectricViolet.copy(alpha = 0.22f),
                                    PremiumCyan.copy(alpha = 0.10f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(ElectricViolet, PremiumCyan.copy(alpha = 0.7f))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showAttachBottomSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Attach files",
                        tint = ElectricViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // (B) Input field
                BasicTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontFamily = InstrumentSansFontFamily,
                        fontSize = 11.5.sp,
                        color = TextPrimaryColor,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    decorationBox = { innerTextField ->
                        if (rawText.isEmpty()) {
                            Text(
                                text = "Describe any situation, decision, or pattern…",
                                fontFamily = InstrumentSansFontFamily,
                                fontSize = 11.5.sp,
                                color = TextMutedColor,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))

                // (C) Voice to Text button next to Send
                val speechCtx = LocalContext.current
                var isListening by remember { mutableStateOf(false) }
                val speechRecognizer = remember {
                    try { SpeechRecognizer.createSpeechRecognizer(speechCtx) } catch (e: Exception) { null }
                }
                val recognitionListener = remember(speechRecognizer) {
                    object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() { isListening = false }
                        override fun onError(error: Int) { isListening = false }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val spoken = matches[0]
                                if (!spoken.isNullOrBlank()) {
                                    rawText = if (rawText.isEmpty()) spoken else "$rawText $spoken"
                                }
                            }
                            isListening = false
                        }
                        override fun onPartialResults(partialResults: Bundle?) {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    }
                }
                LaunchedEffect(speechRecognizer) {
                    speechRecognizer?.setRecognitionListener(recognitionListener)
                }
                DisposableEffect(Unit) {
                    onDispose { speechRecognizer?.destroy() }
                }
                val audioPermLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted && speechRecognizer != null) {
                        isListening = true
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        }
                        speechRecognizer.startListening(intent)
                    }
                }

                IconButton(
                    onClick = {
                        if (isListening) {
                            speechRecognizer?.stopListening()
                            isListening = false
                        } else {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                speechCtx, android.Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                isListening = true
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                }
                                speechRecognizer?.startListening(intent)
                            } else {
                                audioPermLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isListening) ElectricViolet.copy(alpha = 0.25f) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = if (isListening) "Stop" else "Voice to text",
                        tint = if (isListening) ErrorColor else TextSecondaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // (D) Send button
                IconButton(
                    onClick = {
                        if (rawText.isNotBlank()) {
                            val textToSend = rawText
                            rawText = ""
                            onSubmitQuery(textToSend)
                            onNavigateToChat()
                        }
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(ElectricViolet, CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = "↑",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Attachment bottom sheet overlay
    androidx.compose.animation.AnimatedVisibility(
        visible = showAttachBottomSheet,
        enter = androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { showAttachBottomSheet = false }
        )
    }

    val attachPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onAddAttachment(it.toString()) }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = showAttachBottomSheet,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it })
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, BorderSubtle, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = DeepMidnight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 18.dp, horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp, 4.dp)
                            .background(BorderSubtle, CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Add to Conversation",
                        fontSize = 16.sp,
                        fontFamily = InstrumentSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PremiumCyan,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    val attachOptions = listOf(
                        Triple("Images", "🖼️", "image/*"),
                        Triple("Videos", "🎬", "video/*"),
                        Triple("Audio Files", "🎧", "audio/*"),
                        Triple("PDF Documents", "📑", "application/pdf"),
                        Triple("Any File", "🗂️", "*/*")
                    )
                    attachOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface3)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .clickable {
                                    showAttachBottomSheet = false
                                    attachPickerLauncher.launch(option.third)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(text = option.second, fontSize = 18.sp)
                            Text(
                                text = option.first,
                                fontSize = 13.sp,
                                color = TextPrimaryColor,
                                fontFamily = InstrumentSansFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showAttachBottomSheet = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel", fontSize = 13.sp, color = ElectricViolet, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
