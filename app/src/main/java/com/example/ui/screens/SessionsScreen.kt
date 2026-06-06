package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import com.example.data.model.SessionEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionsScreen(
    sessions: List<SessionEntity>,
    activeSessionId: String?,
    onSessionSelected: (String) -> Unit,
    onCreateNewSession: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onNavigateToChat: () -> Unit,
    onTogglePinSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val currentTime = remember { System.currentTimeMillis() }
    
    // Sort pinned sessions to the top, then sort by lastUpdatedAt descending
    val sortedSessions = remember(sessions) {
        sessions.sortedWith(
            compareByDescending<SessionEntity> { it.isPinned }
                .thenByDescending { it.lastUpdatedAt }
        )
    }

    // Filter sessions matching search term
    val filteredSessions = remember(sortedSessions, searchQuery) {
        sortedSessions.filter {
            it.title.contains(searchQuery, ignoreCase = true)
        }
    }

    // Grouping sessions beautifully
    val groupedSessions = remember(filteredSessions, currentTime) {
        val today = mutableListOf<SessionEntity>()
        val yesterday = mutableListOf<SessionEntity>()
        val earlier = mutableListOf<SessionEntity>()

        filteredSessions.forEach { session ->
            val diffMs = currentTime - session.lastUpdatedAt
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            when {
                diffDays < 1 -> today.add(session)
                diffDays < 2 -> yesterday.add(session)
                else -> earlier.add(session)
            }
        }
        listOf(
            Triple("Today", today, today.isNotEmpty()),
            Triple("Yesterday", yesterday, yesterday.isNotEmpty()),
            Triple("Earlier", earlier, earlier.isNotEmpty())
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepMidnight)
    ) {
        // App Header section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface1)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp)) // Safe padding for status icons

            Text(
                text = "Session Library",
                fontFamily = DMSerifDisplayFontFamily,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontSize = 20.sp,
                color = TextPrimaryColor,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            val sessionCount = sessions.size
            Text(
                text = "ARCHIVE ENGINE · $sessionCount ACTIVE TRACES AVAILABLE",
                fontSize = 8.sp,
                fontFamily = DMMonoFontFamily,
                color = TextMutedColor,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Glassy Search field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2, shape = RoundedCornerShape(22.dp))
                    .border(1.dp, BorderSubtle, shape = RoundedCornerShape(22.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMutedColor,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                val customTextSelectionColors = TextSelectionColors(
                    handleColor = ElectricViolet,
                    backgroundColor = ElectricViolet.copy(alpha = 0.4f)
                )
                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = InstrumentSansFontFamily,
                            fontSize = 11.5.sp,
                            color = TextPrimaryColor
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search dynamic profiles and sessions…",
                                    fontFamily = InstrumentSansFontFamily,
                                    fontSize = 11.sp,
                                    color = TextMutedColor
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Premium Primary Action New Session Trigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(ElectricViolet.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp))
                    .border(BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.35f)), shape = RoundedCornerShape(14.dp))
                    .clickable {
                        onCreateNewSession()
                        onNavigateToChat()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 13.sp,
                        color = ElectricViolet,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "Initialize New Analysis Trace",
                        fontSize = 11.sp,
                        fontFamily = InstrumentSansFontFamily,
                        color = ElectricViolet,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Sessions Grouping Scrolling Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            groupedSessions.forEach { (title, sList, isVisible) ->
                if (isVisible) {
                    item(key = title) {
                        Text(
                            text = title.uppercase(),
                            fontSize = 8.sp,
                            fontFamily = DMMonoFontFamily,
                            letterSpacing = 1.3.sp,
                            color = TextMutedColor,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }

                    items(sList, key = { it.id }) { session ->
                        val isActive = session.id == activeSessionId
                        val createdDateStr = remember(session.createdAt) {
                            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                            sdf.format(Date(session.createdAt))
                        }
                        val relativeOpened = remember(session.lastUpdatedAt) {
                            formatRelativeTime(session.lastUpdatedAt)
                        }

                        // Grid item card following bash.html
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isActive) Surface3 else Surface2,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isActive) ElectricViolet else BorderSubtle,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSessionSelected(session.id)
                                    onNavigateToChat()
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // PIN indicator button
                            IconButton(
                                onClick = { onTogglePinSession(session.id) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = if (session.isPinned) "Unpin" else "Pin",
                                    tint = if (session.isPinned) WarningColor else TextMutedColor.copy(alpha = 0.35f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = session.title,
                                        fontSize = 11.5.sp,
                                        color = TextPrimaryColor,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Render exact high fidelity 'Created' and 'Last opened' labels
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Created: $createdDateStr",
                                        fontSize = 8.5.sp,
                                        fontFamily = InstrumentSansFontFamily,
                                        color = TextSecondaryColor
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp)
                                            .background(BorderActive.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    )
                                    Text(
                                        text = "Opened: $relativeOpened",
                                        fontSize = 8.5.sp,
                                        fontFamily = DMMonoFontFamily,
                                        color = TextMutedColor
                                    )
                                }
                            }

                            // Interactive Trash Delete Icon matching prompt
                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Thread",
                                    tint = ErrorColor.copy(alpha = 0.65f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (filteredSessions.isEmpty()) {
                item {
                    Text(
                        text = "No matching analysis profiles found.",
                        fontSize = 11.sp,
                        fontFamily = InstrumentSansFontFamily,
                        color = TextMutedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Relative time format utility
fun formatRelativeTime(time: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - time
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(time))
    }
}
